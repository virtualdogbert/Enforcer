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

import grails.util.Holders
import groovy.transform.CompileStatic
import groovy.transform.TypeChecked
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.classgen.VariableScopeVisitor
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.grails.compiler.injection.GrailsASTUtils
import org.codehaus.groovy.grails.compiler.injection.GrailsArtefactClassInjector
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

import java.lang.reflect.Modifier

/**
 * The annotation reinforceFilter takes one closure, and filters the return statement of a method based on that closure
 *
 * Example:
 * @ReinforceFilter({ Object o -> (o as List).findResults { it % 2 == 0 ? it : null } })
 *
 */
@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class ReinforceFilterASTTransformation extends AbstractASTTransformation {

    public static final ClassNode COMPILE_STATIC_TYPE = ClassHelper.make(CompileStatic)
    public static final ClassNode TYPE_CHECKED_TYPE   = ClassHelper.make(TypeChecked)

    @Override
    public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
        init(nodes, sourceUnit)
        if (nodes.length != 2) return
        ClassNode beforeNode = new ClassNode(ReinforceFilter.class)

        if (nodes[0] instanceof AnnotationNode && nodes[1] instanceof MethodNode) {
            MethodNode methodNode = (MethodNode) nodes[1]
            ClassNode classNode = methodNode.getDeclaringClass()
            AnnotationNode annotationNode = methodNode.getAnnotations(beforeNode)[0]
            weaveMethod(sourceUnit, classNode, methodNode, annotationNode)
        }
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
    protected void weaveMethod(SourceUnit source, ClassNode classNode, MethodNode methodNode, AnnotationNode annotationNode) {
        MethodCallExpression originalMethodCall = moveOriginalCodeToNewMethod(source, classNode, methodNode)
        BlockStatement methodBody = new BlockStatement()
        ListExpression params = new ListExpression(getParamsList(annotationNode.members, originalMethodCall))

        if (methodNode.getReturnType() != ClassHelper.VOID_TYPE) {
            methodBody.addStatement(new ReturnStatement(new CastExpression(methodNode.getReturnType(), createEnforcerCall(params))))
        } else {
            addError("You cannot apply ReinforceFilter to a method that returns void", methodNode)
        }

        methodNode.setCode(methodBody)
        GrailsASTUtils.processVariableScopes(source, classNode, methodNode)
    }

    /**
     * This copies a method to a renamed method, so that it can be wrapped.
     *
     * @param source sourceUnit the source unit which is used for fixing the variable scope
     * @param classNode the class node used for making the new copy of the method
     * @param methodNode the method node to copy and move
     *
     * @return a method call expression to the copied old method
     */
    protected MethodCallExpression moveOriginalCodeToNewMethod(SourceUnit source, ClassNode classNode, MethodNode methodNode) {
        String renamedMethodName = '$Enforcer__' + methodNode.getName()
        Parameter[] newParameters = methodNode.getParameters() ? (copyParameters(methodNode.getParameters() as Parameter[])) : [] as Parameter[]


        Statement body = methodNode.code
        MethodNode renamedMethodNode = new MethodNode(
                renamedMethodName,
                Modifier.PROTECTED, methodNode.getReturnType().getPlainNodeReference(),
                newParameters,
                GrailsArtefactClassInjector.EMPTY_CLASS_ARRAY,
                body
        )


        VariableScope newVariableScope = new VariableScope()
        for (p in newParameters) {
            newVariableScope.putDeclaredVariable(p)
        }

        renamedMethodNode.setVariableScope(
                newVariableScope
        )

        // GrailsCompileStatic and GrailsTypeChecked are not explicitly addressed
        // here but they will be picked up because they are @AnnotationCollector annotations
        // which use CompileStatic and TypeChecked...
        renamedMethodNode.addAnnotations(methodNode.getAnnotations(COMPILE_STATIC_TYPE))
        renamedMethodNode.addAnnotations(methodNode.getAnnotations(TYPE_CHECKED_TYPE))

        methodNode.setCode(null)
        classNode.addMethod(renamedMethodNode)

        // Use a dummy source unit to process the variable scopes to avoid the issue where this is run twice producing an error
        VariableScopeVisitor scopeVisitor = new VariableScopeVisitor(new SourceUnit("dummy", "dummy", source.getConfiguration(), source.getClassLoader(), new ErrorCollector(source.getConfiguration())))
        if (methodNode == null) {
            scopeVisitor.visitClass(classNode)
        } else {
            scopeVisitor.prepareVisit(classNode)
            scopeVisitor.visitMethod(renamedMethodNode)
        }

        final originalMethodCall = new MethodCallExpression(new VariableExpression("this"), renamedMethodName, new ArgumentListExpression(renamedMethodNode.parameters))
        originalMethodCall.setImplicitThis(false)
        originalMethodCall.setMethodTarget(renamedMethodNode)

        originalMethodCall
    }

    /**
     * This copies the parameters from one method to another
     *
     * @param parameterTypes the parameters to copy
     *
     * @return The copied parameters
     */
    private static Parameter[] copyParameters(Parameter[] parameterTypes) {
        Parameter[] newParameterTypes = new Parameter[parameterTypes.length]
        for (int i = 0; i < parameterTypes.length; i++) {
            Parameter parameterType = parameterTypes[i]
            ClassNode parameterTypeCN = parameterType.getType()
            ClassNode newParameterTypeCN = parameterTypeCN.getPlainNodeReference()
            if (parameterTypeCN.isUsingGenerics() && !parameterTypeCN.isGenericsPlaceHolder()) {
                newParameterTypeCN.setGenericsTypes(parameterTypeCN.getGenericsTypes())
            }
            Parameter newParameter = new Parameter(newParameterTypeCN, parameterType.getName(), parameterType.getInitialExpression())
            newParameter.addAnnotations(parameterType.getAnnotations())
            newParameterTypes[i] = newParameter
        }
        return newParameterTypes
    }

    /**
     *  extracts the closure parameters from the members map, into a list in the order that the enforcerService's enforce method expects
     *
     * @param members The map of members / parameters
     * @return A list of the closure parameters passed to the annotation
     */
    private List getParamsList(Map members, Expression returnValue) {
        Expression value = (Expression) members.value
        return [value, returnValue]
    }

    /**
     *  Creates the call to the enforcer service, to be injected, using the list of parameters generated from the get ParamsList
     *
     * @param params the list of closure parameters to pass to the enforce method of the enforcer service
     * @return the statement created for injecting the call to the enforce method of the enforcerService
     */
    private Expression createEnforcerCall(ListExpression params) {
        ClassNode holder = new ClassNode(Holders.class)
        Expression context = new StaticMethodCallExpression(holder, "getApplicationContext", ArgumentListExpression.EMPTY_ARGUMENTS)
        Expression service = new MethodCallExpression(context, "getBean", new ConstantExpression('enforcerService'))
        return new MethodCallExpression(service, 'ReinforceFilter', new ArgumentListExpression(params))
    }
}
