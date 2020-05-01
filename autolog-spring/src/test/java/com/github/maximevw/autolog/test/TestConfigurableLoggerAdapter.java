/*-
 * #%L
 * Autolog Spring integration module
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

package com.github.maximevw.autolog.test;

import com.github.maximevw.autolog.core.configuration.LoggerInterfaceConfiguration;
import com.github.maximevw.autolog.core.logger.ConfigurableLoggerInterface;

/**
 * This class is a valid implementation of {@link ConfigurableLoggerInterface} used in unit tests of Autolog Spring
 * auto-configuration.
 */
public class TestConfigurableLoggerAdapter implements ConfigurableLoggerInterface {

	/**
	 * Gets a configured instance of {@code TestConfigurableLoggerAdapter}.
	 *
	 * @param configuration The configuration.
	 * @return A configured instance of {@code TestConfigurableLoggerAdapter}.
	 */
	public static TestConfigurableLoggerAdapter getInstance(final LoggerInterfaceConfiguration configuration) {
		final TestConfigurableLoggerAdapter loggerAdapter = new TestConfigurableLoggerAdapter();
		loggerAdapter.configure(configuration);
		return loggerAdapter;
	}

	@Override
	public void configure(final LoggerInterfaceConfiguration configuration) {
		// Do nothing: for test purpose only.
	}

	@Override
	public void trace(final String format, final Object... arguments) {
		// Do nothing: for test purpose only.
	}

	@Override
	public void debug(final String format, final Object... arguments) {
		// Do nothing: for test purpose only.
	}

	@Override
	public void info(final String format, final Object... arguments) {
		// Do nothing: for test purpose only.
	}

	@Override
	public void warn(final String format, final Object... arguments) {
		// Do nothing: for test purpose only.
	}

	@Override
	public void error(final String format, final Object... arguments) {
		// Do nothing: for test purpose only.
	}
}
