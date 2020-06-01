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

import org.apiguardian.api.API;

import java.util.Map;

/**
 * Interface wrapping a real logger instance.
 * <p>
 *     Any class implementing this interface should implement the singleton pattern since the {@link LoggerManager}
 *     accepts to register only one instance of each available implementation of {@code LoggerInterface}. The method
 *     to implement in order to get a singleton instance is {@code getInstance()}.
 * </p>
 * <p>
 *     <i>Note: </i> The message template syntax used in the arguments {@code format} of the methods described in this
 *     interface is the same as the one used by SLF4J.
 * </p>
 * @see org.slf4j.Logger
 * @see org.slf4j.helpers.MessageFormatter
 */
@API(status = API.Status.STABLE, since = "1.0.0")
public interface LoggerInterface {

	/**
	 * Logs a message at the TRACE level according to the specified format and arguments.
	 *
	 * @param format    The format string.
	 * @param arguments The list of arguments.
	 * @deprecated Use {@link #trace(String, String, Object...)} instead. The default implementation now uses this
	 * 			   implementation with {@value LoggingUtils#AUTOLOG_DEFAULT_TOPIC} as default logger name.
	 */
	@Deprecated(since = "1.2.0")
	@API(status = API.Status.DEPRECATED, since = "1.2.0")
	default void trace(String format, Object... arguments) {
		trace(LoggingUtils.AUTOLOG_DEFAULT_TOPIC, format, arguments);
	}

	/**
	 * Logs a message at the TRACE level with the given name, according to the specified format and arguments.
	 *
	 * @param topic		The logger name.
	 * @param format	The format string.
	 * @param arguments	The list of arguments.
	 */
	@API(status = API.Status.STABLE, since = "1.2.0")
	void trace(String topic, String format, Object... arguments);

	/**
	 * Logs a message at the TRACE level, including structured contextual data, according to the specified format and
	 * arguments.
	 *
	 * @param format			The format string.
	 * @param contextualData	The structured data stored into the log context. When it is possible the default
	 *                          implementation (simply calling {@link #trace(String, Object...)}) must be overridden.
	 * @param arguments			The list of arguments.
	 * @deprecated Use {@link #trace(String, String, Map, Object...)} instead. The default implementation now uses
	 * 			   {@value LoggingUtils#AUTOLOG_DEFAULT_TOPIC} as default logger name.
	 */
	@Deprecated(since = "1.2.0")
	@API(status = API.Status.DEPRECATED, since = "1.2.0")
	default void trace(String format, Map<String, String> contextualData, Object... arguments) {
		trace(LoggingUtils.AUTOLOG_DEFAULT_TOPIC, format, arguments);
	}

	/**
	 * Logs a message at the TRACE level with the given name, including structured contextual data, according to
	 * the specified format and arguments.
	 *
	 * @param topic				The logger name.
	 * @param format			The format string.
	 * @param contextualData	The structured data stored into the log context. When it is possible the default
	 *                          implementation (simply calling {@link #trace(String, Object...)}) must be overridden.
	 * @param arguments			The list of arguments.
	 */
	@API(status = API.Status.STABLE, since = "1.2.0")
	default void trace(String topic, String format, Map<String, String> contextualData, Object... arguments) {
		trace(topic, format, arguments);
	}

	/**
	 * Logs a message at the DEBUG level according to the specified format and arguments.
	 *
	 * @param format    The format string.
	 * @param arguments The list of arguments.
	 * @deprecated Use {@link #debug(String, String, Object...)} instead. The default implementation now uses this
	 * 			   implementation with {@value LoggingUtils#AUTOLOG_DEFAULT_TOPIC} as default logger name.
	 */
	@Deprecated(since = "1.2.0")
	@API(status = API.Status.DEPRECATED, since = "1.2.0")
	default void debug(String format, Object... arguments) {
		debug(LoggingUtils.AUTOLOG_DEFAULT_TOPIC, format, arguments);
	}

	/**
	 * Logs a message at the DEBUG level with the given name, according to the specified format and arguments.
	 *
	 * @param topic		The logger name.
	 * @param format	The format string.
	 * @param arguments	The list of arguments.
	 */
	@API(status = API.Status.STABLE, since = "1.2.0")
	void debug(String topic, String format, Object... arguments);

