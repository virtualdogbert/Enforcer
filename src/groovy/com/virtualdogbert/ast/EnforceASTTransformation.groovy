/**
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 *
 * Some of the setup is derived from the following grails plugings(Appache Licence)
 * https://github.com/groovy/groovy-core/blob/4993b10737881b2491c2daa01526fb15dd889ac5/src/main/org/codehaus/groovy/transform/NewifyASTTransformation.java
 * https://github.com/grails-plugins/grails-redis/tree/master/src/main/groovy/grails/plugins/redis
 */

package com.virtualdogbert.ast

import grails.util.Holders
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.ASTNode
import org.codehaus.groovy.ast.AnnotationNode
import org.codehaus.groovy.ast.ClassNode
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.classgen.VariableScopeVisitor
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
/**
 * The annotation enforce takes up to 3 closures can injects a call to the enforce method of the enforcerService at the
 * beginning of the method.
 *
 * This can be applied to a method or a class, but the method will take precedence.
 *
 * The first closure is value, just so that the transform can be called without naming the parameter.
 * If your specifying two or more closures you will have to specify there names in the annotation call.
 * Examples:
 * @Enforce ( { true})
 * @Enforce ( value = { true }, failure = { println "nice" })
 * @Enforce ( value = { true }, failure = { println "nice" }, success = { println "not nice" })
 * @Enforce ( value = { false }, failure = { println "not nice" }, success = { println "nice" })
 *
 */
@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class EnforceASTTransformation extends AbstractASTTransformation {

    @Override
    public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
        if (nodes.length != 2) return
        ClassNode beforeNode = new ClassNode(Enforce.class)

        if (nodes[0] instanceof AnnotationNode && nodes[1] instanceof MethodNode) {

            MethodNode methodNode = (MethodNode) nodes[1]
            AnnotationNode annotationNode  = methodNode.getAnnotations(beforeNode)[0]
            ListExpression params = new ListExpression(getParamsList(annotationNode.members))
            applyToMethod(methodNode, sourceUnit, params)

        } else if (nodes[0] instanceof AnnotationNode && nodes[1] instanceof ClassNode) {

            ClassNode classNode = (ClassNode) nodes[1]
            AnnotationNode annotationNode = classNode.getAnnotations(beforeNode)[0]
            ListExpression params = new ListExpression(getParamsList(annotationNode.members))
            classNode.methods.each{ MethodNode methodNode ->
                applyToMethod(methodNode, sourceUnit, params, true)
            }

        }
    }

    /**
     * This applies the enforce logic to a method node.
     *
     * @param methodNode The method node to inject the enforce logic
     * @param sourceUnit the source unit which is used for fixing the variable scope
     * @param params the parameter passed into the annotation at the class or method level
     * @param fromClass If the annotation comes from the class level
     */
    private void applyToMethod(MethodNode methodNode, SourceUnit sourceUnit, ListExpression params, boolean fromClass = false) {

        ClassNode beforeNode = new ClassNode(Enforce.class)
        if (fromClass && methodNode.getAnnotations(beforeNode)[0]) {
            return //If the annotation is from the class level, but the method node has it's own @Enforce annotation don't apply the class level logic.
        }

        BlockStatement methodBody = (BlockStatement) methodNode.getCode()
        List statements = methodBody.getStatements()
        statements.add(0, createEnforcerCall(params))

        VariableScopeVisitor scopeVisitor = new VariableScopeVisitor(sourceUnit)
        sourceUnit.AST.classes.each { ClassNode classNode ->
            scopeVisitor.visitClass(classNode)
        }
    }

    /**
         *  extracts the closure parameters from the members map, into a list in the order that the enforcerService's enforce method expects
         *
         * @param members The map of members / parameters
         * @return A list of the closure parameters passed to the annotation
         */
    private List getParamsList(Map members) {
        Expression value = (Expression) members.value
        Expression failure = (Expression) members.failure
        Expression success = (Expression) members.success
        List paramsList = []

        if (value) {
            paramsList << value
        }

        if (failure) {
            paramsList << failure
        }

        if (success) {
            paramsList << success
        }
        return paramsList
    }

    /**
         *  Creates the call to the enforcer service, to be injected, using the list of parameters generated from the get ParamsList
         *
         * @param params the list of closure parameters to pass to the enforce method of the enforcer service
         * @return the statement created for injecting the call to the enforce method of the enforcerService
         */
    private Statement createEnforcerCall(ListExpression params) {
        ClassNode holder = new ClassNode(Holders.class)
        Expression context = new StaticMethodCallExpression(holder, "getApplicationContext", ArgumentListExpression.EMPTY_ARGUMENTS)
        Expression service = new MethodCallExpression(context, "getBean", new ConstantExpression('enforcerService'));
        Expression call = new MethodCallExpression(service, 'enforce', new ArgumentListExpression(params))
        return new ExpressionStatement(call)
    }
}
