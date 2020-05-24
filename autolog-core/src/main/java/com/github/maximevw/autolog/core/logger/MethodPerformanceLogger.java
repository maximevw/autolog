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

package com.github.maximevw.autolog.core.logger;

import com.github.maximevw.autolog.core.configuration.MethodInputLoggingConfiguration;
import com.github.maximevw.autolog.core.configuration.MethodPerformanceLoggingConfiguration;
import com.github.maximevw.autolog.core.configuration.PrettyDataFormat;
import com.github.maximevw.autolog.core.logger.performance.PerformanceTimer;
import com.github.maximevw.autolog.core.logger.performance.PerformanceTimerContext;
import lombok.NonNull;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.apiguardian.api.API;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * This class provides methods to monitor and log performance of a method invocation.
 */
@API(status = API.Status.STABLE, since = "1.0.0")
public class MethodPerformanceLogger {

	private LoggerManager loggerManager;

	/**
	 * Constructor.
	 *
	 * @param loggerManager  A logger manager instance.
	 */
	public MethodPerformanceLogger(final LoggerManager loggerManager) {
		this.loggerManager = loggerManager;
	}

	/**
	 * Starts a performance timer for a method invocation.
	 *
	 * @param configuration     The configuration used for logging.
	 * @param method            The invoked method.
	 * @return An instance of the running performance timer.
	 */
	public PerformanceTimer start(@NonNull final MethodPerformanceLoggingConfiguration configuration,
								  @NonNull final Method method) {
		String methodName = LoggingUtils.getMethodName(method, configuration.isClassNameDisplayed());
		String httpMethod = null;

		if (configuration.isApiEndpointsAutoConfigured()) {
			methodName = LoggingUtils.retrieveApiPath(method, configuration.getName(),
				configuration.isClassNameDisplayed());
			httpMethod = LoggingUtils.retrieveHttpMethod(method);
		}

		return start(configuration, LoggingUtils.computeTopic(method.getDeclaringClass(), configuration.getTopic(),
			configuration.isCallerClassUsedAsTopic()), methodName, httpMethod);
	}

	/**
	 * Starts a performance timer for a method invocation.
	 * <p>
	 *     This method always uses {@value LoggingUtils#AUTOLOG_DEFAULT_TOPIC} as logger name.
	 * </p>
	 *
	 * @param configuration     The configuration used for logging.
	 * @param methodName        The name of the invoked method.
	 *                          <p>
	 *                              The parameter {@link MethodPerformanceLoggingConfiguration#isClassNameDisplayed()}
	 *                              is ignored here. So, if the enclosing class name must be displayed, the method name
	 *                              must already be prefixed with it.
	 *                          </p>
	 * @return An instance of the running performance timer.
	 */
	public PerformanceTimer start(@NonNull final MethodPerformanceLoggingConfiguration configuration,
								  @NonNull final String methodName) {
		return start(configuration, LoggingUtils.AUTOLOG_DEFAULT_TOPIC, methodName, null);
	}

	/**
	 * Starts a performance timer for a method invocation.
	 *
	 * @param configuration     The configuration used for logging.
	 * @param callerClass		The declaring class of the invoked method.
	 * @param methodName        The name of the invoked method.
	 * @return An instance of the running performance timer.
	 */
	@API(status = API.Status.STABLE, since = "1.2.0")
	public PerformanceTimer start(@NonNull final MethodPerformanceLoggingConfiguration configuration,
								  @NonNull final Class<?> callerClass, @NonNull final String methodName) {
		return start(configuration, LoggingUtils.computeTopic(callerClass, configuration.getTopic(),
			configuration.isCallerClassUsedAsTopic()),
			LoggingUtils.getMethodName(methodName, callerClass, configuration.isClassNameDisplayed()), null);
	}

	/**
	 * Starts a performance timer for a method invocation.
	 *
	 * @param configuration     The configuration used for logging.
	 * @param methodName        The name of the invoked method.
	 *                          <p>
	 *                              The parameter {@link MethodPerformanceLoggingConfiguration#isClassNameDisplayed()}
	 *                              is ignored here. So, if the enclosing class name must be displayed, the method name
	 *                              must already be prefixed with it.
	 *                          </p>
	 * @param httpMethod		The HTTP method of the API endpoint corresponding to the invoked method.
	 * @return An instance of the running performance timer.
	 * @deprecated Use {@link #start(MethodPerformanceLoggingConfiguration, String, String, String)} instead. This
	 * 			   method always uses {@value LoggingUtils#AUTOLOG_DEFAULT_TOPIC} as logger name.
	 */
	@Deprecated
	@API(status = API.Status.DEPRECATED, since = "1.2.0")
	public PerformanceTimer start(@NonNull final MethodPerformanceLoggingConfiguration configuration,
								  @NonNull final String methodName, final String httpMethod) {
		return start(configuration, LoggingUtils.AUTOLOG_DEFAULT_TOPIC, methodName, httpMethod);
	}