	/**
	 * Logs a message at the DEBUG level, including structured contextual data, according to the specified format and
	 * arguments.
	 *
	 * @param format			The format string.
	 * @param contextualData	The structured data stored into the log context. When it is possible the default
	 *                          implementation (simply calling {@link #debug(String, Object...)}) must be overridden.
	 * @param arguments			The list of arguments.
	 * @deprecated Use {@link #debug(String, String, Map, Object...)} instead. The default implementation now uses
	 * 			   {@value LoggingUtils#AUTOLOG_DEFAULT_TOPIC} as default logger name.
	 */
	@Deprecated(since = "1.2.0")
	@API(status = API.Status.DEPRECATED, since = "1.2.0")
	default void debug(String format, Map<String, String> contextualData, Object... arguments) {
		debug(LoggingUtils.AUTOLOG_DEFAULT_TOPIC, format, arguments);
	}

	/**
	 * Logs a message at the DEBUG level with the given name, including structured contextual data, according to
	 * the specified format and arguments.
	 *
	 * @param topic				The logger name.
	 * @param format			The format string.
	 * @param contextualData	The structured data stored into the log context. When it is possible the default
	 *                          implementation (simply calling {@link #trace(String, Object...)}) must be overridden.
	 * @param arguments			The list of arguments.
	 */
	@API(status = API.Status.STABLE, since = "1.2.0")
	default void debug(String topic, String format, Map<String, String> contextualData, Object... arguments) {
		debug(topic, format, arguments);
	}

	/**
	 * Logs a message at the INFO level according to the specified format and arguments.
	 *
	 * @param format    The format string.
	 * @param arguments The list of arguments.
	 * @deprecated Use {@link #info(String, String, Object...)} instead. The default implementation now uses this
	 * 			   implementation with {@value LoggingUtils#AUTOLOG_DEFAULT_TOPIC} as default logger name.
	 */
	@Deprecated(since = "1.2.0")
	@API(status = API.Status.DEPRECATED, since = "1.2.0")
	default void info(String format, Object... arguments) {
		info(LoggingUtils.AUTOLOG_DEFAULT_TOPIC, format, arguments);
	}

	/**
	 * Logs a message at the INFO level with the given name, according to the specified format and arguments.
	 *
	 * @param topic		The logger name.
	 * @param format	The format string.
	 * @param arguments	The list of arguments.
	 */
	@API(status = API.Status.STABLE, since = "1.2.0")
	void info(String topic, String format, Object... arguments);

	/**
	 * Logs a message at the INFO level, including structured contextual data, according to the specified format and
	 * arguments.
	 *
	 * @param format			The format string.
	 * @param contextualData	The structured data stored into the log context. When it is possible the default
	 *                          implementation (simply calling {@link #info(String, Object...)}) must be overridden.
	 * @param arguments			The list of arguments.
	 * @deprecated Use {@link #info(String, String, Map, Object...)} instead. The default implementation now uses
	 * 			   {@value LoggingUtils#AUTOLOG_DEFAULT_TOPIC} as default logger name.
	 */
	@Deprecated(since = "1.2.0")
	@API(status = API.Status.DEPRECATED, since = "1.2.0")
	default void info(String format, Map<String, String> contextualData, Object... arguments) {
		info(LoggingUtils.AUTOLOG_DEFAULT_TOPIC, format, arguments);
	}

	/**
	 * Logs a message at the INFO level with the given name, including structured contextual data, according to
	 * the specified format and arguments.
	 *
	 * @param topic				The logger name.
	 * @param format			The format string.
	 * @param contextualData	The structured data stored into the log context. When it is possible the default
	 *                          implementation (simply calling {@link #trace(String, Object...)}) must be overridden.
	 * @param arguments			The list of arguments.
	 */
	@API(status = API.Status.STABLE, since = "1.2.0")
	default void info(String topic, String format, Map<String, String> contextualData, Object... arguments) {
		info(topic, format, arguments);
	}

	/**
	 * Logs a message at the WARN level according to the specified format and arguments.
	 *
	 * @param format    The format string.
	 * @param arguments The list of arguments.
	 * @deprecated Use {@link #warn(String, String, Object...)} instead. The default implementation now uses this
	 * 			   implementation with {@value LoggingUtils#AUTOLOG_DEFAULT_TOPIC} as default logger name.
	 */
	@Deprecated(since = "1.2.0")
	@API(status = API.Status.DEPRECATED, since = "1.2.0")
	default void warn(String format, Object... arguments) {
		warn(LoggingUtils.AUTOLOG_DEFAULT_TOPIC, format, arguments);
	}

