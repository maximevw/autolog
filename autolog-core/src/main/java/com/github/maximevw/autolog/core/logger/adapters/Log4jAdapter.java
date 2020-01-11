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
import lombok.extern.log4j.Log4j;
import org.apache.log4j.Logger;
import org.apiguardian.api.API;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * This class wraps an instance of {@link Logger} to be used by Autolog.
 *
 * @deprecated Use {@link Log4j2Adapter} with Log4J2 instead. Indeed, Log4J is deprecated and now classified as highly
 * 			   vulnerable (see <a href="https://nvd.nist.gov/vuln/detail/CVE-2019-17571">CVE-2019-17571</a>).
 */
@Deprecated
@API(status = API.Status.DEPRECATED, since = "1.1.0")
@Log4j(topic = "Autolog")
public class Log4jAdapter implements LoggerInterface {

	private Log4jAdapter() {
		// Private constructor to force usage of singleton instance via the method getInstance().
	}

	/**
	 * Gets an instance of the Log4J wrapper for Autolog.
	 *
	 * @return A singleton instance of the Log4J wrapper.
	 */
	public static Log4jAdapter getInstance() {
		return Log4jAdapterInstanceHolder.INSTANCE;
	}

	@Override
	public void trace(final String format, final Object... arguments) {
		log(LogLevel.TRACE, format, arguments);
	}

	@Override
	public void debug(final String format, final Object... arguments) {
		log(LogLevel.DEBUG, format, arguments);
	}

	@Override
	public void info(final String format, final Object... arguments) {
		log(LogLevel.INFO, format, arguments);
	}

	@Override
	public void warn(final String format, final Object... arguments) {
		log(LogLevel.WARN, format, arguments);
	}

	@Override
	public void error(final String format, final Object... arguments) {
		log(LogLevel.ERROR, format, arguments);
	}

	private void log(final LogLevel level, final String format, final Object... arguments) {
		final FormattingTuple formattingTuple = MessageFormatter.arrayFormat(format, arguments);
		final String logMethodName = level.name().toLowerCase();

		try {
			final Method logMethod = log.getClass().getMethod(logMethodName, Object.class, Throwable.class);
			logMethod.invoke(log, formattingTuple.getMessage(), formattingTuple.getThrowable());
		} catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			LoggingUtils.reportError(String.format("Unable to invoke logging method %s() on class: %s.",
					logMethodName, log.getClass().getName()), e);
		}
	}

	private static class Log4jAdapterInstanceHolder {
		private static final Log4jAdapter INSTANCE = new Log4jAdapter();
	}

}
