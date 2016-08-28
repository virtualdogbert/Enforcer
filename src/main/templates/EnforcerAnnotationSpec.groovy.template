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
package services.${packageName}

import ${packageName}.EnforcerService
import com.virtualdogbert.ast.Enforce
import com.virtualdogbert.ast.EnforcerException
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.core.DefaultGrailsApplication
import spock.lang.Specification

@TestFor(EnforcerService)
class EnforcerAnnotationSpec extends Specification {

    def setup() {
        //This enables Enforcer for unit tests because it is turned off by default.
        grailsApplication.config.enforcer.enabled = true
    }


    //Testing Enforce AST transform
    void 'test method closureTrue'() {
        when:
            closureTrue()
        then:
            true
    }

    void 'test method closureTrueWithFailureClosure'() {
        when:
            closureTrueWithFailureClosure()
        then:
            true
    }

    void 'test method closureFalseWithFailureClosure'() {
        when:
            closureFalseWithFailureClosure()
        then:
            thrown EnforcerException
    }

    void 'test method closureTrueWithFailureAndSuccessClosures'() {
        when:
            closureTrueWithFailureAndSuccessClosures()
        then:
            true
    }

    void 'test method closureFalseWithFailureAndSuccessClosures'() {
        when:
            closureFalseWithFailureAndSuccessClosures()
        then:
            thrown EnforcerException
    }

    void 'test method closureTestingParameter'() {
        when:
            closureTestingParameter(5)
        then:
            true
    }

    void 'test method closureFilterParameter'() {
        when:
            def test = closureFilterParameter([1, 2, 3, 4, 5, 6, 7, 8])
        then:
            test == [2, 4, 6, 8]
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

    //Test methods for testing Enforce AST transform
    @Enforce({ true })
    def closureTrue() {
        println 'nice'
    }

    @Enforce(value = { true }, failure = { throw new EnforcerException("not nice") })
    def closureTrueWithFailureClosure() {
        println 'nice'
    }

    @Enforce(value = { false }, failure = { throw new EnforcerException("nice") })
    def closureFalseWithFailureClosure() {
        throw new Exception("this shouldn't happen on closureFalseWithFailureClosure")
    }

    @Enforce(value = { true }, failure = { throw new EnforcerException("not nice") }, success = { println "nice" })
    def closureTrueWithFailureAndSuccessClosures() {

    }

    @Enforce(value = { false }, failure = { throw new EnforcerException("nice") }, success = { println "not nice" })
    def closureFalseWithFailureAndSuccessClosures() {
        throw new Exception("this shouldn't happen on closureFalseWithFailureAndSuccessClosures")
    }

    @Enforce({ number == 5 })
    def closureTestingParameter(number) {
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

    @Enforce({ false })
    class TestEnforcer {
        @Enforce(value = { false }, failure = { throw new EnforcerException("nice") })
        def clazzProtectedMethod1() {
            println 'not nice'
        }

        def clazzProtectedMethod2() {
            println 'not nice'
        }

        @Enforce({ true })
        def methodProtectedMethod1() {
            println 'nice'
        }
    }

}
