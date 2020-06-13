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

import com.github.maximevw.autolog.core.annotations.AutoLogMethodInOut;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Unit tests for the output data logging methods in the class {@link MethodCallLogger}.
 */
class LogMethodOutputTest {

	private static TestLogger logger;
	private static MethodCallLogger sut;

	/**
	 * Initializes the context for all the tests in this class.
	 */
	@BeforeAll
	static void init() {
		logger = TestLoggerFactory.getTestLogger("Autolog");
		sut = spy(new MethodCallLogger(
			new LoggerManager().register(Slf4jAdapter.getInstance())
		));
	}

	/**
	 * Resets the logger before each new test case.
	 */
	@BeforeEach
	void reset() {
		logger.clear();
	}

	/**
	 * Verifies that logging output data with a null configuration throws a {@link NullPointerException}.
	 *
	 * @see MethodCallLogger#logMethodOutput(MethodOutputLoggingConfiguration, Method, Object)
	 */
	@Test
	@SuppressWarnings("ConstantConditions")
	void givenNullConfiguration_whenLogMethodOutput_throwsException() {
		assertThrows(NullPointerException.class, () ->
			sut.logMethodOutput(null, LogTestingClass.class.getMethod("noOp"), null));
	}

	/**
	 * Verifies that logging output data with a null method throws a {@link NullPointerException}.
	 *
	 * @see MethodCallLogger#logMethodOutput(MethodOutputLoggingConfiguration, Method, Object)
	 */
	@Test
	@SuppressWarnings("ConstantConditions")
	void givenNullMethod_whenLogMethodOutput_throwsException() {
		assertThrows(NullPointerException.class, () ->
			sut.logMethodOutput(MethodOutputLoggingConfiguration.builder().build(), (Method) null, null));
	}

	/**
	 * Verifies that logging output data with a null configuration throws a {@link NullPointerException}.
	 *
	 * @see MethodCallLogger#logMethodOutput(MethodOutputLoggingConfiguration, String, String, Object)
	 */
	@Test
	@SuppressWarnings("ConstantConditions")
	void givenNullConfiguration_whenLogMethodOutputByName_throwsException() {
		assertThrows(NullPointerException.class, () -> sut.logMethodOutput(null, LoggingUtils.AUTOLOG_DEFAULT_TOPIC,
			"noOp", null));
	}

	/**
	 * Verifies that logging output data with a null method name throws a {@link NullPointerException}.
	 *
	 * @see MethodCallLogger#logMethodOutput(MethodOutputLoggingConfiguration, String, String, Object)
	 */
	@Test
	void givenNullMethodName_whenLogMethodOutputByName_throwsException() {
		assertThrows(NullPointerException.class, () ->
			sut.logMethodOutput(MethodOutputLoggingConfiguration.builder().build(), LoggingUtils.AUTOLOG_DEFAULT_TOPIC,
				null, null));
	}

	/**
	 * Verifies that logging output data with a null configuration throws a {@link NullPointerException}.
	 *
	 * @see MethodCallLogger#logMethodOutput(MethodOutputLoggingConfiguration, String, String)
	 */
	@Test
	@SuppressWarnings("ConstantConditions")
	void givenNullConfiguration_whenLogMethodExitingByName_throwsException() {
		assertThrows(NullPointerException.class, () -> sut.logMethodOutput(null, LoggingUtils.AUTOLOG_DEFAULT_TOPIC,
			"noOp"));
	}

	/**
	 * Verifies that logging output data with a null method name throws a {@link NullPointerException}.
	 *
	 * @see MethodCallLogger#logMethodOutput(MethodOutputLoggingConfiguration, String, String)
	 */
	@Test
	void givenNullMethodName_whenLogMethodExitingByName_throwsException() {
		assertThrows(NullPointerException.class, () ->
			sut.logMethodOutput(MethodOutputLoggingConfiguration.builder().build(), LoggingUtils.AUTOLOG_DEFAULT_TOPIC,
				null));
	}

	/**
	 * Verifies that logging output data with a null method name throws a {@link NullPointerException}.
	 *
	 * @see MethodCallLogger#logMethodOutput(MethodOutputLoggingConfiguration, String)
	 * @deprecated This test will be removed as soon as the deprecated tested method is removed.
	 */
	@Test
	@Deprecated(forRemoval = true)
	void givenNullMethodReturningVoidName_whenLogMethodOutput_throwsException() {
		assertThrows(NullPointerException.class, () -> sut.logMethodOutput(new MethodOutputLoggingConfiguration(),
			null));
	}

