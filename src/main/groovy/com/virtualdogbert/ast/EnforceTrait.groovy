package com.virtualdogbert.ast

import com.security.enforcer.EnforcerService
import grails.compiler.ast.GrailsArtefactClassInjector
import grails.gorm.transactions.Transactional
import groovy.transform.CompilationUnitAware
import groovy.transform.CompileStatic
import groovyjarjarasm.asm.Opcodes
import org.codehaus.groovy.ast.*
import org.codehaus.groovy.ast.expr.*
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.ast.stmt.ExpressionStatement
import org.codehaus.groovy.ast.stmt.ReturnStatement
import org.codehaus.groovy.ast.stmt.Statement
import org.codehaus.groovy.classgen.VariableScopeVisitor
import org.codehaus.groovy.control.CompilationUnit
import org.codehaus.groovy.control.ErrorCollector
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.sc.StaticCompileTransformation
import org.grails.compiler.injection.GrailsASTUtils
import org.grails.datastore.gorm.transactions.transform.TransactionalTransform

import java.lang.reflect.Modifier

import static org.grails.datastore.mapping.reflect.AstUtils.addAnnotationIfNecessary

/**
 * This trait provides all the base functionality for various enforcer AST Transforms(wrapping methods, dealing with parameters, etc).
 * It has one abstract method additionalMethodProcessing, used to apply the actual enforcer transform, and any other addons like
 * transactionality, static compilation, or both.
 */
@CompileStatic
trait EnforceTrait implements CompilationUnitAware {
    static final String Enforcer_Service      = 'enforcerService'
    static final String Method_Wrapper_Prefix = '$Enforcer_wrapped_method_'

    static final ClassNode COMPILE_STATIC_TYPE = ClassHelper.make(CompileStatic)
    static final ClassNode TRANSACTIONAL_TYPE  = ClassHelper.make(Transactional)
    static final List      skipKeys            = ['value', 'failure', 'success']

    CompilationUnit compilationUnit

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
    void wrapMethod(SourceUnit source, ClassNode classNode, MethodNode methodNode, List<Expression> params, Map<String, Expression> members, boolean fromClass) {
        if (fromClass && hasEnforcerAnnotation(methodNode)) {
            return
            //If the annotation is from the class level, but the method node has it's own @Enforce annotation don't apply the class level logic.
        }

        MethodCallExpression originalMethodCall = moveOriginalCodeToNewMethod(source, classNode, methodNode, params, members)
        BlockStatement methodBody = new BlockStatement()

        if (methodNode.getReturnType() != ClassHelper.VOID_TYPE) {
            methodBody.addStatement(new ReturnStatement(new CastExpression(methodNode.getReturnType(), originalMethodCall)))
        } else {
            methodBody.addStatement(new ExpressionStatement(originalMethodCall))
        }

        methodNode.setCode(methodBody)
        GrailsASTUtils.processVariableScopes(source, classNode, methodNode)
    }

    /**
     * Checks a method node for any Enforcer based annotation, returns true if there is one and false otherwise.
     * This is what is ued to skip over applying class level transforms, if a method level transform exists.
     *
     * @param methodNode the method node to check for enforcer annotations
     *
     * @return true if the methodNode has any Enforcer based Transform, and false otherwise.
     */
    boolean hasEnforcerAnnotation(MethodNode methodNode) {
        ClassNode enforceNode = new ClassNode(Enforce.class)
        ClassNode enforceTNode = new ClassNode(EnforceT.class)
        ClassNode enforceSNode = new ClassNode(EnforceS.class)
        ClassNode enforceTSNode = new ClassNode(EnforceTS.class)
        ClassNode reinforceNode = new ClassNode(Reinforce.class)
        ClassNode reinforceTNode = new ClassNode(ReinforceT.class)
        ClassNode reinforceSNode = new ClassNode(ReinforceS.class)
        ClassNode reinforceTSNode = new ClassNode(ReinforceS.class)
        ClassNode reinforceFilterNode = new ClassNode(ReinforceFilter.class)
        ClassNode reinforceFilterTNode = new ClassNode(ReinforceFilterT.class)
        ClassNode reinforceFilterSNode = new ClassNode(ReinforceFilterS.class)
        ClassNode reinforceFilterTSNode = new ClassNode(ReinforceFilterS.class)
        ClassNode NotTransactionalNodeOld = new ClassNode(grails.transaction.NotTransactional.class)
        ClassNode NotTransactionalNodeNew = new ClassNode(grails.gorm.transactions.NotTransactional.class)
        ClassNode TransactionalNodeOld = new ClassNode(grails.transaction.Transactional.class)
        ClassNode TransactionalNodeNew = new ClassNode(grails.gorm.transactions.Transactional.class)

        return methodNode.getAnnotations(enforceNode)[0] ||
               methodNode.getAnnotations(enforceTNode)[0] ||
               methodNode.getAnnotations(enforceSNode)[0] ||
               methodNode.getAnnotations(enforceTSNode)[0] ||
               methodNode.getAnnotations(reinforceNode)[0] ||
               methodNode.getAnnotations(reinforceTNode)[0] ||
               methodNode.getAnnotations(reinforceSNode)[0] ||
               methodNode.getAnnotations(reinforceTSNode)[0] ||
               methodNode.getAnnotations(reinforceFilterNode)[0] ||
               methodNode.getAnnotations(reinforceFilterTNode)[0] ||
               methodNode.getAnnotations(reinforceFilterSNode)[0] ||
               methodNode.getAnnotations(reinforceFilterTSNode)[0] ||
               methodNode.getAnnotations(NotTransactionalNodeOld)[0] ||
               methodNode.getAnnotations(NotTransactionalNodeNew)[0] ||
               methodNode.getAnnotations(TransactionalNodeOld)[0] ||
               methodNode.getAnnotations(TransactionalNodeNew)[0]
    }

