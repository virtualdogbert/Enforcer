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

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.stmt.BlockStatement
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.ASTTransformation
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation
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
class ReinforceASTTransformation extends AbstractASTTransformation implements EnforceTrait, ASTTransformation {

    void additionalMethodProcessing(SourceUnit source, MethodNode renamedMethodNode, List<Expression> params, Map<String, Expression> members) {
        BlockStatement methodBody = renamedMethodNode.getCode() as BlockStatement
        int statementsLength = methodBody.statements.size()

        applyToMethod(methodBody, params, statementsLength > 1 ? statementsLength - 1 : 0)
    }

}
