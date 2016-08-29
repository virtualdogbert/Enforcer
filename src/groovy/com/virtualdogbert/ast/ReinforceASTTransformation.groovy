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
import org.codehaus.groovy.ast.stmt.ExpressionStatement
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
 * The annotation enforce takes up to 3 closures can injects a call to the enforce method of the enforcerService
 * at the end of the method before returning
 * .
 * This can be applied to a method or a class, but the method will take precedence.
 *
 * The first closure is value, just so that the transform can be called without naming the parameter.
 * If your specifying two or more closures you will have to specify there names in the annotation call.
 * Examples:
 * @Reinforce ( { true } )
 * @Reinforce ( value = { true } , failure = { println " nice " } )
 * @Reinforce ( value = { true } , failure = { println " nice " } , success = { println " not nice " } )
 * @Reinforce ( value = { false } , failure = { println " not nice " } , success = { println " nice " } )
 *
 */
@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
public class ReinforceASTTransformation extends AbstractASTTransformation {

    public static final ClassNode COMPILE_STATIC_TYPE = ClassHelper.make(CompileStatic)
    public static final ClassNode TYPE_CHECKED_TYPE   = ClassHelper.make(TypeChecked)

    @Override
    public void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
        if (nodes.length != 2) return
        ClassNode beforeNode = new ClassNode(Reinforce.class)

        if (nodes[0] instanceof AnnotationNode && nodes[1] instanceof MethodNode) {

            MethodNode methodNode = (MethodNode) nodes[1]
            ClassNode classNode = methodNode.getDeclaringClass()
            AnnotationNode annotationNode = methodNode.getAnnotations(beforeNode)[0]
            ListExpression params = new ListExpression(getParamsList(annotationNode.members))
            weaveMethod(sourceUnit, classNode, methodNode, params)


        } else if (nodes[0] instanceof AnnotationNode && nodes[1] instanceof ClassNode) {

            ClassNode classNode = (ClassNode) nodes[1]
            AnnotationNode annotationNode = classNode.getAnnotations(beforeNode)[0]
            ListExpression params = new ListExpression(getParamsList(annotationNode.members))
            List<MethodNode> methods = new ArrayList<MethodNode>(classNode.getMethods())
            methods.each { MethodNode methodNode ->
                weaveMethod(sourceUnit, classNode, methodNode, params, true)
            }

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
    protected void weaveMethod(SourceUnit source, ClassNode classNode, MethodNode methodNode, ListExpression params, boolean fromClass = false) {
        ClassNode beforeNode = new ClassNode(Reinforce.class)
        if (fromClass && methodNode.getAnnotations(beforeNode)[0]) {
            return
            //If the annotation is from the class level, but the method node has it's own @Reinforce annotation don't apply the class level logic.
        }

        MethodCallExpression originalMethodCall = moveOriginalCodeToNewMethod(source, classNode, methodNode)
        BlockStatement methodBody = new BlockStatement()
        applyToMethod(methodBody, params)

        if (methodNode.getReturnType() != ClassHelper.VOID_TYPE) {
            methodBody.addStatement(new ReturnStatement(new CastExpression(methodNode.getReturnType(), originalMethodCall)))
        } else {
            methodBody.addStatement(new ExpressionStatement(originalMethodCall))
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
     * This applies the enforce logic to a method node.
     *
     * @param methodNode The method node to inject the enforce logic
     * @param sourceUnit the source unit which is used for fixing the variable scope
     * @param params the parameter passed into the annotation at the class or method level
     * @param fromClass If the annotation comes from the class level
     */
    private void applyToMethod(BlockStatement methodBody, ListExpression params) {
        List statements = methodBody.getStatements()
        statements.add(0, createEnforcerCall(params))
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
        Expression service = new MethodCallExpression(context, "getBean", new ConstantExpression('enforcerService'))
        Expression call = new MethodCallExpression(service, 'enforce', new ArgumentListExpression(params))
        return new ExpressionStatement(call)
    }
}
