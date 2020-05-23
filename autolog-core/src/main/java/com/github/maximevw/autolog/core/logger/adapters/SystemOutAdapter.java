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

import com.github.maximevw.autolog.core.logger.LogLevel;
import com.github.maximevw.autolog.core.logger.LoggerInterface;
import com.github.maximevw.autolog.core.logger.LoggingUtils;
import org.apiguardian.api.API;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

/**
 * This class wraps the standard output ({@link System#out} and {@link System#err}) to be used as logger by Autolog.
 */
@API(status = API.Status.STABLE, since = "1.0.0")
public class SystemOutAdapter implements LoggerInterface {

	private SystemOutAdapter() {
		// Private constructor to force usage of singleton instance via the method getInstance().
	}

	/**
	 * Gets an instance of the standard output wrapper for Autolog.
	 *
	 * @return A singleton instance of the standard output wrapper.
	 */
	public static SystemOutAdapter getInstance() {
		return SystemOutAdapterInstanceHolder.INSTANCE;
	}

	@Override
	public void trace(final String format, final Object... arguments) {
		trace(LoggingUtils.AUTOLOG_DEFAULT_TOPIC, format, arguments);
	}

	@Override
	public void trace(final String topic, final String format, final Object... arguments) {
		log(topic, LogLevel.TRACE, format, arguments);
	}

	@Override
	public void debug(final String format, final Object... arguments) {
		debug(LoggingUtils.AUTOLOG_DEFAULT_TOPIC, format, arguments);
	}

	@Override
	public void debug(final String topic, final String format, final Object... arguments) {
		log(topic, LogLevel.DEBUG, format, arguments);
	}

	@Override
	public void info(final String format, final Object... arguments) {
		info(LoggingUtils.AUTOLOG_DEFAULT_TOPIC, format, arguments);
	}

	@Override
	public void info(final String topic, final String format, final Object... arguments) {
		log(topic, LogLevel.INFO, format, arguments);
	}

	@Override
	public void warn(final String format, final Object... arguments) {
		warn(LoggingUtils.AUTOLOG_DEFAULT_TOPIC, format, arguments);
	}

	@Override
	public void warn(final String topic, final String format, final Object... arguments) {
		log(topic, LogLevel.WARN, format, arguments);
	}

	@Override
	public void error(final String format, final Object... arguments) {
		error(LoggingUtils.AUTOLOG_DEFAULT_TOPIC, format, arguments);
	}

	@Override
	public void error(final String topic, final String format, final Object... arguments) {
		final FormattingTuple formattingTuple = MessageFormatter.arrayFormat(format, arguments);
		System.err.println(String.format("[%s] ERROR: %s", topic, formattingTuple.getMessage()));
		if (formattingTuple.getThrowable() != null) {
			formattingTuple.getThrowable().printStackTrace();
		}
	}

	private void log(final String topic, final LogLevel level, final String format, final Object... arguments) {
		final FormattingTuple formattingTuple = MessageFormatter.arrayFormat(format, arguments);
		System.out.println(String.format("[%s] %s: %s", topic, level.name(), formattingTuple.getMessage()));
	}

	private static class SystemOutAdapterInstanceHolder {
		// Unique instance not pre-initialized.
		private static final SystemOutAdapter INSTANCE = new SystemOutAdapter();
	}
}