	/**
	 * Logs a message at the WARN level with the given name, according to the specified format and arguments.
	 *
	 * @param topic		The logger name.
	 * @param format	The format string.
	 * @param arguments	The list of arguments.
	 */
	@API(status = API.Status.STABLE, since = "1.2.0")
	void warn(String topic, String format, Object... arguments);

	/**
	 * Logs a message at the WARN level, including structured contextual data, according to the specified format and
	 * arguments.
	 *
	 * @param format			The format string.
	 * @param contextualData	The structured data stored into the log context. When it is possible the default
	 *                          implementation (simply calling {@link #warn(String, Object...)}) must be overridden.
	 * @param arguments			The list of arguments.
	 * @deprecated Use {@link #warn(String, String, Map, Object...)} instead. The default implementation now uses
	 * 			   {@value LoggingUtils#AUTOLOG_DEFAULT_TOPIC} as default logger name.
	 */
	@Deprecated(since = "1.2.0")
	@API(status = API.Status.DEPRECATED, since = "1.2.0")
	default void warn(String format, Map<String, String> contextualData, Object... arguments) {
		warn(LoggingUtils.AUTOLOG_DEFAULT_TOPIC, format, arguments);
	}

	/**
	 * Logs a message at the WARN level with the given name, including structured contextual data, according to
	 * the specified format and arguments.
	 *
	 * @param topic				The logger name.
	 * @param format			The format string.
	 * @param contextualData	The structured data stored into the log context. When it is possible the default
	 *                          implementation (simply calling {@link #trace(String, Object...)}) must be overridden.
	 * @param arguments			The list of arguments.
	 */
	@API(status = API.Status.STABLE, since = "1.2.0")
	default void warn(String topic, String format, Map<String, String> contextualData, Object... arguments) {
		warn(topic, format, arguments);
	}

	/**
	 * Logs a message at the ERROR level according to the specified format and arguments.
	 *
	 * @param format    The format string.
	 * @param arguments The list of arguments.
	 * @deprecated Use {@link #error(String, String, Object...)} instead. The default implementation now uses this
	 * 			   implementation with {@value LoggingUtils#AUTOLOG_DEFAULT_TOPIC} as default logger name.
	 */
	@Deprecated(since = "1.2.0")
	@API(status = API.Status.DEPRECATED, since = "1.2.0")
	default void error(String format, Object... arguments) {
		error(LoggingUtils.AUTOLOG_DEFAULT_TOPIC, format, arguments);
	}

	/**
	 * Logs a message at the ERROR level with the given name, according to the specified format and arguments.
	 *
	 * @param topic		The logger name.
	 * @param format	The format string.
	 * @param arguments	The list of arguments.
	 */
	@API(status = API.Status.STABLE, since = "1.2.0")
	void error(String topic, String format, Object... arguments);

	/**
	 * Logs a message at the WARN level, including structured contextual data, according to the specified format and
	 * arguments.
	 *
	 * @param format			The format string.
	 * @param contextualData	The structured data stored into the log context. When it is possible the default
	 *                          implementation (simply calling {@link #error(String, Object...)}) must be overridden.
	 * @param arguments			The list of arguments.
	 * @deprecated Use {@link #error(String, String, Map, Object...)} instead. The default implementation now uses
	 * 			   {@value LoggingUtils#AUTOLOG_DEFAULT_TOPIC} as default logger name.
	 */
	@Deprecated(since = "1.2.0")
	@API(status = API.Status.DEPRECATED, since = "1.2.0")
	default void error(String format, Map<String, String> contextualData, Object... arguments) {
		error(LoggingUtils.AUTOLOG_DEFAULT_TOPIC, format, arguments);
	}

	/**
	 * Logs a message at the ERROR level with the given name, including structured contextual data, according to
	 * the specified format and arguments.
	 *
	 * @param topic				The logger name.
	 * @param format			The format string.
	 * @param contextualData	The structured data stored into the log context. When it is possible the default
	 *                          implementation (simply calling {@link #trace(String, Object...)}) must be overridden.
	 * @param arguments			The list of arguments.
	 */
	@API(status = API.Status.STABLE, since = "1.2.0")
	default void error(String topic, String format, Map<String, String> contextualData, Object... arguments) {
		error(topic, format, arguments);
	}

}
