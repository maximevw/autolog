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

package com.github.maximevw.autolog.spring.aspects;

import com.github.maximevw.autolog.core.annotations.AutoLogPerformance;
import com.github.maximevw.autolog.core.logger.LoggerManager;
import com.github.maximevw.autolog.core.logger.adapters.Slf4jAdapter;
import com.github.maximevw.autolog.core.logger.performance.AdditionalDataProvider;
import com.github.maximevw.autolog.core.logger.performance.PerformanceTimerContext;
import com.github.maximevw.autolog.test.JaxRsApiTestClass;
import com.github.maximevw.autolog.test.MethodPerformanceAnnotatedTestClass;
import com.github.maximevw.autolog.test.MethodPerformanceNotAnnotatedTestClass;
import com.github.maximevw.autolog.test.SpringWebApiTestClass;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.aop.aspectj.annotation.AspectJProxyFactory;
import uk.org.lidalia.slf4jext.Level;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.InvocationTargetException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.startsWith;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.matchesRegex;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsIterableContaining.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for the aspect class {@link AutoLogPerformanceAspect}.
 */
class AutoLogPerformanceAspectTest {

	private static TestLogger logger;
	private static ByteArrayOutputStream stdOut = new ByteArrayOutputStream();

	// Proxies on which the test are run.
	private static MethodPerformanceAnnotatedTestClass proxyAnnotatedTestClass;
	private static MethodPerformanceNotAnnotatedTestClass proxyNotAnnotatedTestClass;
	private static JaxRsApiTestClass proxyJaxRsApiTestClass;
	private static SpringWebApiTestClass proxySpringWebApiTestClass;

	/**
	 * Initializes the context for all the tests in this class.
	 * <p>
	 *     We simulate the behaviour of Spring AOP by proxying the classes on which the aspect has to be tested.
	 *     See the Spring documentation for more information about <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#aop-understanding-aop-proxies">
	 *     proxy-based aspects</a>.
	 * </p>
	 */
	@BeforeAll
	static void init() {
		// Redirect standard system output to check Autolog warnings/errors.
		final PrintStream outStream = new PrintStream(stdOut);
		System.setOut(outStream);

		// Setup aspect.
		final AutoLogPerformanceAspect sut =
			new AutoLogPerformanceAspect(new LoggerManager().register(Slf4jAdapter.getInstance()));

		final MethodPerformanceAnnotatedTestClass annotatedTestClassTarget = new MethodPerformanceAnnotatedTestClass();
		final AspectJProxyFactory annotatedTestClassFactory = new AspectJProxyFactory(annotatedTestClassTarget);
		annotatedTestClassFactory.addAspect(sut);
		proxyAnnotatedTestClass = annotatedTestClassFactory.getProxy();

		final MethodPerformanceNotAnnotatedTestClass notAnnotatedTestClassTarget =
			new MethodPerformanceNotAnnotatedTestClass();
		final AspectJProxyFactory notAnnotatedTestClassFactory = new AspectJProxyFactory(notAnnotatedTestClassTarget);
		notAnnotatedTestClassFactory.addAspect(sut);
		proxyNotAnnotatedTestClass = notAnnotatedTestClassFactory.getProxy();

		final JaxRsApiTestClass jaxRsTestClassTarget = new JaxRsApiTestClass();
		final AspectJProxyFactory jaxRsTestClassFactory = new AspectJProxyFactory(jaxRsTestClassTarget);
		jaxRsTestClassFactory.addAspect(sut);
		proxyJaxRsApiTestClass = jaxRsTestClassFactory.getProxy();

		final SpringWebApiTestClass springWebApiTestClassTarget = new SpringWebApiTestClass();
		final AspectJProxyFactory springWebTestClassFactory = new AspectJProxyFactory(springWebApiTestClassTarget);
		springWebTestClassFactory.addAspect(sut);
		proxySpringWebApiTestClass = springWebTestClassFactory.getProxy();
	}

	/**
	 * Initializes the context for each new test case: test logger, reset timers.
	 */
	@BeforeEach
	void initEachTest() {
		logger = TestLoggerFactory.getTestLogger("Autolog");
		TestLoggerFactory.getInstance().setPrintLevel(Level.TRACE);

		// Remove current performance timer from the ThreadLocal to avoid "zombie" timers.
		PerformanceTimerContext.removeCurrent();
	}

