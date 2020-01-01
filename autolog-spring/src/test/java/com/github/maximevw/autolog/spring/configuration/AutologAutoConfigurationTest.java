/*-
 * #%L
 * Autolog Spring integration module
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

package com.github.maximevw.autolog.spring.configuration;

import com.github.maximevw.autolog.core.logger.LoggerManager;
import com.github.maximevw.autolog.core.logger.adapters.Log4jAdapter;
import com.github.maximevw.autolog.core.logger.adapters.Slf4jAdapter;
import com.github.maximevw.autolog.core.logger.adapters.SystemOutAdapter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * Unit tests for the Spring auto-configuration class {@link AutologAutoConfiguration}.
 */
class AutologAutoConfigurationTest {

	private static ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
	private static ByteArrayOutputStream stdErr = new ByteArrayOutputStream();

	private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
		.withBean(LoggerInterfaceConverter.class)
		.withConfiguration(AutoConfigurations.of(AutologAutoConfiguration.class));

	/**
	 * Initializes the context for all the tests in this class.
	 */
	@BeforeAll
	static void init() {
		// Redirect standard system output and standard error output.
		final PrintStream outStream = new PrintStream(stdOut);
		final PrintStream errStream = new PrintStream(stdErr);
		System.setOut(outStream);
		System.setErr(errStream);
	}

	/**
	 * Verifies that the default logger ({@link SystemOutAdapter}) is registered by default in the {@link LoggerManager}
	 * when the Autolog auto-configuration is performed without specific configuration.
	 */
	@Test
	public void givenNoSpecificConfiguration_whenAutoconfigureLoggerManager_registersDefaultLogger() {
		this.contextRunner.run(context -> {
			final LoggerManager loggerManager = context.getBean(LoggerManager.class);
			assertNotNull(loggerManager);
			assertThat(loggerManager.getRegisteredLoggers().size(), is(1));
			assertThat(loggerManager.getRegisteredLoggers(), hasItem(instanceOf(SystemOutAdapter.class)));
		});
	}

	/**
	 * Verifies that the valid configured loggers are registered in the {@link LoggerManager} and the invalid ones are
	 * ignored when the Autolog auto-configuration is performed.
	 */
	@Test
	public void givenConfiguredLoggers_whenAutoconfigureLoggerManager_registersExpectedLoggers() {
		this.contextRunner
			/*
			 * Define 2 valid loggers:
			 * - (Case V1) Slf4jAdapter without fully qualified name
			 * - (Case V2) Log4jAdapter with fully qualified name
			 * and 4 invalid loggers:
			 * - (Case I1) an empty class name
			 * - (Case I2) a not-existing class (com.github.maximevw.FakeLoggerClass)
			 * - (Case I3) a valid class name not implementing LoggerInterface (java.lang.Object)
			 * - (Case I4) a valid class name implementing LoggerInterface but not providing a getInstance() method to
			 *   get a singleton instance (com.github.maximevw.autolog.test.TestInvalidLoggerAdapter)
			 */
			.withPropertyValues("autolog.loggers:Slf4jAdapter,"
				+ "com.github.maximevw.autolog.core.logger.adapters.Log4jAdapter,,java.lang.Object,"
				+ "com.github.maximevw.FakeLoggerClass,com.github.maximevw.autolog.test.TestInvalidLoggerAdapter")
			.run(context -> {
				final LoggerManager loggerManager = context.getBean(LoggerManager.class);
				assertNotNull(loggerManager);
				assertThat(loggerManager.getRegisteredLoggers().size(), is(2));
				// Check for Case V1.
				assertThat(loggerManager.getRegisteredLoggers(), hasItem(instanceOf(Slf4jAdapter.class)));
				// Check for Case V2.
				assertThat(loggerManager.getRegisteredLoggers(), hasItem(instanceOf(Log4jAdapter.class)));

				// Check logs for Case I1.
				assertThat(stdOut.toString(), containsString("[WARN] Autolog: Empty class name for logger interface, "
					+ "it will be skipped.")
				);

				// Check logs for Case I2.
				assertThat(stdErr.toString(), containsString("[ERROR] Autolog: Unable to find logger interface: "
					+ "com.github.maximevw.FakeLoggerClass"));

				// Check logs for Case I3.
				assertThat(stdOut.toString(), containsString("[WARN] Autolog: java.lang.Object is not a valid logger: "
					+ "it doesn't implement LoggerInterface.")
				);

				// Check logs for Case I4.
				assertThat(stdErr.toString(), containsString("[ERROR] Autolog: Unable to get an instance of logger "
					+ "interface: com.github.maximevw.autolog.test.TestInvalidLoggerAdapter"));
			});
	}
}
