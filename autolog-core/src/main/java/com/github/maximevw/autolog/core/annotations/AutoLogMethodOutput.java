/*-
 * #%L
 * Autolog core module
 * %%
 * Copyright (C) 2019 Maxime WIEWIORA
 * %%
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
 * #L%
 */

package com.github.maximevw.autolog.core.annotations;

import com.github.maximevw.autolog.core.configuration.PrettyDataFormat;
import com.github.maximevw.autolog.core.logger.LogLevel;
import net.logstash.logback.argument.StructuredArguments;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.CloseableThreadContext;
import org.apiguardian.api.API;
import org.slf4j.MDC;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation allows to identify methods for which the output data have to be automatically logged.
 *
 * <h1>Annotation priorities</h1>
 * <p>
 *     If this annotation is used with {@link AutoLogMethodInOut} at the same level (class or method), it will be
 *     ignored since {@link AutoLogMethodInOut} annotation gets the priority.
 * </p>
 * <p>
 *     If the annotation is applied at class level, all the methods of the class will be marked as auto-loggable with
 *     the parameters defined in the annotation. See section "Usage with Spring AOP" about restrictions relative to
 *     the methods eligible for auto-logging in Spring applications.
 * </p>
 * <p>
 *     If a method is also annotated with {@code AutoLogMethodOutput} or {@link AutoLogMethodInOut} in a class annotated
 *     with {@code AutoLogMethodOutput}, the annotation defined at the method level gets the priority.
 * </p>
 *
 * <h1>Log automation by aspect programming</h1>
 * <p>
 *     In order to automate the logging of output value of methods calls, this annotation is activated by aspect
 *     programming. Currently, the only available implementation is based on Spring AOP.
 * </p>
 * <h2>Usage with Spring AOP</h2>
 * <p>
 *     To use this annotation in Spring applications, you must activate Spring AOP and add module {@code autolog-spring}
 *     as dependency of your project. Then, this annotation will be taken into account by Spring AOP, what implies some
 *     limitations due the proxy-based aspects used by Spring AOP:
 *     <ul>
 *         <li>Only beans managed by Spring will be taken into account</li>
 *         <li>Only public methods will be taken into account</li>
 *         <li>If a method in a bean calls another method of the same bean, the logs will be generated only for the
 *         caller method</li>
 *     </ul>
 * </p>
 * <p>
 *     See <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#aop">Spring AOP documentation</a>
 *     and especially the section about <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#aop-understanding-aop-proxies">
 *     proxy-based aspects</a> for more explanations about these limitations.
 * </p>
 *
 * <h2>Alternative usage</h2>
 * <p>
 *     As explained above, if you are using this annotation with Spring AOP, the auto-logging cannot be activated on any
 *     methods. If you want to log output value of methods not managed by Spring AOP, a possible workaround is to
 *     manually log them thanks to:
 *     {@code MethodCallLogger.logMethodOutput(MethodOutputLoggingConfiguration, String, Object)}.
 * </p>
 *
 * <h1>Example</h1>
 * <p>
 *     For example, if the annotation is applied on a method as below:
 *     <pre>
 *         public class MyClass {
 *              {@literal @}AutoLogMethodOutput(level = LogLevel.DEBUG)
 *               public String myMethod(final String arg0, final int arg1) {
 *                   log.info("This is a log entry.");
 *                   return String.format("%s:%d", arg0, arg1);
 *               }
 *         }
 *     </pre>
 *     The generated logs will be like this:
 *     <pre>
 *         INFO  This is a log entry.
 *         DEBUG Exiting MyClass.myMethod() returning value0:10
 *     </pre>
 * </p>
 *
 * @see AutoLogMethodInOut
 */
