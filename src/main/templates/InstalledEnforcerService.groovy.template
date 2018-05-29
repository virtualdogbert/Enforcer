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
 */

package com.security.enforcer

import grails.core.GrailsApplication
import grails.plugin.springsecurity.SpringSecurityService
import grails.transaction.Transactional
import grails.util.Environment

/**
 * The EnforcerService has one enforce method for enforcing business rules, and is extended by the traits it implements.
 */
@Transactional
class InstalledEnforcerService extends EnforcerService implements RoleTrait,DomainRoleTrait{

    GrailsApplication     grailsApplication
    SpringSecurityService springSecurityService

    /**
     * The enforce method enforced business rules given 3 closures, a predicate to check, failure and success to rune depending on if the
     * predicate returns true. The enforce method will run if the environment is not TEST or the grailsApplication.config.enforcer.enabled is
     * set true
     *
     * @param predicate a closure that when run it's result will be checked using groovy's truth model and  if true the success closure is run else the failure closure is run.
     * @param failure the failure closure to run if the predicate results false, this is defaulted to throw new EnforcerException("Access Denied")
     * @param success the success closure to run if  the predicate results true, this is defaulted to return true
     */
    @Override
    def enforce(
            @DelegatesTo(InstalledEnforcerService)Closure predicate,
            @DelegatesTo(InstalledEnforcerService)Closure failure = { -> throw new EnforcerException("Access Denied") },
            @DelegatesTo(InstalledEnforcerService)Closure success = { -> true }) {

        if (Environment.current != Environment.TEST || grailsApplication.config.enforcer.enabled) {
            predicate.delegate = this
            failure.delegate = this
            success.delegate = this

            if (predicate()) {
                success()
            } else {
                failure()
            }
        }

    }

    /**
     * this is used to filter the values of a return statement. This is meant to be used by the ReinforceFilter AST transform
     *
     * @param filter a closure that takes and object value, and filters it, and returns the filtered value.
     * @param value the object to be filtered.
     *
     * @return the filtered value.
     */
    @Override
    Object ReinforceFilter(@DelegatesTo(InstalledEnforcerService)Closure filter, Object value) {
        filter(value)
    }
}
