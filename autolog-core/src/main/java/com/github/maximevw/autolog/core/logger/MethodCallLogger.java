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

import com.github.maximevw.autolog.core.annotations.Mask;
import com.github.maximevw.autolog.core.configuration.MethodInputLoggingConfiguration;
import com.github.maximevw.autolog.core.configuration.MethodOutputLoggingConfiguration;
import com.github.maximevw.autolog.core.configuration.PrettyDataFormat;
import lombok.NonNull;
import org.apache.commons.lang3.tuple.Pair;
import org.apiguardian.api.API;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * This class provides methods to log:
 * <ul>
 *     <li>method input data</li>
 *     <li>method output value</li>
 *     <li>exceptions or errors thrown during a method invocation</li>
 * </ul>
 */
@API(status = API.Status.STABLE, since = "1.0.0")
public class MethodCallLogger {

	private static final String INVOKED_METHOD_PROPERTY = "invokedMethod";

	private LoggerManager loggerManager;

	/**
	 * Constructor.
	 *
	 * @param loggerManager  A logger manager instance.
	 */
	public MethodCallLogger(final LoggerManager loggerManager) {
        this.loggerManager = loggerManager;
    }

    /**
     * Logs the input of a method invocation.
     *
     * @param configuration     The configuration used for logging.
     * @param method            The invoked method.
     * @param argsValues        The values of the method input arguments.
     */
    public void logMethodInput(@NonNull final MethodInputLoggingConfiguration configuration,
                               @NonNull final Method method, final Object... argsValues) {
        // Build the map of input arguments.
        final List<Pair<String, Object>> argsList = new ArrayList<>();
        final Parameter[] parameters = method.getParameters();
        for (int i = 0; i < parameters.length; i++) {
            final Parameter parameter = parameters[i];
            Object argValue = argsValues[i];

            // Apply mask to the argument value if required and applicable.
            if (parameter.isAnnotationPresent(Mask.class)) {
				argValue = MaskingUtils.maskValue(argValue, parameter.getAnnotation(Mask.class));
			}

            if (parameter.isNamePresent()) {
                argsList.add(Pair.of(parameter.getName(), argValue));
            } else {
                argsList.add(Pair.of(String.format(LoggingUtils.AUTO_GENERATED_ARG_NAME_FORMATTER, i), argValue));
                LoggingUtils.report("Unable to get parameter name. Exclusions and restrictions will be ignored. "
					+ "Compile your application in debug mode (javac with -g argument).");
            }
        }

        logMethodInput(configuration, LoggingUtils.getMethodName(method, configuration.isClassNameDisplayed()),
			argsList);
    }

    /**
     * Logs the input of a method invocation.
     *
     * @param configuration     The configuration used for logging.
     * @param methodName        The name of the invoked method.
     *                          <p>
	 *                              The parameter {@link MethodInputLoggingConfiguration#isClassNameDisplayed()} is
	 *                              ignored here. So, if the enclosing class name must be displayed, the method name
	 *                              must already be prefixed with it.
     *                          </p>
     * @param args              The method input arguments.
     *                          <p>
	 *                              The parameters {@link MethodInputLoggingConfiguration#getExcludedArguments()} and
	 *                              {@link MethodInputLoggingConfiguration#getOnlyLoggedArguments()} will be applied on
	 *                              the names defined as keys of each pair in the list.
     *                          </p>
     */
    public void logMethodInput(@NonNull final MethodInputLoggingConfiguration configuration,
                               @NonNull final String methodName, @NonNull final List<Pair<String, Object>> args) {
		final Map<String, String> methodArgsMap = LoggingUtils.mapMethodArguments(args, configuration);

		// Build the map of data to store in the log context if required.
		Map<String, String> contextualData = null;
		if (configuration.isDataLoggedInContext()) {
			contextualData = new HashMap<>() {
				{
					put(INVOKED_METHOD_PROPERTY, methodName);
					putAll(methodArgsMap);
				}
			};
		}

	    // Effectively log the input data.
    	if (configuration.isStructuredMessage()) {
    		final MethodInputLogEntry structuredMessage = MethodInputLogEntry.builder()
				.calledMethod(methodName)
				.inputParameters(methodArgsMap)
				.build();
    		loggerManager.logWithLevel(configuration.getLogLevel(),
				LoggingUtils.prettify(structuredMessage,
					Optional.ofNullable(configuration.getPrettyFormat()).orElse(PrettyDataFormat.JSON)),
				contextualData);
		} else {
			loggerManager.logWithLevel(configuration.getLogLevel(), configuration.getMessageTemplate(), contextualData,
				methodName, LoggingUtils.formatMethodArguments(args, configuration));
		}
    }

