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
 *
 * Some of the setup is derived from the fllowing grails plugings(Appache Licence)
 * https://github.com/groovy/groovy-core/blob/4993b10737881b2491c2daa01526fb15dd889ac5/src/main/org/codehaus/groovy/transform/NewifyASTTransformation.java
 * https://github.com/grails-plugins/grails-redis/tree/master/src/main/groovy/grails/plugins/redis
 */

package com.virtualdogbert.ast

import org.codehaus.groovy.transform.GroovyASTTransformationClass
import org.grails.datastore.mapping.core.connections.ConnectionSourcesProvider
import org.springframework.transaction.annotation.Isolation
import org.springframework.transaction.annotation.Propagation

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * The annotation reinforceFilter takes one closure, and filters the return statement of a method based on that closure
 *
 * Example:
 * @ReinforceFilter({ Object o -> (o as List).findResults { it % 2 == 0 ? it : null } })
 *
 * parameters
 * value is the filter for the enforce service, named value so that you don't have to name it
 */
@Retention(RetentionPolicy.SOURCE)
@Target([ElementType.METHOD])
@GroovyASTTransformationClass("com.virtualdogbert.ast.ReinforceFilterTASTTransformation")
public @interface ReinforceFilterT {
    Class value()

    /**
     * A qualifier value for the specified transaction.
     * <p>May be used to determine the target transaction manager,
     * matching the qualifier value (or the bean name) of a specific
     * {@link org.springframework.transaction.PlatformTransactionManager}
     * bean definition.
     */
    String transactionalValue() default "";

    /**
     * The transaction propagation type.
     * Defaults to {@link org.springframework.transaction.annotation.Propagation#REQUIRED}.
     * @see org.springframework.transaction.interceptor.TransactionAttribute#getPropagationBehavior()
     */
    Propagation propagation() default Propagation.REQUIRED;

    /**
     * The transaction isolation level.
     * Defaults to {@link org.springframework.transaction.annotation.Isolation#DEFAULT}.
     * @see org.springframework.transaction.interceptor.TransactionAttribute#getIsolationLevel()
     */
    Isolation isolation() default Isolation.DEFAULT;

    /**
     * The timeout for this transaction.
     * Defaults to the default timeout of the underlying transaction system.
     * @see org.springframework.transaction.interceptor.TransactionAttribute#getTimeout()
     */
    int timeout() default -1;

    /**
     * {@code true} if the transaction is read-only.
     * Defaults to {@code false}.
     * <p>This just serves as a hint for the actual transaction subsystem;
     * it will <i>not necessarily</i> cause failure of write access attempts.
     * A transaction manager which cannot interpret the read-only hint will
     * <i>not</i> throw an exception when asked for a read-only transaction.
     * @see org.springframework.transaction.interceptor.TransactionAttribute#isReadOnly()
     */
    boolean readOnly() default false;

    /**
     * Defines zero (0) or more exception {@link Class classes}, which must be a
     * subclass of {@link Throwable}, indicating which exception types must cause
     * a transaction rollback.
     * <p>This is the preferred way to construct a rollback rule, matching the
     * exception class and subclasses.
     * <p>Similar to {@link org.springframework.transaction.interceptor.RollbackRuleAttribute#RollbackRuleAttribute(Class clazz)}
     */
    Class<? extends Throwable>[] rollbackFor() default [];

    /**
     * Defines zero (0) or more exception names (for exceptions which must be a
     * subclass of {@link Throwable}), indicating which exception types must cause
     * a transaction rollback.
     * <p>This can be a substring, with no wildcard support at present.
     * A value of "ServletException" would match
     * javax.servlet.ServletException and subclasses, for example.
     * <p><b>NB: </b>Consider carefully how specific the pattern is, and whether
     * to include package information (which isn't mandatory). For example,
     * "Exception" will match nearly anything, and will probably hide other rules.
     * "java.lang.Exception" would be correct if "Exception" was meant to define
     * a rule for all checked exceptions. With more unusual {@link Exception}
     * names such as "BaseBusinessException" there is no need to use a FQN.
     * <p>Similar to {@link org.springframework.transaction.interceptor.RollbackRuleAttribute#RollbackRuleAttribute(String exceptionName)}
     */
    String[] rollbackForClassName() default [];

    /**
     * Defines zero (0) or more exception {@link Class Classes}, which must be a
     * subclass of {@link Throwable}, indicating which exception types must <b>not</b>
     * cause a transaction rollback.
     * <p>This is the preferred way to construct a rollback rule, matching the
     * exception class and subclasses.
     * <p>Similar to {@link org.springframework.transaction.interceptor.NoRollbackRuleAttribute#NoRollbackRuleAttribute(Class clazz)}
     */
    Class<? extends Throwable>[] noRollbackFor() default [];

    /**
     * Defines zero (0) or more exception names (for exceptions which must be a
     * subclass of {@link Throwable}) indicating which exception types must <b>not</b>
     * cause a transaction rollback.
     * <p>See the description of {@link #rollbackForClassName()} for more info on how
     * the specified names are treated.
     * <p>Similar to {@link org.springframework.transaction.interceptor.NoRollbackRuleAttribute#NoRollbackRuleAttribute(String exceptionName)}
     */
    String[] noRollbackForClassName() default [];

    /**
     * In Spring, when there are nested transaction calls, the execution of the outermost callback will throw UnexpectedRollbackException if TransactionStatus.setRollbackOnly() was called in a nested transaction callback.
     *
     * This feature will make the setRollbackOnly state get inherited to parent level transaction template calls and therefore prevent UnexpectedRollbackException.
     * The default value is true.
     *
     */
    boolean inheritRollbackOnly() default true;

    /**
     * If you are using multiple GORM implementations and wish to create a transaction for a specific implementation then use this. For example {@code @Transactional(forDatastore=HibernateDatastore) }
     *
     * @return The type of the datastore
     */
    Class<? extends ConnectionSourcesProvider>[] datastore() default [];

    /**
     * The connection to use by default
     */
    String connection() default "DEFAULT";
}