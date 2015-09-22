The Enforcer Plugin, is a light wieght, easier to maintain/extend/use, alternative to Spring Security ACL. The plugin works off of a EnforcerService and an Enforce Ast transform, The service take s up to 3 closures, a predicate, a failure(defaults to an EnforcerException if not specified) and a success(defaulted to a closure that returns true).

For documentation see the github page:
[documentation](https://virtualdogbert.github.io/Enforcer)
