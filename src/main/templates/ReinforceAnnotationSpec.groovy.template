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

import com.security.enforcer.EnforcerService
import com.virtualdogbert.ast.EnforcerException
import com.virtualdogbert.ast.Reinforce
import com.virtualdogbert.ast.ReinforceFilter
import grails.test.mixin.TestFor
import spock.lang.Specification

@TestFor(EnforcerService)
class ReinforceAnnotationSpec extends Specification {

    def setup() {
        //This enables Enforcer for unit tests because it is turned off by default.
        grailsApplication.config.enforcer.enabled = true
    }

    void 'test method reinforceClosureTrue'() {
        when:
            reinforceClosureTrue()
        then:
            true
    }

    void 'test method reinforceClosureTrueWithFailureClosure'() {
        when:
            reinforceClosureTrueWithFailureClosure()
        then:
            true
    }

    void 'test method reinforceClosureFalseWithFailureClosure'() {
        when:
            reinforceClosureFalseWithFailureClosure()
        then:
            thrown EnforcerException
    }

    void 'test method reinforceClosureTrueWithFailureAndSuccessClosures'() {
        when:
            reinforceClosureTrueWithFailureAndSuccessClosures()
        then:
            true
    }

    void 'test method reinforceClosureFalseWithFailureAndSuccessClosures'() {
        when:
            reinforceClosureFalseWithFailureAndSuccessClosures()
        then:
            thrown EnforcerException
    }

    void 'test method reinforceClosureTestingParameter'() {
        when:
            reinforceClosureTestingParameter(5)
        then:
            true
    }

    void 'test class protection'() {
        setup:
            TestEnforcer t = new TestEnforcer()
        when:
            t.clazzProtectedMethod1()
        then:
            thrown EnforcerException
        when:
            t.clazzProtectedMethod2()
        then:
            thrown EnforcerException
        when:
            t.methodProtectedMethod1()
        then:
            true
    }

    void 'test method reinforceFilter'() {
        when:
            def returnedList = reinforceFilter()
        then:
            returnedList == [2, 4, 6, 8]
    }

    @Reinforce({ true })
    def reinforceClosureTrue() {
        println 'nice'
    }

    @Reinforce({ false })
    def reinforceClosureFalse() {
        println 'nice'
    }

    @Reinforce(value = { true }, failure = { throw new EnforcerException("not nice") })
    def reinforceClosureTrueWithFailureClosure() {
        println 'nice'
    }

    @Reinforce(value = { false }, failure = { throw new EnforcerException("nice") })
    def reinforceClosureFalseWithFailureClosure() {
        throw new Exception("this shouldn't happen on closureFalseWithFailureClosure")
    }

    @Reinforce(value = { true }, failure = { throw new EnforcerException("not nice") }, success = { println "nice" })
    def reinforceClosureTrueWithFailureAndSuccessClosures() {

    }

    @Reinforce(value = { false }, failure = { throw new EnforcerException("nice") }, success = { println "not nice" })
    def reinforceClosureFalseWithFailureAndSuccessClosures() {
        throw new Exception("this shouldn't happen on closureFalseWithFailureAndSuccessClosures")
    }

    @Reinforce({ number == 5 })
    def reinforceClosureTestingParameter(number) {
        println 'nice'
    }

    @Reinforce({ false })
    class TestEnforcer {
        @Reinforce(value = { false }, failure = { throw new EnforcerException("nice") })
        def clazzProtectedMethod1() {
            println 'not nice'
        }

        def clazzProtectedMethod2() {
            println 'not nice'
        }

        @Reinforce({ true })
        def methodProtectedMethod1() {
            println 'nice'
        }
    }

    @ReinforceFilter({ Object o -> (o as List).findResults { it % 2 == 0 ? it : null } })
    List<Integer> reinforceFilter() {
        [1, 2, 3, 4, 5, 6, 7, 8, 9]
    }
}
