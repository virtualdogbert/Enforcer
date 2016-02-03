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
package ${ packageName }
import ${packageName}.FeatureFlag
${importDomains}

class FeatureFlagService implements FeatureFlagTrait{
    Map getFeatureFlags(){
        Map featureFlags = [:]
        FeatureFlag.list.each{featureFlags[it.name]=it.enabled}
${otherFeatureFlags}

        [
                featureFlags:featureFlags,
${listFeatureFlags}
        ]
    }


    def createFlag(String name){
        new FeatureFlag(name:name).save()
    }

    //\${createOtherFlags}

    def deleteFlag(String name){
        //TODO write code to delete other flags \${deleteOtherSubFlags}
        FeatureFlag.findByName(name).delete()
    }

    //\${deleteOtherFlags}

    def setFeatureFlag(String name, Boolean enable){
        FeatureFlag featureFlag = FeatureFlag.findByName(name)
        featureFlags.enabled= enable
        featureFlag.save()
    }

    //\${setFlagsMethods}
}