@API(status = API.Status.STABLE, since = "1.0.0")
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface AutoLogMethodOutput {

	/**
     * @return The level of log to use. By default: {@code INFO}.
     */
	LogLevel level() default LogLevel.INFO;

    /**
     * @return Whether the method name must be prefixed with the name of its enclosing class. By default: {@code true}.
     */
    boolean showClassName() default true;

    /**
     * @return Whether the collections and maps data must be fully logged. If set to {@code false}, only the size of
     *         the collections or maps is logged. By default: {@code false}.
     */
    boolean expandCollectionsAndMaps() default false;

    /**
     * @return The format used to prettify the logged output data. By default: {@link PrettyDataFormat#JSON}.
     */
    PrettyDataFormat prettyFormat() default PrettyDataFormat.JSON;

    /**
     * @return Whether any thrown exception or error must be logged before being re-thrown. The log level used to log
     *         the {@link Throwable} is {@link LogLevel#ERROR}. By default: {@code false}.
     */
    boolean logThrowable() default false;

    /**
     * @return The message template will be used to log the method exiting for methods having return type {@code void}.
     *         By default: {@value AutoLogMethodInOut#VOID_OUTPUT_DEFAULT_MESSAGE_TEMPLATE}.
     * <p>
     *     The defined template must contain a placeholder (symbolized by "<code>{}</code>") will be replaced by the
     *     called method name.
     * </p>
	 * <p>
	 *     <i>Note:</i> The message template syntax is the same as the one used by SLF4J.
	 * </p>
     * @see org.slf4j.Logger
     * @see org.slf4j.helpers.MessageFormatter
     */
    String voidOutputMessageTemplate() default AutoLogMethodInOut.VOID_OUTPUT_DEFAULT_MESSAGE_TEMPLATE;

    /**
     * @return The message template will be used to log the method output value. By default:
     * {@value AutoLogMethodInOut#OUTPUT_DEFAULT_MESSAGE_TEMPLATE}.
     * <p>
     *     The defined template must contain two placeholders (symbolized by "<code>{}</code>"):
     *     <ul>
     *         <li>the first one will be replaced by the called method name;</li>
     *         <li>the second one will be replaced by the method output value.</li>
     *     </ul>
     * </p>
	 * <p>
	 *     <i>Note:</i> The message template syntax is the same as the one used by SLF4J.
	 * </p>
     * @see org.slf4j.Logger
	 * @see org.slf4j.helpers.MessageFormatter
     */
    String messageTemplate() default AutoLogMethodInOut.OUTPUT_DEFAULT_MESSAGE_TEMPLATE;

	/**
	 * @return Whether the format of the logged message is fully structured (using the format defined by the parameter
	 *         {@link #prettyFormat()}) and not "human readable" (that's to say using the message templates defined by
	 *         the parameters {@link #messageTemplate()} and {@link #voidOutputMessageTemplate()}). By default:
	 *         {@code false}.
	 * <p>
	 *     <i>Note: </i>When the structured format is active, the values of the following parameters are ignored:
	 *     {@link #messageTemplate()} and {@link #voidOutputMessageTemplate()}.
	 * </p>
	 *
	 * @see com.github.maximevw.autolog.core.logger.MethodOutputLogEntry
	 */
	boolean structuredMessage() default false;

	/**
	 * @return Whether the output data should also be logged into the log context when it is possible (i.e. using
	 *         {@link MDC} for SLF4J implementations, {@link StructuredArguments} for Logback with Logstash encoder or
	 *         {@link CloseableThreadContext} for Log4j2). By default: {@code false}.
	 * 		   <p>
	 * 		       The data stored in the log context are:
	 * 		       <ul>
	 * 		           <li>method name (property {@code invokedMethod})</li>
	 * 		           <li>the output value of the method (property {@code outputValue}) in accordance with the rules
	 * 		           defined by the other parameters of this annotation. If the method returns nothing, the property
	 * 		           {@code outputValue} will be set to {@code void}.</li>
	 * 		           <li>if an exception or an error is thrown by the invoked method, the class of the exception
	 * 		           is stored in the property {@code throwableType} and the property {@code outputValue} will be
	 * 		           set to {@code n/a}.</li>
	 * 		       </ul>
	 * 		   </p>
	 */
	@API(status = API.Status.STABLE, since = "1.1.0")
	boolean logDataInContext() default false;

	/**
	 * @return The logger name to use. If not specified, the default value "Autolog" will be used.
	 */
	@API(status = API.Status.STABLE, since = "1.2.0")
	String topic() default StringUtils.EMPTY;
}