    /**
     * Logs the output value of a method invocation.
     *
     * @param configuration     The configuration used for logging.
     * @param method            The invoked method.
     * @param outputValue       The method output value.
     */
    public void logMethodOutput(@NonNull final MethodOutputLoggingConfiguration configuration,
                                @NonNull final Method method, final Object outputValue) {
        if (void.class.equals(method.getReturnType())) {
            logMethodOutput(configuration, LoggingUtils.getMethodName(method, configuration.isClassNameDisplayed()));
        } else {
            logMethodOutput(configuration, LoggingUtils.getMethodName(method, configuration.isClassNameDisplayed()),
                    outputValue);
        }
    }

    /**
     * Logs the output value of a method invocation.
     *
     * @param configuration     The configuration used for logging.
     * @param methodName        The name of the invoked method.
     *                          <p>
	 *                              The parameter {@link MethodOutputLoggingConfiguration#isClassNameDisplayed()} is
	 *                              ignored here. So, if the enclosing class name must be displayed, the method name
	 *                              must already be prefixed with it.
     *                          </p>
     * @param outputValue       The method output value.
     */
    public void logMethodOutput(@NonNull final MethodOutputLoggingConfiguration configuration,
                                @NonNull final String methodName, final Object outputValue) {
    	final String formattedOutputValue = LoggingUtils.formatData(outputValue, configuration.getPrettyFormat(),
			configuration.isCollectionsAndMapsExpanded());

		// Build the map of data to store in the log context if required.
		Map<String, String> contextualData = null;
		if (configuration.isDataLoggedInContext()) {
			contextualData = buildOutputContextualData(methodName, formattedOutputValue);
		}

		// Effectively log the output data.
		if (configuration.isStructuredMessage()) {
			final MethodOutputLogEntry structuredMessage = MethodOutputLogEntry.builder()
				.calledMethod(methodName)
				.outputValue(formattedOutputValue)
				.build();
			loggerManager.logWithLevel(configuration.getLogLevel(),
				LoggingUtils.prettify(structuredMessage,
					Optional.ofNullable(configuration.getPrettyFormat()).orElse(PrettyDataFormat.JSON)),
				contextualData);
		} else {
			loggerManager.logWithLevel(configuration.getLogLevel(), configuration.getMessageTemplate(), contextualData,
				methodName, formattedOutputValue);
		}
    }

    /**
     * Logs the end of the invocation of a method returning {@code void}.
     *
     * @param configuration     The configuration used for logging.
     * @param methodName        The name of the invoked method.
	 *                          <p>
	 *                              The parameter {@link MethodOutputLoggingConfiguration#isClassNameDisplayed()} is
	 *                              ignored here. So, if the enclosing class name must be displayed, the method name
	 *                              must already be prefixed with it.
	 *                          </p>
     */
    public void logMethodOutput(@NonNull final MethodOutputLoggingConfiguration configuration,
                                @NonNull final String methodName) {
		// Build the map of data to store in the log context if required.
		Map<String, String> contextualData = null;
		if (configuration.isDataLoggedInContext()) {
			contextualData = buildOutputContextualData(methodName, Void.TYPE.getName());
		}

		// Effectively log the output data.
		if (configuration.isStructuredMessage()) {
			final MethodOutputLogEntry structuredMessage = MethodOutputLogEntry.builder()
				.calledMethod(methodName)
				.outputValue(Void.TYPE.getName())
				.build();
			loggerManager.logWithLevel(configuration.getLogLevel(),
				LoggingUtils.prettify(structuredMessage,
					Optional.ofNullable(configuration.getPrettyFormat()).orElse(PrettyDataFormat.JSON)),
				contextualData);
		} else {
			loggerManager.logWithLevel(configuration.getLogLevel(), configuration.getVoidOutputMessageTemplate(),
				contextualData, methodName);
		}
    }

