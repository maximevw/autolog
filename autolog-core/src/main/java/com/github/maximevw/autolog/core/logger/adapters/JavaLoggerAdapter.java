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
import lombok.extern.java.Log;
import org.apiguardian.api.API;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class wraps an instance of {@link Logger} to be used by Autolog.
 */
@API(status = API.Status.STABLE, since = "1.0.0")
@Log(topic = "Autolog")
public class JavaLoggerAdapter implements LoggerInterface {

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
		log(Level.FINEST, format, arguments);
	}

	@Override
	public void debug(final String format, final Object... arguments) {
		log(Level.FINE, format, arguments);
	}

	@Override
	public void info(final String format, final Object... arguments) {
		log(Level.INFO, format, arguments);
	}

	@Override
	public void warn(final String format, final Object... arguments) {
		log(Level.WARNING, format, arguments);
	}

	@Override
	public void error(final String format, final Object... arguments) {
		log(Level.SEVERE, format, arguments);
	}

	private void log(final Level javaLogLevel, final String format, final Object... arguments) {
		final FormattingTuple formattingTuple = MessageFormatter.arrayFormat(format, arguments);
		log.log(javaLogLevel, formattingTuple.getMessage());
	}

	private static class JavaLoggerAdapterInstanceHolder {
		private static final JavaLoggerAdapter INSTANCE = new JavaLoggerAdapter();
	}

}
