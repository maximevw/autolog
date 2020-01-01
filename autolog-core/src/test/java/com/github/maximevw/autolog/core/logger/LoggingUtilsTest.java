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

package com.github.maximevw.autolog.core.logger;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the class {@link LoggingUtils}.
 */
@ExtendWith(MockitoExtension.class)
class LoggingUtilsTest {

	private static ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
	private static ByteArrayOutputStream stdErr = new ByteArrayOutputStream();

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
	 * Verifies that reporting a given message effectively logs it with level DEBUG in the standard output.
	 */
	@Test
	void givenMessage_whenReportDebugMessage_logsMessageInStandardOutput() {
		LoggingUtils.report("Test message.");
		assertThat(stdOut.toString(), containsString("[DEBUG] Autolog: Test message."));
	}

	/**
	 * Verifies that reporting a given message effectively logs it with the specified level in the standard output.
	 *
	 * @param level The log level to use.
	 */
	@ParameterizedTest
	@ValueSource(strings = {"TRACE", "DEBUG", "INFO", "WARN", "ERROR"})
	void givenMessageAndLevel_whenReportMessage_logsMessageInStandardOutput(final String level) {
		LoggingUtils.report("Test message.", LogLevel.valueOf(level));
		assertThat(stdOut.toString(), containsString(String.format("[%s] Autolog: Test message.", level)));
	}

	/**
	 * Verifies that reporting an error effectively logs it with level ERROR in the error output.
	 */
	@Test
	void givenMessageAndThrowable_whenReportError_logsMessageInErrorOutput() {
		final Throwable throwable = mock(Throwable.class);
		LoggingUtils.reportError("Test error message.", throwable);
		assertThat(stdErr.toString(), containsString("[ERROR] Autolog: Test error message."));
		verify(throwable, times(1)).printStackTrace();
	}

	/**
	 * Verifies that formatting a duration in milliseconds returns the expected formatted value.
	 *
	 * @param durationInMs		The duration (in milliseconds) to format.
	 * @param expectedResult	The expected formatted value.
	 */
	@ParameterizedTest
	@MethodSource("provideDurations")
	void givenDuration_whenFormatDuration_returnsExpectedValue(final long durationInMs, final String expectedResult) {
		final String result = LoggingUtils.formatDuration(durationInMs);
		assertThat(result, is(expectedResult));
	}

	/**
	 * Builds arguments for the parameterized test relative to the formatting of durations:
	 * {@link #givenDuration_whenFormatDuration_returnsExpectedValue(long, String)}.
	 *
	 * @return The duration in milliseconds and the corresponding formatted value for the parameterized tests.
	 */
	private static Stream<Arguments> provideDurations() {
		return Stream.of(
			Arguments.of(10, "10 ms"),
			Arguments.of(1010, "1 s 010 ms"),
			Arguments.of(65_010, "1 m 5 s 010 ms"),
			Arguments.of(3_665_010, "1 h 1 m 5 s 010 ms"),
			Arguments.of(87_005_010, "1 day(s) 0 h 10 m 5 s 010 ms")
		);
	}

}
