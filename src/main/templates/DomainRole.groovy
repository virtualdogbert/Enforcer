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

/**
 * This is for adding roles at an object level for business rules
 */
class DomainRole {
    String role        //The role to apply to the object
    Long   domainId    //The id of the object
    String domainName  //The domain name of the object
    User   user        //The user associated with the permission
    Date   dateCreated
    Date   lastUpdated

    static constraints = {
        role inList: ['owner', 'editor', 'viewer']
        dateCreated nullable: true
        lastUpdated nullable: true
    }

    static mapping = {
        version false
        cache true
    }
}