    /**
     * Logs an exception or an error thrown during a method invocation.
     *
	 * @param configuration     The configuration used for logging.
	 * @param method        	The invoked method throwing the exception or error.
     * @param throwable 		The exception or error to log.
     */
    public void logThrowable(@NonNull final MethodOutputLoggingConfiguration configuration,
							 @NonNull final Method method, @NonNull final Throwable throwable) {
    	final String methodName = LoggingUtils.getMethodName(method, configuration.isClassNameDisplayed());

		// Build the map of data to store in the log context if required.
		Map<String, String> contextualData = null;
		if (configuration.isDataLoggedInContext()) {
			contextualData = buildThrowableContextualData(methodName, throwable.getClass());
		}

		// Effectively log the Throwable.
		if (configuration.isStructuredMessage()) {
			final MethodOutputLogEntry structuredMessage = MethodOutputLogEntry.builder()
				.calledMethod(methodName)
				.thrown(throwable.getClass())
				.thrownMessage(throwable.getMessage())
				.stackTrace(Arrays.stream(throwable.getStackTrace())
					.map(StackTraceElement::toString)
					.collect(Collectors.toList()))
				.build();
			loggerManager.logWithLevel(LogLevel.ERROR,
				LoggingUtils.prettify(structuredMessage,
					Optional.ofNullable(configuration.getPrettyFormat()).orElse(PrettyDataFormat.JSON)),
				contextualData);
		} else {
			logThrowable(throwable, contextualData);
		}
    }

    /**
     * Logs an exception or an error thrown during a method invocation.
     *
     * @param throwable 	 The exception or error to log.
     * @param contextualData The data to store into the log context.
     */
    public void logThrowable(@NonNull final Throwable throwable, final Map<String, String> contextualData) {
    	loggerManager.logWithLevel(LogLevel.ERROR, MethodOutputLoggingConfiguration.THROWABLE_MESSAGE_TEMPLATE,
			contextualData, throwable.getClass().getName(), throwable.getMessage(), throwable);
    }

	/**
	 * Logs an exception or an error thrown during a method invocation.
	 *
	 * @param throwable 	 The exception or error to log.
	 */
	public void logThrowable(@NonNull final Throwable throwable) {
		logThrowable(throwable, null);
	}

	/**
	 * Builds a map of contextual data for logging of the output value of a method invocation.
	 *
	 * @param methodName	The name of the invoked method.
	 * @param outputValue	The string representation of the output value.
	 * @return The map of contextual data to store in the log context.
	 */
	private static Map<String, String> buildOutputContextualData(final String methodName, final String outputValue) {
		return new HashMap<>() {
			{
				put(INVOKED_METHOD_PROPERTY, methodName);
				put("outputValue", outputValue);
			}
		};
	}

	/**
	 * Builds a map of contextual data for logging of the information related to a {@link Throwable} thrown during a
	 * method invocation.
	 *
	 * @param methodName	The name of the invoked method.
	 * @param throwableType The class of the throwen exception or error.
	 * @return The map of contextual data to store in the log context.
	 */
	private static Map<String, String> buildThrowableContextualData(final String methodName,
																	final Class<? extends Throwable> throwableType) {
		return new HashMap<>() {
			{
				put(INVOKED_METHOD_PROPERTY, methodName);
				put("outputValue", "n/a");
				put("throwableType", throwableType.getName());
			}
		};
	}

}
