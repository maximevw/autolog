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
import lombok.extern.slf4j.Slf4j;
import org.apiguardian.api.API;
import org.slf4j.Logger;

/**
 * This class wraps an instance of {@link Logger} to be used by Autolog.
 */
@API(status = API.Status.STABLE, since = "1.0.0")
@Slf4j(topic = "Autolog")
public class Slf4jAdapter implements LoggerInterface {

	private Slf4jAdapter() {
		// Private constructor to force usage of singleton instance via the method getInstance().
	}

	/**
	 * Gets an instance of the SLF4J wrapper for Autolog.
	 *
	 * @return A singleton instance of the SLF4J wrapper.
	 */
	public static Slf4jAdapter getInstance() {
		return Slf4jAdapterInstanceHolder.INSTANCE;
	}

	@Override
	public void trace(final String format, final Object... arguments) {
		log.trace(format, arguments);
	}

	@Override
	public void debug(final String format, final Object... arguments) {
		log.debug(format, arguments);
	}

	@Override
	public void info(final String format, final Object... arguments) {
		log.info(format, arguments);
	}

	@Override
	public void warn(final String format, final Object... arguments) {
		log.warn(format, arguments);
	}

	@Override
	public void error(final String format, final Object... arguments) {
		log.error(format, arguments);
	}

	private static class Slf4jAdapterInstanceHolder {
		private static final Slf4jAdapter INSTANCE = new Slf4jAdapter();
	}
}
