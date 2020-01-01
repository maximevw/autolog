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
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import uk.org.lidalia.slf4jext.Level;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import java.lang.reflect.Method;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

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
	 * Verifies that logging output data with a null configuration throws a {@link NullPointerException}.
	 *
	 * @see MethodCallLogger#logMethodOutput(MethodOutputLoggingConfiguration, Method, Object)
	 */
	@Test
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
	 * @see MethodCallLogger#logMethodOutput(MethodOutputLoggingConfiguration, String, Object)
	 */
	@Test
	@SuppressWarnings("ConstantConditions")
	void givenNullConfiguration_whenLogMethodOutputByName_throwsException() {
		assertThrows(NullPointerException.class, () -> sut.logMethodOutput(null, "noOp", null));
	}

	/**
	 * Verifies that logging output data with a null method name throws a {@link NullPointerException}.
	 *
	 * @see MethodCallLogger#logMethodOutput(MethodOutputLoggingConfiguration, String, Object)
	 */
	@Test
	void givenNullMethodName_whenLogMethodOutputByName_throwsException() {
		assertThrows(NullPointerException.class, () ->
			sut.logMethodOutput(MethodOutputLoggingConfiguration.builder().build(), (String) null, null));
	}

	/**
	 * Verifies that logging output data with a null configuration throws a {@link NullPointerException}.
	 *
	 * @see MethodCallLogger#logMethodOutput(MethodOutputLoggingConfiguration, String)
	 */
	@Test
	@SuppressWarnings("ConstantConditions")
	void givenNullConfiguration_whenLogMethodExitingByName_throwsException() {
		assertThrows(NullPointerException.class, () -> sut.logMethodOutput(null, "noOp"));
	}

	/**
	 * Verifies that logging output data with a null method name throws a {@link NullPointerException}.
	 *
	 * @see MethodCallLogger#logMethodOutput(MethodOutputLoggingConfiguration, String)
	 */
	@Test
	void givenNullMethodName_whenLogMethodExitingByName_throwsException() {
		assertThrows(NullPointerException.class, () ->
			sut.logMethodOutput(MethodOutputLoggingConfiguration.builder().build(), null));
	}

	/**
	 * Verifies the effective logging of the end of invocation of a method without returned value.
	 *
	 * @throws NoSuchMethodException if method {@link LogTestingClass#methodReturningVoid()} is not defined.
	 * @see MethodCallLogger#logMethodOutput(MethodOutputLoggingConfiguration, String, Object)
	 */
	@Test
	void givenMethodReturningVoid_whenLogMethodOutput_generatesLog() throws NoSuchMethodException {
		sut.logMethodOutput(MethodOutputLoggingConfiguration.builder().build(),
			LogTestingClass.class.getMethod("methodReturningVoid"), null);

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(AutoLogMethodInOut.VOID_OUTPUT_DEFAULT_MESSAGE_TEMPLATE)),
			hasProperty("arguments", hasItem("LogTestingClass.methodReturningVoid")),
			hasProperty("level", is(Level.INFO))
		)));
	}

	/**
	 * Verifies the effective logging of the end of invocation of a method without returned value as a structured
	 * message.
	 *
	 * @throws NoSuchMethodException if method {@link LogTestingClass#methodReturningVoid()} is not defined.
	 * @see MethodCallLogger#logMethodOutput(MethodOutputLoggingConfiguration, String, Object)
	 */
	@Test
	void givenMethodReturningVoid_whenLogMethodOutputAsStrucutredMessage_generatesLog() throws NoSuchMethodException {
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
	 * @throws NoSuchMethodException if method {@link LogTestingClass#methodOutputData()} is not defined.
	 * @see MethodCallLogger#logMethodOutput(MethodOutputLoggingConfiguration, String, Object)
	 */
	@Test
	void givenMethod_whenLogMethodOutput_generatesLog() throws NoSuchMethodException {
		sut.logMethodOutput(MethodOutputLoggingConfiguration.builder().build(),
			LogTestingClass.class.getMethod("methodOutputData"), "test");

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(AutoLogMethodInOut.OUTPUT_DEFAULT_MESSAGE_TEMPLATE)),
			hasProperty("arguments", hasItem("LogTestingClass.methodOutputData")),
			hasProperty("arguments", hasItem("\"test\"")),
			hasProperty("level", is(Level.INFO))
		)));
	}

	/**
	 * Verifies the effective logging of output data for a given method as a structured message.
	 *
	 * @throws NoSuchMethodException if method {@link LogTestingClass#methodOutputData()} is not defined.
	 * @see MethodCallLogger#logMethodOutput(MethodOutputLoggingConfiguration, String, Object)
	 */
	@Test
	void givenMethod_whenLogMethodOutputAsStrucutredMessage_generatesLog() throws NoSuchMethodException {
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

}
