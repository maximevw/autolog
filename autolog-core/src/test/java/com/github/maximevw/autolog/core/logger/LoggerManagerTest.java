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

import com.github.maximevw.autolog.core.logger.adapters.JavaLoggerAdapter;
import com.github.maximevw.autolog.core.logger.adapters.Log4j2Adapter;
import com.github.maximevw.autolog.core.logger.adapters.Log4jAdapter;
import com.github.maximevw.autolog.core.logger.adapters.LogbackWithLogstashAdapter;
import com.github.maximevw.autolog.core.logger.adapters.Slf4jAdapter;
import com.github.maximevw.autolog.core.logger.adapters.SystemOutAdapter;
import com.github.maximevw.autolog.core.logger.adapters.XSlf4jAdapter;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the class {@link LoggerManager}.
 */
@SuppressWarnings("deprecation")
class LoggerManagerTest {

	private static ByteArrayOutputStream stdOut = new ByteArrayOutputStream();
	private static SystemOutAdapter sysOutAdapter;
	private static Slf4jAdapter slf4jAdapter;
	private static XSlf4jAdapter xslf4jAdapter;
	private static Log4jAdapter log4jAdapter;
	private static Log4j2Adapter log4j2Adapter;
	private static JavaLoggerAdapter javaLogAdapter;
	private static LogbackWithLogstashAdapter logbackWithLogstashAdapter;

	private LoggerManager sut;

	/**
	 * Initializes the context for all the tests in this class.
	 */
	@BeforeAll
	static void init() {
		// Redirect standard system output to check Autolog warnings/errors.
		final PrintStream outStream = new PrintStream(stdOut);
		System.setOut(outStream);
	}

	/**
	 * Initializes the context for each new test case.
	 * <p>
	 *     It resets the subject under test ({@link LoggerManager}) and the the loggers.
	 * </p>
	 */
	@BeforeEach
	void reset() {
		sut = new LoggerManager();

		sysOutAdapter = spy(SystemOutAdapter.getInstance());
		slf4jAdapter = spy(Slf4jAdapter.getInstance());
		xslf4jAdapter = spy(XSlf4jAdapter.getInstance());
		log4jAdapter = spy(Log4jAdapter.getInstance());
		log4j2Adapter = spy(Log4j2Adapter.getInstance());
		javaLogAdapter = spy(JavaLoggerAdapter.getInstance());
		logbackWithLogstashAdapter = spy(LogbackWithLogstashAdapter.getInstance());
	}

	/**
	 * Verifies the effective registering of a {@link LoggerInterface} in the {@link LoggerManager}.
	 */
	@Test
	void givenLoggerInterface_whenRegister_addsToRegisteredLoggers() {
		assertThat(sut.getRegisteredLoggers(), is(empty()));
		sut.register(mock(LoggerInterface.class));
		assertThat(sut.getRegisteredLoggers().size(), is(1));
	}

	/**
	 * Verifies that registering of a {@link LoggerInterface} already registered in the {@link LoggerManager} is not
	 * registered again and generates a warning.
	 */
	@Test
	void givenAlreadyRegisteredLoggerInterface_whenRegister_ignoresItAndWarns() {
		assertThat(sut.getRegisteredLoggers(), is(empty()));
		final LoggerInterface loggerInterface = mock(LoggerInterface.class);
		// For test purpose, register twice the same logger interface.
		sut.register(loggerInterface);
		sut.register(loggerInterface);
		assertThat(stdOut.toString(), containsString(
			String.format("[WARN] Autolog: Logger of type %s already registered.",
				loggerInterface.getClass().getSimpleName())
		));
		assertThat(sut.getRegisteredLoggers().size(), is(1));
	}

