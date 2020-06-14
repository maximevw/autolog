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

package com.github.maximevw.autolog.test;

import com.github.maximevw.autolog.core.annotations.AutoLogMethodInOut;
import com.github.maximevw.autolog.core.annotations.AutoLogMethodInput;
import com.github.maximevw.autolog.core.annotations.AutoLogMethodOutput;
import com.github.maximevw.autolog.core.configuration.PrettyDataFormat;
import com.github.maximevw.autolog.core.logger.LogLevel;
import com.github.maximevw.autolog.spring.aspects.AutoLogMethodInOutAspect;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class contains methods annotated with {@link AutoLogMethodInOut}, {@link AutoLogMethodInput} and
 * {@link AutoLogMethodOutput} specially designed to test {@link AutoLogMethodInOutAspect}.
 */
public class MethodInOutNotAnnotatedTestClass {

	/**
	 * Test annotated method returning List in a non-annotated class.
	 * <ul>
	 *     <li>Input: a map</li>
	 *     <li>Output: a list</li>
	 *     <li>@AutoLogMethodInOut at method level with specific parameters:
	 *         <ul>
	 *             <li>Input and output are fully logged.</li>
	 *             <li>The class name of the method is not logged.</li>
	 *         </ul>
	 *     </li>
	 * </ul>
	 *
	 * @param mapArg Fake map argument for test purpose.
	 * @return A list of String values.
	 */
	@AutoLogMethodInOut(expandCollectionsAndMaps = true, showClassName = false)
	public List<String> testMethodReturningList(final Map<String, String> mapArg) {
		return new ArrayList<>(mapArg.keySet());
	}

	/**
	 * Test annotated method returning an object in a non-annotated class.
	 * <ul>
	 *     <li>Input: nothing</li>
	 *     <li>Output: an object (TestObject)</li>
	 *     <li>@AutoLogMethodInOut at method level with specific parameters:
	 *         <ul>
	 *             <li>Output data prettified in JSON format</li>
	 *         </ul>
	 *     </li>
	 * </ul>
	 *
	 * @return A TestObject instance.
	 */
	@AutoLogMethodInOut(prettify = AutoLogMethodInOut.OUTPUT_DATA)
	public TestObject testMethodReturningObject() {
		return new TestObject("abc", 123.45);
	}

	/**
	 * Test annotated method in a non-annotated class with XML formatting for input and output.
	 * <ul>
	 *     <li>Input: an object (TestObject)</li>
	 *     <li>Output: an object (TestObject)</li>
	 *     <li>@AutoLogMethodInOut at method level with specific parameters:
	 *         <ul>
	 *             <li>Data prettified in XML format</li>
	 *         </ul>
	 *     </li>
	 * </ul>
	 *
	 * @param objArg Fake object argument for test purpose.
	 * @return A TestObject instance.
	 */
	@AutoLogMethodInOut(prettify = AutoLogMethodInOut.ALL_DATA, prettyFormat = PrettyDataFormat.XML)
	public TestObject testMethodReturningObjectLoggedAsXml(final TestObject objArg) {
		return objArg;
	}

	/**
	 * Test annotated method in a non-annotated class with JSON formatting of input only.
	 * <ul>
	 *     <li>Input: 3 parameters (String, int, TestObject)</li>
	 *     <li>Output: a String</li>
	 *     <li>@AutoLogMethodInOut at method level with specific parameters:
	 *         <ul>
	 *             <li>Input data prettified in JSON format</li>
	 *         </ul>
	 *     </li>
	 * </ul>
	 *
	 * @param strArg Fake string argument for test purpose.
	 * @param intArg Fake integer argument for test purpose.
	 * @param objArg Fake object argument for test purpose.
	 * @return A string value.
	 */
	@AutoLogMethodInOut(prettify = AutoLogMethodInOut.INPUT_DATA)
	public String testMethodInputLoggedAsJson(final String strArg, final int intArg, final TestObject objArg) {
		return strArg;
	}

	/**
	 * Test annotated method in a non-annotated class with JSON formatting of some input arguments only.
	 * <ul>
	 *     <li>Input: 2 parameters of type TestObject</li>
	 *     <li>Output: none</li>
	 *     <li>@AutoLogMethodInOut at method level with specific parameters:
	 *         <ul>
	 *             <li>Only the first input argument prettified in JSON format</li>
	 *         </ul>
	 *     </li>
	 * </ul>
	 *
	 * @param objArg0 Fake object argument for test purpose.
	 * @param objArg1 Fake object argument for test purpose.
	 */
	@AutoLogMethodInOut(prettify = {"objArg0", "fakeArg"})
	public void testMethodInputPartiallyLoggedAsJson(final TestObject objArg0, final TestObject objArg1) {
		// Do nothing: for test purpose only.
	}

	/**
	 * Test annotated method in a non-annotated class with logging restrictions.
	 * <ul>
	 *     <li>Input: 3 parameters (String, int, boolean)</li>
	 *     <li>Output: a String</li>
	 *     <li>@AutoLogMethodInOut at method level with specific parameters:
	 *         <ul>
	 *             <li>Logging restriction to one parameter of 3.</li>
	 *         </ul>
	 *     </li>
	 * </ul>
	 *
	 * @param strArg 	Fake string argument for test purpose.
	 * @param intArg 	Fake integer argument for test purpose.
	 * @param boolArg 	Fake boolean argument for test purpose.
	 * @return A string value.
	 */
	@AutoLogMethodInOut(restrict = "strArg")
	public String testMethodWithLoggingRestrictions(final String strArg, final int intArg, final boolean boolArg) {
		return strArg;
	}

