package ${ packageName }

class FeatureFlag {

    String  name
    Boolean Enabled = false
    Date    dateCreated
    Date    lastUpdated

    static constraints = {
        dateCreated nullable: true
        lastUpdated nullable: true
    }

    static mapping = {
        version false
        cache true
    }
}