	/**
	 * Verifies the effective unregistering of a specific {@link LoggerInterface} from the {@link LoggerManager}.
	 */
	@Test
	void givenLoggerInterfaceClass_whenUnregister_removesFromRegisteredLoggers() {
		sut.register(Slf4jAdapter.getInstance());
		sut.register(SystemOutAdapter.getInstance());
		assertThat(sut.getRegisteredLoggers().size(), is(2));
		sut.unregister(Slf4jAdapter.class);
		assertThat(sut.getRegisteredLoggers().size(), is(1));
	}

	/**
	 * Registers all the available loggers in the {@link LoggerManager} then logs a message with a specified level.
	 *
	 * @param level The log level to use.
	 * @return The set of registered loggers.
	 */
	private Set<LoggerInterface> prepareLogWithLevelTest(final LogLevel level) {
		final Set<LoggerInterface> loggers = Set.of(sysOutAdapter, slf4jAdapter, xslf4jAdapter, log4jAdapter,
			log4j2Adapter, javaLogAdapter, logbackWithLogstashAdapter);
		loggers.forEach(loggerInterface -> sut.register(loggerInterface));
		sut.logWithLevel(level, "This is a test: {}.", level.name());
		return loggers;
	}

	/**
	 * Registers all the available loggers able to manage a log context in the {@link LoggerManager} then logs a
	 * message with a specified level and contextual data.
	 *
	 * @param level The log level to use.
	 * @return The set of registered loggers.
	 */
	private Set<LoggerInterface> prepareLogWithLevelAndContextualDataTest(final LogLevel level) {
		final Set<LoggerInterface> loggers = Set.of(slf4jAdapter, log4j2Adapter, logbackWithLogstashAdapter);
		loggers.forEach(loggerInterface -> sut.register(loggerInterface));
		sut.logWithLevel(level, "This is a test: {}.", Map.of("testContext", level.name()), level.name());
		return loggers;
	}

	/**
	 * Verifies that the message logged by {@link #prepareLogWithLevelTest(LogLevel)} at level TRACE is effectively
	 * logged by the given {@link LoggerInterface}, i.e. the method {@link LoggerInterface#trace(String, Object...)} is
	 * invoked.
	 *
	 * @param loggerInterface The logger interface to check.
	 */
	private void verifyLogTraceCalled(final LoggerInterface loggerInterface) {
		verify(loggerInterface, only()).trace(eq("This is a test: {}."), eq(LogLevel.TRACE.name()));
	}

	/**
	 * Verifies that the message logged by {@link #prepareLogWithLevelTest(LogLevel)} at level TRACE is effectively
	 * logged by each logger registered in the {@link LoggerManager}.
	 */
	@Test
	void givenLoggerManagerWithAdapters_whenLogTrace_callsLogMethodInEachAdapter() {
		final Set<LoggerInterface> loggers = prepareLogWithLevelTest(LogLevel.TRACE);
		loggers.forEach(this::verifyLogTraceCalled);
	}

	/**
	 * Verifies that the message logged by {@link #prepareLogWithLevelAndContextualDataTest(LogLevel)} at level TRACE is
	 * effectively logged by the given {@link LoggerInterface}, i.e. the method
	 * {@link LoggerInterface#trace(String, Map, Object...)} is invoked.
	 *
	 * @param loggerInterface The logger interface to check.
	 */
	private void verifyLogTraceWithContextualDataCalled(final LoggerInterface loggerInterface) {
		verify(loggerInterface, times(1)).trace(eq("This is a test: {}."),
			argThat(argument -> argument.containsKey("testContext")
				&& argument.get("testContext").equals(LogLevel.TRACE.name())), eq(LogLevel.TRACE.name()));
	}

	/**
	 * Verifies that the message logged by {@link #prepareLogWithLevelAndContextualDataTest(LogLevel)} at level TRACE is
	 * effectively logged by each logger registered in the {@link LoggerManager}.
	 */
	@Test
	void givenLoggerManagerWithAdapters_whenLogTraceWithContextualData_callsLogMethodInEachAdapter() {
		final Set<LoggerInterface> loggers = prepareLogWithLevelAndContextualDataTest(LogLevel.TRACE);
		loggers.forEach(this::verifyLogTraceWithContextualDataCalled);
	}

