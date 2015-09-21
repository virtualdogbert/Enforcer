package ${packageName}

/**
 * This is for adding roles at an object level for business rules
 */
class DomainRole {
    String role        //The role to apply to the object
    Long   domainId    //The id of the object
    String domainName  //The domain name of the object
    User   user        //The user associated with the permission
    Date   dateCreated
    Date   lastUpdated

    static constraints = {
        role inList: ['owner', 'editor', 'viewer']
        dateCreated nullable: true
        lastUpdated nullable: true
    }

    static mapping = {
        version false
        cache true
    }
}
