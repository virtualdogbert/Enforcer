/* Copyright 2006-2015 SpringSource.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
//This is an altered copy of the s2quick start script from the spring security core plugin.
includeTargets << new File(enforcerPluginDir, 'scripts/_EnforcerCommon.groovy')

USAGE = '''
Usage: grails enforcer-quickstart <package name you used for spring security core plugin>

Example: grails enforcer-quickstart com.yourapp
'''

includeTargets << grailsScript('_GrailsBootstrap')

packageName = ''

target(enforcerQuickstart: 'Creates artifacts for the Spring Security plugin') {
    depends(checkVersion, configureProxy, packageApp, classpath)

    if (!configure()) {
        return 1
    }

    createDomains()
}

private boolean configure() {

    def argValues = parseArgs()
    if (!argValues) {
        return false
    }

    if (argValues.size() == 1) {
        (packageName) = argValues
    } else {
        return false
    }


    templateAttributes = [packageName: packageName]

    true
}




private parseArgs() {
    def args = argsMap.params

    if (1 == args.size()) {
        printMessage "Creating Enforcer files in package ${args[0]}"
        return args
    }

    errorMessage USAGE
    null
}

setDefaultTarget 'enforcerQuickstart'
private void createDomains() {

    String dir = packageToDir(packageName)
    String domainDir = "$appDir/domain/com/security/enforcer"
    String serviceDir = "$appDir/services/com/security/enforcer"
    String testDir = "$basedir/test/unit/services/com/security/enforcer"
    generateFile "$templateDir/DomainRole.groovy.template", "${domainDir}/DomainRole.groovy"
    generateFile "$templateDir/DomainRoleTrait.groovy.template", "${serviceDir}/DomainRoleTrait.groovy"
    generateFile "$templateDir/EnforcerService.groovy.template", "${serviceDir}/EnforcerService.groovy"
    generateFile "$templateDir/EnforcerServiceSpec.groovy.template", "${testDir}/EnforcerServiceSpec.groovy"
    generateFile "$templateDir/EnforcerAnnotationSpec.groovy.template", "${testDir}/EnforcerAnnotationSpec.groovy"
    generateFile "$templateDir/ReinforceAnnotationSpec.groovy.template", "${testDir}/ReinforceAnnotationSpec.groovy"
    generateFile "$templateDir/RoleTrait.groovy.template", "${serviceDir}/RoleTrait.groovy"
}