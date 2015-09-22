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

package ${packageName}

import com.virtualdogbert.ast.EnforcerException
import grails.transaction.Transactional
import grails.util.Environment

@Transactional
class EnforcerService implements RoleTrait,DomainRoleTrait{

    def grailsApplication
    def springSecurityService

    def enforce(Closure predicate, Closure failure = { throw new EnforcerException("Access Denied") }, Closure success = { return true }) {

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
}