    /**
     * The default visit method, used by the AST transform to add Enforcer transformations to class or method nodes.
     *
     * @param nodes An array with the annotation node and the method/class node.
     * @param sourceUnit the source compilation used used for fixing variable scope.
     */
    void visit(ASTNode[] nodes, SourceUnit sourceUnit) {
        ClassNode beforeNode = ((AnnotationNode) nodes[0]).classNode

        if (nodes[1] instanceof MethodNode) {

            MethodNode methodNode = (MethodNode) nodes[1]
            addEnforcerService(methodNode)
            ClassNode classNode = methodNode.getDeclaringClass()
            AnnotationNode annotationNode = methodNode.getAnnotations(beforeNode)[0]
            List<Expression> params = getParamsList(annotationNode.members)
            wrapMethod(sourceUnit, classNode, methodNode, params, annotationNode.members, false)


        } else if (nodes[1] instanceof ClassNode) {

            ClassNode classNode = (ClassNode) nodes[1]
            addEnforcerService(classNode)
            AnnotationNode annotationNode = classNode.getAnnotations(beforeNode)[0]
            List<Expression> params = getParamsList(annotationNode.members)
            List<MethodNode> methods = new ArrayList<MethodNode>(classNode.getMethods())
            methods.each { MethodNode methodNode ->
                wrapMethod(sourceUnit, classNode, methodNode, params, annotationNode.members, true)
            }

        }
    }

    /**
     * List of default expansions to apply to compile static, which is taken from the @GrailsCompileStatic.
     */
    private static List<String> extensions = ['org.grails.compiler.ValidateableTypeCheckingExtension',
                                              'org.grails.compiler.NamedQueryTypeCheckingExtension',
                                              'org.grails.compiler.HttpServletRequestTypeCheckingExtension',
                                              'org.grails.compiler.WhereQueryTypeCheckingExtension',
                                              'org.grails.compiler.DynamicFinderTypeCheckingExtension',
                                              'org.grails.compiler.DomainMappingTypeCheckingExtension',
                                              'org.grails.compiler.RelationshipManagementMethodTypeCheckingExtension']

    /**
     * Adds compile static to the methodNode passed in, similar to how it's done in @Transactional, but takes extensions from the annotation,
     * however if there are none provided, then the defaults are the same as @GrailsCompileStatic.
     *
     * @param sourceUnit used for calling the visit method of StaticCompileTransformation
     * @param methodNode the methodNode to apply StaticCompileTransformation to.
     * @param members The parameters passed in from the original annotation.
     */
    void compileMethodStatically(SourceUnit sourceUnit, MethodNode methodNode, Map<String, Expression> members = [:]) {
        if (compilationUnit != null) {

            if (!members.containsKey('extensions')) {
                ListExpression grailsExtensions = new ListExpression()

                extensions.each { String extension ->
                    grailsExtensions.addExpression(new ConstantExpression(extension))
                }

                ((LinkedHashMap) members).extensions = grailsExtensions
            }


            addAnnotationIfNecessary(methodNode, CompileStatic)
            StaticCompileTransformation staticCompileTransformation = new StaticCompileTransformation(compilationUnit: compilationUnit)
            AnnotationNode annotationNode = new AnnotationNode(COMPILE_STATIC_TYPE)

            addMembers(annotationNode, members)

            staticCompileTransformation.visit([annotationNode, methodNode] as ASTNode[], sourceUnit)
        }
    }

