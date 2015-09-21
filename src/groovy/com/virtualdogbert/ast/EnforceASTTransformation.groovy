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
 * Some of the setup is derived from the fllowing grails plugings(Appache Licence)
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
 * The annotation enforce takes up to 3 closures can injects a call to the enforce method of the enforcerService.
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
        if (nodes[0] instanceof AnnotationNode && nodes[1] instanceof MethodNode) {
            MethodNode methodNode = (MethodNode) nodes[1]
            ClassNode beforeNode = new ClassNode(Enforce.class)

            for (AnnotationNode annotationNode : methodNode.getAnnotations(beforeNode)) {

                ListExpression params = new ListExpression(getParamsList(annotationNode.members))
                BlockStatement methodBody = (BlockStatement) methodNode.getCode()
                List statements = methodBody.getStatements()
                statements.add(0, createEnforcerCall(params))
                break
            }

            VariableScopeVisitor scopeVisitor = new VariableScopeVisitor(sourceUnit)
            sourceUnit.AST.classes.each {
                scopeVisitor.visitClass(it)
            }
        }
    }

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

    private Statement createEnforcerCall(ListExpression params) {
        ClassNode holder = new ClassNode(Holders.class)
        Expression context = new StaticMethodCallExpression(holder, "getApplicationContext", ArgumentListExpression.EMPTY_ARGUMENTS)
        Expression service = new MethodCallExpression(context, "getBean", new ConstantExpression('enforcerService'));
        Expression call = new MethodCallExpression(service, 'enforce', new ArgumentListExpression(params))
        return new ExpressionStatement(call)
    }
}
