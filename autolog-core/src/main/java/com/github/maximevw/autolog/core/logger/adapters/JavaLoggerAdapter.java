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
import org.apiguardian.api.API;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class wraps an instance of {@link Logger} to be used by Autolog.
 */
@API(status = API.Status.STABLE, since = "1.0.0")
public class JavaLoggerAdapter extends LoggerFactoryBasedAdapter<Logger> implements LoggerInterface {

	private JavaLoggerAdapter() {
		// Private constructor to force usage of singleton instance via the method getInstance().
	}

	/**
	 * Gets an instance of the classic Java logging wrapper for Autolog.
	 *
	 * @return A singleton instance of the classic Java logging wrapper.
	 */
	public static JavaLoggerAdapter getInstance() {
		return JavaLoggerAdapterInstanceHolder.INSTANCE;
	}

	@Override
	public void trace(final String format, final Object... arguments) {
		trace(LoggingUtils.AUTOLOG_DEFAULT_TOPIC, format, arguments);
	}

	@Override
	public void trace(final String topic, final String format, final Object... arguments) {
		log(topic, Level.FINEST, format, arguments);
	}

	@Override
	public void debug(final String format, final Object... arguments) {
		debug(LoggingUtils.AUTOLOG_DEFAULT_TOPIC, format, arguments);
	}

	@Override
	public void debug(final String topic, final String format, final Object... arguments) {
		log(topic, Level.FINE, format, arguments);
	}

	@Override
	public void info(final String format, final Object... arguments) {
		info(LoggingUtils.AUTOLOG_DEFAULT_TOPIC, format, arguments);
	}

	@Override
	public void info(final String topic, final String format, final Object... arguments) {
		log(topic, Level.INFO, format, arguments);
	}

	@Override
	public void warn(final String format, final Object... arguments) {
		warn(LoggingUtils.AUTOLOG_DEFAULT_TOPIC, format, arguments);
	}

	@Override
	public void warn(final String topic, final String format, final Object... arguments) {
		log(topic, Level.WARNING, format, arguments);
	}

	@Override
	public void error(final String format, final Object... arguments) {
		error(LoggingUtils.AUTOLOG_DEFAULT_TOPIC, format, arguments);
	}

	@Override
	public void error(final String topic, final String format, final Object... arguments) {
		log(topic, Level.SEVERE, format, arguments);
	}

	private void log(final String topic, final Level javaLogLevel, final String format, final Object... arguments) {
		final FormattingTuple formattingTuple = MessageFormatter.arrayFormat(format, arguments);
		getLogger(topic).log(javaLogLevel, formattingTuple.getMessage());
	}

	/**
	 * Gets an instance of classic Java {@link Logger} with the given name.
	 *
	 * @param topic The logger name.
	 * @return The configured instance of classic Java logger.
	 */
	Logger getLoggerInstance(final String topic) {
		return Logger.getLogger(topic);
	}

	private static class JavaLoggerAdapterInstanceHolder {
		private static final JavaLoggerAdapter INSTANCE = new JavaLoggerAdapter();
	}

}