	/**
	 * Verifies that logging output data with a null configuration throws a {@link NullPointerException}.
	 *
	 * @see MethodCallLogger#logMethodOutput(MethodOutputLoggingConfiguration, String)
	 * @deprecated This test will be removed as soon as the deprecated tested method is removed.
	 */
	@Test
	@Deprecated(forRemoval = true)
	void givenMethodReturningVoidNameAndNullConfiguration_whenLogMethodOutput_throwsException() {
		assertThrows(NullPointerException.class, () -> sut.logMethodOutput(null, "testMethodName"));
	}

	/**
	 * Verifies that logging output data with a null method name throws a {@link NullPointerException}.
	 *
	 * @see MethodCallLogger#logMethodOutput(MethodOutputLoggingConfiguration, String, Object)
	 * @deprecated This test will be removed as soon as the deprecated tested method is removed.
	 */
	@Test
	@Deprecated(forRemoval = true)
	void givenNullMethodName_whenLogMethodOutput_throwsException() {
		assertThrows(NullPointerException.class, () -> sut.logMethodOutput(new MethodOutputLoggingConfiguration(),
			(String) null, new Object()));
	}

	/**
	 * Verifies that logging output data with a null configuration throws a {@link NullPointerException}.
	 *
	 * @see MethodCallLogger#logMethodOutput(MethodOutputLoggingConfiguration, String, Object)
	 * @deprecated This test will be removed as soon as the deprecated tested method is removed.
	 */
	@Test
	@Deprecated(forRemoval = true)
	void givenMethodNameAndNullConfiguration_whenLogMethodOutput_throwsException() {
		assertThrows(NullPointerException.class, () -> sut.logMethodOutput(null, "testMethodName", new Object()));
	}

	/**
	 * Verifies the deprecated method
	 * {@link MethodCallLogger#logMethodOutput(MethodOutputLoggingConfiguration, String)} calls the new
	 * implementation {@link MethodCallLogger#logMethodOutput(MethodOutputLoggingConfiguration, String, String)}.
	 * @deprecated This test will be removed as soon as the deprecated tested method is removed.
	 */
	@Test
	@Deprecated(forRemoval = true)
	void givenConfigurationAndMethodReturningVoidName_whenLogMethodOutputWithDefaultTopic_generatesLog() {
		final MethodOutputLoggingConfiguration configuration = MethodOutputLoggingConfiguration.builder().build();
		final String methodName = "testMethodName";
		sut.logMethodOutput(configuration, methodName);
		verify(sut, times(1)).logMethodOutput(eq(configuration), eq(LoggingUtils.AUTOLOG_DEFAULT_TOPIC),
			eq(methodName));
	}

	/**
	 * Verifies the deprecated method
	 * {@link MethodCallLogger#logMethodOutput(MethodOutputLoggingConfiguration, String, Object)} calls the new
	 * implementation
	 * {@link MethodCallLogger#logMethodOutput(MethodOutputLoggingConfiguration, String, String, Object)}.
	 * @deprecated This test will be removed as soon as the deprecated tested method is removed.
	 */
	@Test
	@Deprecated(forRemoval = true)
	void givenConfigurationAndMethodName_whenLogMethodOutputWithDefaultTopic_generatesLog() {
		final MethodOutputLoggingConfiguration configuration = MethodOutputLoggingConfiguration.builder().build();
		final String methodName = "testMethodName";
		final Object returnedValue = new Object();
		sut.logMethodOutput(configuration, methodName, returnedValue);
		verify(sut, times(1)).logMethodOutput(eq(configuration), eq(LoggingUtils.AUTOLOG_DEFAULT_TOPIC),
			eq(methodName), eq(returnedValue));
	}

