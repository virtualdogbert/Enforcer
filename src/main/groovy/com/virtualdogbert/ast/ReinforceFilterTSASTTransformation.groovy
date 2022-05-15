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
 * Some of the code is derived from the following grails AST transform(Appache Licence)
 * https://github.com/grails/grails-core/blob/master/grails-core/src/main/groovy/org/grails/transaction/transform/TransactionalTransform.groovy
 */

package com.virtualdogbert.ast

import groovy.transform.CompilationUnitAware
import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
import org.grails.compiler.injection.GrailsASTUtils

/**
 * The annotation reinforceFilter takes one closure, and filters the return statement of a method based on that closure.
 *
 * This also statically compiles, and adds transactionality to the method, using the transforms from @Transactional, and @CompileStatic.
 *
 * Example:
 * @ReinforceFilter ( { Object o -> (o as List).findResults { it % 2 == 0 ? it : null } })
 *
 * parameters
 * value is the filter for the enforce service, named value so that you don't have to name it.
 * TypeCheckingMode the type checking mode pass or skip.
 * extensions any type extensions you would like to add, by default this annotation adds the same extensions as @GrailsCompileStatic.
 * All the parameters that @ Transactional can take.
 */
@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.CANONICALIZATION)
class ReinforceFilterTSASTTransformation extends AbstractASTTransformation implements EnforceTrait, CompilationUnitAware, ASTTransformation {

    @Override
    void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
        ClassNode beforeNode = ((AnnotationNode) nodes[0]).classNode

        MethodNode methodNode = (MethodNode) nodes[1]
        addEnforcerService(methodNode)
        ClassNode classNode = methodNode.getDeclaringClass()
        AnnotationNode annotationNode = methodNode.getAnnotations(beforeNode)[0]
        MethodCallExpression originalMethodCall = moveOriginalCodeToNewMethod(sourceUnit, classNode, methodNode, null, annotationNode.members)
        List<Expression> params = getParamsList(annotationNode.members, originalMethodCall)
        wrapMethod(sourceUnit, classNode, methodNode, params, annotationNode.members)
    }

    /**
     * Copies the ordinal method to a new one and replaces the ordinal method with a new one that calls  the new method,
     * then the enforcerService, and then returns
     *
     * @param source sourceUnit the source unit which is used for fixing the variable scope
     * @param classNode the class node used for moving the method, and fixing the variable scope
     * @param methodNode The method node to inject the enforce logic
     * @param params the parameter passed into the annotation at the class or method level
     * @param fromClass If the annotation comes from the class level
     */
    @Override
    void wrapMethod(SourceUnit source, ClassNode classNode, MethodNode methodNode, List<Expression> params, Map<String, Expression> members , boolean fromClass = false) {

        BlockStatement methodBody = new BlockStatement()

        if (methodNode.getReturnType() != ClassHelper.VOID_TYPE) {
            methodBody.addStatement(createEnforcerCall(methodNode, params))
        } else {
            addError("You cannot apply ReinforceFilter to a method that returns void", methodNode)
        }

        methodNode.setCode(methodBody)
        GrailsASTUtils.processVariableScopes(source, classNode, methodNode)
    }

    /**
     *  extracts the closure parameters from the members map, into a list in the order that the enforcerService's enforce method expects
     *
     * @param members The map of members / parameters
     * @return A list of the closure parameters passed to the annotation
     */
    List<Expression> getParamsList(Map members, Expression returnValue) {
        Expression value = (Expression) members.value
        return [value, returnValue]
    }

    /**
     *  Creates the call to the enforcer service, to be injected, using the list of parameters generated from the get ParamsList
     *
     * @param params the list of closure parameters to pass to the enforce method of the enforcer service
     * @return the statement created for injecting the call to the enforce method of the enforcerService
     */
    Statement createEnforcerCall(MethodNode methodNode, List<Expression> params) {
        Expression enforcerServiceExpression = new VariableExpression("enforcerService")
        ArgumentListExpression arguments = new ArgumentListExpression()

        params.each { Expression expression ->
            arguments.addExpression(expression)
        }

        MethodCallExpression methodCall = new MethodCallExpression(enforcerServiceExpression, 'ReinforceFilter', arguments)
        return new ReturnStatement(new CastExpression(methodNode.getReturnType(), methodCall))
    }


    void additionalMethodProcessing(SourceUnit source, MethodNode renamedMethodNode, List<Expression> params, Map<String, Expression> members) {
        addTransactional(source, renamedMethodNode, members)
        compileMethodStatically(source, renamedMethodNode, members)
    }
}
