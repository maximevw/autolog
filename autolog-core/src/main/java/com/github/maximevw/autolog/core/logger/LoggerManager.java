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

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apiguardian.api.API;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class manages the loggers used by Autolog.
 * <p>
 *     The real loggers are wrapped into an instance of a class implementing {@link LoggerInterface}. Only one instance
 *     of a given implementation of {@link LoggerInterface} can be registered in the {@code LoggerManager}.
 * </p>
 */
@API(status = API.Status.STABLE, since = "1.0.0")
@NoArgsConstructor
public class LoggerManager {

	/**
	 * Registered loggers.
	 */
	@Getter
	private final List<LoggerInterface> registeredLoggers = new ArrayList<>();

	/**
	 * Registers an additional logger.
	 * <p>
	 *     Only one instance of a given implementation of {@link LoggerInterface} can be registered in the
	 *     {@code LoggerManager}.
	 * </p>
	 *
	 * @param logger An instance of {@link LoggerInterface} to register.
	 * @return The manager with the latest registered logger.
	 */
	public LoggerManager register(final LoggerInterface logger) {
		if (this.registeredLoggers.stream()
			.noneMatch(loggerInterface -> loggerInterface.getClass().isAssignableFrom(logger.getClass()))) {
			this.registeredLoggers.add(logger);
		} else {
			LoggingUtils.report(String.format("Logger of type %s already registered.",
				logger.getClass().getSimpleName()), LogLevel.WARN);
		}
		return this;
	}

	/**
	 * Unregisters a logger from the manager.
	 *
	 * @param loggerType The type of logger to unregister.
	 * @return The manager with the remaining registered loggers.
	 */
	public LoggerManager unregister(final Class<? extends LoggerInterface> loggerType) {
		this.registeredLoggers.removeIf(loggerInterface -> loggerInterface.getClass().isAssignableFrom(loggerType));
		return this;
	}

	/**
	 * Logs a message with the specified log level.
	 *
	 * @param logLevel  		The level to use for logging.
	 * @param message   		The message to log. It can contains placeholders ("<code>{}</code>") which will be
	 *                          replaced by values of {@code arguments}.
	 *                          <p>
	 *                              <i>Note:</i> The message template syntax is the same as the one used by SLF4J.
	 *                  		</p>
	 * @param arguments 		The arguments to include in the log message.
	 * @see org.slf4j.Logger
	 * @see org.slf4j.helpers.MessageFormatter
	 */
	void logWithLevel(final LogLevel logLevel, final String message, final Object... arguments) {
		logWithLevel(logLevel, message, null, arguments);
	}

	/**
	 * Logs a message with the specified log level and the given contextual values.
	 *
	 * @param logLevel  		The level to use for logging.
	 * @param message   		The message to log. It can contains placeholders ("<code>{}</code>") which will be
	 *                          replaced by values of {@code arguments}.
	 *                          <p>
	 *                              <i>Note:</i> The message template syntax is the same as the one used by SLF4J.
	 *                  		</p>
	 * @param contextualData 	The structured data stored into the log context, when it is possible (see
	 *                          implementations of {@link LoggerInterface} for details).
	 * @param arguments 		The arguments to include in the log message.
	 * @see org.slf4j.Logger
	 * @see org.slf4j.helpers.MessageFormatter
	 */
	@API(status = API.Status.STABLE, since = "1.1.0")
	void logWithLevel(final LogLevel logLevel, final String message, final Map<String, String> contextualData,
					  final Object... arguments) {
		logWithLevel(logLevel, LoggingUtils.AUTOLOG_DEFAULT_TOPIC, message, contextualData, arguments);
	}

	/**
	 * Logs a message with the specified log level, logger name and the given contextual values.
	 *
	 * @param logLevel  		The level to use for logging.
	 * @param topic		  		The logger name.
	 * @param message   		The message to log. It can contains placeholders ("<code>{}</code>") which will be
	 *                          replaced by values of {@code arguments}.
	 *                          <p>
	 *                              <i>Note:</i> The message template syntax is the same as the one used by SLF4J.
	 *                  		</p>
	 * @param contextualData 	The structured data stored into the log context, when it is possible (see
	 *                          implementations of {@link LoggerInterface} for details).
	 * @param arguments 		The arguments to include in the log message.
	 * @see org.slf4j.Logger
	 * @see org.slf4j.helpers.MessageFormatter
	 */
	@API(status = API.Status.STABLE, since = "1.2.0")
	void logWithLevel(final LogLevel logLevel, final String topic, final String message,
					  final Map<String, String> contextualData, final Object... arguments) {
		final String logMethodName = logLevel.name().toLowerCase();
		final String safeTopic = StringUtils.defaultIfBlank(topic, LoggingUtils.AUTOLOG_DEFAULT_TOPIC);

		this.registeredLoggers.forEach(logger -> {
			try {
				final Method logMethod;
				if (contextualData == null) {
					logMethod = logger.getClass().getMethod(logMethodName, String.class, String.class, Object[].class);
					logMethod.invoke(logger, safeTopic, StringUtils.defaultString(message, StringUtils.EMPTY),
						arguments);
				} else {
					logMethod = logger.getClass().getMethod(logMethodName, String.class, String.class, Map.class,
						Object[].class);
					logMethod.invoke(logger, safeTopic, StringUtils.defaultString(message, StringUtils.EMPTY),
						contextualData, arguments);
				}
			} catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				LoggingUtils.reportError(String.format("Unable to invoke logging method %s() on class: %s.",
					logLevel.name().toLowerCase(), logger.getClass().getName()), e);
			}
		});
	}

}
