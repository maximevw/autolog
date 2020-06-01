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

import com.github.maximevw.autolog.core.configuration.LoggerInterfaceConfiguration;
import com.github.maximevw.autolog.core.configuration.adapters.JdbcAdapterConfiguration;
import com.github.maximevw.autolog.core.logger.LogLevel;
import com.github.maximevw.autolog.core.logger.LoggingUtils;
import net.logstash.logback.encoder.org.apache.commons.lang3.StringUtils;
import org.h2.jdbcx.JdbcDataSource;
import org.h2.tools.RunScript;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the class {@link JdbcAdapter}.
 */
class JdbcAdapterTest {

	private static final ByteArrayOutputStream STD_OUT = new ByteArrayOutputStream();
	private static final ByteArrayOutputStream STD_ERR = new ByteArrayOutputStream();

	private static final String CUSTOM_LOG_CATEGORY = "customCategory";

	/**
	 * Initializes the context for all the tests in this class.
	 */
	@BeforeAll
	static void init() {
		// Redirect standard system output and standard error output.
		final PrintStream outStream = new PrintStream(STD_OUT);
		final PrintStream errStream = new PrintStream(STD_ERR);
		System.setOut(outStream);
		System.setErr(errStream);
	}

	/**
	 * Verifies that an invalid configuration throws an {@link IllegalArgumentException}.
	 *
	 * @see JdbcAdapter#configure(LoggerInterfaceConfiguration)
	 */
	@Test
	void givenInvalidConfiguration_whenConfigureAdapter_throwsException() {
		final JdbcAdapter sut = JdbcAdapter.getInstance(mock(JdbcAdapterConfiguration.class));
		final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
			() -> sut.configure(mock(LoggerInterfaceConfiguration.class)));
		assertEquals("JdbcAdapter configuration is invalid.", illegalArgumentException.getMessage());
	}

	/**
	 * Verifies that a warning message is logged and nothing is persisted when the data source is {@code null}.
	 */
	@Test
	void givenNullDataSource_whenLogEvent_logsWarningAndPersistsNothing() {
		final JdbcAdapter sut = JdbcAdapter.getInstance(mock(JdbcAdapterConfiguration.class));
		sut.debug(LoggingUtils.AUTOLOG_DEFAULT_TOPIC, "Test logging a message without data source.");
		assertThat(STD_OUT.toString(),
			containsString("[WARN] Autolog: No data source configured: unable to persist log event."));
	}

	/**
	 * Verifies that an error is logged if the persistence of a log event throws an {@link SQLException}.
	 *
	 * @throws SQLException thrown by design for this test.
	 */
	@Test
	void givenInvalidConfiguration_whenConfigureAdapter_logsError() throws SQLException {
		final DataSource mockedDataSource = mock(DataSource.class);
		when(mockedDataSource.getConnection()).thenThrow(mock(SQLException.class));
		final JdbcAdapter sut = JdbcAdapter.getInstance(
			JdbcAdapterConfiguration.builder()
				.dataSource(mockedDataSource)
				.build()
		);

		sut.debug(LoggingUtils.AUTOLOG_DEFAULT_TOPIC, "Test logging a message without connection.");
		assertThat(STD_ERR.toString(),
			containsString("[ERROR] Autolog: Unable to persist log event in the specified database."));
	}

	/**
	 * Verifies that any log event is persisted in database.
	 *
	 * @param tablePrefix The table prefix to use for the test.
	 * @param logLevel	  The log level to use for the test.
	 * @throws SQLException if something went wrong during the initialization of the H2 in-memory database or during
	 * 						the execution of the tested code.
	 */
	@ParameterizedTest
	@MethodSource("provideJdbcAdapterTestConfigurations")
	void givenValidConfiguration_whenLogEvent_persistsLogEventInDatabase(final String tablePrefix,
																		 final LogLevel logLevel) throws SQLException {
		final JdbcDataSource h2DataSource = new JdbcDataSource();
		h2DataSource.setURL("jdbc:h2:mem:test");
		h2DataSource.setUser("sa");
		final InputStream initScriptStream = this.getClass().getClassLoader().getResourceAsStream("init-h2.sql");
		if (initScriptStream != null) {
			RunScript.execute(h2DataSource.getConnection(), new InputStreamReader(initScriptStream));
			final JdbcAdapter sut = JdbcAdapter.getInstance(
				JdbcAdapterConfiguration.builder()
					.dataSource(h2DataSource)
					.tablePrefix(tablePrefix)
					.build()
			);

			log(sut, logLevel);

			try (Connection connection = h2DataSource.getConnection()) {
				String tableName = "LOG_EVENTS";
				if (StringUtils.isNotBlank(tablePrefix)) {
					tableName = tablePrefix + "_" + tableName;
				}

				// Verify the log event has been inserted.
				final ResultSet rsCount = connection.createStatement()
					.executeQuery("SELECT COUNT(*) FROM " + tableName);
				if (rsCount.next()) {
					assertEquals(1, rsCount.getInt(1));
				}
				rsCount.close();

				// Verify the correctness of persisted values.
				final ResultSet rsLogEvents = connection.createStatement()
					.executeQuery("SELECT TOPIC, LOG_LEVEL, MESSAGE FROM " + tableName);
				while (rsLogEvents.next()) {
					assertEquals(CUSTOM_LOG_CATEGORY, rsLogEvents.getString(1));
					assertEquals(logLevel.name(), rsLogEvents.getString(2));
					assertEquals("This is a test: " + logLevel.name(), rsLogEvents.getString(3));
				}
				rsLogEvents.close();
			}
		} else {
			fail("Unable to instantiate H2 in-memory database: missing script 'init-h2.sql'.");
		}
	}

	/**
	 * Builds arguments for the parameterized tests relative to the logging by the class {@link JdbcAdapter}:
	 * {@link #givenValidConfiguration_whenLogEvent_persistsLogEventInDatabase(String, LogLevel)}.
	 *
	 * @return The arguments to execute the different test cases.
	 */
	private static Stream<Arguments> provideJdbcAdapterTestConfigurations() {
		return Stream.of(
			Arguments.of(StringUtils.EMPTY, LogLevel.TRACE),
			Arguments.of(StringUtils.EMPTY, LogLevel.DEBUG),
			Arguments.of(StringUtils.EMPTY, LogLevel.INFO),
			Arguments.of("TEST", LogLevel.WARN),
			Arguments.of(null, LogLevel.ERROR)
		);
	}

	private static void log(final JdbcAdapter sut, final LogLevel logLevel) {
		// For each log level, insert an entry using a custom log category.
		switch (logLevel) {
			case TRACE:
				sut.trace(CUSTOM_LOG_CATEGORY, "This is a test: {}", logLevel);
				break;
			case DEBUG:
				sut.debug(CUSTOM_LOG_CATEGORY, "This is a test: {}", logLevel);
				break;
			case INFO:
				sut.info(CUSTOM_LOG_CATEGORY, "This is a test: {}", logLevel);
				break;
			case WARN:
				sut.warn(CUSTOM_LOG_CATEGORY, "This is a test: {}", logLevel);
				break;
			case ERROR:
				sut.error(CUSTOM_LOG_CATEGORY, "This is a test: {}", logLevel);
				break;
			default:
				fail("Unknown level of log: " + logLevel);
		}
	}
}
