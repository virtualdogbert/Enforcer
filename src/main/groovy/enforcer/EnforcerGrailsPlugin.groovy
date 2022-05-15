package enforcer

import grails.plugins.*

class EnforcerGrailsPlugin extends Plugin {
    def grailsVersion = "5.1.7 > *"
    def title = 'Enforcer'
    def author = 'Tucker J. Pelletier AKA virtualdogbert'
    def description = 'A plugin for enforcing business rules/permissions, that works with Spring Security Core, is easier to implement, and extend. It can also be used as an alternative to Spring Security ACL'
    def profiles = ['web']
    def documentation = 'https://virtualdogbert.github.io/Enforcer'
    def license = 'APACHE'
    def developers = [ [ name: 'Tucker J. Pelletier']]
    def issueManagement = [ system: 'GITHUB', url: 'https://github.com/virtualdogbert/Enforcer/issues' ]
    def scm = [ url: 'https://github.com/virtualdogbert/Enforcer' ]
}
