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

import com.github.maximevw.autolog.core.annotations.AutoLogMethodInOut;
import com.github.maximevw.autolog.core.annotations.AutoLogMethodInput;
import com.github.maximevw.autolog.core.annotations.AutoLogMethodOutput;
import com.github.maximevw.autolog.core.configuration.MethodOutputLoggingConfiguration;
import com.github.maximevw.autolog.core.logger.LoggerManager;
import com.github.maximevw.autolog.core.logger.adapters.Slf4jAdapter;
import com.github.maximevw.autolog.test.MethodInOutAnnotatedTestClass;
import com.github.maximevw.autolog.test.MethodInOutNotAnnotatedTestClass;
import com.github.maximevw.autolog.test.MethodInputAnnotatedTestClass;
import com.github.maximevw.autolog.test.MethodOutputAnnotatedTestClass;
import com.github.maximevw.autolog.test.TestObject;
import org.apache.commons.lang3.StringUtils;
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

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.core.IsIterableContaining.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Unit tests for the aspect class {@link AutoLogMethodInOutSpringAspect}.
 */
class AutoLogMethodInOutSpringAspectTest {

	private static TestLogger logger;

	// Proxies on which the test are run.
	private static MethodInOutAnnotatedTestClass proxyAnnotatedTestClass;
	private static MethodInOutNotAnnotatedTestClass proxyNotAnnotatedTestClass;
	private static MethodInputAnnotatedTestClass proxyInputAnnotatedTestClass;
	private static MethodOutputAnnotatedTestClass proxyOutputAnnotatedTestClass;

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
		// Setup aspect.
		final AutoLogMethodInOutSpringAspect sut =
			new AutoLogMethodInOutSpringAspect(new LoggerManager().register(Slf4jAdapter.getInstance()));

		final MethodInOutAnnotatedTestClass annotatedTestClassTarget = new MethodInOutAnnotatedTestClass();
		final AspectJProxyFactory annotatedTestClassFactory = new AspectJProxyFactory(annotatedTestClassTarget);
		annotatedTestClassFactory.addAspect(sut);
		proxyAnnotatedTestClass = annotatedTestClassFactory.getProxy();

		final MethodInOutNotAnnotatedTestClass notAnnotatedTestClassTarget = new MethodInOutNotAnnotatedTestClass();
		final AspectJProxyFactory notAnnotatedTestClassFactory = new AspectJProxyFactory(notAnnotatedTestClassTarget);
		notAnnotatedTestClassFactory.addAspect(sut);
		proxyNotAnnotatedTestClass = notAnnotatedTestClassFactory.getProxy();

		final MethodInputAnnotatedTestClass inputAnnotatedTestClassTarget = new MethodInputAnnotatedTestClass();
		final AspectJProxyFactory inputAnnotatedTestClassFactory =
			new AspectJProxyFactory(inputAnnotatedTestClassTarget);
		inputAnnotatedTestClassFactory.addAspect(sut);
		proxyInputAnnotatedTestClass = inputAnnotatedTestClassFactory.getProxy();