	/**
	 * Starts a performance timer for a method invocation.
	 *
	 * @param configuration     The configuration used for logging.
	 * @param topic		        The logger name.
	 * @param methodName        The name of the invoked method.
	 *                          <p>
	 *                              The parameter {@link MethodPerformanceLoggingConfiguration#isClassNameDisplayed()}
	 *                              is ignored here. So, if the enclosing class name must be displayed, the method name
	 *                              must already be prefixed with it.
	 *                          </p>
	 * @param httpMethod		The HTTP method of the API endpoint corresponding to the invoked method.
	 * @return An instance of the running performance timer.
	 */
	@API(status = API.Status.STABLE, since = "1.2.0")
	public PerformanceTimer start(@NonNull final MethodPerformanceLoggingConfiguration configuration,
								  final String topic, @NonNull final String methodName, final String httpMethod) {
		String loggedMethodName = methodName;
		if (StringUtils.isNotBlank(configuration.getName())) {
			loggedMethodName = configuration.getName();
		}

		final StopWatch stopWatch = new StopWatch();
		final LocalDateTime startTime = LocalDateTime.now();
		final MethodPerformanceLogEntry performanceLogEntry = MethodPerformanceLogEntry.builder()
			.invokedMethod(loggedMethodName)
			.httpMethod(httpMethod)
			.startTime(startTime)
			.comments(configuration.getComments())
			.failed(false)
			.topic(topic)
			.build();

		stopWatch.start();

		final PerformanceTimer performanceTimer = PerformanceTimer.builder()
			.timer(stopWatch)
			.performanceLogEntry(performanceLogEntry)
			.running(true)
			.build();

		// Set the parent timer if a timer is already running in the same thread.
		performanceTimer.setParent(PerformanceTimerContext.current());
		// Set this timer as the running one in the current thread.
		PerformanceTimerContext.setCurrent(performanceTimer);

		return performanceTimer;
	}

	/**
	 * Stops the given performance timer and logs the performance information.
	 *
	 * @param configuration		The configuration used for logging.
	 * @param performanceTimer 	The performance timer to stop.
	 */
	public void stopAndLog(@NonNull final MethodPerformanceLoggingConfiguration configuration,
						   @NonNull final PerformanceTimer performanceTimer) {
		performanceTimer.stop();
		final LocalDateTime endTime = LocalDateTime.now();

		final MethodPerformanceLogEntry methodPerformanceLogEntry = performanceTimer.getPerformanceLogEntry();
		methodPerformanceLogEntry.setEndTime(endTime);
		methodPerformanceLogEntry.setExecutionTimeInMs(performanceTimer.getTimer().getTime());
		methodPerformanceLogEntry.computeAverageExecutionTime();

		// If required, log the performance information related to the stopped timer.
		if (configuration.isEachTimerLogged()) {
			logPerformanceInformation(configuration, methodPerformanceLogEntry);
		}

		// If required, log the full stack of calls when the initial monitored method ends.
		if (configuration.isCallsStackDumped() && performanceTimer.getParent() == null) {
			dumpCallsStack(configuration, performanceTimer);
		}
	}

