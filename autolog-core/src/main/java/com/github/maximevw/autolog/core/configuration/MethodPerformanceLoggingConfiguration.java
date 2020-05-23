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

package com.github.maximevw.autolog.core.configuration;

import com.github.maximevw.autolog.core.annotations.AutoLogPerformance;
import com.github.maximevw.autolog.core.logger.LogLevel;
import com.github.maximevw.autolog.core.logger.LoggingUtils;
import com.github.maximevw.autolog.core.logger.performance.AdditionalDataProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Configuration for auto-logging of performance data of methods invocations.
 */
@API(status = API.Status.STABLE, since = "1.0.0")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class MethodPerformanceLoggingConfiguration {

	/**
	 * The name of the performance timer. By default, the name of the invoked method will be used.
	 * @see #isApiEndpointsAutoConfigured()
	 */
	@Builder.Default
	private String name = StringUtils.EMPTY;

	/**
	 * The level of log to use. By default: {@code DEBUG}.
	 */
	@Builder.Default
	private LogLevel logLevel = LogLevel.DEBUG;

	/**
	 * Whether the method name must be prefixed with the name of its enclosing class. By default: {@code true}.
	 * <p>
	 *     This parameter is applied when no custom name has been defined in parameter {@link #getName()}.
	 * </p>
	 */
	@Builder.Default
	private boolean classNameDisplayed = true;

	/**
	 * Whether the full stack of calls with performance information must be dumped and logged as a tree when the initial
	 * method invocation ends. By default: {@code true}.
	 */
	@Builder.Default
	private boolean callsStackDumped = true;

	/**
	 * Whether the performance timer of each method must be logged individually at the end of the method invocation.
	 * By default: {@code true}.
	 */
	@Builder.Default
	private boolean eachTimerLogged = true;

	/**
	 * Whether the performance timer name must be automatically set with the path of the API endpoint if
	 * the method to monitor corresponds to an API call. The HTTP method is also automatically logged as a prefix of
	 * the endpoint path. By default: {@code true}.
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
	@Builder.Default
	private boolean apiEndpointsAutoConfigured = true;

	/**
	 * The static comments to log with the performance timer.
	 */
	@Builder.Default
	private List<String> comments = new ArrayList<>();

	/**
	 * The field name to use to get/set additional data to log with the performance timer. By default, no additional
	 * data provider is defined.
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
	@Builder.Default
	private String additionalDataProvider = StringUtils.EMPTY;

	/**
	 * The format used to log the performance data when {@link #isStructuredMessage()} ()} is set to {@code true}.
	 * By default: {@link PrettyDataFormat#JSON}.
	 */
	@Builder.Default
	private PrettyDataFormat prettyFormat = PrettyDataFormat.JSON;

	/**
	 * Whether the format of the logged message is fully structured (using the format defined by the parameter
	 * {@link #getPrettyFormat()}) and not "human readable". By default: {@code false}.
	 *
	 * @see com.github.maximevw.autolog.core.logger.MethodPerformanceLogEntry
	 */
	@Builder.Default
	private boolean structuredMessage = false;

	/**
	 * Whether the performance data should also be logged into the log context when it is possible (i.e. using
	 * {@link MDC} for SLF4J implementations, {@link StructuredArguments} for Logback with Logstash encoder or
	 * {@link CloseableThreadContext} for Log4j2). By default: {@code false}.
	 * <p>
	 *     The data stored in the log context are:
	 *     <ul>
	 *         <li>method name (property {@code invokedMethod})</li>
	 * 		   <li>duration of the execution in milliseconds (property {@code executionTimeInMs})</li>
	 * 		   <li>execution status (property {@code failed})</li>
	 * 		   <li>comments (if available, property {@code comments})</li>
	 * 		   <li>number of processed items (if available, property {@code processedItems})</li>
	 *     </ul>
	 *  </p>
	 */
	@API(status = API.Status.STABLE, since = "1.1.0")
	@Builder.Default
	private boolean dataLoggedInContext = false;

	/**
	 * The logger name to use. If not specified, the default value {@value LoggingUtils#AUTOLOG_DEFAULT_TOPIC} will be
	 * used.
	 */
	@API(status = API.Status.STABLE, since = "1.2.0")
	private String topic;

	/**
	 * Builds a new instance of configuration for auto-logging of performance data of methods invocations based on an
	 * annotation {@link AutoLogPerformance}.
	 *
	 * @param autoLogPerformance An annotation {@link AutoLogPerformance}.
	 * @return A new instance of {@code MethodPerformanceLoggingConfiguration}.
	 */
	public static MethodPerformanceLoggingConfiguration from(final AutoLogPerformance autoLogPerformance) {
		return MethodPerformanceLoggingConfiguration.builder()
			.name(autoLogPerformance.name())
			.logLevel(autoLogPerformance.level())
			.classNameDisplayed(autoLogPerformance.showClassName())
			.callsStackDumped(autoLogPerformance.dumpCallsStack())
			.eachTimerLogged(autoLogPerformance.logEachTimer())
			.apiEndpointsAutoConfigured(autoLogPerformance.apiEndpointsAutoConfig())
			.comments(new ArrayList<>(Arrays.asList(autoLogPerformance.comments())))
			.additionalDataProvider(autoLogPerformance.additionalDataProvider())
			.prettyFormat(autoLogPerformance.prettyFormat())
			.structuredMessage(autoLogPerformance.structuredMessage())
			.dataLoggedInContext(autoLogPerformance.logDataInContext())
			.topic(autoLogPerformance.topic())
			.build();
	}
}