	/**
	 * Verifies that the message logged by {@link #prepareLogWithLevelTest(LogLevel)} at level DEBUG is effectively
	 * logged by the given {@link LoggerInterface}, i.e. the method {@link LoggerInterface#debug(String, Object...)} is
	 * invoked.
	 *
	 * @param loggerInterface The logger interface to check.
	 */
	private void verifyLogDebugCalled(final LoggerInterface loggerInterface) {
		verify(loggerInterface, only()).debug(eq("This is a test: {}."), eq(LogLevel.DEBUG.name()));
	}

	/**
	 * Verifies that the message logged by {@link #prepareLogWithLevelTest(LogLevel)} at level DEBUG is effectively
	 * logged by each logger registered in the {@link LoggerManager}.
	 */
	@Test
	void givenLoggerManagerWithAdapters_whenLogDebug_callsLogMethodInEachAdapter() {
		final Set<LoggerInterface> loggers = prepareLogWithLevelTest(LogLevel.DEBUG);
		loggers.forEach(this::verifyLogDebugCalled);
	}

	/**
	 * Verifies that the message logged by {@link #prepareLogWithLevelAndContextualDataTest(LogLevel)} at level DEBUG is
	 * effectively logged by the given {@link LoggerInterface}, i.e. the method
	 * {@link LoggerInterface#debug(String, Map, Object...)} is invoked.
	 *
	 * @param loggerInterface The logger interface to check.
	 */
	private void verifyLogDebugWithContextualDataCalled(final LoggerInterface loggerInterface) {
		verify(loggerInterface, times(1)).debug(eq("This is a test: {}."),
			argThat(argument -> argument.containsKey("testContext")
				&& argument.get("testContext").equals(LogLevel.DEBUG.name())), eq(LogLevel.DEBUG.name()));
	}

	/**
	 * Verifies that the message logged by {@link #prepareLogWithLevelAndContextualDataTest(LogLevel)} at level DEBUG is
	 * effectively logged by each logger registered in the {@link LoggerManager}.
	 */
	@Test
	void givenLoggerManagerWithAdapters_whenLogDebugWithContextualData_callsLogMethodInEachAdapter() {
		final Set<LoggerInterface> loggers = prepareLogWithLevelAndContextualDataTest(LogLevel.DEBUG);
		loggers.forEach(this::verifyLogDebugWithContextualDataCalled);
	}

	/**
	 * Verifies that the message logged by {@link #prepareLogWithLevelTest(LogLevel)} at level INFO is effectively
	 * logged by the given {@link LoggerInterface}, i.e. the method {@link LoggerInterface#info(String, Object...)} is
	 * invoked.
	 *
	 * @param loggerInterface The logger interface to check.
	 */
	private void verifyLogInfoCalled(final LoggerInterface loggerInterface) {
		verify(loggerInterface, only()).info(eq("This is a test: {}."), eq(LogLevel.INFO.name()));
	}

	/**
	 * Verifies that the message logged by {@link #prepareLogWithLevelTest(LogLevel)} at level INFO is effectively
	 * logged by each logger registered in the {@link LoggerManager}.
	 */
	@Test
	void givenLoggerManagerWithAdapters_whenLogInfo_callsLogMethodInEachAdapter() {
		final Set<LoggerInterface> loggers = prepareLogWithLevelTest(LogLevel.INFO);
		loggers.forEach(this::verifyLogInfoCalled);
	}

	/**
	 * Verifies that the message logged by {@link #prepareLogWithLevelAndContextualDataTest(LogLevel)} at level INFO is
	 * effectively logged by the given {@link LoggerInterface}, i.e. the method
	 * {@link LoggerInterface#info(String, Map, Object...)} is invoked.
	 *
	 * @param loggerInterface The logger interface to check.
	 */
	private void verifyLogInfoWithContextualDataCalled(final LoggerInterface loggerInterface) {
		verify(loggerInterface, times(1)).info(eq("This is a test: {}."),
			argThat(argument -> argument.containsKey("testContext")
				&& argument.get("testContext").equals(LogLevel.INFO.name())), eq(LogLevel.INFO.name()));
	}

