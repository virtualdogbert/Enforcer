class EnforcerGrailsPlugin {
    // the plugin version
    def version = "0.2.1"
    // the version or versions of Grails the plugin is designed for
    def grailsVersion = "2.4 > *"
    // resources that are excluded from plugin packaging
    def pluginExcludes = [
        "grails-app/views/error.gsp"
    ]

    def title = "The Enforcer Plugin" // Headline display name of the plugin
    def author = "Tucker J. Pelletier AKA virtualdogbert"
    def description = '''
The Enforcer plug-in is an alternative for Spring Security ACL, that is easier to implment, and extend.
'''

    // URL to the plugin's documentation
    def documentation = "https://virtualdogbert.github.io/Enforcer"
    def license = "APACHE"

    // Any additional developers beyond the author specified above.
    def developers = [ [ name: "Tucker J. Pelletier"]]

    // Location of the plugin's issue tracker.
    def issueManagement = [ system: "GITHUB", url: "https://github.com/virtualdogbert/Enforcer/issues" ]

    // Online location of the plugin's browseable source code.
    def scm = [ url: "https://github.com/virtualdogbert/Enforcer" ]
}