		final MethodOutputAnnotatedTestClass outputAnnotatedTestClassTarget = new MethodOutputAnnotatedTestClass();
		final AspectJProxyFactory outputAnnotatedTestClassFactory =
			new AspectJProxyFactory(outputAnnotatedTestClassTarget);
		outputAnnotatedTestClassFactory.addAspect(sut);
		proxyOutputAnnotatedTestClass = outputAnnotatedTestClassFactory.getProxy();
	}

	/**
	 * Initializes the test logger before each new test case.
	 */
	@BeforeEach
	void initLoggers() {
		logger = TestLoggerFactory.getTestLogger("Autolog");
		TestLoggerFactory.getInstance().setPrintLevel(Level.TRACE);
	}

	/**
	 * Clears the test logger after each new test case.
	 */
	@AfterEach
	void clearLoggers() {
		TestLoggerFactory.clear();
	}

	/**
	 * Builds arguments for the parameterized tests relative to the usage of other logger names than the default one:
	 * {@link #givenMethodAnnotatedToNotUseDefaultLoggerName_whenLogDataInOutByAspect_generateExpectedLog(String,
	 * String)}.
	 * <p>
	 *     It provides the name of the method from {@link MethodInOutNotAnnotatedTestClass} to call during the test and
	 *     the logger name which is expected to be used.
	 * </p>
	 *
	 * @return The arguments required by the parameterized tests.
	 */
	private static Stream<Arguments> provideMethodAnnotatedToNotUseDefaultLoggerName() {
		final String customLoggerName = "custom-logger";
		final String callerClassLoggerName = "com.github.maximevw.autolog.test.MethodInOutNotAnnotatedTestClass";
		return Stream.of(
			Arguments.of("testMethodWithCustomLogger", customLoggerName),
			Arguments.of("testTwiceAnnotatedMethodWithCustomLogger", customLoggerName),
			Arguments.of("testMethodWithCallerClassLogger", callerClassLoggerName),
			Arguments.of("testTwiceAnnotatedMethodWithCallerClassLogger", callerClassLoggerName)
		);
	}

	/**
	 * Verifies that the input data and the end of method invocation are correctly logged by aspect for a method without
	 * returned value in a class annotated with {@link AutoLogMethodInOut} using the default configuration.
	 *
	 * @see MethodInOutAnnotatedTestClass#testMethodReturningVoid(String, int)
	 */
	@Test
	void givenMethodReturningVoid_whenLogDataInOutByAspect_generateExpectedLog() {
		final String executedMethod = "MethodInOutAnnotatedTestClass.testMethodReturningVoid";

		proxyAnnotatedTestClass.testMethodReturningVoid("abc", 123);

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(AutoLogMethodInOut.INPUT_DEFAULT_MESSAGE_TEMPLATE)),
			hasProperty("arguments", hasItem(executedMethod)),
			hasProperty("arguments", hasItem("strArg=abc, intArg=123")),
			hasProperty("level", is(Level.INFO))
		)));

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(AutoLogMethodInOut.VOID_OUTPUT_DEFAULT_MESSAGE_TEMPLATE)),
			hasProperty("arguments", hasItem(executedMethod)),
			hasProperty("level", is(Level.INFO))
		)));
	}

	/**
	 * Verifies that the input and output data are correctly logged by aspect for a method returning a String in a class
	 * annotated with {@link AutoLogMethodInOut} using the default configuration.
	 *
	 * @see MethodInOutAnnotatedTestClass#testMethodReturningString(String, int)
	 */
	@Test
	void givenMethodReturningString_whenLogDataInOutByAspect_generateExpectedLog() {
		final String executedMethod = "MethodInOutAnnotatedTestClass.testMethodReturningString";

		proxyAnnotatedTestClass.testMethodReturningString("abc", 123);

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(AutoLogMethodInOut.INPUT_DEFAULT_MESSAGE_TEMPLATE)),
			hasProperty("arguments", hasItem(executedMethod)),
			hasProperty("arguments", hasItem("strArg=abc, intArg=123")),
			hasProperty("level", is(Level.INFO))
		)));

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(AutoLogMethodInOut.OUTPUT_DEFAULT_MESSAGE_TEMPLATE)),
			hasProperty("arguments", hasItem(executedMethod)),
			hasProperty("arguments", hasItem("abc123")),
			hasProperty("level", is(Level.INFO))
		)));
	}

	/**
	 * Verifies that the input data are correctly logged by aspect and that the error message is not for a method
	 * throwing an exception in a class annotated with {@link AutoLogMethodInOut} using the default configuration.
	 *
	 * @see MethodInOutAnnotatedTestClass#testMethodThrowingExceptionMustNotBeLogged(String)
	 */
	@Test
	void givenMethodThrowingExceptionMustNotBeLogged_whenLogDataInOutByAspect_generateExpectedLog() {
		final String executedMethod = "MethodInOutAnnotatedTestClass.testMethodThrowingExceptionMustNotBeLogged";

		final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
			() -> proxyAnnotatedTestClass.testMethodThrowingExceptionMustNotBeLogged("abc"));
		assertEquals("Wrong value: abc", illegalArgumentException.getMessage());

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(AutoLogMethodInOut.INPUT_DEFAULT_MESSAGE_TEMPLATE)),
			hasProperty("arguments", hasItem(executedMethod)),
			hasProperty("arguments", hasItem("strArg=abc")),
			hasProperty("level", is(Level.INFO))
		)));

		assertThat(logger.getLoggingEvents(), not(hasItem(allOf(
			hasProperty("message", is(MethodOutputLoggingConfiguration.THROWABLE_MESSAGE_TEMPLATE))
		))));
	}

	/**
	 * Verifies that the input data and the error message are correctly logged by aspect for a method throwing an
	 * exception, annotated with {@link AutoLogMethodInOut} configured to log {@link Throwable}, in a class annotated
	 * with {@link AutoLogMethodInOut} using the default configuration.
	 *
	 * @see MethodInOutAnnotatedTestClass#testMethodThrowingException(String)
	 */
	@Test
	void givenMethodThrowingException_whenLogDataInOutByAspect_generateExpectedLog() {
		final String executedMethod = "MethodInOutAnnotatedTestClass.testMethodThrowingException";

		final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
			() -> proxyAnnotatedTestClass.testMethodThrowingException("abc"));
		assertEquals("Wrong value: abc", illegalArgumentException.getMessage());

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(AutoLogMethodInOut.INPUT_DEFAULT_MESSAGE_TEMPLATE)),
			hasProperty("arguments", hasItem(executedMethod)),
			hasProperty("arguments", hasItem("strArg=abc")),
			hasProperty("level", is(Level.DEBUG))
		)));

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(MethodOutputLoggingConfiguration.THROWABLE_MESSAGE_TEMPLATE)),
			hasProperty("arguments", hasItem(IllegalArgumentException.class.getName())),
			hasProperty("arguments", hasItem("Wrong value: abc")),
			hasProperty("level", is(Level.ERROR))
		)));
	}

	/**
	 * Verifies that the input and output data are correctly logged by aspect for a method returning a list, annotated
	 * with {@link AutoLogMethodInOut} configured to log the expanded collections, in a class not annotated with
	 * {@link AutoLogMethodInOut}.
	 *
	 * @see MethodInOutNotAnnotatedTestClass#testMethodReturningList(Map)
	 */
	@Test
	void givenMethodReturningList_whenLogDataInOutByAspect_generateExpectedLog() {
		final String executedMethod = "testMethodReturningList";

		final Map<String, String> inMap = Map.of("key0", "val0", "key1", "val1");
		final List<String> res = proxyNotAnnotatedTestClass.testMethodReturningList(inMap);

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(AutoLogMethodInOut.INPUT_DEFAULT_MESSAGE_TEMPLATE)),
			hasProperty("arguments", hasItem(executedMethod)),
			hasProperty("arguments", hasItem("mapArg=2 key-value pair(s): " + inMap.toString())),
			hasProperty("level", is(Level.INFO))
		)));

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(AutoLogMethodInOut.OUTPUT_DEFAULT_MESSAGE_TEMPLATE)),
			hasProperty("arguments", hasItem(executedMethod)),
			hasProperty("arguments", hasItem("2 item(s): " + res.toString())),
			hasProperty("level", is(Level.INFO))
		)));
	}

	/**
	 * Verifies that the input and output data are correctly logged by aspect for a method returning an Object,
	 * annotated with {@link AutoLogMethodInOut} configured to log the output value in JSON, in a class not annotated
	 * with {@link AutoLogMethodInOut}.
	 *
	 * @see MethodInOutNotAnnotatedTestClass#testMethodReturningObject()
	 */
	@Test
	void givenMethodReturningObject_whenLogDataInOutByAspect_generateExpectedLog() {
		final String executedMethod = "MethodInOutNotAnnotatedTestClass.testMethodReturningObject";
		proxyNotAnnotatedTestClass.testMethodReturningObject();

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(AutoLogMethodInOut.INPUT_DEFAULT_MESSAGE_TEMPLATE)),
			hasProperty("arguments", hasItem(executedMethod)),
			hasProperty("arguments", hasItem(StringUtils.EMPTY)),
			hasProperty("level", is(Level.INFO))
		)));

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(AutoLogMethodInOut.OUTPUT_DEFAULT_MESSAGE_TEMPLATE)),
			hasProperty("arguments", hasItem(executedMethod)),
			hasProperty("arguments", hasItem("{\"fieldStr\":\"abc\",\"fieldDbl\":123.45}")),
			hasProperty("level", is(Level.INFO))
		)));
	}

	/**
	 * Verifies that the input and output data are correctly logged by aspect for a method returning an Object,
	 * annotated with {@link AutoLogMethodInOut} configured to log the input arguments and output value in XML, in a
	 * class not annotated with {@link AutoLogMethodInOut}.
	 *
	 * @see MethodInOutNotAnnotatedTestClass#testMethodReturningObjectLoggedAsXml(TestObject)
	 */
	@Test
	void givenMethodReturningObjectLoggedAsXml_whenLogDataInOutByAspect_generateExpectedLog() {
		final String executedMethod = "MethodInOutNotAnnotatedTestClass.testMethodReturningObjectLoggedAsXml";
		proxyNotAnnotatedTestClass.testMethodReturningObjectLoggedAsXml(new TestObject("abc", 123.45));

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(AutoLogMethodInOut.INPUT_DEFAULT_MESSAGE_TEMPLATE)),
			hasProperty("arguments", hasItem(executedMethod)),
			hasProperty("arguments", hasItem("objArg=<TestObject><fieldStr>abc</fieldStr><fieldDbl>123.45</fieldDbl>"
				+ "</TestObject>")),
			hasProperty("level", is(Level.INFO))
		)));

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(AutoLogMethodInOut.OUTPUT_DEFAULT_MESSAGE_TEMPLATE)),
			hasProperty("arguments", hasItem(executedMethod)),
			hasProperty("arguments", hasItem("<TestObject><fieldStr>abc</fieldStr><fieldDbl>123.45</fieldDbl>"
				+ "</TestObject>")),
			hasProperty("level", is(Level.INFO))
		)));
	}

	/**
	 * Verifies that the input and output data are correctly logged by aspect for a method returning a String,
	 * annotated with {@link AutoLogMethodInOut} configured to log the input arguments in XML, in a class not annotated
	 * with {@link AutoLogMethodInOut}.
	 *
	 * @see MethodInOutNotAnnotatedTestClass#testMethodInputLoggedAsJson(String, int, TestObject)
	 */
	@Test
	void givenMethodInputLoggedAsJson_whenLogDataInOutByAspect_generateExpectedLog() {
		final String executedMethod = "MethodInOutNotAnnotatedTestClass.testMethodInputLoggedAsJson";
		final String res = proxyNotAnnotatedTestClass.testMethodInputLoggedAsJson("abc", 123,
			new TestObject("def", 456.78));

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(AutoLogMethodInOut.INPUT_DEFAULT_MESSAGE_TEMPLATE)),
			hasProperty("arguments", hasItem(executedMethod)),
			hasProperty("arguments", hasItem("strArg=\"abc\", intArg=123, objArg={\"fieldStr\":\"def\","
				+ "\"fieldDbl\":456.78}")),
			hasProperty("level", is(Level.INFO))
		)));

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(AutoLogMethodInOut.OUTPUT_DEFAULT_MESSAGE_TEMPLATE)),
			hasProperty("arguments", hasItem(executedMethod)),
			hasProperty("arguments", hasItem(res)),
			hasProperty("level", is(Level.INFO))
		)));
	}

	/**
	 * Verifies that the input data and the end of method invocation are correctly logged by aspect for a method without
	 * returned value, annotated with {@link AutoLogMethodInOut} configured to log only some of the input arguments in
	 * JSON, in a class not annotated with {@link AutoLogMethodInOut}.
	 *
	 * @see MethodInOutNotAnnotatedTestClass#testMethodInputPartiallyLoggedAsJson(TestObject, TestObject)
	 */
	@Test
	void givenMethodInputPartiallyLoggedAsJson_whenLogDataInOutByAspect_generateExpectedLog() {
		final String executedMethod = "MethodInOutNotAnnotatedTestClass.testMethodInputPartiallyLoggedAsJson";
		final TestObject testObject0 = new TestObject("abc", 123.45);
		final TestObject testObject1 = new TestObject("def", 678.9);
		proxyNotAnnotatedTestClass.testMethodInputPartiallyLoggedAsJson(testObject0, testObject1);

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(AutoLogMethodInOut.INPUT_DEFAULT_MESSAGE_TEMPLATE)),
			hasProperty("arguments", hasItem(executedMethod)),
			hasProperty("arguments", hasItem("objArg0={\"fieldStr\":\"abc\",\"fieldDbl\":123.45}, objArg1="
				+ testObject1.toString())),
			hasProperty("level", is(Level.INFO))
		)));

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(AutoLogMethodInOut.VOID_OUTPUT_DEFAULT_MESSAGE_TEMPLATE)),
			hasProperty("arguments", hasItem(executedMethod)),
			hasProperty("level", is(Level.INFO))
		)));
	}

	/**
	 * Verifies that the input and output data are correctly logged by aspect for a method returning a String, annotated
	 * with {@link AutoLogMethodInOut} configured to restrict the logged input arguments, in a class not annotated with
	 * {@link AutoLogMethodInOut}.
	 *
	 * @see MethodInOutNotAnnotatedTestClass#testMethodWithLoggingRestrictions(String, int, boolean)
	 */
	@Test
	void givenMethodWithLoggingRestrictions_whenLogDataInOutByAspect_generateExpectedLog() {
		final String executedMethod = "MethodInOutNotAnnotatedTestClass.testMethodWithLoggingRestrictions";
		final String res = proxyNotAnnotatedTestClass.testMethodWithLoggingRestrictions("abc", 123, false);

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(AutoLogMethodInOut.INPUT_DEFAULT_MESSAGE_TEMPLATE)),
			hasProperty("arguments", hasItem(executedMethod)),
			hasProperty("arguments", hasItem("strArg=abc")),
			hasProperty("level", is(Level.INFO))
		)));

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(AutoLogMethodInOut.OUTPUT_DEFAULT_MESSAGE_TEMPLATE)),
			hasProperty("arguments", hasItem(executedMethod)),
			hasProperty("arguments", hasItem(res)),
			hasProperty("level", is(Level.INFO))
		)));
	}

	/**
	 * Verifies that the input and output data are correctly logged by aspect for a method returning a String, annotated
	 * with {@link AutoLogMethodInOut} configured to exclude some input arguments from log, in a class not annotated
	 * with {@link AutoLogMethodInOut}.
	 *
	 * @see MethodInOutNotAnnotatedTestClass#testMethodWithLoggingExclusions(String, int, boolean)
	 */
	@Test
	void givenMethodWithLoggingExclusions_whenLogDataInOutByAspect_generateExpectedLog() {
		final String executedMethod = "MethodInOutNotAnnotatedTestClass.testMethodWithLoggingExclusions";
		final String res = proxyNotAnnotatedTestClass.testMethodWithLoggingExclusions("abc", 123, false);

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(AutoLogMethodInOut.INPUT_DEFAULT_MESSAGE_TEMPLATE)),
			hasProperty("arguments", hasItem(executedMethod)),
			hasProperty("arguments", hasItem("intArg=123, boolArg=false")),
			hasProperty("level", is(Level.INFO))
		)));

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(AutoLogMethodInOut.OUTPUT_DEFAULT_MESSAGE_TEMPLATE)),
			hasProperty("arguments", hasItem(executedMethod)),
			hasProperty("arguments", hasItem(res)),
			hasProperty("level", is(Level.INFO))
		)));
	}

	/**
	 * Verifies that the input and output data are correctly logged by aspect for a method returning a String, annotated
	 * with {@link AutoLogMethodInput} and {@link AutoLogMethodOutput} using the default configuration, in a class
	 * annotated with {@link AutoLogMethodInOut} using the default configuration.
	 *
	 * @see MethodInOutAnnotatedTestClass#testTwiceAnnotatedMethodReturningString(String, int)
	 */
	@Test
	void givenMethodReturningStringInputAndOutputAnnotated_whenLogDataInOutByAspect_generateExpectedLog() {
		final String executedMethod = "MethodInOutAnnotatedTestClass.testTwiceAnnotatedMethodReturningString";

		proxyAnnotatedTestClass.testTwiceAnnotatedMethodReturningString("abc", 123);

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(AutoLogMethodInOut.INPUT_DEFAULT_MESSAGE_TEMPLATE)),
			hasProperty("arguments", hasItem(executedMethod)),
			hasProperty("arguments", hasItem("strArg=abc, intArg=123")),
			hasProperty("level", is(Level.TRACE))
		)));

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(AutoLogMethodInOut.OUTPUT_DEFAULT_MESSAGE_TEMPLATE)),
			hasProperty("arguments", hasItem(executedMethod)),
			hasProperty("arguments", hasItem("\"abc123\"")),
			hasProperty("level", is(Level.TRACE))
		)));
	}

	/**
	 * Verifies that the input and output data are correctly logged by aspect for a method returning a String, annotated
	 * with {@link AutoLogMethodInput} and {@link AutoLogMethodOutput} using the default configuration, in a class not
	 * annotated with {@link AutoLogMethodInOut}.
	 *
	 * @see MethodInOutNotAnnotatedTestClass#testTwiceAnnotatedMethodReturningString(String, int)
	 */
	@Test
	void givenMethodReturningStringOnlyInputAndOutputAnnotated_whenLogDataInOutByAspect_generateExpectedLog() {
		final String executedMethod = "MethodInOutNotAnnotatedTestClass.testTwiceAnnotatedMethodReturningString";

		proxyNotAnnotatedTestClass.testTwiceAnnotatedMethodReturningString("abc", 123);

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(AutoLogMethodInOut.INPUT_DEFAULT_MESSAGE_TEMPLATE)),
			hasProperty("arguments", hasItem(executedMethod)),
			hasProperty("arguments", hasItem("strArg=abc, intArg=123")),
			hasProperty("level", is(Level.INFO))
		)));

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(AutoLogMethodInOut.OUTPUT_DEFAULT_MESSAGE_TEMPLATE)),
			hasProperty("arguments", hasItem(executedMethod)),
			hasProperty("arguments", hasItem("\"abc123\"")),
			hasProperty("level", is(Level.INFO))
		)));
	}

	/**
	 * Verifies that the input and output data are correctly logged by aspect for a method returning a String, annotated
	 * with {@link AutoLogMethodInput} and {@link AutoLogMethodOutput} configured to log at debug level and also with
	 * {@link AutoLogMethodInOut} using the default configuration, in a class not annotated with
	 * {@link AutoLogMethodInOut}.
	 *
	 * @see MethodInOutNotAnnotatedTestClass#testThriceAnnotatedMethodReturningString(String, int)
	 */
	@Test
	void givenMethodReturningStringWithThreeAnnotations_whenLogDataInOutByAspect_generateExpectedLog() {
		final String executedMethod = "MethodInOutNotAnnotatedTestClass.testThriceAnnotatedMethodReturningString";

		proxyNotAnnotatedTestClass.testThriceAnnotatedMethodReturningString("abc", 123);

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(AutoLogMethodInOut.INPUT_DEFAULT_MESSAGE_TEMPLATE)),
			hasProperty("arguments", hasItem(executedMethod)),
			hasProperty("arguments", hasItem("strArg=abc, intArg=123")),
			hasProperty("level", is(Level.INFO))
		)));

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(AutoLogMethodInOut.OUTPUT_DEFAULT_MESSAGE_TEMPLATE)),
			hasProperty("arguments", hasItem(executedMethod)),
			hasProperty("arguments", hasItem("abc123")),
			hasProperty("level", is(Level.INFO))
		)));
	}

	/**
	 * Verifies that the input data are correctly logged by aspect for a method without returned value in a class
	 * annotated with {@link AutoLogMethodInput} using the default configuration.
	 *
	 * @see MethodInputAnnotatedTestClass#testNotAnnotatedMethodReturningVoid(String, int)
	 */
	@Test
	void givenNotAnnotatedMethodReturningVoid_whenLogDataInputByAspect_generateExpectedLog() {
		final String executedMethod = "MethodInputAnnotatedTestClass.testNotAnnotatedMethodReturningVoid";

		proxyInputAnnotatedTestClass.testNotAnnotatedMethodReturningVoid("abc", 123);

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(AutoLogMethodInOut.INPUT_DEFAULT_MESSAGE_TEMPLATE)),
			hasProperty("arguments", hasItem(executedMethod)),
			hasProperty("arguments", hasItem("strArg=abc, intArg=123")),
			hasProperty("level", is(Level.INFO))
		)));
	}

	/**
	 * Verifies that the input data are correctly logged by aspect for a method without returned value, annotated with
	 * {@link AutoLogMethodInput} configured to log at debug level in a class annotated with {@link AutoLogMethodInput}
	 * using the default configuration.
	 *
	 * @see MethodInputAnnotatedTestClass#testAnnotatedMethodReturningVoid(String, int)
	 */
	@Test
	void givenAnnotatedMethodReturningVoid_whenLogDataInputByAspect_generateExpectedLog() {
		final String executedMethod = "MethodInputAnnotatedTestClass.testAnnotatedMethodReturningVoid";

		proxyInputAnnotatedTestClass.testAnnotatedMethodReturningVoid("abc", 123);

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(AutoLogMethodInOut.INPUT_DEFAULT_MESSAGE_TEMPLATE)),
			hasProperty("arguments", hasItem(executedMethod)),
			hasProperty("arguments", hasItem("strArg=abc, intArg=123")),
			hasProperty("level", is(Level.DEBUG))
		)));
	}

	/**
	 * Verifies that the output data are correctly logged by aspect for a method returning a String in a class
	 * annotated with {@link AutoLogMethodOutput} using the default configuration.
	 *
	 * @see MethodOutputAnnotatedTestClass#testNotAnnotatedMethodReturningString(String, int)
	 */
	@Test
	void givenNotAnnotatedMethodReturningString_whenLogOutputByAspect_generateExpectedLog() {
		final String executedMethod = "MethodOutputAnnotatedTestClass.testNotAnnotatedMethodReturningString";

		proxyOutputAnnotatedTestClass.testNotAnnotatedMethodReturningString("abc", 123);

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(AutoLogMethodInOut.OUTPUT_DEFAULT_MESSAGE_TEMPLATE)),
			hasProperty("arguments", hasItem(executedMethod)),
			hasProperty("arguments", hasItem("\"abc123\"")),
			hasProperty("level", is(Level.INFO))
		)));
	}

	/**
	 * Verifies that the output data are correctly logged by aspect for a method returning a String, annotated with
	 * {@link AutoLogMethodOutput} configured to log at debug level in a class annotated with
	 * {@link AutoLogMethodOutput} using the default configuration.
	 *
	 * @see MethodOutputAnnotatedTestClass#testAnnotatedMethodReturningString(String, int)
	 */
	@Test
	void givenAnnotatedMethodReturningString_whenLogOutputByAspect_generateExpectedLog() {
		final String executedMethod = "MethodOutputAnnotatedTestClass.testAnnotatedMethodReturningString";

		proxyOutputAnnotatedTestClass.testAnnotatedMethodReturningString("abc", 123);

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(AutoLogMethodInOut.OUTPUT_DEFAULT_MESSAGE_TEMPLATE)),
			hasProperty("arguments", hasItem(executedMethod)),
			hasProperty("arguments", hasItem("\"abc123\"")),
			hasProperty("level", is(Level.DEBUG))
		)));
	}

	/**
	 * Verifies that the input data and the end of method invocation are correctly logged by aspect for a method
	 * annotated with {@link AutoLogMethodInOut}, {@link AutoLogMethodInput} or {@link AutoLogMethodOutput} and
	 * configured to not use the default logger name.
	 *
	 * @param executedMethodName 	The name of the method from {@link MethodInOutNotAnnotatedTestClass} to execute.
	 * @param targetLoggerName 		The logger name which should be used during the test.
	 * @see #provideMethodAnnotatedToNotUseDefaultLoggerName()
	 */
	@ParameterizedTest
	@MethodSource("provideMethodAnnotatedToNotUseDefaultLoggerName")
	void givenMethodAnnotatedToNotUseDefaultLoggerName_whenLogDataInOutByAspect_generateExpectedLog(
		final String executedMethodName, final String targetLoggerName) {
		final String prefixedMethodName = String.format("MethodInOutNotAnnotatedTestClass.%s", executedMethodName);
		final TestLogger targetLogger = TestLoggerFactory.getTestLogger(targetLoggerName);

		try {
			proxyNotAnnotatedTestClass.getClass().getMethod(executedMethodName).invoke(proxyNotAnnotatedTestClass);
		} catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			fail("Unable to execute the method " + executedMethodName, e);
		}

		assertThat(logger.getLoggingEvents(), is(empty()));
		assertThat(targetLogger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(AutoLogMethodInOut.INPUT_DEFAULT_MESSAGE_TEMPLATE)),
			hasProperty("arguments", hasItem(prefixedMethodName)),
			hasProperty("level", is(Level.INFO))
		)));

		assertThat(targetLogger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(AutoLogMethodInOut.VOID_OUTPUT_DEFAULT_MESSAGE_TEMPLATE)),
			hasProperty("arguments", hasItem(prefixedMethodName)),
			hasProperty("level", is(Level.INFO))
		)));
	}
}
