/*-
 * #%L
 * Autolog core module
 * %%
 * Copyright (C) 2019 - 2020 Maxime WIEWIORA
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
import net.logstash.logback.argument.StructuredArgument;
import net.logstash.logback.encoder.LogstashEncoder;
import org.apache.commons.lang3.ArrayUtils;
import org.apiguardian.api.API;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static net.logstash.logback.argument.StructuredArguments.keyValue;

/**
 * This class wraps an instance of {@link Logger} (using Logback implementation with {@link LogstashEncoder}) to be
 * used by Autolog.
 * <p>
 *     If you are using Logback implementation of SLF4J without {@link LogstashEncoder}, use {@link Slf4jAdapter}
 *     instead.
 * </p>
 */
@API(status = API.Status.STABLE, since = "1.1.0")
public class LogbackWithLogstashAdapter extends LoggerFactoryBasedAdapter<Logger> implements LoggerInterface {

	private LogbackWithLogstashAdapter() {
		// Private constructor to force usage of singleton instance via the method getInstance().
	}

	/**
	 * Gets an instance of the Logback with Logstash encoder wrapper for Autolog.
	 *
	 * @return A singleton instance of the Logback with Logstash encoder wrapper.
	 */
	public static LogbackWithLogstashAdapter getInstance() {
		return LogbackWithLogstashAdapterInstanceHolder.INSTANCE;
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
		trace(format, ArrayUtils.addAll(arguments, toStructuredArguments(contextualData)));
	}

	@Override
	public void trace(final String topic, final String format, final Map<String, String> contextualData,
					  final Object... arguments) {
		trace(topic, format, ArrayUtils.addAll(arguments, toStructuredArguments(contextualData)));
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
		debug(format, ArrayUtils.addAll(arguments, toStructuredArguments(contextualData)));
	}

	@Override
	public void debug(final String topic, final String format, final Map<String, String> contextualData,
					  final Object... arguments) {
		debug(topic, format, ArrayUtils.addAll(arguments, toStructuredArguments(contextualData)));
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
		info(format, ArrayUtils.addAll(arguments, toStructuredArguments(contextualData)));
	}

	@Override
	public void info(final String topic, final String format, final Map<String, String> contextualData,
					  final Object... arguments) {
		info(topic, format, ArrayUtils.addAll(arguments, toStructuredArguments(contextualData)));
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
		warn(format, ArrayUtils.addAll(arguments, toStructuredArguments(contextualData)));
	}

	@Override
	public void warn(final String topic, final String format, final Map<String, String> contextualData,
					  final Object... arguments) {
		warn(topic, format, ArrayUtils.addAll(arguments, toStructuredArguments(contextualData)));
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
		error(format, ArrayUtils.addAll(arguments, toStructuredArguments(contextualData)));
	}

	@Override
	public void error(final String topic, final String format, final Map<String, String> contextualData,
					  final Object... arguments) {
		error(topic, format, ArrayUtils.addAll(arguments, toStructuredArguments(contextualData)));
	}

	/**
	 * Maps the data to store in the log context to an array of {@link StructuredArgument} passed as argument of the
	 * logging method.
	 *
	 * @param contextualData The structured data stored into the log context.
	 * @return An array of {@link StructuredArgument}.
	 */
	private Object[] toStructuredArguments(final Map<String, String> contextualData) {
		return contextualData.entrySet().stream()
			.map(entry -> keyValue(entry.getKey(), entry.getValue()))
			.toArray();
	}

	/**
	 * Gets an instance of SLF4J {@link Logger} with the given name.
	 *
	 * @param topic The logger name.
	 * @return The configured instance of SLF4J logger.
	 */
	@Override
	Logger getLoggerInstance(final String topic) {
		return LoggerFactory.getLogger(topic);
	}

	private static class LogbackWithLogstashAdapterInstanceHolder {
		private static final LogbackWithLogstashAdapter INSTANCE = new LogbackWithLogstashAdapter();
	}

}
