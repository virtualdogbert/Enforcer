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

import com.security.Sprocket
import com.security.User

/**
 * This trait is for the EnforcerService, extending it's capability to enforcing domain roles, without the verbosity of calling a service.
 */
trait CreatorTrait {
    def springSecurityService



    /**
         * This method checks the domain object to see if it has a reference to a user(passed in or defaulted to springSecurityService.currentUser)
         * This makes it so that the original creator of an object can add permissions to that object.
         *
         * @param domainObject The domain object to check for a user reference domainObject.creator
         * @param user  the user(defaulted to springSecurityService.currentUser) to compare to domainObject.creator
         * @return true if the user is the same as the creator user reference, false otherwise
         */
    Boolean isCreator(Sprocket domainObject, User user = null) {
        if (!user) {
            return domainObject.creator == springSecurityService.currentUser
        }

        domainObject.creator.id == user.id
    }

}