    /**
     * Adds transactionality to the methodNode passed in, similar to how @CompileStatic it is done in @Transactional, applying the
     * TransactionalTransform to the methodNode.
     *
     * @param sourceUnit used for calling the visit method of TransactionalTransform
     * @param methodNode the methodNode to apply TransactionalTransform to.
     * @param members The parameters passed in from the original annotation.
     */
    void addTransactional(SourceUnit sourceUnit, MethodNode methodNode, Map<String, Expression> members = [:]) {
        if (compilationUnit != null) {
            addAnnotationIfNecessary(methodNode, Transactional)
            TransactionalTransform transactionalTransformation = new TransactionalTransform(compilationUnit: compilationUnit)
            AnnotationNode annotationNode = new AnnotationNode(TRANSACTIONAL_TYPE)

            addMembers(annotationNode, members)

            transactionalTransformation.visit([annotationNode, methodNode] as ASTNode[], sourceUnit)
        }
    }

    /**
     * A helper method to normalize/map the key names used for parameters, Since the original annotation, can have params for Enforcer,
     * CompileStatic, and Transactional, which all collide over the value key.
     *
     * @param key The key to look up the name for.
     *
     * @return the member name to use, which is the same as the input unless the input is transactionalValue or staticValue, then the name
     * returned is value.
     */
    String getMemberName(String key) {
        if (key == 'transactionalValue' || key == 'staticValue') {
            return 'value'
        }

        return key
    }

    /**
     * Adds members to the annotations node, used for calling the CompileStatic, and Transactional transforms.
     *
     * @param annotationNode The annotation node to add members/params to.
     * @param members The members map of names, to expression.
     */
    void addMembers(AnnotationNode annotationNode, Map<String, Expression> members) {
        members.each { String key, Expression value ->
            if (key in skipKeys) {
                return
            }

            annotationNode.addMember(getMemberName(key), value)
        }
    }

    /**
     * Adds the Enforcer service bean to be later injected to a service, to a MethodNodes ClassNode. This will only add the reference if it
     * doesn't exist.
     *
     * @param methodNode The MethodNode to used to look up it's declaring ClassNode, and add the EnforcerService bean to.
     */
    void addEnforcerService(MethodNode methodNode) {
        addEnforcerService(methodNode.declaringClass)
    }

    /**
     * Adds Enforcer service bean to a ClassNode, to be injected later. This will only add the reference if it doesn't exist.
     *
     * @param classNode the class node to add the Enforcer Service to.
     */
    void addEnforcerService(ClassNode classNode) {

        if (!classNode.properties*.name.contains(Enforcer_Service)) {
            ClassNode enforcerService = new ClassNode(EnforcerService.class)
            PropertyNode property = new PropertyNode('enforcerService', Opcodes.ACC_PUBLIC, enforcerService, classNode, null, null, null)
            classNode.addProperty(property)
        }
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
    MethodCallExpression moveOriginalCodeToNewMethod(SourceUnit source, ClassNode classNode, MethodNode methodNode, List<Expression> params, Map<String, Expression> members) {
        String renamedMethodName = Method_Wrapper_Prefix + methodNode.getName()
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

        methodNode.setCode(null)
        classNode.addMethod(renamedMethodNode)
        additionalMethodProcessing(source, renamedMethodNode, params, members)

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
     * This abstract method is where enforcer transforms, and other transforms(CompileStatic, Transactional) should be applied.
     *
     * @param source The source unit to use for fixing variable scope.
     * @param renamedMethodNode the method node to apply the transforms to.
     * @param params The Enforcer parameters passed in from the annotation.
     * @param members The members/parameters passed into the annotation, that can be used for the CompileStatic and Transactional transforms.
     */
    abstract void additionalMethodProcessing(SourceUnit source, MethodNode renamedMethodNode, List<Expression> params, Map<String, Expression> members)

    /**
     * This copies the parameters from one method to another
     *
     * @param parameterTypes the parameters to copy
     *
     * @return The copied parameters
     */
    static Parameter[] copyParameters(Parameter[] parameterTypes) {
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
    void applyToMethod(BlockStatement methodBody, List<Expression> params, int index = 0) {
        List statements = methodBody.getStatements()
        statements.add(index, createEnforcerCall(params))
    }

    /**
     *  extracts the closure parameters from the members map, into a list in the order that the enforcerService's enforce method expects
     *
     * @param members The map of members / parameters
     *
     * @return A list of the closure parameters passed to the annotation
     */
    List<Expression> getParamsList(Map members) {
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
     *
     * @return the statement created for injecting the call to the enforce method of the enforcerService
     */
    Statement createEnforcerCall(List<Expression> params) {
        Expression enforcerServiceExpression = new VariableExpression(Enforcer_Service)
        ArgumentListExpression arguments = new ArgumentListExpression()

        params.each { Expression expression ->
            arguments.addExpression(expression)
        }

        Expression call = new MethodCallExpression(enforcerServiceExpression, 'enforce', arguments)
        return new ExpressionStatement(call)
    }
}
