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

package com.github.maximevw.autolog.core.logger.adapters;

import com.github.maximevw.autolog.core.logger.LoggerInterface;
import com.github.maximevw.autolog.core.logger.LoggingUtils;
import org.apache.logging.log4j.CloseableThreadContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apiguardian.api.API;

import java.util.Map;

/**
 * This class wraps an instance of {@link Logger} to be used by Autolog.
 */
@API(status = API.Status.STABLE, since = "1.0.0")
public class Log4j2Adapter extends LoggerFactoryBasedAdapter<Logger> implements LoggerInterface {

	private Log4j2Adapter() {
		// Private constructor to force usage of singleton instance via the method getInstance().
	}

	/**
	 * Gets an instance of the Log4J2 wrapper for Autolog.
	 *
	 * @return A singleton instance of the Log4J2 wrapper.
	 */
	public static Log4j2Adapter getInstance() {
		return Log4j2AdapterInstanceHolder.INSTANCE;
	}

	@Override
	public void trace(final String format, final Object... arguments) {
		trace(LoggingUtils.AUTOLOG_DEFAULT_TOPIC, format, arguments);
	}

	@Override
	public void trace(final String topic, final String format, final Object... arguments) {
		getLogger(topic).trace(format, arguments);
	}

	@Override
	public void trace(final String format, final Map<String, String> contextualData, final Object... arguments) {
		trace(LoggingUtils.AUTOLOG_DEFAULT_TOPIC, format, contextualData, arguments);
	}

	@Override
	public void trace(final String topic, final String format, final Map<String, String> contextualData,
					  final Object... arguments) {
		try (var ignored = CloseableThreadContext.putAll(contextualData)) {
			trace(topic, format, arguments);
		}
	}

	@Override
	public void debug(final String format, final Object... arguments) {
		debug(LoggingUtils.AUTOLOG_DEFAULT_TOPIC, format, arguments);
	}

	@Override
	public void debug(final String topic, final String format, final Object... arguments) {
		getLogger(topic).debug(format, arguments);
	}

	@Override
	public void debug(final String format, final Map<String, String> contextualData, final Object... arguments) {
		debug(LoggingUtils.AUTOLOG_DEFAULT_TOPIC, format, contextualData, arguments);
	}

	@Override
	public void debug(final String topic, final String format, final Map<String, String> contextualData,
					  final Object... arguments) {
		try (var ignored = CloseableThreadContext.putAll(contextualData)) {
			debug(topic, format, arguments);
		}
	}

	@Override
	public void info(final String format, final Object... arguments) {
		info(LoggingUtils.AUTOLOG_DEFAULT_TOPIC, format, arguments);
	}

	@Override
	public void info(final String topic, final String format, final Object... arguments) {
		getLogger(topic).info(format, arguments);
	}

	@Override
	public void info(final String format, final Map<String, String> contextualData, final Object... arguments) {
		info(LoggingUtils.AUTOLOG_DEFAULT_TOPIC, format, contextualData, arguments);
	}

	@Override
	public void info(final String topic, final String format, final Map<String, String> contextualData,
					  final Object... arguments) {
		try (var ignored = CloseableThreadContext.putAll(contextualData)) {
			info(topic, format, arguments);
		}
	}

	@Override
	public void warn(final String format, final Object... arguments) {
		warn(LoggingUtils.AUTOLOG_DEFAULT_TOPIC, format, arguments);
	}

	@Override
	public void warn(final String topic, final String format, final Object... arguments) {
		getLogger(topic).warn(format, arguments);
	}

	@Override
	public void warn(final String format, final Map<String, String> contextualData, final Object... arguments) {
		warn(LoggingUtils.AUTOLOG_DEFAULT_TOPIC, format, contextualData, arguments);
	}

	@Override
	public void warn(final String topic, final String format, final Map<String, String> contextualData,
					  final Object... arguments) {
		try (var ignored = CloseableThreadContext.putAll(contextualData)) {
			warn(topic, format, arguments);
		}
	}

	@Override
	public void error(final String format, final Object... arguments) {
		error(LoggingUtils.AUTOLOG_DEFAULT_TOPIC, format, arguments);
	}

	@Override
	public void error(final String topic, final String format, final Object... arguments) {
		getLogger(topic).error(format, arguments);
	}

	@Override
	public void error(final String format, final Map<String, String> contextualData, final Object... arguments) {
		error(LoggingUtils.AUTOLOG_DEFAULT_TOPIC, format, contextualData, arguments);
	}

	@Override
	public void error(final String topic, final String format, final Map<String, String> contextualData,
					  final Object... arguments) {
		try (var ignored = CloseableThreadContext.putAll(contextualData)) {
			error(topic, format, arguments);
		}
	}

	/**
	 * Gets an instance of Log4J2 {@link Logger} with the given name.
	 *
	 * @param topic The logger name.
	 * @return The configured instance of Log4J2 logger.
	 */
	@Override
	Logger getLoggerInstance(final String topic) {
		return LogManager.getLogger(topic);
	}

	private static class Log4j2AdapterInstanceHolder {
		private static final Log4j2Adapter INSTANCE = new Log4j2Adapter();
	}

}
