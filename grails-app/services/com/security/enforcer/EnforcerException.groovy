package com.security.enforcer

import groovy.transform.InheritConstructors

/**
 * The default exception for the enforcer service, when  the predicate closure returns false.
 */
@InheritConstructors
class EnforcerException extends RuntimeException {}