	/**
	 * Clears the test logger after each new test case.
	 */
	@AfterEach
	void clearLoggers() {
		TestLoggerFactory.clear();
	}

	/**
	 * Builds arguments for the parameterized tests relative to the log of performance in classes declaring API
	 * endpoints: {@link #givenBadApiEndpointMethod_whenLogPerformanceByAspect_generateExpectedLog(Object)} and
	 * {@link #givenApiEndpointMethodNoAutoconfig_whenLogPerformanceByAspect_generateExpectedLog(Object)}.
	 * <p>
	 *     It provides instances of proxied classes declaring API endpoints thanks to JAX-RS and Spring Web Framework.
	 * </p>
	 *
	 * @return The proxied classes for the parameterized tests.
	 */
	private static Stream<Arguments> provideApiTestClasses() {
		return Stream.of(
			Arguments.of(proxyJaxRsApiTestClass), Arguments.of(proxySpringWebApiTestClass)
		);
	}

	/**
	 * Builds arguments for the parameterized tests relative to the log of performance in classes declaring API
	 * endpoints: {@link #givenApiEndpointMethod_whenLogPerformanceByAspect_generateExpectedLog(Object, String)}.
	 * <p>
	 *     It provides instances of proxied classes declaring API endpoints thanks to JAX-RS and Spring Web Framework
	 *     and, for each instance, the HTTP methods to test (GET, POST, PUT, DELETE, PATCH, OPTIONS, HEAD).
	 * </p>
	 *
	 * @return The proxied classes and HTTP methods for the parameterized tests.
	 */
	private static Stream<Arguments> provideApiTestClassesAndHttpMethods() {
		final List<Arguments> arguments = new ArrayList<>();
		Stream.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD")
			.forEach(httpMethod -> Stream.of(proxyJaxRsApiTestClass, proxySpringWebApiTestClass)
				.forEach(apiTestClass -> arguments.add(Arguments.of(apiTestClass, httpMethod)))
			);
		return arguments.stream();
	}

