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

import ${packageName}.DomainRole
import ${packageName}.User
import com.virtualdogbert.ast.Enforce

trait DomainRoleTrait {

    boolean hasDomainRole(String role, String domainName, Long id, User user = null) {
        if (!user) {
            user = springSecurityService.currentUser
        }

        Map roleHierarchy = [
                owner : ['owner', 'editor', 'viewer'],
                editor: ['editor', 'viewer'],
                viewer: ['viewer']
        ]
        DomainRole domainRole = DomainRole.where { role == role && domainName == domainName && domainId == id && user == user }.find()
        domainRole?.role in roleHierarchy[role]
    }

    @Enforce({ hasDomainRole('owner', domainName, id) || haRole('ROLE_ADMIN') })
    void changeDomainRole(String role, String domainName, Long id, User user = null) {
        if (!user) {
            user = springSecurityService.currentUser
        }

        DomainRole domainRole = DomainRole.where { domainName == domainName && domainId == id && user == user }.find()

        if (domainRole) {
            domainRole.role = role
        } else {
            domainRole = new DomainRole(role: role, domainName: domainName, domainId: id, user: user)
        }

        domainRole.save()
    }

    @Enforce({ hasDomainRole('owner', domainName, id) || haRole('ROLE_ADMIN') })
    void removeDomainRole(String domainName, Long id, User user = null) {
        if (!user) {
            user = springSecurityService.currentUser
        }

        DomainRole domainRole = DomainRole.where { domainName == domainName && domainId == id && user == user }.find()

        domainRole?.delete()
    }
}
