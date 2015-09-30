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

import ${packageName}.User

/**
 *  This trait is for the EnforcerService, extending it's capability to enforcing user roles, without the verbosity of calling a service.
 */
trait RoleTrait {

    /**
     * This method check to see if a user had a given role
     *
     * @param role the role/authority to check
     * @param user the user to check to see if it has a role(defaulted to springSecurityService.currentUser)
     * @return true if the user does had the role, false otherwise.
     */
    boolean hasRole(String role, User user = null){
        if(!user){
            user = springSecurityService.currentUser
        }

        role in user.authorities*.authority
    }
}