	/**
	 * Logs the performance information at the end of a method invocation.
	 *
	 * @param configuration					The configuration used for logging.
	 * @param methodPerformanceLogEntry		The performance information to log.
	 */
	private void logPerformanceInformation(final MethodPerformanceLoggingConfiguration configuration,
										   final MethodPerformanceLogEntry methodPerformanceLogEntry) {
		// Build the map of data to store in the log context if required.
		Map<String, String> contextualData = null;
		if (configuration.isDataLoggedInContext()) {
			contextualData = new HashMap<>() {
				{
					put("invokedMethod", methodPerformanceLogEntry.getInvokedMethod());
					put("executionTimeInMs", String.valueOf(methodPerformanceLogEntry.getExecutionTimeInMs()));
					put("failed", String.valueOf(methodPerformanceLogEntry.isFailed()));
					if (methodPerformanceLogEntry.hasComments()) {
						put("comments", String.join(LoggingUtils.LIST_ITEMS_DELIMITER,
							methodPerformanceLogEntry.getComments()));
					}
					if (methodPerformanceLogEntry.getProcessedItems() != null) {
						put("processedItems", String.valueOf(methodPerformanceLogEntry.getProcessedItems()));
					}
				}
			};
		}

		// Effectively log the performance data.
		if (configuration.isStructuredMessage()) {
			loggerManager.logWithLevel(configuration.getLogLevel(), methodPerformanceLogEntry.getTopic(),
				LoggingUtils.prettify(methodPerformanceLogEntry,
					Optional.ofNullable(configuration.getPrettyFormat()).orElse(PrettyDataFormat.JSON)),
				contextualData);
		} else {
			final List<Object> performanceMessageArgs = new ArrayList<>();
			String performanceMessageTemplate = "Method {}";
			if (StringUtils.isNotEmpty(methodPerformanceLogEntry.getHttpMethod())) {
				performanceMessageArgs.add(String.format("[%s] %s", methodPerformanceLogEntry.getHttpMethod(),
					methodPerformanceLogEntry.getInvokedMethod()));
			} else {
				performanceMessageArgs.add(methodPerformanceLogEntry.getInvokedMethod());
			}

			if (methodPerformanceLogEntry.isFailed()) {
				performanceMessageTemplate = performanceMessageTemplate.concat(" failed after {}");
				performanceMessageArgs.add(methodPerformanceLogEntry.getExecutionTime());
			} else if (methodPerformanceLogEntry.getProcessedItems() != null) {
				performanceMessageTemplate = performanceMessageTemplate
					.concat(" processed {} item(s) (avg. {}/item) in {}");
				performanceMessageArgs.add(methodPerformanceLogEntry.getProcessedItems());
				performanceMessageArgs.add(
					Optional.ofNullable(methodPerformanceLogEntry.getAverageExecutionTimeByItem())
						.orElse(LoggingUtils.formatDuration(0))
				);
				performanceMessageArgs.add(methodPerformanceLogEntry.getExecutionTime());
			} else {
				performanceMessageTemplate = performanceMessageTemplate.concat(" executed in {}");
				performanceMessageArgs.add(methodPerformanceLogEntry.getExecutionTime());
			}

			performanceMessageTemplate = performanceMessageTemplate.concat(" (started: {}, ended: {}).");
			performanceMessageArgs.add(methodPerformanceLogEntry.getStartTime());
			performanceMessageArgs.add(methodPerformanceLogEntry.getEndTime());

			if (!methodPerformanceLogEntry.getComments().isEmpty()) {
				performanceMessageTemplate = performanceMessageTemplate.concat(" Details: {}.");
				performanceMessageArgs.add(String.join(LoggingUtils.LIST_ITEMS_DELIMITER,
					methodPerformanceLogEntry.getComments()));
			}

			loggerManager.logWithLevel(configuration.getLogLevel(), methodPerformanceLogEntry.getTopic(),
				performanceMessageTemplate, contextualData, performanceMessageArgs.toArray());
		}
	}

	/**
	 * Logs the performance information for the full stack of calls.
	 *
	 * @param configuration		The configuration used for logging.
	 * @param performanceTimer	The performance timer related to the initial call of the stack.
	 */
	private void dumpCallsStack(final MethodPerformanceLoggingConfiguration configuration,
								final PerformanceTimer performanceTimer) {
		// Check the timer is the "root" one and has children.
		if (performanceTimer.getChildren() != null && performanceTimer.getParent() == null) {
			dumpCallsStackRecursively(configuration, performanceTimer, 0);
		}
	}

	/**
	 * Recursively builds and logs the full stack of calls from the given {@code PerformanceTimer}.
	 *
	 * @param configuration		The configuration used for logging.
	 * @param performanceTimer 	The performance timer instance.
	 * @param depth            	The initial level of the stack of calls (0 for the "root" timer).
	 */
	private void dumpCallsStackRecursively(final MethodPerformanceLoggingConfiguration configuration,
										   final PerformanceTimer performanceTimer,
										   final int depth) {
		final MethodPerformanceLogEntry performanceLogEntry = performanceTimer.getPerformanceLogEntry();
		// Log the start of the stack of calls.
		if (depth == 0) {
			loggerManager.logWithLevel(configuration.getLogLevel(),
				"Performance summary report for {}:", performanceLogEntry.getInvokedMethod());
		}

		// Build the performance log entry.
		String methodStateVerb = "executed in";
		if (performanceLogEntry.isFailed()) {
			methodStateVerb = "failed after";
		}
		final String performanceData = String.format("%s %s %s", performanceLogEntry.getInvokedMethod(),
			methodStateVerb, performanceLogEntry.getExecutionTime());
		loggerManager.logWithLevel(configuration.getLogLevel(), String.format("%s> %s",
			"|_ ".repeat(Math.max(0, depth)), performanceData));

		// Process children timers.
		if (performanceTimer.getChildren() != null) {
			performanceTimer.getChildren().forEach(childTimer ->
				dumpCallsStackRecursively(configuration, childTimer, depth + 1));
		}
	}
}
