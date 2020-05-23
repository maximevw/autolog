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
import com.github.maximevw.autolog.core.logger.performance.AdditionalDataProvider;
import net.logstash.logback.argument.StructuredArguments;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.CloseableThreadContext;
import org.apiguardian.api.API;
import org.slf4j.MDC;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation allows to identify methods for which the performance has to be monitored and automatically logged.
 *
 * <h1>Annotation priorities</h1>
 * <p>
 *     If the annotation is applied at class level, all the methods of the class will be marked as monitored with
 *     the parameters defined in the annotation. See section "Usage with Spring AOP" about restrictions relative to
 *     the methods eligible for auto-logging in Spring applications.
 * </p>
 *
 * <h1>Log automation by aspect programming</h1>
 * <p>
 *     In order to automate the logging of performance of methods invocations, this annotation is activated by aspect
 *     programming. Currently, the only available implementation is based on Spring AOP.
 * </p>
 *
 * <h2>Important warning</h2>
 * <p>
 *     The usage of this annotation may impact the performance of your application. If you need high performance during
 *     the execution of your code, don't use it (especially in production).
 * </p>
 *
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
 *     methods. If you want to monitor and log performance of methods not managed by Spring AOP, a possible workaround
 *     is to manually log them thanks to:
 *     {@code MethodPerformanceLogger.start(MethodPerformanceLoggingConfiguration, String)}
 *     and {@code MethodPerformanceLogger.stopAndLog(MethodPerformanceLoggingConfiguration, PerformanceTimer)}.
 * </p>
 *
 * <h1>Example</h1>
 * <p>
 *     For example, if the annotation is applied on a method as below:
 *     <pre>
 *         public class MyClass {
 *              {@literal @}AutoLogPerformance
 *               public String myMethod(final String arg0, final int arg1) {
 *                   log.info("This is a log entry.");
 *                   return String.format("%s:%d", arg0, arg1);
 *               }
 *         }
 *     </pre>
 *     The generated logs will be like this:
 *     <pre>
 *         INFO  This is a log entry.
 *         DEBUG Method MyClass.myMethod executed in 18ms (started: 2019-12-30 14:30:25.600,
 *         ended: 2019-12-30 14:30:25.618).
 *     </pre>
 * </p>
 */
@API(status = API.Status.STABLE, since = "1.0.0")
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD, ElementType.TYPE})
public @interface AutoLogPerformance {

	/**
	 * @return The name of the performance timer. By default, the name of the invoked method will be used.
	 * @see #apiEndpointsAutoConfig()
	 */
	String name() default StringUtils.EMPTY;

	/**
	 * @return The level of log to use. By default: {@code DEBUG}.
	 */
	LogLevel level() default LogLevel.DEBUG;

	/**
	 * @return Whether the method name must be prefixed with the name of its enclosing class. By default: {@code true}.
	 * <p>
	 *     This parameter is applied when no custom name has been defined in parameter {@link #name()}.
	 * </p>
	 */
	boolean showClassName() default true;

	/**
	 * @return Whether the full stack of calls with performance information must be dumped and logged as a tree when the
	 *         initial method invocation ends. By default: {@code true}.
	 */
	boolean dumpCallsStack() default true;

	/**
	 * @return Whether the performance timer of each method must be logged individually at the end of the method
	 *         invocation. By default: {@code true}.
	 */
	boolean logEachTimer() default true;

	/**
	 * @return Whether the performance timer name must be automatically set with the path of the API endpoint if
	 *         the method to monitor corresponds to an API call. The HTTP method is also automatically logged as a
	 *         prefix of the endpoint path. By default: {@code true}.
	 * <p>
	 *     The automatic configuration of the performance log (name and comments) is based on the following annotations
	 *     (located on the method and/or enclosing class):
	 *     <ul>
	 *         <b>For applications using JAX-RS:</b>
	 *         <li>{@link Path} and the available HTTP methods
	 *         annotations ({@link GET}, {@link POST}, {@link PUT}, {@link DELETE}, {@link OPTIONS}, {@link HEAD}
	 *         and {@link PATCH}).</li>
	 *         <b>For Spring Web applications:</b>
	 *         <li>{@link RequestMapping} (and the parameter value {@link RequestMapping#method()})</li>
	 *         <li>{@link GetMapping}</li>
	 *         <li>{@link PostMapping}</li>
	 *         <li>{@link PutMapping}</li>
	 *         <li>{@link DeleteMapping}</li>
	 *         <li>{@link PatchMapping}</li>
	 *     </ul>
	 * </p>
	 */
	boolean apiEndpointsAutoConfig() default true;

	/**
	 * @return The static comments to log with the performance timer.
	 */
	String[] comments() default {};

	/**
	 * @return The field name to use to get/set additional data to log with the performance timer. By default, no
	 * 		   additional data provider is defined.
	 * <p>
	 *     If the method to monitor has to log additional data relative to its execution in the performance log (e.g.
	 *     dynamic comments or number of processed items), you can define a field of type {@link AdditionalDataProvider}
	 *     in the same class as the invoked method to retrieve these data at the end of the method invocation.
	 * </p>
	 * <p>
	 *     This data provider MUST be an instance of {@link AdditionalDataProvider}, otherwise you will have an error at
	 *     compilation time. The provided data are set by using methods
	 *     {@link AdditionalDataProvider#addComments(String...)} and
	 *     {@link AdditionalDataProvider#setProcessedItems(int)} of the data provider.
	 * </p>
	 * <p>
	 *     Example of additional data provider usage:
	 *
	 *     <pre>
	 *         private AdditionalDataProvider dataProviderForMyMethod = new AdditionalDataProvider();
	 *
	 *         // The method to monitor.
	 *         {@literal @}LogPerformance(additionalDataProvider = "dataProviderForMyMethod")
	 *         public void myMethod(String[] input, String name) {
	 *         		// ... do something ...
	 *
	 *         		// Add comments to log in the performance log.
	 *         		this.dataProviderForMyMethod.addComments("name=" + name);
	 *         		// Set number of processed items in the performance log.
	 *         		this.dataProviderForMyMethod.setProcessedItems(input.length);
	 *     		}
	 *	   </pre>
	 * </p>
	 */
	String additionalDataProvider() default StringUtils.EMPTY;

	/**
	 * @return The format used to log the performance data when {@link #structuredMessage()} is set to {@code true}.
	 * 		   By default: {@link PrettyDataFormat#JSON}.
	 */
	PrettyDataFormat prettyFormat() default PrettyDataFormat.JSON;

	/**
	 * @return Whether the format of the logged message is fully structured (using the format defined by the parameter
	 * {@link #prettyFormat()}) and not "human readable". By default: {@code false}.
	 */
	boolean structuredMessage() default false;

	/**
	 * @return Whether the performance data should also be logged into the log context when it is possible (i.e.
	 * 		   using {@link MDC} for SLF4J implementations, {@link StructuredArguments} for Logback with Logstash
	 * 		   encoder or {@link CloseableThreadContext} for Log4j2). By default: {@code false}.
	 * 		   <p>
	 * 		       The data stored in the log context are:
	 * 		       <ul>
	 * 		           <li>method name (property {@code invokedMethod})</li>
	 * 		           <li>duration of the execution in milliseconds (property {@code executionTimeInMs})</li>
	 * 		           <li>execution status (property {@code failed})</li>
	 * 		           <li>comments (if available, property {@code comments})</li>
	 * 		           <li>number of processed items (if available, property {@code processedItems})</li>
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
