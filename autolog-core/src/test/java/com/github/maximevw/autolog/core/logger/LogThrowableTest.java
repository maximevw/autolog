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

import com.github.maximevw.autolog.core.configuration.MethodOutputLoggingConfiguration;
import com.github.maximevw.autolog.core.logger.adapters.Slf4jAdapter;
import com.github.maximevw.autolog.test.LogTestingClass;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import uk.org.lidalia.slf4jext.Level;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import java.lang.reflect.Method;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesRegex;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for the {@link Throwable} logging methods in the class {@link MethodCallLogger}.
 */
class LogThrowableTest {

    private static TestLogger logger;
	private static MethodCallLogger sut;

	/**
	 * Initializes the context for all the tests in this class.
	 */
	@BeforeAll
	static void init() {
		logger = TestLoggerFactory.getTestLogger("Autolog");
		sut = new MethodCallLogger(
			new LoggerManager().register(Slf4jAdapter.getInstance())
		);
	}

	/**
	 * Resets the logger before each new test case.
	 */
	@BeforeEach
	void reset() {
		logger.clear();
	}

	/**
	 * Verifies that logging a null {@link Throwable} throws a {@link NullPointerException}.
	 *
	 * @see MethodCallLogger#logThrowable(Throwable)
	 * @see MethodCallLogger#logThrowable(Throwable, Map)
	 * @see MethodCallLogger#logThrowable(Throwable, String, Map)
	 */
	@Test
    void givenNull_whenLogThrowable_throwsException() {
        assertThrows(NullPointerException.class, () -> sut.logThrowable(null));
		assertThrows(NullPointerException.class, () -> sut.logThrowable(null, null));
		assertThrows(NullPointerException.class, () -> sut.logThrowable(null, (String) null, null));
    }

	/**
	 * Verifies the effective logging of a {@link Throwable}.
	 *
	 * @see MethodCallLogger#logThrowable(Throwable)
	 */
	@Test
    void givenThrowable_whenLogThrowable_generatesLog() {
    	final Throwable throwable = new Throwable("Test throwable");
        sut.logThrowable(throwable);

        assertThat(logger.getLoggingEvents(), hasItem(allOf(
                hasProperty("message", is(MethodOutputLoggingConfiguration.THROWABLE_MESSAGE_TEMPLATE)),
                hasProperty("arguments", hasItem(Throwable.class.getName())),
                hasProperty("arguments", hasItem("Test throwable")),
                hasProperty("level", is(Level.ERROR))
        )));
    }

	/**
	 * Verifies that logging a {@link Throwable} with a null configuration throws a {@link NullPointerException}.
	 *
	 * @see MethodCallLogger#logThrowable(MethodOutputLoggingConfiguration, Method, Throwable)
	 */
	@Test
	@SuppressWarnings("ConstantConditions")
	void givenNullConfiguration_whenLogThrowable_throwsException() {
		assertThrows(NullPointerException.class, () -> sut.logThrowable(null, LogTestingClass.class.getMethod("noOp"),
			new Throwable()));
	}

	/**
	 * Verifies that logging a {@link Throwable} with a null method throws a {@link NullPointerException}.
	 *
	 * @see MethodCallLogger#logThrowable(MethodOutputLoggingConfiguration, Method, Throwable)
	 */
	@Test
	void givenNullMethod_whenLogThrowable_throwsException() {
		assertThrows(NullPointerException.class, () ->
			sut.logThrowable(MethodOutputLoggingConfiguration.builder().build(), null, new Throwable()));
	}

	/**
	 * Verifies that logging a null {@link Throwable} throws a {@link NullPointerException}.
	 *
	 * @see MethodCallLogger#logThrowable(MethodOutputLoggingConfiguration, Method, Throwable)
	 */
	@Test
	void givenNullThrowable_whenLogThrowable_throwsException() {
		assertThrows(NullPointerException.class, () ->
			sut.logThrowable(MethodOutputLoggingConfiguration.builder().build(),
				LogTestingClass.class.getMethod("noOp"), null));
	}

