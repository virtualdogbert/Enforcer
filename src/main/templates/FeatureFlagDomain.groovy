package ${packageName}

import ${domainPackage}.${domainName}
import grails.gorm.DetachedCriteria
import org.apache.commons.lang.builder.HashCodeBuilder



class Feature${domainName} {

    FeatureFlag featureFlag
    ${domainName} ${lowerDomainName}
    Boolean Enabled = false
    Date    dateCreated
    Date    lastUpdated


    @Override
    boolean equals(other) {
        if (!(other instanceof FeatureFlag${domainName})) {
            return false
        }

        other.featureFlag?.id == featureFlag?.id && other.${lowerDomainName}?.id == ${lowerDomainName}?.id
    }

    @Override
    int hashCode() {
        def builder = new HashCodeBuilder()
        if (featureFlag) builder.append(featureFlag.id)
        if (${lowerDomainName}) builder.append(${lowerDomainName}.id)
        builder.toHashCode()
    }

    static FeatureFlag${domainName} get(long featureFlagId, long ${lowerDomainName}Id) {
        criteriaFor(featureFlagId, ${lowerDomainName}Id).get()
    }

    static boolean exists(long featureFlagId, long ${lowerDomainName}Id) {
        criteriaFor(featureFlagId, ${lowerDomainName}Id).count()
    }

    private static DetachedCriteria criteriaFor(long featureFlagId, long ${lowerDomainName}Id) {
        FeatureFlag${domainName}.where {
            featureFlag == FeatureFlag.load(featureFlagId) &&
                    ${lowerDomainName} == ${domainName}.load(${lowerDomainName}Id)
        }
    }

    static FeatureFlag${domainName} create(FeatureFlag featureFlag, ${domainName} ${lowerDomainName}, boolean flush = false) {
        def instance = new FeatureFlag${domainName}(featureFlag, ${lowerDomainName})
        instance.save(flush: flush, insert: true)
        instance
    }

    static boolean remove(FeatureFlag u, ${domainName} r, boolean flush = false) {
        if (u == null || r == null) return false

        int rowCount = FeatureFlag${domainName}.where { featureFlag == u && ${lowerDomainName} == r }.deleteAll()

        if (flush) {
            FeatureFlag${domainName}.withSession { it.flush() }
        }

        rowCount
    }

    static void removeAll(FeatureFlag u, boolean flush = false) {
        if (u == null) return

        FeatureFlag${domainName}.where { featureFlag == u }.deleteAll()

        if (flush) {
            FeatureFlag${domainName}.withSession { it.flush() }
        }
    }

    static void removeAll(${domainName} r, boolean flush = false) {
        if (r == null) return

        FeatureFlag${domainName}.where { ${lowerDomainName} == r }.deleteAll()

        if (flush) {
            FeatureFlag${domainName}.withSession { it.flush() }
        }
    }

    static constraints = {
        dateCreated nullable: true
        lastUpdated nullable: true
        ${lowerDomainName} validator: { ${domainName} f, FeatureFlag${domainName} fd ->
            if (fd.featureFlag == null || fd.featureFlag.id == null) return
            boolean existing = false
            FeatureFlag${domainName}.withNewSession {
                existing = FeatureFlag${domainName}.exists(fd.featureFlag.id, f.id)
            }
            if (existing) {
                return 'featureFlag${domainName}.exists'
            }
        }
    }

    static mapping = {
        id composite: ['featureFlag', '${lowerDomainName}']
        version false
    }
}