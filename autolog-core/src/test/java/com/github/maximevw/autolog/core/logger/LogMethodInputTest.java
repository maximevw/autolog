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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.maximevw.autolog.core.annotations.AutoLogMethodInOut;
import com.github.maximevw.autolog.core.configuration.MethodInputLoggingConfiguration;
import com.github.maximevw.autolog.core.configuration.PrettyDataFormat;
import com.github.maximevw.autolog.core.logger.adapters.Slf4jAdapter;
import com.github.maximevw.autolog.test.LogTestingClass;
import com.github.maximevw.autolog.test.TestObject;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import uk.org.lidalia.slf4jext.Level;
import uk.org.lidalia.slf4jtest.TestLogger;
import uk.org.lidalia.slf4jtest.TestLoggerFactory;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.emptyCollectionOf;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasProperty;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.stringContainsInOrder;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for the input data logging methods in the class {@link MethodCallLogger}.
 */
class LogMethodInputTest {

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
	 * Verifies that logging input data with a null configuration throws a {@link NullPointerException}.
	 *
	 * @see MethodCallLogger#logMethodInput(MethodInputLoggingConfiguration, Method, Object...)
	 */
	@Test
	void givenNullConfiguration_whenLogMethodInput_throwsException() {
		assertThrows(NullPointerException.class, () ->
			sut.logMethodInput(null, LogTestingClass.class.getMethod("noOp")));
	}

	/**
	 * Verifies that logging input data with a null method throws a {@link NullPointerException}.
	 *
	 * @see MethodCallLogger#logMethodInput(MethodInputLoggingConfiguration, Method, Object...)
	 */
	@Test
	@SuppressWarnings("ConstantConditions")
	void givenNullMethod_whenLogMethodInput_throwsException() {
		assertThrows(NullPointerException.class, () ->
			sut.logMethodInput(MethodInputLoggingConfiguration.builder().build(), null));
	}

	/**
	 * Verifies that logging input data with a null method name throws a {@link NullPointerException}.
	 *
	 * @see MethodCallLogger#logMethodInput(MethodInputLoggingConfiguration, String, List)
	 */
	@Test
	void givenNullMethodName_whenLogMethodInputByName_throwsException() {
		assertThrows(NullPointerException.class, () ->
			sut.logMethodInput(MethodInputLoggingConfiguration.builder().build(), null, Collections.emptyList()));
	}

	/**
	 * Verifies that logging input data with a null configuration throws a {@link NullPointerException}.
	 *
	 * @see MethodCallLogger#logMethodInput(MethodInputLoggingConfiguration, String, List)
	 */
	@Test
	@SuppressWarnings("ConstantConditions")
	void givenNullConfiguration_whenLogMethodInputByName_throwsException() {
		assertThrows(NullPointerException.class, () -> sut.logMethodInput(null, "noOp", Collections.emptyList()));
	}

	/**
	 * Verifies that logging input data with a null list of arguments name throws a {@link NullPointerException}.
	 *
	 * @see MethodCallLogger#logMethodInput(MethodInputLoggingConfiguration, String, List)
	 */
	@Test
	void givenNullArgumentsList_whenLogMethodInputByName_throwsException() {
		assertThrows(NullPointerException.class, () ->
			sut.logMethodInput(MethodInputLoggingConfiguration.builder().build(), "noOp", null));
	}