	/**
	 * Verifies the effective logging of the end of invocation of a method without returned value.
	 *
	 * @param withDataLoggedInContext Whether the method output data must be stored in the log context.
	 * @throws NoSuchMethodException if method {@link LogTestingClass#methodReturningVoid()} is not defined.
	 */
	@ParameterizedTest
	@ValueSource(strings = {"true", "false"})
	void givenMethodReturningVoid_whenLogMethodOutput_generatesLog(final boolean withDataLoggedInContext)
		throws NoSuchMethodException {
		sut.logMethodOutput(MethodOutputLoggingConfiguration.builder()
				.dataLoggedInContext(withDataLoggedInContext)
				.build(),
			LogTestingClass.class.getMethod("methodReturningVoid"), null);

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(AutoLogMethodInOut.VOID_OUTPUT_DEFAULT_MESSAGE_TEMPLATE)),
			hasProperty("arguments", hasItem("LogTestingClass.methodReturningVoid")),
			hasProperty("level", is(Level.INFO)),
			buildMdcMatcher(withDataLoggedInContext, "LogTestingClass.methodReturningVoid", "void")
		)));
	}

	/**
	 * Verifies the effective logging of the end of invocation of a method without returned value as a structured
	 * message.
	 *
	 * @throws NoSuchMethodException if method {@link LogTestingClass#methodReturningVoid()} is not defined.
	 */
	@Test
	void givenMethodReturningVoid_whenLogMethodOutputAsStructuredMessage_generatesLog() throws NoSuchMethodException {
		sut.logMethodOutput(MethodOutputLoggingConfiguration.builder().structuredMessage(true).build(),
			LogTestingClass.class.getMethod("methodReturningVoid"), null);

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", allOf(
				containsString("\"calledMethod\":\"LogTestingClass.methodReturningVoid\""),
				containsString("\"outputValue\":\"void\"")
			)),
			hasProperty("arguments", emptyCollectionOf(String.class)),
			hasProperty("level", is(Level.INFO))
		)));
	}

	/**
	 * Verifies the effective logging of output data for a given method.
	 *
	 * @param withDataLoggedInContext Whether the method output data must be stored in the log context.
	 * @throws NoSuchMethodException if method {@link LogTestingClass#methodOutputData()} is not defined.
	 */
	@ParameterizedTest
	@ValueSource(strings = {"true", "false"})
	void givenMethod_whenLogMethodOutput_generatesLog(final boolean withDataLoggedInContext)
		throws NoSuchMethodException {
		sut.logMethodOutput(MethodOutputLoggingConfiguration.builder()
				.dataLoggedInContext(withDataLoggedInContext)
				.build(),
			LogTestingClass.class.getMethod("methodOutputData"), "test");

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(AutoLogMethodInOut.OUTPUT_DEFAULT_MESSAGE_TEMPLATE)),
			hasProperty("arguments", hasItem("LogTestingClass.methodOutputData")),
			hasProperty("arguments", hasItem("\"test\"")),
			hasProperty("level", is(Level.INFO)),
			buildMdcMatcher(withDataLoggedInContext, "LogTestingClass.methodOutputData", "\"test\"")
		)));
	}

	/**
	 * Verifies the effective logging of output data for a given method as a structured message.
	 *
	 * @throws NoSuchMethodException if method {@link LogTestingClass#methodOutputData()} is not defined.
	 */
	@Test
	void givenMethod_whenLogMethodOutputAsStructuredMessage_generatesLog() throws NoSuchMethodException {
		sut.logMethodOutput(MethodOutputLoggingConfiguration.builder().structuredMessage(true).build(),
			LogTestingClass.class.getMethod("methodOutputData"), "test");

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", allOf(
				containsString("\"calledMethod\":\"LogTestingClass.methodOutputData\""),
				containsString("\"outputValue\":\"\\\"test\\\"\"")
			)),
			hasProperty("arguments", emptyCollectionOf(String.class)),
			hasProperty("level", is(Level.INFO))
		)));
	}

	/**
	 * Builds a matcher to verify the content of MDC in a method output log entry.
	 *
	 * @param withDataLoggedInContext 	Whether the method output data must be stored in the log context.
	 * @param expectedMethodName		The expected value of the key {@code invokedMethod}.
	 * @param expectedOutputValue		The expected value of the key {@code outputValue}.
	 * @return The matcher verifying the content of MDC in the log entry.
	 */
	private static Matcher<Map<?, ?>> buildMdcMatcher(final boolean withDataLoggedInContext,
													  final String expectedMethodName,
													  final String expectedOutputValue) {
		Matcher<Map<?, ?>> mdcMatcher = hasProperty("mdc", anEmptyMap());
		if (withDataLoggedInContext) {
			mdcMatcher = hasProperty("mdc", allOf(
				aMapWithSize(2),
				hasEntry("invokedMethod", expectedMethodName),
				hasEntry("outputValue", expectedOutputValue)
			));
		}
		return mdcMatcher;
	}
}
