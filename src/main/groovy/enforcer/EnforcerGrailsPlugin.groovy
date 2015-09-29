package enforcer

import grails.plugins.*

class EnforcerGrailsPlugin extends Plugin {
    def grailsVersion = "3.0.4 > *"
    def title = "Enforcer"
    def author = "Tucker J. Pelletier AKA virtualdogbert"
    def description = 'An alternative for Spring Security ACL, that is easier to implment, and extend.'
    def profiles = ['web']
    def documentation = "https://virtualdogbert.github.io/Enforcer"
    def license = "APACHE"
    def developers = [ [ name: "Tucker J. Pelletier"]]
    def issueManagement = [ system: "GITHUB", url: "https://github.com/virtualdogbert/Enforcer/issues" ]
    def scm = [ url: "https://github.com/virtualdogbert/Enforcer" ]
}
