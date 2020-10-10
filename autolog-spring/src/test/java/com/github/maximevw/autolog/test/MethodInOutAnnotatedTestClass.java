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
import com.github.maximevw.autolog.core.logger.LogLevel;
import com.github.maximevw.autolog.spring.aspects.AutoLogMethodInOutAspect;

/**
 * This class annotated with {@link AutoLogMethodInOut} contains methods specially designed to test
 * {@link AutoLogMethodInOutAspect}.
 */
@AutoLogMethodInOut
public class MethodInOutAnnotatedTestClass {

	/**
     * Test non-annotated method without returned value in an annotated class.
	 * <ul>
	 *     <li>Input: two parameters (String, int)</li>
	 *     <li>Output: none</li>
	 *     <li>@AutoLogMethodInOut at class level with default parameters.</li>
	 * </ul>
     *
     * @param strArg Fake string argument for test purpose.
     * @param intArg Fake integer argument for test purpose.
     */
	public void testMethodReturningVoid(final String strArg, final int intArg) {
		// Do nothing: for test purpose only.
	}

	/**
	 * Test non-annotated method returning value in an annotated class.
	 * <ul>
	 *     <li>Input: two parameters (String, int)</li>
	 *     <li>Output: a String concatenating the both method arguments</li>
	 *     <li>@AutoLogMethodInOut at class level with default parameters.</li>
	 * </ul>
	 *
	 * @param strArg Fake string argument for test purpose.
	 * @param intArg Fake integer argument for test purpose.
	 * @return A string value.
	 */
	public String testMethodReturningString(final String strArg, final int intArg) {
		return strArg.concat(String.valueOf(intArg));
	}

	/**
	 * Test annotated method throwing exception that must not automatically be logged.
	 * <ul>
	 *     <li>Input: a String</li>
	 *     <li>Output: throw exception</li>
	 *     <li>@AutoLogMethodInOut at class level with default parameters.</li>
	 * </ul>
	 *
	 * @param strArg Fake string argument for test purpose.
	 * @throws IllegalArgumentException Generated exception for test purpose.
	 */
	public void testMethodThrowingExceptionMustNotBeLogged(final String strArg) {
		throw new IllegalArgumentException("Wrong value: " + strArg);
	}

	/**
	 * Test annotated method throwing exception.
	 * <ul>
	 *     <li>Input: a String</li>
	 *     <li>Output: throw exception</li>
	 *     <li>@AutoLogMethodInOut at method level with specific parameters:
	 *         <ul>
	 *             <li>Logging level is DEBUG</li>
	 *             <li>Throwable are logged before being re-thrown</li>
	 *         </ul>
	 *     </li>
	 * </ul>
	 *
	 * @param strArg Fake string argument for test purpose.
	 * @throws IllegalArgumentException Generated exception for test purpose.
	 */
	@AutoLogMethodInOut(level = LogLevel.DEBUG, logThrowable = true)
	public void testMethodThrowingException(final String strArg) {
		throw new IllegalArgumentException("Wrong value: " + strArg);
	}

	/**
	 * Test @AutoLogMethodInput and @AutoLogMethodOutput annotated method returning value in an @AutoLogMethodInOut
	 * annotated class.
	 * <ul>
	 *     <li>Input: two parameters (String, int)</li>
	 *     <li>Output: a String concatenating the both method arguments</li>
	 *     <li>@AutoLogMethodInput and @AutoLogMethodOutput at method level with specific parameters:
	 *         <ul>
	 *             <li>Logging level is TRACE</li>
	 *         </ul>
	 *     </li>
	 * </ul>
	 *
	 * @param strArg Fake string argument for test purpose.
	 * @param intArg Fake integer argument for test purpose.
	 * @return A string value.
	 */
	@AutoLogMethodInput(level = LogLevel.TRACE)
	@AutoLogMethodOutput(level = LogLevel.TRACE)
	public String testTwiceAnnotatedMethodReturningString(final String strArg, final int intArg) {
		return strArg.concat(String.valueOf(intArg));
	}
}
