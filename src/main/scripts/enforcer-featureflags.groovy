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

description("Installs default implmentaiton for Enforcer, whch can be changed/enhanced by the user") {
    usage "grails enforcer-featureflags [package] [listOfDomains]"
    argument name: 'package', description: 'The name of the package to put the enforcer files under'
    argument name: 'listOfDomains', description: 'this is a comma separated list of fully qualified domain classes'
    completer DomainClassCompleter
}

domains = args[1].split(',').collect{
   lastDot =  it.lastIndexOf('.')
    domainName = it[lastDot+1..-1]
 [packageName: args[0], domainName: domainName, lowerDomainName: domainName[0].toLowerCase() + domainName[1..-1], domainPackage: it[0..lastDot-1]]
}

dir = args[0].replace('.', '/')
importDomains = ''
checkOtherFeatureFlags = ''

addStatus "Installing Enforcer feature flags"


render template: template("FeatureFlag.groovy"),
        destination: file("grails-app/domain/${dir}/FeatureFlag.groovy"),
        model: [packageName: args[0]],
        overwrite: true

domains.each{
    importDomains += "import ${it.domainPackage}${it.domainName}\n"
    checkOtherFeatureFlags += "        else if(Feature${it.domainName}.findByFeatureFlagAnd${it.domainName}(featureFlag,?????)?.enabled){return true}\n"
    render template: template("FeatureFlagDomain.groovy"),
            destination: file("grails-app/domain/${dir}/FeatureFlag${it.domainName}.groovy"),
            model: it,
            overwrite: true
}

render template: template("FeatureFlagTrait.groovy"),
        destination: file("grails-app/services/${dir}/FeatureFlagTrait.groovy"),
        model: [packageName: args[0], importDomains: importDomains, checkOtherFeatureFlags:checkOtherFeatureFlags],
        overwrite: true


addStatus "Installing Enforcer feature flags complete, but you need to manually add the FeatureFlagTrait to the EnforcerService."
addStatus "You will also need to update the FeatureFlagTrait to lookup the domain, that's linked toe the FeatureDomain."
