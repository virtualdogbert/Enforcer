package services.${packageName}

import ${packageName}.DomainRole
import ${packageName}.Role
import ${packageName}.User
import ${packageName}.UserRole
import ${packageName}.EnforcerService
import com.virtualdogbert.ast.Enforce
import com.virtualdogbert.ast.EnforcerException
import grails.plugin.springsecurity.SpringSecurityService
import grails.test.mixin.Mock
import grails.test.mixin.TestFor
import grails.core.DefaultGrailsApplication
import spock.lang.Specification

/**
 * See the API for {@link grails.test.mixin.services.ServiceUnitTestMixin} for usage instructions
 */

@Mock([Role, User, UserRole, DomainRole])
@TestFor(EnforcerService)
class EnforcerServiceSpec extends Specification {

    def testUser

    def setup() {
        def adminRole = new Role('ROLE_ADMIN').save(flush: true, failOnError: true)
        def userRole = new Role('ROLE_USER').save(flush: true, failOnError: true)
        testUser = new User(username: 'me', password: 'password').save(flush: true, failOnError: true)

        UserRole.create testUser, adminRole, true
        UserRole.create testUser, userRole, true

        SpringSecurityService.metaClass.getCurrentUser = {-> testUser }

        service.grailsApplication = new DefaultGrailsApplication()
        service.grailsApplication.config.enforcer.enabled = true//This enables Enforcer for unit tests because it is turned off by default.
    }

    //Testing EnforcerService
    void 'test enforce { true }'() {
        when:
            service.enforce({ true })
        then:
            true
    }

    void 'test enforce { false }'() {
        when:
            service.enforce({ false })
        then:
            EnforcerException e = thrown()
            e.message == 'Access Denied'
    }

    void 'test enforce { true }, { throw new EnforcerException("not nice") }'() {
        when:
            service.enforce({ true }, { throw new EnforcerException("not nice") })
        then:
            true
    }

    void 'test enforce { false }, { throw new EnforcerException("nice") }'() {
        when:
            service.enforce({ false }, { throw new EnforcerException("nice") })
        then:
            thrown EnforcerException
    }

    void 'test enforce { true }, { throw new EnforcerException("not nice")}, { println "nice" }'() {
        when:
            service.enforce({ true }, { throw new EnforcerException("not nice") }, { println "nice" })
        then:
            true
    }

    void 'test enforce { false }, { throw new EnforcerException("nice") }, { throw new EnforcerException("not nice") }'() {
        when:
            service.enforce({ false }, { throw new EnforcerException("nice") }, { println("not nice") })
        then:
            thrown EnforcerException
    }

    //Testing DomainRoleTrait
    void 'test enforce hasDomainRole("owner", "DomainRole", DomainRole.id, testUser)'() {
        when:
            DomainRole domainRole = new DomainRole(role: 'owner', domainName: 'DomainRole', domainId: 10, user: testUser )
            domainRole.save(failOnError: true)
            service.changeDomainRole('owner', 'DomainRole', domainRole.id, testUser)
            service.enforce({ hasDomainRole('owner', 'DomainRole', domainRole.id, testUser) })
        then:
            true
    }

    void 'test fail enforce hasDomainRole("owner", "DomainRole", 1, testUser)'() {
        when:
            service.enforce({ hasDomainRole('owner', 'DomainRole', 1, testUser) })
        then:
            thrown EnforcerException
    }

     //Testing RoleTrait
    void 'test enforce hasRole("ROLE_ADMIN", testUser)'(){
        when:
            service.enforce({ hasRole('ROLE_ADMIN', testUser) })
        then:
            true
    }

    void 'test enforce hasRole("ROLE_USER", testUser)'(){
        when:
            service.enforce({ hasRole('ROLE_USER', testUser) })
        then:
            true
    }

    void 'test enforce hasRole("ROLE_ADMIN", testUser)'(){
        when:
            service.enforce({ hasRole('ROLE_SUPER_USER', testUser) })
        then:
            thrown EnforcerException
    }

    //Testing Enforce AST transform
    void 'test method 1'() {
        when:
            method1()
        then:
            true
    }

    void 'test method 2'() {
        when:
            method2()
        then:
            true
    }

    void 'test method 3'() {
        when:
            method3()
        then:
            thrown EnforcerException
    }

    void 'test method 4'() {
        when:
            method4()
        then:
            true
    }

    void 'test method 5'() {
        when:
            method5()
        then:
            thrown EnforcerException
    }

    void 'test method 6'() {
        when:
            method6(5)
        then:
            true
    }


    //Test methods for testing Enforce AST transform
    @Enforce({ true })
    def method1() {
        println 'nice'
    }

    @Enforce(value = { true }, failure = { throw new EnforcerException("not nice") })
    def method2() {
        println 'nice'
    }

    @Enforce(value = { false }, failure = { throw new EnforcerException("nice") })
    def method3() {
        throw new EnforcerException("this shouldn't happen on method3")
    }

    @Enforce(value = { true }, failure = { throw new EnforcerException("not nice") }, success = { println "nice" })
    def method4() {

    }

    @Enforce(value = { false }, failure = { throw new EnforcerException("nice") }, success = { println "not nice" })
    def method5() {
        throw new EnforcerException("this shouldn't happen on method5")
    }

    @Enforce({ number == 5 })
    def method6(number) {
        println 'nice'
    }
}
