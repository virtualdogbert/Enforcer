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

import com.security.Role
import com.security.User
import com.security.UserRole
import grails.gorm.transactions.Transactional

class UserService {

    @Transactional
    void initUsers() {
        Role userRole = new Role(authority: 'ROLE_USER').save(flush: true, failOnError: true)
        Role adminRole = new Role(authority: 'ROLE_ADMIN').save(flush: true, failOnError: true)

        User testUser = new User(username: 'me', password: 'password').save(flush: true, failOnError: true)
        User testUser2 = new User(username: 'me2', password: 'password').save(flush: true, failOnError: true)

        UserRole.create testUser, adminRole, true
        UserRole.create testUser, userRole, true

        UserRole.create testUser2, userRole, true
    }
}