	/**
	 * Verifies the effective logging of a {@link Throwable} as a structured message.
	 *
	 * @param withDataLoggedInContext Whether the {@link Throwable} data must be stored in the log context.
	 * @throws NoSuchMethodException if the method {@link LogTestingClass#noOp()} is not defined.
	 * @see MethodCallLogger#logThrowable(MethodOutputLoggingConfiguration, Method, Throwable)
	 */
	@ParameterizedTest
	@ValueSource(strings = {"true", "false"})
	void givenThrowable_whenLogThrowableAsStructuredMessage_generatesLog(final boolean withDataLoggedInContext)
		throws NoSuchMethodException {
		final Throwable throwable = new Throwable("Test throwable");
		final MethodOutputLoggingConfiguration configuration = MethodOutputLoggingConfiguration.builder()
			.structuredMessage(true)
			.dataLoggedInContext(withDataLoggedInContext)
			.build();
		sut.logThrowable(configuration, LogTestingClass.class.getMethod("noOp"), throwable);

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", allOf(
				containsString("\"calledMethod\":\"LogTestingClass.noOp\""),
				containsString("\"thrown\":\"" + throwable.getClass().getName() + "\""),
				containsString("\"thrownMessage\":\"Test throwable\""),
				matchesRegex(".*\\\"stackTrace\\\":\\[(\\\".*\\\",?)\\].*")
			)),
			hasProperty("arguments", emptyCollectionOf(String.class)),
			hasProperty("level", is(Level.ERROR)),
			buildMdcMatcher(withDataLoggedInContext, "LogTestingClass.noOp", throwable.getClass().getName())
		)));
	}

	/**
	 * Verifies the effective logging of a {@link Throwable} without displaying the class name of the method that
	 * failed.
	 *
	 * @param withDataLoggedInContext Whether the {@link Throwable} data must be stored in the log context.
	 * @throws NoSuchMethodException if the method {@link LogTestingClass#methodThrowingThrowable()} is not defined.
	 * @see MethodCallLogger#logThrowable(MethodOutputLoggingConfiguration, Method, Throwable)
	 */
	@ParameterizedTest
	@ValueSource(strings = {"true", "false"})
	void givenMethodThrowingThrowable_whenLogThrowable_generatesLog(final boolean withDataLoggedInContext)
		throws NoSuchMethodException {
		String caughtThrowableType = null;
		try {
			new LogTestingClass().methodThrowingThrowable();
		} catch (final Throwable throwable) {
			caughtThrowableType = throwable.getClass().getName();
			sut.logThrowable(MethodOutputLoggingConfiguration.builder()
					.classNameDisplayed(false)
					.dataLoggedInContext(withDataLoggedInContext)
					.build(),
				LogTestingClass.class.getMethod("methodThrowingThrowable"), throwable);
		}

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(MethodOutputLoggingConfiguration.THROWABLE_MESSAGE_TEMPLATE)),
			hasProperty("arguments", hasItem(Throwable.class.getName())),
			hasProperty("arguments", hasItem("Test throwable")),
			hasProperty("level", is(Level.ERROR)),
			buildMdcMatcher(withDataLoggedInContext, "methodThrowingThrowable", caughtThrowableType)
		)));
	}

	/**
	 * Builds a matcher to verify the content of MDC in the output log entry of a method throwing a {@link Throwable}.
	 *
	 * @param withDataLoggedInContext 	Whether the {@link Throwable} data must be stored in the log context.
	 * @param expectedMethodName		The expected value of the key {@code invokedMethod}.
	 * @param expectedThrowableType		The expected value of the key {@code throwableType}.
	 * @return The matcher verifying the content of MDC in the log entry.
	 */
	private static Matcher<Map<?, ?>> buildMdcMatcher(final boolean withDataLoggedInContext,
													  final String expectedMethodName,
													  final String expectedThrowableType) {
		Matcher<Map<?, ?>> mdcMatcher = hasProperty("mdc", anEmptyMap());
		if (withDataLoggedInContext) {
			mdcMatcher = hasProperty("mdc", allOf(
				aMapWithSize(3),
				hasEntry("invokedMethod", expectedMethodName),
				hasEntry("outputValue", "n/a"),
				hasEntry("throwableType", expectedThrowableType)
			));
		}
		return mdcMatcher;
	}
}
