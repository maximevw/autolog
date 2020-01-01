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

import com.github.maximevw.autolog.core.annotations.AutoLogMethodInput;
import com.github.maximevw.autolog.core.logger.LogLevel;
import com.github.maximevw.autolog.spring.aspects.AutoLogMethodInOutAspect;

/**
 * This class annotated with {@link AutoLogMethodInput} contains methods specially designed to test
 * {@link AutoLogMethodInOutAspect}.
 */
@AutoLogMethodInput
public class MethodInputAnnotatedTestClass {

	/**
	 * Test non-annotated method without returned value in an annotated class.
	 * <ul>
	 *     <li>Input: two parameters (String, int)</li>
	 *     <li>Output: none</li>
	 *     <li>@AutoLogMethodInput at class level with default.</li>
	 * </ul>
	 *
	 * @param strArg Fake string argument for test purpose.
	 * @param intArg Fake integer argument for test purpose.
	 */
	public void testNotAnnotatedMethodReturningVoid(final String strArg, final int intArg) {
		// Do nothing: for test purpose only.
	}

	/**
	 * Test annotated method without returned value in an annotated class.
	 * <ul>
	 *     <li>Input: two parameters (String, int)</li>
	 *     <li>Output: none</li>
	 *     <li>@AutoLogMethodInput at method level with specific parameters:
	 *         <ul>
	 *             <li>Logging level is DEBUG</li>
	 *         </ul>
	 *     </li>
	 * </ul>
	 *
	 * @param strArg Fake string argument for test purpose.
	 * @param intArg Fake integer argument for test purpose.
	 */
	@AutoLogMethodInput(level = LogLevel.DEBUG)
	public void testAnnotatedMethodReturningVoid(final String strArg, final int intArg) {
		// Do nothing: for test purpose only.
	}
}
