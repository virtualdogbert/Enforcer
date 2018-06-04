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

description("Installs testing files to test the enforcer plugin.") {
    usage "grails enforcer-testing [package]"
    argument name: 'package', description: 'The name of the package you installed spring security core under'
    completer DomainClassCompleter
}

model = [packageName: args[0]]

addStatus "Installing Enforcer testing setup"


render template: template("test/application.groovy.template"),
        destination: file("grails-app/config/application.groovy"),
        model: model,
        overwrite: true

render template: template("test/ApplicationController.groovy.template"),
        destination: file("grails-app/controllers/com/security/enforcer/ApplicationController.groovy"),
        model: model,
        overwrite: true

render template: template("test/BootStrap.groovy.template"),
        destination: file("grails-app/init/BootStrap.groovy"),
        model: model,
        overwrite: true

render template: template("test/CreatorTrait.groovy.template"),
        destination: file("grails-app/services/com/security/enforcer/CreatorTrait.groovy"),
        model: model,
        overwrite: true

render template: template("test/EnforcerTestTService.groovy.template"),
        destination: file("grails-app/services/com/security/enforcer/EnforcerTestTService.groovy"),
        model: model,
        overwrite: true

render template: template("test/EnforcerTestTSService.groovy.template"),
        destination: file("grails-app/services/com/security/enforcer/EnforcerTestTSService.groovy"),
        model: model,
        overwrite: true

render template: template("test/integration/EnforcerTestTServiceSpec.groovy.template"),
        destination: file("src/integration-test/groovy/services/services/com/security/enforcer/EnforcerTestTServiceSpec.groovy"),
        model: model,
        overwrite: true

render template: template("test/unit/EnforcerTestTServiceSpec.groovy.template"),
        destination: file("src/test/groovy/services/services/com/security/enforcer/EnforcerTestTServiceSpec.groovy"),
        model: model,
        overwrite: true

render template: template("test/integration/EnforcerTestTSServiceSpec.groovy.template"),
        destination: file("src/integration-test/groovy/services/com/security/enforcer/EnforcerTestTSServiceSpec.groovy"),
        model: model,
        overwrite: true

render template: template("test/Sprocket.groovy.template"),
        destination: file("grails-app/domain/com/security/Sprocket.groovy"),
        model: model,
        overwrite: true

render template: template("test/TestController.groovy.template"),
        destination: file("grails-app/controllers/com/security/enforcer/TestController.groovy"),
        model: model,
        overwrite: true

render template: template("test/UserService.groovy.template"),
        destination: file("grails-app/services/com/security/enforcer/UserService.groovy"),
        model: model,
        overwrite: true

render template: template("test/application/index.gson.template"),
        destination: file("grails-app/views/application/index.gson"),
        model: model,
        overwrite: true

render template: template("test/test/index.gson.template"),
        destination: file("grails-app/views/test/index.gson"),
        model: model,
        overwrite: true

addStatus "Installing Enforcer testing setup complete"
