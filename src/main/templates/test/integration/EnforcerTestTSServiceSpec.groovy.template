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
package services.com.security.enforcer

import com.security.Sprocket
import com.security.User
import com.security.enforcer.EnforcerTestTSService
import grails.gorm.transactions.Transactional
import grails.plugin.springsecurity.SpringSecurityService
import grails.testing.mixin.integration.Integration
import spock.lang.Specification

@Integration
@Transactional
class EnforcerTestTSServiceSpec extends Specification {
    EnforcerTestTSService enforcerTestTSService

    def setup() {
        enforcerTestTSService.springSecurityService = [getCurrentUser: { -> User.get(1) }] as SpringSecurityService
    }

    void 'test createSprocket'() {
        when:
            enforcerTestTSService.createSprocket()
            Sprocket sprocket = Sprocket.get(1)
        then:
            sprocket.material == "metal"
    }

    void 'test updateSprocket'() {
        when:
            Sprocket sprocketSaved = enforcerTestTSService.createSprocket()
            enforcerTestTSService.updateSprocket(sprocketSaved)
            Sprocket sprocket = Sprocket.get(2)
        then:
            sprocket.material == "plastic"
    }

    void 'test updateSprocketCompileStatic'() {
        when:
            Sprocket sprocketSaved = enforcerTestTSService.createSprocket()
            enforcerTestTSService.updateSprocketCompileStatic(sprocketSaved)
            Sprocket sprocket = Sprocket.get(3)
        then:
            sprocket.material == "plastic"
    }

    void 'test updateSprocketException'() {
        when:
            Sprocket sprocketSaved

            Sprocket.withNewTransaction {
                sprocketSaved = enforcerTestTSService.createSprocket()
            }

            enforcerTestTSService.updateSprocketException(sprocketSaved)
        then:
            thrown RuntimeException

            Sprocket.withNewTransaction {
                Sprocket.get(4).material == 'metal'
            }
    }

    void 'test updateSprocketCompileStaticException'() {
        when:
            Sprocket sprocketSaved

            Sprocket.withNewTransaction {
                sprocketSaved = enforcerTestTSService.createSprocket()
            }

            enforcerTestTSService.updateSprocketCompileStaticException(sprocketSaved)
        then:
            thrown RuntimeException

            Sprocket.withNewTransaction {
                Sprocket.get(5).material == 'metal'
            }
    }

    void 'test createSprocketException'() {
        when:
            enforcerTestTSService.createSprocketException()
        then:
            thrown RuntimeException

            Sprocket.withNewTransaction {
                Sprocket.get(6) == null
            }
    }


}

