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
import org.slf4j.ext.XLogger;
import org.slf4j.ext.XLoggerFactory;

/**
 * This class wraps an instance of {@link XLogger} to be used by Autolog.
 */
@API(status = API.Status.STABLE, since = "1.0.0")
public class XSlf4jAdapter extends LoggerFactoryBasedAdapter<XLogger> implements LoggerInterface {

	private XSlf4jAdapter() {
		// Private constructor to force usage of singleton instance via the method getInstance().
	}

	/**
	 * Gets an instance of the XSLF4J wrapper for Autolog.
	 *
	 * @return A singleton instance of the XSLF4J wrapper.
	 */
	public static XSlf4jAdapter getInstance() {
		return XSlf4jAdapterInstanceHolder.INSTANCE;
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
	public void debug(final String format, final Object... arguments) {
		debug(LoggingUtils.AUTOLOG_DEFAULT_TOPIC, format, arguments);
	}

	@Override
	public void debug(final String topic, final String format, final Object... arguments) {
		getLogger(topic).debug(format, arguments);
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
	public void warn(final String format, final Object... arguments) {
		warn(LoggingUtils.AUTOLOG_DEFAULT_TOPIC, format, arguments);
	}

	@Override
	public void warn(final String topic, final String format, final Object... arguments) {
		getLogger(topic).warn(format, arguments);
	}

	@Override
	public void error(final String format, final Object... arguments) {
		error(LoggingUtils.AUTOLOG_DEFAULT_TOPIC, format, arguments);
	}

	@Override
	public void error(final String topic, final String format, final Object... arguments) {
		getLogger(topic).error(format, arguments);
	}

	/**
	 * Gets an instance of XSLF4J {@link XLogger} with the given name.
	 *
	 * @param topic The logger name.
	 * @return The configured instance of XSLF4J logger.
	 */
	@Override
	XLogger getLoggerInstance(final String topic) {
		return XLoggerFactory.getXLogger(topic);
	}

	private static class XSlf4jAdapterInstanceHolder {
		private static final XSlf4jAdapter INSTANCE = new XSlf4jAdapter();
	}
}