	/**
	 * Verifies the effective logging of the input data for a given method.
	 *
	 * @param configuration			The configuration to use.
	 * @param method				The invoked method for which the input data are logged.
	 * @param expectedMethodName	The expected method name in the log event.
	 * @param expectedArgsList		The expected list of arguments in the log event.
	 * @param args					The arguments to pass to the invoked method.
	 */
	@ParameterizedTest
	@MethodSource("provideMethodsAndConfigurations")
	void givenMethod_whenLogMethodInput_generatesLog(final MethodInputLoggingConfiguration configuration,
													 final Method method, final String expectedMethodName,
													 final String expectedArgsList, final Object[] args) {
		sut.logMethodInput(configuration, method, args);

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", is(configuration.getMessageTemplate())),
			hasProperty("arguments", hasItem(expectedMethodName)),
			hasProperty("arguments", hasItem(expectedArgsList)),
			hasProperty("level", is(Level.valueOf(configuration.getLogLevel().name()))
			))));
	}

	/**
	 * Verifies the effective logging of the input data for a given method as a structured message.
	 *
	 * @param configuration			The configuration to use.
	 * @param method				The invoked method for which the input data are logged.
	 * @param expectedMethodName	The expected method name in the log event.
	 * @param expectedArgsList		Not used here but kept to re-use the same arguments source
	 *                              ({@link #provideMethodsAndConfigurations()}).
	 * @param args					The arguments to pass to the invoked method.
	 */
	@ParameterizedTest
	@MethodSource("provideMethodsAndConfigurations")
	void givenMethod_whenLogMethodInputAsStructuredMessage_generatesLog(
		final MethodInputLoggingConfiguration configuration, final Method method, final String expectedMethodName,
		final String expectedArgsList, final Object[] args) {
		configuration.setStructuredMessage(true);
		sut.logMethodInput(configuration, method, args);

		assertThat(logger.getLoggingEvents(), hasItem(allOf(
			hasProperty("message", allOf(
				stringContainsInOrder("calledMethod", expectedMethodName),
				containsString("inputParameters")
			)),
			hasProperty("arguments", emptyCollectionOf(String.class)),
			hasProperty("level", is(Level.valueOf(configuration.getLogLevel().name()))
			))));
	}

	/**
	 * Builds arguments for the parameterized tests relative to the logging of the input data of a given method:
	 * {@link #givenMethod_whenLogMethodInput_generatesLog(MethodInputLoggingConfiguration, Method, String, String,
	 * Object[])} and {@link #givenMethod_whenLogMethodInputAsStructuredMessage_generatesLog(
	 * MethodInputLoggingConfiguration, Method, String, String, Object[])}.
	 *
	 * @return The arguments to execute the different test cases.
	 * @throws NoSuchMethodException if the invoked method is not defined in the {@link LogTestingClass}.
	 * @throws JsonProcessingException if the data cannot be formatted in JSON or XML.
	 */
	private static Stream<Arguments> provideMethodsAndConfigurations()
		throws NoSuchMethodException, JsonProcessingException {
		final ObjectMapper objectMapper = new ObjectMapper();
		final XmlMapper xmlMapper = new XmlMapper();
		final TestObject data = new TestObject("testVal", 123.45d);
		final Collection<String> collection = Arrays.asList("val1", "val2", "val3");
		final Map<String, Integer> map = Map.of("key1", 10, "key2", 20);

		return Stream.of(
			// Default configuration and method without arguments.
			Arguments.of(MethodInputLoggingConfiguration.builder().build(),
				LogTestingClass.class.getMethod("noOp"),
				"LogTestingClass.noOp",
				StringUtils.EMPTY,
				new Object[]{}),
			// Don't display enclosing class name and method without arguments.
			Arguments.of(MethodInputLoggingConfiguration.builder()
					.classNameDisplayed(false)
					.build(),
				LogTestingClass.class.getMethod("noOp"),
				"noOp",
				StringUtils.EMPTY,
				new Object[]{}),
			// Not default log level and method without arguments.
			Arguments.of(MethodInputLoggingConfiguration.builder()
					.logLevel(LogLevel.DEBUG)
					.build(),
				LogTestingClass.class.getMethod("noOp"),
				"LogTestingClass.noOp",
				StringUtils.EMPTY,
				new Object[]{}),
			// Default configuration and method with arguments.
			Arguments.of(MethodInputLoggingConfiguration.builder().build(),
				LogTestingClass.class.getMethod("methodInputData", int.class, String.class, boolean.class),
				"LogTestingClass.methodInputData",
				"argInt=10, argStr=test, argBool=false",
				new Object[]{10, "test", false}),
			// Default configuration and method with at least one null argument.
			Arguments.of(MethodInputLoggingConfiguration.builder().build(),
				LogTestingClass.class.getMethod("methodInputData", int.class, String.class, boolean.class),
				"LogTestingClass.methodInputData",
				"argInt=10, argStr=null, argBool=false",
				new Object[]{10, null, false}),
			// Excluding arguments and method with arguments.
			Arguments.of(MethodInputLoggingConfiguration.builder()
					.excludedArguments(Set.of("argInt", "argBool"))
					.build(),
				LogTestingClass.class.getMethod("methodInputData", int.class, String.class, boolean.class),
				"LogTestingClass.methodInputData",
				"argStr=test",
				new Object[]{10, "test", false}),
			// Restricting logged arguments and method with arguments.
			Arguments.of(MethodInputLoggingConfiguration.builder()
					.onlyLoggedArguments(Set.of("argInt", "argStr"))
					.build(),
				LogTestingClass.class.getMethod("methodInputData", int.class, String.class, boolean.class),
				"LogTestingClass.methodInputData",
				"argInt=10, argStr=test",
				new Object[]{10, "test", false}),
			// Default configuration and method with arguments having names simulating generated names (when the
			// original name cannot be retrieved).
			Arguments.of(MethodInputLoggingConfiguration.builder().build(),
				LogTestingClass.class.getMethod("methodSimulatingGeneratedArgNames", String.class, long.class),
				"LogTestingClass.methodSimulatingGeneratedArgNames",
				"$arg0=test, $arg1=1234",
				new Object[]{"test", 1234L}),
			// Format all arguments in JSON and method with complex type argument.
			Arguments.of(MethodInputLoggingConfiguration.builder()
					.prettyFormat(PrettyDataFormat.JSON)
					.prettifiedValues(Set.of(AutoLogMethodInOut.INPUT_DATA))
					.build(),
				LogTestingClass.class.getMethod("methodInputComplexData", TestObject.class),
				"LogTestingClass.methodInputComplexData",
				"data=" + objectMapper.writeValueAsString(data),
				new Object[]{data}),
			// Format all arguments in XML and method with complex type argument.
			Arguments.of(MethodInputLoggingConfiguration.builder()
					.prettyFormat(PrettyDataFormat.XML)
					.prettifiedValues(Set.of(AutoLogMethodInOut.ALL_DATA))
					.build(),
				LogTestingClass.class.getMethod("methodInputComplexData", TestObject.class),
				"LogTestingClass.methodInputComplexData",
				"data=" + xmlMapper.writeValueAsString(data),
				new Object[]{data}),
			// Undefined pretty format and method with complex type argument.
			Arguments.of(MethodInputLoggingConfiguration.builder()
					.prettyFormat(null)
					.prettifiedValues(Set.of(AutoLogMethodInOut.INPUT_DATA))
					.build(),
				LogTestingClass.class.getMethod("methodInputComplexData", TestObject.class),
				"LogTestingClass.methodInputComplexData",
				"data=" + data.toString(),
				new Object[]{data}),
			// Default configuration and method with collection and map arguments.
			Arguments.of(MethodInputLoggingConfiguration.builder().build(),
				LogTestingClass.class.getMethod("methodInputCollectionAndMap", Collection.class, Map.class),
				"LogTestingClass.methodInputCollectionAndMap",
				"collection=3 item(s), map=2 key-value pair(s)",
				new Object[]{collection, map}),
			// Partial pretty formatting in JSON, expand collections and maps and method with collection and map
			// arguments.
			Arguments.of(MethodInputLoggingConfiguration.builder()
					.prettifiedValues(Set.of("collection"))
					.collectionsAndMapsExpanded(true)
					.build(),
				LogTestingClass.class.getMethod("methodInputCollectionAndMap", Collection.class, Map.class),
				"LogTestingClass.methodInputCollectionAndMap",
				"collection=3 item(s): " + objectMapper.writeValueAsString(collection)
					+ ", map=2 key-value pair(s): " + map.toString(),
				new Object[]{collection, map}),
			// Expand collections and maps and method with empty collection and map arguments.
			Arguments.of(MethodInputLoggingConfiguration.builder()
					.collectionsAndMapsExpanded(true)
					.build(),
				LogTestingClass.class.getMethod("methodInputCollectionAndMap", Collection.class, Map.class),
				"LogTestingClass.methodInputCollectionAndMap",
				"collection=0 item(s), map=0 key-value pair(s)",
				new Object[]{Collections.EMPTY_LIST, Map.of()})
		);
	}
}