	/**
	 * Verifies that the performance data are correctly logged by aspect for a method in a class annotated with
	 * {@link AutoLogPerformance} using the default configuration.
	 *
	 * @see MethodPerformanceAnnotatedTestClass#testMethodToMonitor()
	 */
	@Test
	void givenMethodToMonitorInAnnotatedClass_whenLogPerformanceByAspect_generateExpectedLog() {
		proxyAnnotatedTestClass.testMethodToMonitor();

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is("Method {} executed in {} (started: {}, ended: {}).")),
			hasProperty("arguments", hasItem("MethodPerformanceAnnotatedTestClass.testMethodToMonitor")),
			hasProperty("arguments", hasItem(matchesRegex("\\d+ ms"))),
			hasProperty("arguments", hasItem(instanceOf(LocalDateTime.class))),
			hasProperty("level", is(Level.DEBUG))
		)));
	}

	/**
	 * Verifies that the performance data are correctly logged by aspect for a method annotated with
	 * {@link AutoLogPerformance} configured to log at info level with a specific name and static comments, in a class
	 * annotated with {@link AutoLogPerformance} using the default configuration.
	 *
	 * @see MethodPerformanceAnnotatedTestClass#testAnnotatedMethodToMonitor()
	 */
	@Test
	void givenAnnotatedMethodToMonitorInAnnotatedClass_whenLogPerformanceByAspect_generateExpectedLog() {
		proxyAnnotatedTestClass.testAnnotatedMethodToMonitor();

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is("Method {} executed in {} (started: {}, ended: {}). Details: {}.")),
			hasProperty("arguments", hasItem("methodPerformance")),
			hasProperty("arguments", hasItem(matchesRegex("\\d+ ms"))),
			hasProperty("arguments", hasItem(instanceOf(LocalDateTime.class))),
			hasProperty("arguments", hasItem("comment1, comment2")),
			hasProperty("level", is(Level.INFO))
		)));
	}

	/**
	 * Verifies that the performance data are correctly logged by aspect for a method calling another annotated one,
	 * in a class annotated with {@link AutoLogPerformance} using the default configuration.
	 *
	 * @see MethodPerformanceAnnotatedTestClass#testMethodCallingAnotherOne(MethodPerformanceNotAnnotatedTestClass)
	 */
	@Test
	void givenMethodCallingAnotherOne_whenLogPerformanceByAspect_generateExpectedLog() {
		proxyAnnotatedTestClass.testMethodCallingAnotherOne(proxyNotAnnotatedTestClass);

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is("Method {} executed in {} (started: {}, ended: {}).")),
			hasProperty("arguments", hasItem("MethodPerformanceAnnotatedTestClass.testMethodCallingAnotherOne")),
			hasProperty("arguments", hasItem(matchesRegex("\\d+ ms"))),
			hasProperty("arguments", hasItem(instanceOf(LocalDateTime.class))),
			hasProperty("level", is(Level.DEBUG))
		)));

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is("Performance summary report for {}:")),
			hasProperty("arguments", hasItem("MethodPerformanceAnnotatedTestClass.testMethodCallingAnotherOne")),
			hasProperty("level", is(Level.DEBUG))
		)));

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message",
				matchesRegex("> MethodPerformanceAnnotatedTestClass.testMethodCallingAnotherOne executed in \\d+ ms")),
			hasProperty("level", is(Level.DEBUG))
		)));

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message",
				matchesRegex("\\|_ > MethodPerformanceNotAnnotatedTestClass.testMethodToMonitor executed in \\d+ ms")),
			hasProperty("level", is(Level.DEBUG))
		)));

		assertThat(logger.getLoggingEvents(), not(hasItem(allOf(
			hasProperty("message", startsWith("Method {} executed in {}")),
			hasProperty("arguments", hasItem("MethodPerformanceNotAnnotatedTestClass.testMethodToMonitor")),
			hasProperty("level", is(Level.DEBUG))
		))));
	}

	/**
	 * Verifies that the performance data are correctly logged by aspect for a method calling another annotated one and
	 * throwing an exception, annotated with {@link AutoLogPerformance} configured to not dump the full stack of calls,
	 * in a class annotated with {@link AutoLogPerformance} using the default configuration.
	 *
	 * @see MethodPerformanceAnnotatedTestClass#testMethodToMonitorThrowingException(int)
	 */
	@Test
	void givenMethodThrowingException_whenLogPerformanceByAspect_generateExpectedLog() {
		final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
			() -> proxyAnnotatedTestClass.testMethodToMonitorThrowingException(123));
		assertEquals("Wrong value: 123", illegalArgumentException.getMessage());

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is("Method {} failed after {} (started: {}, ended: {}).")),
			hasProperty("arguments",
				hasItem("MethodPerformanceAnnotatedTestClass.testMethodToMonitorThrowingException")),
			hasProperty("arguments", hasItem(matchesRegex("\\d+ ms"))),
			hasProperty("arguments", hasItem(instanceOf(LocalDateTime.class))),
			hasProperty("level", is(Level.DEBUG))
		)));

		assertThat(logger.getLoggingEvents(), not(hasItem(allOf(
			hasProperty("message", is("Performance summary report for {}:")),
			hasProperty("arguments",
				hasItem("MethodPerformanceAnnotatedTestClass.testMethodToMonitorThrowingException")),
			hasProperty("level", is(Level.DEBUG))
		))));
	}

	/**
	 * Verifies that the performance data are correctly logged by aspect for a method annotated with
	 * {@link AutoLogPerformance} configured to use an {@link AdditionalDataProvider}, in a class not annotated with
	 * {@link AutoLogPerformance}.
	 *
	 * @see MethodPerformanceNotAnnotatedTestClass#testMethodToMonitorWithAdditionalDataProvider(String, List)
	 */
	@Test
	void givenMethodWithAdditionalDataProvider_whenLogPerformanceByAspect_generateExpectedLog() {
		proxyNotAnnotatedTestClass.testMethodToMonitorWithAdditionalDataProvider("abc", Arrays.asList("def", "ghi"));

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message",
				is("Method {} processed {} item(s) (avg. {}/item) in {} (started: {}, ended: {}). Details: {}.")),
			hasProperty("arguments", hasItem("testMethodToMonitorWithAdditionalDataProvider")),
			hasProperty("arguments", hasItem(2)),
			hasProperty("arguments", hasItem(matchesRegex("\\d+ ms"))),
			hasProperty("arguments", hasItem(instanceOf(LocalDateTime.class))),
			hasProperty("arguments", hasItem("additional comment, abc")),
			hasProperty("level", is(Level.DEBUG))
		)));
	}

	/**
	 * Verifies that the performance data are correctly logged by aspect and a warning is logged for a method annotated
	 * with {@link AutoLogPerformance} configured to use an invalid {@link AdditionalDataProvider}, in a class not
	 * annotated with {@link AutoLogPerformance}.
	 *
	 * @see MethodPerformanceNotAnnotatedTestClass#testMethodToMonitorWithWrongAdditionalDataProvider()
	 */
	@Test
	void givenMethodWithWrongAdditionalDataProvider_whenLogPerformanceByAspect_generateLogWithoutAdditionalData() {
		proxyNotAnnotatedTestClass.testMethodToMonitorWithWrongAdditionalDataProvider();

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is("Method {} executed in {} (started: {}, ended: {}).")),
			hasProperty("arguments",
				hasItem("MethodPerformanceNotAnnotatedTestClass.testMethodToMonitorWithWrongAdditionalDataProvider")),
			hasProperty("arguments", hasItem(matchesRegex("\\d+ ms"))),
			hasProperty("arguments", hasItem(instanceOf(LocalDateTime.class))),
			hasProperty("level", is(Level.DEBUG))
		)));

		assertThat(stdOut.toString(), containsString("[WARN] Autolog: Unable to retrieve additional data through "
			+ "provider methodToMonitorWrongDataProvider for method "
			+ "testMethodToMonitorWithWrongAdditionalDataProvider:")
		);
	}

	/**
	 * Verifies that the performance data are correctly logged by aspect for a method annotated with
	 * {@link AutoLogPerformance} configured to logged the data in JSON, in a class not annotated with
	 * {@link AutoLogPerformance}.
	 *
	 * @see MethodPerformanceNotAnnotatedTestClass#testMethodToMonitorJsonStructuredMessage()
	 */
	@Test
	void givenMethodAnnotatedToBeLoggedAsJson_whenLogPerformanceByAspect_generateExpectedLog() {
		proxyNotAnnotatedTestClass.testMethodToMonitorJsonStructuredMessage();

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", containsString("\"invokedMethod\":"
				+ "\"MethodPerformanceNotAnnotatedTestClass.testMethodToMonitorJsonStructuredMessage\"")),
			hasProperty("message", matchesRegex(".+\\\"executionTimeInMs\\\":\\d+.+")),
			hasProperty("message", matchesRegex(".+\\\"executionTime\\\":\\\"\\d+ ms\\\".+")),
			hasProperty("message", containsString("\"startTime\":")),
			hasProperty("message", containsString("\"endTime\":")),
			hasProperty("level", is(Level.DEBUG))
		)));
	}

	/**
	 * Verifies that the performance data are correctly logged by aspect for a method annotated with
	 * {@link AutoLogPerformance} configured to logged the data in XML, in a class not annotated with
	 * {@link AutoLogPerformance}.
	 *
	 * @see MethodPerformanceNotAnnotatedTestClass#testMethodToMonitorXmlStructuredMessage()
	 */
	@Test
	void givenMethodAnnotatedToBeLoggedAsXml_whenLogPerformanceByAspect_generateExpectedLog() {
		proxyNotAnnotatedTestClass.testMethodToMonitorXmlStructuredMessage();

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", matchesRegex("<MethodPerformanceLogEntry>.+</MethodPerformanceLogEntry>$")),
			hasProperty("message", containsString("<invokedMethod>"
				+ "MethodPerformanceNotAnnotatedTestClass.testMethodToMonitorXmlStructuredMessage</invokedMethod>")),
			hasProperty("message", matchesRegex(".+<executionTimeInMs>\\d+</executionTimeInMs>.+")),
			hasProperty("message", matchesRegex(".+<executionTime>\\d+ ms</executionTime>.+")),
			hasProperty("message", matchesRegex(".+<startTime>.+</startTime>.+")),
			hasProperty("message", matchesRegex(".+<endTime>.+</endTime>.+")),
			hasProperty("level", is(Level.DEBUG))
		)));
	}

	/**
	 * Verifies that the performance data are correctly logged by aspect for a method in a class declaring API endpoints
	 * annotated with {@link AutoLogPerformance} using the default configuration.
	 *
	 * @param apiTestClass	The instance of the class declaring the API endpoints.
	 * @throws NoSuchMethodException if the method {@code testApiBadEndpointMethod()} is not found in the tested class.
	 * @throws InvocationTargetException if the method {@code testApiBadEndpointMethod()} has thrown an exception.
	 * @throws IllegalAccessException if the method {@code testApiBadEndpointMethod()} cannot be executed.
	 * @see JaxRsApiTestClass#testApiBadEndpointMethod()
	 * @see SpringWebApiTestClass#testApiBadEndpointMethod()
	 * @see #provideApiTestClasses()
	 */
	@ParameterizedTest
	@MethodSource("provideApiTestClasses")
	void givenBadApiEndpointMethod_whenLogPerformanceByAspect_generateExpectedLog(final Object apiTestClass)
		throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		apiTestClass.getClass().getMethod("testApiBadEndpointMethod").invoke(apiTestClass);

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is("Method {} executed in {} (started: {}, ended: {}).")),
			hasProperty("arguments", hasItem("/apiTest")),
			hasProperty("arguments", hasItem(matchesRegex("\\d+ ms"))),
			hasProperty("arguments", hasItem(instanceOf(LocalDateTime.class))),
			hasProperty("level", is(Level.DEBUG))
		)));
	}

	/**
	 * Verifies that the performance data are correctly logged by aspect for a method annotated with
	 * {@link AutoLogPerformance} configured to not automatically detect API endpoints, in a class declaring API
	 * endpoints annotated with {@link AutoLogPerformance} using the default configuration.
	 *
	 * @param apiTestClass	The instance of the class declaring the API endpoints.
	 * @throws NoSuchMethodException if the method {@code testApiEndpointMethodNoAutoconfig()} is not found in the
	 * 								 tested class.
	 * @throws InvocationTargetException if the method {@code testApiEndpointMethodNoAutoconfig()} has thrown an
	 * 									 exception.
	 * @throws IllegalAccessException if the method {@code testApiEndpointMethodNoAutoconfig()} cannot be executed.
	 * @see JaxRsApiTestClass#testApiEndpointMethodNoAutoconfig()
	 * @see SpringWebApiTestClass#testApiEndpointMethodNoAutoconfig()
	 * @see #provideApiTestClasses()
	 */
	@ParameterizedTest
	@MethodSource("provideApiTestClasses")
	void givenApiEndpointMethodNoAutoconfig_whenLogPerformanceByAspect_generateExpectedLog(final Object apiTestClass)
		throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		apiTestClass.getClass().getMethod("testApiEndpointMethodNoAutoconfig").invoke(apiTestClass);

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is("Method {} executed in {} (started: {}, ended: {}).")),
			hasProperty("arguments", hasItem("testApiEndpointMethodNoAutoconfig")),
			hasProperty("arguments", hasItem(matchesRegex("\\d+ ms"))),
			hasProperty("arguments", hasItem(instanceOf(LocalDateTime.class))),
			hasProperty("level", is(Level.DEBUG))
		)));
	}

	/**
	 * Verifies that the performance data are correctly logged by aspect for a method corresponding to an API endpoint,
	 * in a class annotated with {@link AutoLogPerformance} using the default configuration.
	 *
	 * @param apiTestClass	The instance of the class declaring the API endpoints.
	 * @param httpMethod	The HTTP method designating the API endpoint to test.
	 * @throws NoSuchMethodException if the method {@code testApi[httpMethod]EndpointMethod()} is not found in the
	 * 								 tested class.
	 * @throws InvocationTargetException if the method {@code testApi[httpMethod]EndpointMethod()} has thrown an
	 * 									 exception.
	 * @throws IllegalAccessException if the method {@code testApi[httpMethod]EndpointMethod()} cannot be executed.
	 * @see JaxRsApiTestClass
	 * @see SpringWebApiTestClass
	 * @see #provideApiTestClassesAndHttpMethods()
	 */
	@ParameterizedTest
	@MethodSource("provideApiTestClassesAndHttpMethods")
	void givenApiEndpointMethod_whenLogPerformanceByAspect_generateExpectedLog(final Object apiTestClass,
																			   final String httpMethod)
		throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		apiTestClass.getClass().getMethod("testApi" + httpMethod + "EndpointMethod").invoke(apiTestClass);

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is("Method {} executed in {} (started: {}, ended: {}).")),
			hasProperty("arguments", hasItem("[" + httpMethod + "] /apiTest/test")),
			hasProperty("arguments", hasItem(matchesRegex("\\d+ ms"))),
			hasProperty("arguments", hasItem(instanceOf(LocalDateTime.class))),
			hasProperty("level", is(Level.DEBUG))
		)));
	}
}
