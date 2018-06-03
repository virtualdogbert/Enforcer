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
import org.codehaus.groovy.control.CompilePhase
import org.codehaus.groovy.control.SourceUnit
import org.codehaus.groovy.transform.AbstractASTTransformation
import org.codehaus.groovy.transform.GroovyASTTransformation

/**
 * The annotation  will statically compile, and make transactional a method or class, with the method taking precedence. However it won't
 * interfere with enforce based annotations, like the traditional @CompileStatic, and @Transactional will. This annotation takes the same
 * parameters as @CompileStatic and @Transactional as it uses the  same transforms under the covers.
 *
 * parameters
 * TypeCheckingMode the type checking mode pass or skip.
 * extensions any type extensions you would like to add, by default this annotation adds the same extensions as @GrailsCompileStatic.
 * All the paramas for @Transactional.
 */
@CompileStatic
@GroovyASTTransformation(phase = CompilePhase.SEMANTIC_ANALYSIS)
class EnforcerTransactionalCompileStaticASTTransformation extends AbstractASTTransformation implements EnforceTrait {

    void additionalMethodProcessing(SourceUnit source, MethodNode renamedMethodNode, List<Expression> params, Map<String, Expression> members) {
        addTransactional(source, renamedMethodNode, members)
        compileMethodStatically(source, renamedMethodNode, members)
    }
}
