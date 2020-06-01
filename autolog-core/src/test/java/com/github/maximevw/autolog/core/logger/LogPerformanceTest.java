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

import com.github.maximevw.autolog.core.configuration.MethodPerformanceLoggingConfiguration;
import com.github.maximevw.autolog.core.configuration.PrettyDataFormat;
import com.github.maximevw.autolog.core.logger.adapters.Slf4jAdapter;
import com.github.maximevw.autolog.core.logger.performance.AdditionalDataProvider;
import com.github.maximevw.autolog.core.logger.performance.PerformanceTimer;
import com.github.maximevw.autolog.test.LogTestingClass;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.org.lidalia.slf4jext.Level;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.aMapWithSize;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.anEmptyMap;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesRegex;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for the class {@link MethodPerformanceLogger}.
 */
class LogPerformanceTest {

	private static TestLogger logger;
	private static MethodPerformanceLogger sut;

	/**
	 * Initializes the context for all the tests in this class.
	 */
	@BeforeAll
	static void init() {
		logger = TestLoggerFactory.getTestLogger("Autolog");
		sut = new MethodPerformanceLogger(
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
	 * Verifies that logging performance with a null configuration throws a {@link NullPointerException}.
	 *
	 * @see MethodPerformanceLogger#stopAndLog(MethodPerformanceLoggingConfiguration, PerformanceTimer)
	 */
	@Test
	@SuppressWarnings("ConstantConditions")
	void givenNullConfiguration_whenStopTimerAndLogPerformance_throwsException() {
		assertThrows(NullPointerException.class, () -> sut.stopAndLog(null, new PerformanceTimer()));
	}

	/**
	 * Verifies that logging performance with a null performance timer throws a {@link NullPointerException}.
	 *
	 * @see MethodPerformanceLogger#stopAndLog(MethodPerformanceLoggingConfiguration, PerformanceTimer)
	 */
	@Test
	@SuppressWarnings("ConstantConditions")
	void givenNullPerformanceTimer_whenStopTimerAndLogPerformance_throwsException() {
		assertThrows(NullPointerException.class, () -> sut.stopAndLog(new MethodPerformanceLoggingConfiguration(),
			null));
	}

	/**
	 * Verifies that starting performance monitoring with a null configuration throws a {@link NullPointerException}.
	 *
	 * @see MethodPerformanceLogger#start(MethodPerformanceLoggingConfiguration, Method)
	 */
	@Test
	@SuppressWarnings("ConstantConditions")
	void givenNullConfiguration_whenStartTimerWithMethod_throwsException() {
		assertThrows(NullPointerException.class, () -> sut.start(null, LogTestingClass.class.getMethod("noOp")));
	}

	/**
	 * Verifies that starting performance monitoring with a null method throws a {@link NullPointerException}.
	 *
	 * @see MethodPerformanceLogger#start(MethodPerformanceLoggingConfiguration, Method)
	 */
	@Test
	void givenNullMethod_whenStartTimerWithMethod_throwsException() {
		assertThrows(NullPointerException.class, () -> sut.start(new MethodPerformanceLoggingConfiguration(),
			(Method) null));
	}

	/**
	 * Verifies that starting performance monitoring with a null configuration throws a {@link NullPointerException}.
	 *
	 * @see MethodPerformanceLogger#start(MethodPerformanceLoggingConfiguration, String)
	 */
	@Test
	void givenNullConfiguration_whenStartTimerWithMethodName_throwsException() {
		assertThrows(NullPointerException.class, () -> sut.start(null, "noOp"));
	}

	/**
	 * Verifies that starting performance monitoring with a null method name throws a {@link NullPointerException}.
	 *
	 * @see MethodPerformanceLogger#start(MethodPerformanceLoggingConfiguration, String)
	 */
	@Test
	void givenNullMethod_whenStartTimerWithMethodName_throwsException() {
		assertThrows(NullPointerException.class, () -> sut.start(new MethodPerformanceLoggingConfiguration(),
			(String) null));
	}

	/**
	 * Verifies that starting performance monitoring with a null configuration throws a {@link NullPointerException}.
	 *
	 * @see MethodPerformanceLogger#start(MethodPerformanceLoggingConfiguration, String, String, String)
	 */
	@Test
	@SuppressWarnings("ConstantConditions")
	void givenNullConfiguration_whenStartTimerWithMethodNameAndHttpMethod_throwsException() {
		assertThrows(NullPointerException.class, () ->
			sut.start(null, LoggingUtils.AUTOLOG_DEFAULT_TOPIC, "noOp", "GET"));
	}

	/**
	 * Verifies that starting performance monitoring with a null method name throws a {@link NullPointerException}.
	 *
	 * @see MethodPerformanceLogger#start(MethodPerformanceLoggingConfiguration, String, String, String)
	 */
	@Test
	void givenNullMethod_whenStartTimerWithMethodNameAndHttpMethod_throwsException() {
		assertThrows(NullPointerException.class, () -> sut.start(new MethodPerformanceLoggingConfiguration(),
			LoggingUtils.AUTOLOG_DEFAULT_TOPIC, null, "GET"));
	}

	/**
	 * Verifies the effective logging of the performance data for a method calling another monitored one with the
	 * specified parameters.
	 *
	 * @param throwException	Whether the called method must throw an exception.
	 * @param logEachTimer		Whether the performance timer of each method must be logged individually at the end of
	 *                          the method invocation.
	 * @param dumpStackOfCalls  Whether the full stack of calls with performance information must be dumped and logged
	 *                          as a tree when the initial method invocation ends.
	 * @param methodStateVerb	The expected verb corresponding to the method state (success/failure) in the log.
	 * @throws NoSuchMethodException if the method {@link LogTestingClass#methodCallingAnotherMonitoredOne(
	 * 								 MethodPerformanceLogger, MethodPerformanceLoggingConfiguration, boolean)} is not
	 * 								 found.
	 * @see MethodPerformanceLogger#start(MethodPerformanceLoggingConfiguration, Method)
	 * @see MethodPerformanceLogger#stopAndLog(MethodPerformanceLoggingConfiguration, PerformanceTimer)
	 */
	@ParameterizedTest
	@MethodSource("provideConfigurationsForTestWithMethodCallingAnotherOne")
	void givenConfigurationAndMethodCallingAnotherOne_whenLogPerformance_generatesLog(final boolean throwException,
																					  final boolean logEachTimer,
																					  final boolean dumpStackOfCalls,
																					  final String methodStateVerb)
		throws NoSuchMethodException {
		final MethodPerformanceLoggingConfiguration configuration = MethodPerformanceLoggingConfiguration.builder()
			.eachTimerLogged(logEachTimer)
			.callsStackDumped(dumpStackOfCalls)
			.build();
		final PerformanceTimer timer = sut.start(configuration,
			LogTestingClass.class.getMethod("methodCallingAnotherMonitoredOne", MethodPerformanceLogger.class,
				MethodPerformanceLoggingConfiguration.class, boolean.class));

		assertNotNull(timer);
		assertNull(timer.getParent());
		assertNull(timer.getChildren());
		assertTrue(timer.isRunning());

		final LogTestingClass logTestingClass = new LogTestingClass();
		try {
			logTestingClass.methodCallingAnotherMonitoredOne(sut, configuration, throwException);
		} catch (Exception e) {
			// Just log the error in the standard error output since this exception is deliberately thrown for the test.
			System.err.println(e.getMessage());
		} finally {
			sut.stopAndLog(configuration, timer);
		}

		assertFalse(timer.isRunning());

		if (logEachTimer) {
			assertThat(logger.getLoggingEvents(), hasItem(allOf(
				hasProperty("message", is("Method {} executed in {} (started: {}, ended: {}).")),
				hasProperty("arguments", hasItem("LogTestingClass.methodCallingAnotherMonitoredOne"))
			)));
			assertThat(logger.getLoggingEvents(), hasItem(allOf(
				hasProperty("message", is("Method {} " + methodStateVerb + " {} (started: {}, ended: {}).")),
				hasProperty("arguments", hasItem("calledMethod"))
			)));
		}

		if (dumpStackOfCalls) {
			assertThat(logger.getLoggingEvents(), hasItem(allOf(
				hasProperty("message", is("Performance summary report for {}:")),
				hasProperty("arguments", hasItem("LogTestingClass.methodCallingAnotherMonitoredOne"))
			)));
			assertThat(logger.getLoggingEvents(), hasItem(allOf(
				hasProperty("message",
					matchesRegex("> LogTestingClass.methodCallingAnotherMonitoredOne executed in \\d+ ms"))
			)));
			assertThat(logger.getLoggingEvents(), hasItem(allOf(
				hasProperty("message", matchesRegex("\\|_ > calledMethod " + methodStateVerb + " \\d+ ms"))
			)));
		}
	}

	/**
	 * Builds arguments for the parameterized test relative to the logging of the performance data for a method calling
	 * another monitored one: {@link #givenConfigurationAndMethodCallingAnotherOne_whenLogPerformance_generatesLog(
	 * boolean, boolean, boolean, String)}.
	 *
	 * @return The arguments to execute the different test cases.
	 */
	private static Stream<Arguments> provideConfigurationsForTestWithMethodCallingAnotherOne() {
		return Stream.of(
			Arguments.of(false, true, true, "executed in"),
			Arguments.of(false, false, true, "executed in"),
			Arguments.of(false, true, false, "executed in"),
			Arguments.of(true, true, true, "failed after")
		);
	}

	/**
	 * Verifies the effective logging of the performance data for a given method with the specified parameters.
	 *
	 * @param configuration				The configuration to use.
	 * @param method					The invoked method for which the performance is logged.
	 * @param expectedMessageMatchers	The set of matchers to verify the logged message.
	 * @param expectedArgsMatchers		The set of matchers to verify the arguments of the log event.
	 * @param expectedMdcMatcher		The matcher to verify the MDC values of the log event. It can be {@code null}.
	 * @param methodArgs				The arguments to pass to the invoked method.
	 * @throws IllegalAccessException if the invoked method cannot be executed.
	 * @see MethodPerformanceLogger#start(MethodPerformanceLoggingConfiguration, Method)
	 * @see MethodPerformanceLogger#stopAndLog(MethodPerformanceLoggingConfiguration, PerformanceTimer)
	 */
	@ParameterizedTest
	@MethodSource("provideMethodsAndConfigurations")
	void givenConfiguration_whenLogPerformance_generatesLog(final MethodPerformanceLoggingConfiguration configuration,
															final Method method,
															final Matcher<? super Object>[] expectedMessageMatchers,
															final Matcher<? super Object>[] expectedArgsMatchers,
															final Matcher<? super Object> expectedMdcMatcher,
															final Object[] methodArgs)
		throws IllegalAccessException {
		final PerformanceTimer timer = sut.start(configuration, method);

		assertNotNull(timer);
		assertNull(timer.getParent());
		assertNull(timer.getChildren());
		assertTrue(timer.isRunning());

		final LogTestingClass logTestingClass = new LogTestingClass();
		try {
			final Object methodResult = method.invoke(logTestingClass, methodArgs);
			// If an additional data provider has been set in the configuration, consider that the result of the
			// executed method returns is the instance of the additional data provider and complete the performance log
			// entry in consequence.
			if (methodResult instanceof AdditionalDataProvider && configuration.getAdditionalDataProvider() != null) {
				timer.getPerformanceLogEntry().setProcessedItems(
					((AdditionalDataProvider) methodResult).getProcessedItems());
				timer.getPerformanceLogEntry().addComments(((AdditionalDataProvider) methodResult).getComments()
					.toArray(new String[]{}));
			}
		} catch (final InvocationTargetException e) {
			// If the invoked method threw an exception, update the log entry and just log the error in the standard
			// error output (it is not the purpose of this test to log the exception properly).
			timer.getPerformanceLogEntry().setFailed(true);
			System.err.println(e.getMessage());
		} finally {
			sut.stopAndLog(configuration, timer);
		}

		assertFalse(timer.isRunning());

		Matcher<? super Object> mdcMatcher = hasProperty("mdc", anEmptyMap());
		if (configuration.isDataLoggedInContext() && expectedMdcMatcher != null) {
			mdcMatcher = expectedMdcMatcher;
		}
		final Matcher<? super Object> messageMatcher = hasProperty("message", allOf(List.of(expectedMessageMatchers)));
		final Matcher<? super Object> argumentsMatcher = hasProperty("arguments", allOf(List.of(expectedArgsMatchers)));
		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			messageMatcher, argumentsMatcher, mdcMatcher,
			hasProperty("level", is(Level.valueOf(configuration.getLogLevel().name()))
			))));
	}

	/**
	 * Builds a matcher to verify the content of MDC in a performance log entry.
	 *
	 * @param expectedMethodName		The expected value of the key {@code invokedMethod}.
	 * @param isExpectedMethodFailed	The expected value of the key {@code failed}.
	 * @param expectedComments			The expected value of the key {@code comments}. This value is nullable: if
	 *                                  {@code null}, the matcher will verify that the key is not present.
	 * @param expectedProcessedItems    The expected value of the key {@code processedItems}. This value is nullable:
	 *                                  if {@code null}, the matcher will verify that the key is not present.
	 * @return The matcher verifying the content of MDC in the log entry.
	 */
	@SuppressWarnings({"unchecked", "rawtypes"})
	private static Matcher<? super Object> buildMdcMatcher(final String expectedMethodName,
														   final boolean isExpectedMethodFailed,
														   final String expectedComments,
														   final Integer expectedProcessedItems) {
		int expectedNumberOfMdcProperties = 3;
		Matcher[] matchers = new Matcher[]{
			hasEntry("invokedMethod", expectedMethodName),
			hasEntry(is("executionTimeInMs"), matchesRegex("\\d+")),
			hasEntry("failed", String.valueOf(isExpectedMethodFailed))
		};
		if (expectedComments != null) {
			matchers = ArrayUtils.add(matchers, hasEntry("comments", expectedComments));
			expectedNumberOfMdcProperties++;
		} else {
			matchers = ArrayUtils.add(matchers, not(hasKey("comments")));
		}
		if (expectedProcessedItems != null) {
			matchers = ArrayUtils.add(matchers, hasEntry("processedItems", String.valueOf(expectedProcessedItems)));
			expectedNumberOfMdcProperties++;
		} else {
			matchers = ArrayUtils.add(matchers, not(hasKey("processedItems")));
		}
		matchers = ArrayUtils.add(matchers, aMapWithSize(expectedNumberOfMdcProperties));
		return hasProperty("mdc", allOf(List.of(matchers)));
	}

	/**
	 * Builds arguments for the parameterized test relative to the logging of the performance data for a given method:
	 * {@link #givenConfiguration_whenLogPerformance_generatesLog(MethodPerformanceLoggingConfiguration, Method,
	 * Matcher[], Matcher[], Matcher, Object[])}
	 *
	 * @return The arguments to execute the different test cases.
	 * @throws NoSuchMethodException if the invoked method is not defined in {@link LogTestingClass}.
	 */
	private static Stream<Arguments> provideMethodsAndConfigurations() throws NoSuchMethodException {
		return Stream.of(
			// Default configuration and simple method call.
			Arguments.of(MethodPerformanceLoggingConfiguration.builder().build(),
				LogTestingClass.class.getMethod("noOp"),
				new Matcher[]{
					is("Method {} executed in {} (started: {}, ended: {}).")
				},
				new Matcher[]{
					hasItem(is("LogTestingClass.noOp")),
					hasItem(matchesRegex("\\d+ ms")),
					hasItem(instanceOf(LocalDateTime.class))
				},
				null,
				new Object[]{}),
			// Default configuration with population of log context and call to a method throwing exception.
			Arguments.of(MethodPerformanceLoggingConfiguration.builder()
					.dataLoggedInContext(true)
					.build(),
				LogTestingClass.class.getMethod("methodThrowingThrowable"),
				new Matcher[]{
					is("Method {} failed after {} (started: {}, ended: {}).")
				},
				new Matcher[]{
					hasItem(is("LogTestingClass.methodThrowingThrowable")),
					hasItem(matchesRegex("\\d+ ms")),
					hasItem(instanceOf(LocalDateTime.class))
				},
				buildMdcMatcher("LogTestingClass.methodThrowingThrowable", true, null, null),
				new Object[]{}),
			// Don't display enclosing class name, not default log level and log comments.
			Arguments.of(MethodPerformanceLoggingConfiguration.builder()
					.classNameDisplayed(false)
					.logLevel(LogLevel.INFO)
					.comments(List.of("comment1", "comment2"))
					.build(),
				LogTestingClass.class.getMethod("noOp"),
				new Matcher[]{
					is("Method {} executed in {} (started: {}, ended: {}). Details: {}.")
				},
				new Matcher[]{
					hasItem(is("noOp")),
					hasItem(matchesRegex("\\d+ ms")),
					hasItem(instanceOf(LocalDateTime.class)),
					hasItem(is("comment1, comment2"))
				},
				null,
				new Object[]{}),
			// Log performance data as structured message JSON-formatted and populate the log context.
			Arguments.of(MethodPerformanceLoggingConfiguration.builder()
					.structuredMessage(true)
					.dataLoggedInContext(true)
					.build(),
				LogTestingClass.class.getMethod("noOp"),
				new Matcher[]{
					containsString("\"invokedMethod\":\"LogTestingClass.noOp\""),
					matchesRegex(".+\\\"executionTimeInMs\\\":\\d+.+"),
					matchesRegex(".+\\\"executionTime\\\":\\\"\\d+ ms\\\".+"),
					containsString("\"startTime\":"),
					containsString("\"endTime\":")
				},
				new Matcher[]{},
				buildMdcMatcher("LogTestingClass.noOp", false, null, null),
				new Object[]{}),
			// Log performance data as structured message XML-formatted.
			Arguments.of(MethodPerformanceLoggingConfiguration.builder()
					.structuredMessage(true)
					.prettyFormat(PrettyDataFormat.XML)
					.build(),
				LogTestingClass.class.getMethod("noOp"),
				new Matcher[]{
					matchesRegex("<MethodPerformanceLogEntry>.+</MethodPerformanceLogEntry>$"),
					containsString("<invokedMethod>LogTestingClass.noOp</invokedMethod>"),
					matchesRegex(".+<executionTimeInMs>\\d+</executionTimeInMs>.+"),
					matchesRegex(".+<executionTime>\\d+ ms</executionTime>.+"),
					matchesRegex(".+<startTime>.+</startTime>.+"),
					matchesRegex(".+<endTime>.+</endTime>.+")
				},
				new Matcher[]{},
				null,
				new Object[]{}),
			// Method processing 100 items with configured additional data provider: the additional data provider
			// doesn't exist in the tested class. Indeed, we just define a fake name to simulate its behaviour since
			// the additional data provider is managed by aspect programming to complete the log performance entry.
			// See implementation of givenConfiguration_whenLogPerformance_generatesLog() for more details.
			// It also populates the log context.
			Arguments.of(MethodPerformanceLoggingConfiguration.builder()
					.additionalDataProvider("useMethodResultAsNumberOfProcessedItems")
					.dataLoggedInContext(true)
					.build(),
				LogTestingClass.class.getMethod("methodUsingAdditionalDataProvider", int.class),
				new Matcher[]{
					is("Method {} processed {} item(s) (avg. {}/item) in {} (started: {}, ended: {}). Details: {}.")
				},
				new Matcher[]{
					hasItem(is("LogTestingClass.methodUsingAdditionalDataProvider")),
					hasItem(is(100)),
					hasItem(matchesRegex("\\d+ ms")),
					hasItem(instanceOf(LocalDateTime.class)),
					hasItem(is("additionalDataProviderUsed")),
				},
				buildMdcMatcher("LogTestingClass.methodUsingAdditionalDataProvider", false,
					"additionalDataProviderUsed", 100),
				new Object[]{100}),
			// Method processing 0 item with configured additional data provider. See test case above for more details.
			Arguments.of(MethodPerformanceLoggingConfiguration.builder()
					.additionalDataProvider("useMethodResultAsNumberOfProcessedItems")
					.build(),
				LogTestingClass.class.getMethod("methodUsingAdditionalDataProvider", int.class),
				new Matcher[]{
					is("Method {} processed {} item(s) (avg. {}/item) in {} (started: {}, ended: {}). Details: {}.")
				},
				new Matcher[]{
					hasItem(is("LogTestingClass.methodUsingAdditionalDataProvider")),
					hasItem(is(0)),
					hasItem(matchesRegex("\\d+ ms")),
					hasItem(instanceOf(LocalDateTime.class)),
					hasItem(is("additionalDataProviderUsed")),
				},
				null,
				new Object[]{0})
		);
	}

	/**
	 * Verifies the effective logging of the performance data for a given method corresponding to an API endpoint with
	 * auto-configuration for detection of API endpoints.
	 *
	 * @param testingClass			The class defining the tested API endpoints.
	 * @param methodName			The invoked method for which the performance is logged.
	 * @param performanceTimerName	The name of the performance timer (used as fallback).
	 * @param httpMethod			The HTTP method designating the API endpoint to test.
	 * @param expectedMethodName	The expected method name in the log event.
	 * @throws InvocationTargetException if the invoked method has thrown an exception.
	 * @throws IllegalAccessException if the invoked method cannot be executed.
	 * @throws NoSuchMethodException if the invoked method is not defined in the {@code testingClass}.
	 * @see MethodPerformanceLogger#start(MethodPerformanceLoggingConfiguration, Method)
	 * @see MethodPerformanceLogger#stopAndLog(MethodPerformanceLoggingConfiguration, PerformanceTimer)
	 */
	@ParameterizedTest
	@MethodSource("provideApiMethods")
	void givenApiEndpointsAutoConfigured_whenLogPerformance_generatesLog(final Object testingClass,
																		 final String methodName,
																		 final String performanceTimerName,
																		 final String httpMethod,
																		 final String expectedMethodName)
		throws InvocationTargetException, IllegalAccessException, NoSuchMethodException {
		final MethodPerformanceLoggingConfiguration configuration = MethodPerformanceLoggingConfiguration.builder()
			.apiEndpointsAutoConfigured(true)
			.name(performanceTimerName)
			.build();
		final Method method = testingClass.getClass().getMethod(methodName);
		final PerformanceTimer timer = sut.start(configuration, method);

		method.invoke(testingClass);
		sut.stopAndLog(configuration, timer);

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is("Method {} executed in {} (started: {}, ended: {}).")),
			hasProperty("arguments", hasItem(containsString(expectedMethodName))),
			hasProperty("arguments", hasItem(containsString(httpMethod)))
		)));
	}

	/**
	 * Builds arguments for the parameterized test relative to the logging of the performance data for a method
	 * corresponding to an API endpoint: {@link #givenApiEndpointsAutoConfigured_whenLogPerformance_generatesLog(
	 * Object, String, String, String, String)}
	 *
	 * @return The arguments to execute the different test cases.
	 */
	private static Stream<Arguments> provideApiMethods() {
		final LogTestingClass logTestingClass = new LogTestingClass();
		final LogTestingClass.JaxRsApiTestClass jaxRsApiTestClass = new LogTestingClass.JaxRsApiTestClass();
		final LogTestingClass.SpringWebApiTestClass springWebApiTestClass = new LogTestingClass.SpringWebApiTestClass();

		return Stream.of(
			// Test JAX-RS annotations.
			Arguments.of(jaxRsApiTestClass, "httpGetEndpointMethod", null, "GET", "/apiTest/test"),
			Arguments.of(jaxRsApiTestClass, "httpPostEndpointMethod", null, "POST", "/apiTest"),
			Arguments.of(jaxRsApiTestClass, "httpPutEndpointMethod", null, "PUT", "/apiTest"),
			Arguments.of(jaxRsApiTestClass, "httpPatchEndpointMethod", null, "PATCH", "/apiTest"),
			Arguments.of(jaxRsApiTestClass, "httpDeleteEndpointMethod", null, "DELETE", "/apiTest"),
			Arguments.of(jaxRsApiTestClass, "httpOptionsEndpointMethod", null, "OPTIONS", "/apiTest"),
			Arguments.of(jaxRsApiTestClass, "httpHeadEndpointMethod", null, "HEAD", "/apiTest"),
			// Test Spring Web annotations.
			Arguments.of(springWebApiTestClass, "httpGetEndpointMethod", null, "GET", "/apiTest/test"),
			Arguments.of(springWebApiTestClass, "httpPostEndpointMethod", null, "POST", "/apiTest"),
			Arguments.of(springWebApiTestClass, "httpPutEndpointMethod", null, "PUT", "/apiTest"),
			Arguments.of(springWebApiTestClass, "httpPatchEndpointMethod", null, "PATCH", "/apiTest"),
			Arguments.of(springWebApiTestClass, "httpDeleteEndpointMethod", null, "DELETE", "/apiTest"),
			Arguments.of(springWebApiTestClass, "httpOptionsEndpointMethod", null, "OPTIONS", "/apiTest/test"),
			Arguments.of(springWebApiTestClass, "httpHeadEndpointMethod", null, "HEAD", "/apiTest/test"),
			// Test not annotated method without fallback name.
			Arguments.of(logTestingClass, "noOp", null, StringUtils.EMPTY, "LogTestingClass.noOp"),
			// Test not annotated method with fallback name.
			Arguments.of(logTestingClass, "noOp", "fallbackName", StringUtils.EMPTY, "fallbackName")
		);
	}
}
