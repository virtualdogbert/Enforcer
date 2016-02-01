import org.grails.cli.interactive.completers.DomainClassCompleter

description("Installs default implmentaiton for Enforcer, whch can be changed/enhanced by the user") {
    usage "grails enforcer-featureflags [package] [listOfDomains]"
    argument name: 'package', description: 'The name of the package to put the enforcer files under'
    argument listOfDomains: 'listOfDomains', description: 'this is a comma seperated list of fully qualified domain classes'
    completer DomainClassCompleter
}

domains = args[1].split(',').collect{
   lastDot =  domainClass.lastIndexOf('.')
    domainName = it[lastDot+1..-1]
 [package: args[0], domainName: domainName, lowerDomainName: domainName[0].toLowerCase() + domainName[1..-1], domainPackage: it[0..lastDot-1]]
}

dir = args[0].replace('.', '/')
importDomains = ''
checkOtherFeatureFlags ''

addStatus "Installing Enforcer feature flags"


render template: template("FeatureFlag.groovy"),
        destination: file("grails-app/domain/${dir}/FeatureFlag.groovy"),
        model: [package: args[0]],
        overwrite: true

domains.each{
    importDomains += "import ${it.domainPackage}${it.domainName}\n"
    checkOtherFeatureFlags += "        else if(Feature${it.domainName}.findByFeatureFlagAnd${domainName}(featureFlag,?????)?.enabled){return true}\n"
    render template: template("FeatureFlagDomain.groovy"),
            destination: file("grails-app/domain/${dir}/FeatureFlag${it.domainName}.groovy"),
            model: it,
            overwrite: true
}

render template: template("FeatureFlagTrait.groovy"),
        destination: file("grails-app/services/${dir}/FeatureFlagTrait.groovy"),
        model: [package: args[0], importDomains: importDomains, checkOtherFeatureFlags:checkOtherFeatureFlags],
        overwrite: true


addStatus "Installing Enforcer feature flags complete, but you need to manually add the FeatureFlagTrait to the EnforcerService."
addStatus "You will also need to update the FeatureFlagTrait to lookup the domain, that's linked toe the FeatureDomain."
