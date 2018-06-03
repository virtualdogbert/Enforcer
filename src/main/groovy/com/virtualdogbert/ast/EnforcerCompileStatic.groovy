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
 * The annotation  will statically compile a method or class, with the method taking precedence, however it won't interfere with enforce
 * based annotations, like the traditional @CompileStatic will. This annotation takes the same parameters as @CompileStatic as it uses the
 * same transform under the covers.
 *
 * parameters
 * TypeCheckingMode the type checking mode pass or skip.
 * extensions any type extensions you would like to add, by default this annotation adds the same extensions as @GrailsCompileStatic.
 */
@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.TYPE, ElementType.METHOD])
@GroovyASTTransformationClass("com.virtualdogbert.ast.EnforceSASTTransformation")
@interface EnforcerCompileStatic {
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
            'org.grails.compiler.RelationshipManagementMethodTypeCheckingExtension'];
}