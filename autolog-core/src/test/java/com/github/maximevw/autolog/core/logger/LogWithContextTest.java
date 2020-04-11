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

package com.github.maximevw.autolog.core.logger;

import com.github.maximevw.autolog.core.logger.adapters.Log4j2Adapter;
import com.github.maximevw.autolog.core.logger.adapters.LogbackWithLogstashAdapter;
import net.logstash.logback.marker.ObjectAppendingMarker;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Appender;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.org.lidalia.slf4jtest.LoggingEvent;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.AdditionalAnswers.answerVoid;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

/**
 * Unit tests for the logging of contextual data by specific implementations through the method
 * {@link LoggerManager#logWithLevel(LogLevel, String, Map, Object...)}.
 */
@ExtendWith(MockitoExtension.class)
class LogWithContextTest {

	private final TestLogger slf4jLogger = TestLoggerFactory.getTestLogger("Autolog");
	private final Logger log4j2Logger = (Logger) LogManager.getLogger("Autolog");
	@Mock
	private Appender mockAppender;

	/**
	 * Builds arguments for the parameterized tests requiring a log level.
	 *
	 * @return The available log levels for the parameterized tests.
	 */
	private static Stream<Arguments> provideLogLevels() {
		return Arrays.stream(LogLevel.values())
			.map(Arguments::of)
			.collect(Collectors.toSet())
			.stream();
	}

	/**
	 * Logs a message with some contextual data.
	 *
	 * @param loggerManager The logger manager to use.
	 * @param logLevel		The log level to use.
	 */
	private static void logWithContextualData(final LoggerManager loggerManager, final LogLevel logLevel) {
		loggerManager.logWithLevel(logLevel, "This is a test log: {}.", Map.of(
			"testKey1", "testValue1",
			"testKey2", "testValue2"
		), logLevel.name());
	}

	/**
	 * Verifies the effective logging of contextual data in Log4j2.
	 *
	 * @param logLevel The log level to use for the test.
	 */
	@ParameterizedTest
	@MethodSource("provideLogLevels")
	void givenLoggerManagerWithLog4j2_whenLogWithContextualData_generatesLogWithContext(final LogLevel logLevel) {
		// Prepare mock of Log4j2 appender.
		// See https://codingcraftsman.wordpress.com/2015/04/28/log4j2-mocking-with-mockito-and-junit/ for more
		// details.
		when(mockAppender.getName()).thenReturn("testAppender");
		when(mockAppender.isStarted()).thenReturn(true);

		final List<LogEvent> capturedEvents = new ArrayList<>();
		// When append method is called, convert the log event to Immutable and add it to the list of captured events.
		doAnswer(answerVoid((LogEvent event) -> capturedEvents.add(event.toImmutable())))
			.when(mockAppender).append(any());

		log4j2Logger.addAppender(mockAppender);
		log4j2Logger.setLevel(org.apache.logging.log4j.Level.ALL);

		final LoggerManager sut = new LoggerManager().register(Log4j2Adapter.getInstance());
		logWithContextualData(sut, logLevel);

		assertThat(capturedEvents.size(), is(1));
		final LogEvent capturedLogEvent = capturedEvents.get(0);
		assertThat(capturedLogEvent.getMessage().getFormattedMessage(),
			is(String.format("This is a test log: %s.", logLevel.name())));
		// Retrieve the log context and check its content.
		final Map<String, String> logContextData = capturedLogEvent.getContextData().toMap();
		assertThat(logContextData, allOf(List.of(
			hasEntry("testKey1", "testValue1"),
			hasEntry("testKey2", "testValue2")
		)));

		// Remove the log appender.
		log4j2Logger.removeAppender(mockAppender);
	}

	/**
	 * Verifies the effective logging of contextual data in Logback with Logstash layout.
	 *
	 * @param logLevel The log level to use for the test.
	 */
	@ParameterizedTest
	@MethodSource("provideLogLevels")
	void givenLoggerManagerWithLogbackUsingLogstash_whenLogWithContextualData_generatesLogWithContext(
		final LogLevel logLevel) {

		final LoggerManager sut = new LoggerManager().register(LogbackWithLogstashAdapter.getInstance());
		logWithContextualData(sut, logLevel);

		assertThat(slf4jLogger.getLoggingEvents().size(), is(1));
		final LoggingEvent capturedLogEvent = slf4jLogger.getLoggingEvents().get(0);
		assertThat(capturedLogEvent.getMessage(), is("This is a test log: {}."));
		// Retrieve the log context and check its content.
		// In this specific case, we should have 3 arguments:
		// - The first one is the standard message argument.
		// - The next ones are the StructuredArguments (appearing here as ObjectAppendingMarker objects).
		assertNotNull(capturedLogEvent.getArguments());
		assertThat(capturedLogEvent.getArguments().size(), is(3));
		assertThat(capturedLogEvent.getArguments(), allOf(
			hasItem(is(logLevel.name())),
			hasItem(allOf(isA(ObjectAppendingMarker.class),
				hasProperty("fieldName", is("testKey1")),
				hasProperty("fieldValue", is("testValue1")))),
			hasItem(allOf(isA(ObjectAppendingMarker.class),
				hasProperty("fieldName", is("testKey2")),
				hasProperty("fieldValue", is("testValue2"))))
		));

		// Clear the logging events.
		slf4jLogger.clear();
	}

}