	/**
	 * Verifies that the message logged by {@link #prepareLogWithLevelAndContextualDataTest(LogLevel)} at level INFO is
	 * effectively logged by each logger registered in the {@link LoggerManager}.
	 */
	@Test
	void givenLoggerManagerWithAdapters_whenLogInfoWithContextualData_callsLogMethodInEachAdapter() {
		final Set<LoggerInterface> loggers = prepareLogWithLevelAndContextualDataTest(LogLevel.INFO);
		loggers.forEach(this::verifyLogInfoWithContextualDataCalled);
	}

	/**
	 * Verifies that the message logged by {@link #prepareLogWithLevelTest(LogLevel)} at level WARN is effectively
	 * logged by the given {@link LoggerInterface}, i.e. the method {@link LoggerInterface#warn(String, Object...)} is
	 * invoked.
	 *
	 * @param loggerInterface The logger interface to check.
	 */
	private void verifyLogWarnCalled(final LoggerInterface loggerInterface) {
		verify(loggerInterface, only()).warn(eq("This is a test: {}."), eq(LogLevel.WARN.name()));
	}

	/**
	 * Verifies that the message logged by {@link #prepareLogWithLevelTest(LogLevel)} at level WARN is effectively
	 * logged by each logger registered in the {@link LoggerManager}.
	 */
	@Test
	void givenLoggerManagerWithAdapters_whenLogWarn_callsLogMethodInEachAdapter() {
		final Set<LoggerInterface> loggers = prepareLogWithLevelTest(LogLevel.WARN);
		loggers.forEach(this::verifyLogWarnCalled);
	}

	/**
	 * Verifies that the message logged by {@link #prepareLogWithLevelAndContextualDataTest(LogLevel)} at level WARN is
	 * effectively logged by the given {@link LoggerInterface}, i.e. the method
	 * {@link LoggerInterface#warn(String, Map, Object...)} is invoked.
	 *
	 * @param loggerInterface The logger interface to check.
	 */
	private void verifyLogWarnWithContextualDataCalled(final LoggerInterface loggerInterface) {
		verify(loggerInterface, times(1)).warn(eq("This is a test: {}."),
			argThat(argument -> argument.containsKey("testContext")
				&& argument.get("testContext").equals(LogLevel.WARN.name())), eq(LogLevel.WARN.name()));
	}

	/**
	 * Verifies that the message logged by {@link #prepareLogWithLevelAndContextualDataTest(LogLevel)} at level WARN is
	 * effectively logged by each logger registered in the {@link LoggerManager}.
	 */
	@Test
	void givenLoggerManagerWithAdapters_whenLogWarnWithContextualData_callsLogMethodInEachAdapter() {
		final Set<LoggerInterface> loggers = prepareLogWithLevelAndContextualDataTest(LogLevel.WARN);
		loggers.forEach(this::verifyLogWarnWithContextualDataCalled);
	}

	/**
	 * Verifies that the message logged by {@link #prepareLogWithLevelTest(LogLevel)} at level ERROR is effectively
	 * logged by the given {@link LoggerInterface}, i.e. the method {@link LoggerInterface#error(String, Object...)} is
	 * invoked.
	 *
	 * @param loggerInterface The logger interface to check.
	 */
	private void verifyLogErrorCalled(final LoggerInterface loggerInterface) {
		verify(loggerInterface, only()).error(eq("This is a test: {}."), eq(LogLevel.ERROR.name()));
	}