	/**
	 * Test annotated method in a non-annotated class with logging restrictions.
	 * <ul>
	 *     <li>Input: 3 parameters (String, int, boolean)</li>
	 *     <li>Output: a String</li>
	 *     <li>@AutoLogMethodInOut at method level with specific parameters:
	 *         <ul>
	 *             <li>Logging exclusion of one parameter of 3.</li>
	 *         </ul>
	 *     </li>
	 * </ul>
	 *
	 * @param strArg 	Fake string argument for test purpose.
	 * @param intArg 	Fake integer argument for test purpose.
	 * @param boolArg 	Fake boolean argument for test purpose.
	 * @return A string value.
	 */
	@AutoLogMethodInOut(exclude = "strArg")
	public String testMethodWithLoggingExclusions(final String strArg, final int intArg, final boolean boolArg) {
		return strArg;
	}

	/**
	 * Test @AutoLogMethodInput and @AutoLogMethodOutput annotated method returning value in a non-annotated class.
	 * <ul>
	 *     <li>Input: two parameters (String, int)</li>
	 *     <li>Output: a String concatenating the both method arguments</li>
	 *     <li>@AutoLogMethodInput and @AutoLogMethodOutput at method level with default parameters.</li>
	 * </ul>
	 *
	 * @param strArg 	Fake string argument for test purpose.
	 * @param intArg 	Fake integer argument for test purpose.
	 * @return A string value.
	 */
	@AutoLogMethodInput
	@AutoLogMethodOutput
	public String testTwiceAnnotatedMethodReturningString(final String strArg, final int intArg) {
		return strArg.concat(String.valueOf(intArg));
	}

	/**
	 * Test @AutoLogMethodInOut, @AutoLogMethodInput and @AutoLogMethodOutput annotated method returning value in a
	 * non-annotated class.
	 * <ul>
	 *     <li>Input: two parameters (String, int)</li>
	 *     <li>Output: a String concatenating the both method arguments</li>
	 *     <li>@AutoLogMethodInput and @AutoLogMethodOutput at method level with specific parameters:
	 *         <ul>
	 *             <li>Logging level is DEBUG</li>
	 *         </ul>
	 *     </li>
	 *     <li>@AutoLogMethodInOut at method level with default parameters. Only this annotation should be taken into
	 *     account.</li>
	 * </ul>
	 *
	 * @param strArg 	Fake string argument for test purpose.
	 * @param intArg 	Fake integer argument for test purpose.
	 * @return A string value.
	 */
	@SuppressWarnings("AutoLogMethodAnnotationsConflict")
	@AutoLogMethodInOut
	@AutoLogMethodInput(level = LogLevel.DEBUG)
	@AutoLogMethodOutput(level = LogLevel.DEBUG)
	public String testThriceAnnotatedMethodReturningString(final String strArg, final int intArg) {
		return strArg.concat(String.valueOf(intArg));
	}

	/**
	 * Test annotated method in a non-annotated class using custom logger.
	 * <ul>
	 *     <li>Input: none</li>
	 *     <li>Output: none</li>
	 *     <li>@AutoLogMethodInOut at method level with specific parameters:
	 *         <ul>
	 *             <li>Custom logger named {@code custom-logger}.</li>
	 *         </ul>
	 *     </li>
	 * </ul>
	 */
	@AutoLogMethodInOut(topic = "custom-logger")
	public void testMethodWithCustomLogger() {
		// Do nothing: for test purpose only.
	}

	/**
	 * Test annotated method in a non-annotated class using custom logger.
	 * <ul>
	 *     <li>Input: none</li>
	 *     <li>Output: none</li>
	 *     <li>@AutoLogMethodInOut at method level with specific parameters:
	 *         <ul>
	 *             <li>Use caller class as logger name.</li>
	 *         </ul>
	 *     </li>
	 * </ul>
	 */
	@AutoLogMethodInOut(callerClassAsTopic = true)
	public void testMethodWithCallerClassLogger() {
		// Do nothing: for test purpose only.
	}

	/**
	 * Test @AutoLogMethodInput and @AutoLogMethodOutput annotated method in a non-annotated class using custom logger.
	 * <ul>
	 *     <li>Input: none</li>
	 *     <li>Output: none</li>
	 *     <li>@AutoLogMethodInput and @AutoLogMethodOutput at method level with specific parameters:
	 *         <ul>
	 *             <li>Custom logger named {@code custom-logger}.</li>
	 *         </ul>
	 *     </li>
	 * </ul>
	 */
	@AutoLogMethodInput(topic = "custom-logger")
	@AutoLogMethodOutput(topic = "custom-logger")
	public void testTwiceAnnotatedMethodWithCustomLogger() {
		// Do nothing: for test purpose only.
	}

	/**
	 * Test @AutoLogMethodInput and @AutoLogMethodOutput annotated method in a non-annotated class using custom logger.
	 * <ul>
	 *     <li>Input: none</li>
	 *     <li>Output: none</li>
	 *     <li>@AutoLogMethodInput and @AutoLogMethodOutput at method level with specific parameters:
	 *         <ul>
	 *             <li>Use caller class as logger name.</li>
	 *         </ul>
	 *     </li>
	 * </ul>
	 */
	@AutoLogMethodInput(callerClassAsTopic = true)
	@AutoLogMethodOutput(callerClassAsTopic = true)
	public void testTwiceAnnotatedMethodWithCallerClassLogger() {
		// Do nothing: for test purpose only.
	}
}
