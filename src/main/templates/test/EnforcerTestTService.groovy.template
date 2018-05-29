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
import com.virtualdogbert.ast.*
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityService

@EnforcerTransactional
class EnforcerTestTService {
    SpringSecurityService springSecurityService

    @Transactional
    Sprocket createSprocket() {
        springSecurityService.currentUser

        Sprocket sprocket = new Sprocket(
                material: 'metal',
                creator: springSecurityService.currentUser
        )

        sprocket.save()
    }


    Sprocket createSprocketException() {
        springSecurityService.currentUser

        Sprocket sprocket = new Sprocket(
                material: 'metal',
                creator: springSecurityService.currentUser
        )

        sprocket.save()
        throw new RuntimeException('this should cause a rollback.')
    }

    @EnforceT({ isCreator(sprocket) })
    Sprocket updateSprocket(Sprocket sprocket) {
        sprocket.material = 'plastic'
        sprocket.save()
    }

    @EnforceTS({ isCreator(sprocket) })
    Sprocket updateSprocketCompileStatic(Sprocket sprocket) {
        sprocket.material = 'plastic'
        (Sprocket) sprocket.save()
    }

    @EnforceT({ isCreator(sprocket) })
    Sprocket updateSprocketException(Sprocket sprocket) {
        sprocket.material = 'plastic'
        sprocket.save()
        throw new RuntimeException('this should cause a rollback.')
        sprocket
    }

    @EnforceTS({ isCreator(sprocket) })
    Sprocket updateSprocketCompileStaticException(Sprocket sprocket) {
        sprocket.material = 'plastic'
        (Sprocket) sprocket.save()
        throw new RuntimeException('this should cause a rollback.')
        sprocket
    }

    @Enforce({ isCreator(sprocket) })
    Sprocket getSprocket(Sprocket sprocket) {
        return sprocket
    }

     def wrapClosureTestingParameter(Integer number){
        closureTestingParameter(number)
    }

    @Enforce({ number == 5 })
    def closureTestingParameter(Integer number) {
        println 'nice'
    }

    @Enforce(
            {
                numbers = numbers.findResults { it % 2 == 0 ? it : null }
                return true
            }
    )
    def closureFilterParameter(List numbers) {
        return numbers
    }

    @Reinforce({ number == 5 })
    def reinforceClosureTestingParameter(number) {
        println 'nice'
    }
}