	/**
	 * Verifies that the message logged by {@link #prepareLogWithLevelTest(LogLevel)} at level ERROR is effectively
	 * logged by each logger registered in the {@link LoggerManager}.
	 */
	@Test
	void givenLoggerManagerWithAdapters_whenLogError_callsLogMethodInEachAdapter() {
		final Set<LoggerInterface> loggers = prepareLogWithLevelTest(LogLevel.ERROR);
		loggers.forEach(this::verifyLogErrorCalled);
	}

	/**
	 * Verifies that the message logged by {@link #prepareLogWithLevelAndContextualDataTest(LogLevel)} at level ERROR is
	 * effectively logged by the given {@link LoggerInterface}, i.e. the method
	 * {@link LoggerInterface#error(String, Map, Object...)} is invoked.
	 *
	 * @param loggerInterface The logger interface to check.
	 */
	private void verifyLogErrorWithContextualDataCalled(final LoggerInterface loggerInterface) {
		verify(loggerInterface, times(1)).error(eq("This is a test: {}."),
			argThat(argument -> argument.containsKey("testContext")
				&& argument.get("testContext").equals(LogLevel.ERROR.name())), eq(LogLevel.ERROR.name()));
	}

	/**
	 * Verifies that the message logged by {@link #prepareLogWithLevelAndContextualDataTest(LogLevel)} at level ERROR is
	 * effectively logged by each logger registered in the {@link LoggerManager}.
	 */
	@Test
	void givenLoggerManagerWithAdapters_whenLogErrorWithContextualData_callsLogMethodInEachAdapter() {
		final Set<LoggerInterface> loggers = prepareLogWithLevelAndContextualDataTest(LogLevel.ERROR);
		loggers.forEach(this::verifyLogErrorWithContextualDataCalled);
	}

	/**
	 * Verifies that logging a message and a {@link Throwable} at level ERROR is effectively logged each loggers
	 * registered in the {@link LoggerManager}, i.e. the method {@link LoggerInterface#error(String, Object...)} is
	 * invoked.
	 */
	@Test
	void givenLoggerManagerWithAdapters_whenLogThrowable_callsLogMethodInEachAdapter() {
		final Set<LoggerInterface> loggers = Set.of(sysOutAdapter, slf4jAdapter, xslf4jAdapter, log4jAdapter,
			log4j2Adapter, javaLogAdapter, logbackWithLogstashAdapter);
		loggers.forEach(loggerInterface -> sut.register(loggerInterface));
		final Throwable throwable = spy(new Throwable("Something went wrong."));
		sut.logWithLevel(LogLevel.ERROR, "This is a test.", throwable);
		loggers.forEach(loggerInterface -> verify(loggerInterface, only()).error(eq("This is a test."), eq(throwable)));
		// Check that the stack trace is printed by SystemOutAdapter.
		verify(throwable, atLeastOnce()).printStackTrace();
	}

	/**
	 * Verifies that logging a message and a {@link Throwable} with contextual data at level ERROR is effectively
	 * logged each loggers able to manage a log context registered in the {@link LoggerManager}, i.e. the method
	 * {@link LoggerInterface#error(String, Map, Object...)} is invoked.
	 */
	@Test
	void givenLoggerManagerWithAdapters_whenLogThrowableWithContextualData_callsLogMethodInEachAdapter() {
		final Set<LoggerInterface> loggers = Set.of(slf4jAdapter, log4j2Adapter, logbackWithLogstashAdapter);
		loggers.forEach(loggerInterface -> sut.register(loggerInterface));
		final Throwable throwable = spy(new Throwable("Something went wrong."));
		sut.logWithLevel(LogLevel.ERROR, "This is a test.", Map.of("testContext", LogLevel.ERROR.name()), throwable);
		loggers.forEach(loggerInterface -> verify(loggerInterface, times(1))
			.error(eq("This is a test."), argThat(argument -> argument.containsKey("testContext")
				&& argument.get("testContext").equals(LogLevel.ERROR.name())), eq(throwable)));
	}
}
