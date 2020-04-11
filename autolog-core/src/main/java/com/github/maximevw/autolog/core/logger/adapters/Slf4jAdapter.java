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
import org.slf4j.MDC;
import org.slf4j.MDC.MDCCloseable;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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
	public void trace(final String format, final Map<String, String> contextualData, final Object... arguments) {
		try (var ignored = buildMdcCloseable(contextualData)) {
			trace(format, arguments);
		}
	}

	@Override
	public void debug(final String format, final Object... arguments) {
		log.debug(format, arguments);
	}

	@Override
	public void debug(final String format, final Map<String, String> contextualData, final Object... arguments) {
		try (var ignored = buildMdcCloseable(contextualData)) {
			debug(format, arguments);
		}
	}

	@Override
	public void info(final String format, final Object... arguments) {
		log.info(format, arguments);
	}

	@Override
	public void info(final String format, final Map<String, String> contextualData, final Object... arguments) {
		try (var ignored = buildMdcCloseable(contextualData)) {
			info(format, arguments);
		}
	}

	@Override
	public void warn(final String format, final Object... arguments) {
		log.warn(format, arguments);
	}

	@Override
	public void warn(final String format, final Map<String, String> contextualData, final Object... arguments) {
		try (var ignored = buildMdcCloseable(contextualData)) {
			warn(format, arguments);
		}
	}

	@Override
	public void error(final String format, final Object... arguments) {
		log.error(format, arguments);
	}

	@Override
	public void error(final String format, final Map<String, String> contextualData, final Object... arguments) {
		try (var ignored = buildMdcCloseable(contextualData)) {
			error(format, arguments);
		}
	}

	/**
	 * Populates Mapped Diagnostic Context ({@link MDC}) with provided data and returns a list of {@link MDCCloseable}
	 * wrapped into a {@link MDCCloseables} instance.
	 *
	 * @param contextualData The data to store into the log context.
	 * @return A {@link MDCCloseables} instance.
	 */
	private MDCCloseables buildMdcCloseable(final Map<String, String> contextualData) {
		return new MDCCloseables(
			contextualData.entrySet().stream()
				.map(entry -> MDC.putCloseable(entry.getKey(), entry.getValue()))
				.collect(Collectors.toList())
		);
	}

	private static class Slf4jAdapterInstanceHolder {
		private static final Slf4jAdapter INSTANCE = new Slf4jAdapter();
	}

	/**
	 * {@link Closeable} class wrapping a list of {@link MDCCloseable} instances.
	 */
	private static class MDCCloseables implements Closeable {
		private final List<MDCCloseable> mdcCloseables = new ArrayList<>();

		MDCCloseables(final List<MDCCloseable> mdcCloseables) {
			this.mdcCloseables.addAll(mdcCloseables);
		}

		@Override
		public void close() {
			mdcCloseables.forEach(MDCCloseable::close);
		}
	}

}
