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
import org.grails.cli.interactive.completers.DomainClassCompleter

description("Installs default implementation for Enforcer, which can be changed/enhanced by the user") {
    usage "grails enforcer-quickstart [package]"
    argument name: 'package', description: 'The name of the package you installed spring security core under'
    completer DomainClassCompleter
}

model = [packageName: args[0]]

addStatus "Installing Enforcer defaults"


render template: template("DomainRole.groovy.template"),
        destination: file("grails-app/domain/com/security/enforcer/DomainRole.groovy"),
        model: model,
        overwrite: true

render template: template("DomainRoleTrait.groovy.template"),
        destination: file("grails-app/services/com/security/enforcer/DomainRoleTrait.groovy"),
        model: model,
        overwrite: true

render template: template("InstalledEnforcerService.groovy.template"),
        destination: file("grails-app/services/com/security/enforcer/InstalledEnforcerService.groovy"),
        model: model,
        overwrite: true

render template: template("RoleTrait.groovy.template"),
        destination: file("grails-app/services/com/security/enforcer/RoleTrait.groovy"),
        model: model,
        overwrite: true

render template: template("EnforcerServiceSpec.groovy.template"),
        destination: file("src/test/groovy/services/com/security/enforcer/EnforcerServiceSpec.groovy"),
        model: model,
        overwrite: true

render template: template("EnforcerAnnotationSpec.groovy.template"),
        destination: file("src/test/groovy/services/com/security/enforcer/EnforcerAnnotationSpec.groovy"),
        model: model,
        overwrite: true

render template: template("ReinforceAnnotationSpec.groovy.template"),
        destination: file("src/test/groovy/services/com/security/enforcer/ReinforceAnnotationSpec.groovy"),
        model: model,
        overwrite: true

def beansList = [[import: "import com.security.enforcer.InstalledEnforcerService", definition: """
    enforcerService(InstalledEnforcerService) {
        grailsApplication = ref('grailsApplication')
        springSecurityService = ref('springSecurityService')
    }
""".toString()]]
addBeans(beansList, 'grails-app/conf/spring/resources.groovy')

addStatus "Installing Enforcer defaults complete"

//Snagged from https://github.com/grails-plugins/grails-spring-security-core/blob/master/plugin/src/main/scripts/s2-quickstart.groovy
private void addBeans(List<Map> beans, String pathname) {
    File file = new File(pathname)
    List lines = []

    beans.each { Map bean ->
        lines << bean.import
    }

    if (file.exists()) {
        file.eachLine { String line ->
            lines << line

            if (line.contains('beans = {')) {
                beans.each { Map bean ->
                    lines << bean.definition
                }
            }
        }
    } else {
        lines << 'beans = {'

        beans.each { Map bean ->
            lines << bean.definition
        }

        lines << '}'
    }

    file.withWriter('UTF-8') { writer ->

        lines.each { String line ->
            writer.write "${line}${System.lineSeparator()}"
        }
    }
}