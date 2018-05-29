package testenforcer33

import com.security.User
import grails.core.GrailsApplication
import grails.plugin.springsecurity.SpringSecurityService
import grails.plugin.springsecurity.annotation.Secured
import grails.plugins.*

@Secured('permitAll')
class ApplicationController implements PluginManagerAware {

    GrailsApplication     grailsApplication
    GrailsPluginManager   pluginManager
    SpringSecurityService springSecurityService

    def index() {
        [
                user             : springSecurityService?.currentUser?.username ?: 'not logged in',
                roles            : ((User) springSecurityService?.currentUser)?.authorities*.authority ?: [],
                grailsApplication: grailsApplication,
                pluginManager    : pluginManager]
    }
}
