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

import groovy.transform.TypeCheckingMode
import org.codehaus.groovy.transform.GroovyASTTransformationClass

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * The annotation enforce takes up to 3 closures can injects a call to the enforce method of the enforcerService
 * at the end of the method before returning
 * .
 * This can be applied to a method or a class, but the method will take precedence.
 *
 * This also applies static compilation to the method using the transform from @CompileStatic.
 *
 * The first closure is value, just so that the transform can be called without naming the parameter.
 * If your specifying two or more closures you will have to specify there names in the annotation call.
 * Examples:
 * @Reinforce ( { true } )
 * @Reinforce ( value = { true } , failure = { println " nice " } )
 * @Reinforce ( value = { true } , failure = { println " nice " } , success = { println " not nice " } )
 * @Reinforce ( value = { false } , failure = { println " not nice " } , success = { println " nice " } )
 *
 * parameters
 * value is the predicate for the enforce service, named value so that you don't have to name it
 * failure is the code to run if the predicate returns false, if not specified, the default for the enforcerService is used.
 * success the code to run if the predicate returns true, if not specified, the default for the enforcerService is used.
 * TypeCheckingMode the type checking mode pass or skip.
 * extensions any type extensions you would like to add, by default this annotation adds the same extensions as @GrailsCompileStatic.
 */
@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.TYPE, ElementType.METHOD])
@GroovyASTTransformationClass("com.virtualdogbert.ast.ReinforceSASTTransformation")
public @interface ReinforceS {
    Class value()

    Class failure() default { false }

    Class success() default { true }

    TypeCheckingMode staticValue() default TypeCheckingMode.PASS;

    /**
     * The list of (classpath resources) paths to type checking DSL scripts, also known
     * as type checking extensions.
     * @return an array of paths to groovy scripts that must be on compile classpath
     */
    String[] extensions() default ['org.grails.compiler.ValidateableTypeCheckingExtension',
            'org.grails.compiler.NamedQueryTypeCheckingExtension',
            'org.grails.compiler.HttpServletRequestTypeCheckingExtension',
            'org.grails.compiler.WhereQueryTypeCheckingExtension',
            'org.grails.compiler.DynamicFinderTypeCheckingExtension',
            'org.grails.compiler.DomainMappingTypeCheckingExtension',
            'org.grails.compiler.RelationshipManagementMethodTypeCheckingExtension']
}