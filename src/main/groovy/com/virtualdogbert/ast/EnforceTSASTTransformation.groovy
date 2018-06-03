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

import groovy.transform.CompileStatic
import org.codehaus.groovy.ast.MethodNode
import org.codehaus.groovy.ast.expr.Expression
import org.codehaus.groovy.ast.stmt.BlockStatement
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
 * This also statically compiles, and adds transactionality to the method, using the transforms from @Transactional, and @CompileStatic.
 *
 * The first closure is value, just so that the transform can be called without naming the parameter.
 * If your specifying two or more closures you will have to specify there names in the annotation call.
 * Examples:
 * @Enforce ( { true } )
 * @Enforce ( value = { true } , failure = { println " nice " } )
 * @Enforce ( value = { true } , failure = { println " nice " } , success = { println " not nice " } )
 * @Enforce ( value = { false } , failure = { println " not nice " } , success = { println " nice " } )
 *
 * parameters
 * value is the predicate for the enforce service, named value so that you don't have to name it
 * failure is the code to run if the predicate returns false, if not specified, the default for the enforcerService is used.
 * success the code to run if the predicate returns true, if not specified, the default for the enforcerService is used.
 * TypeCheckingMode the type checking mode pass or skip.
 * extensions any type extensions you would like to add, by default this annotation adds the same extensions as @GrailsCompileStatic.
 * All the parameters that @ Transactional can take.
 */
@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
class EnforceTSASTTransformation extends AbstractASTTransformation implements  EnforceTrait {

    void additionalMethodProcessing(SourceUnit source, MethodNode renamedMethodNode, List<Expression> params, Map<String, Expression> members) {
        BlockStatement methodBody = renamedMethodNode.getCode() as BlockStatement
        applyToMethod(methodBody, params)

        addTransactional(source, renamedMethodNode, members)
        compileMethodStatically(source, renamedMethodNode, members)
    }
}
