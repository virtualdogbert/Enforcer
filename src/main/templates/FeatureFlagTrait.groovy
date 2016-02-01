package ${packageName}

import ${packageName}.FeatureFlag
${importDomains}


trait FeatureFlagTrait {


    boolean isEnabled(String featureName){
        FeatureFlag featureFlag = FeatureFlag.findByName(featureName)
        if(featureFlag?.enabled){
            return true
        }
${checkOtherFeatureFlags}

        return false
    }
}