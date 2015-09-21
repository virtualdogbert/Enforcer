import org.grails.cli.interactive.completers.DomainClassCompleter

description ("Installs default implmentaiton for Enforcer, whch can be changed/enhanced by the user"){
	usage "grails enforcer-quickstart [package]"
	argument name: 'package', description:'The name of the package to put the enforcer files under'
	completer DomainClassCompleter
}

model = [packageName:args[0]]
dir = args[0].replace('.','/')

addStatus "Installing Enforcer defaults"


render template: template("DomainRole.groovy"),
		destination: file( "grails-app/domain/${dir}/DomainRole.groovy"),
		model: model,
		overwrite: true

render template: template("DomainRoleTrait.groovy"),
		destination: file( "grails-app/services/${dir}/DomainRoleTrait.groovy"),
		model: model,
		overwrite: true
		
render template: template("EnforcerService.groovy"),
		destination: file( "grails-app/services/${dir}/EnforcerService.groovy"),
		model: model,
		overwrite: true

render template: template("RoleTrait.groovy"),
		destination: file( "grails-app/services/${dir}/RoleTrait.groovy"),
		model: model,
		overwrite: true



render template: template("EnforcerServiceSpec.groovy"),
		destination: file( "src/test/groovy/unit/services/${dir}/EnforcerServiceSpec.groovy"),
		model: model,
		overwrite: true



addStatus "Installing Enforcer defaults complete"
