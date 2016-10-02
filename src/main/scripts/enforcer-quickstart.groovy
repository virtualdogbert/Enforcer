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
    argument name: 'package', description: 'The name of the package to put the enforcer files under'
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

render template: template("EnforcerService.groovy.template"),
        destination: file("grails-app/services/com/security/enforcer/EnforcerService.groovy"),
        model: model,
        overwrite: true

render template: template("RoleTrait.groovy.template"),
        destination: file("grails-app/services/com/security/enforcer/RoleTrait.groovy"),
        model: model,
        overwrite: true

render template: template("EnforcerServiceSpec.groovy.template"),
        destination: file("src/test/groovy/unit/services/com/security/enforcer/EnforcerServiceSpec.groovy"),
        model: model,
        overwrite: true

render template: template("EnforcerAnnotationSpec.groovy.template"),
        destination: file("src/test/groovy/unit/services/com/security/enforcer/EnforcerAnnotationSpec.groovy"),
        model: model,
        overwrite: true

render template: template("ReinforceAnnotationSpec.groovy.template"),
        destination: file("src/test/groovy/unit/services/com/security/enforcer/ReinforceAnnotationSpec.groovy"),
        model: model,
        overwrite: true

addStatus "Installing Enforcer defaults complete"
