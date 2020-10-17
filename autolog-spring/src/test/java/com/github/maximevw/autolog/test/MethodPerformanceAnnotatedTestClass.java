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

import com.github.maximevw.autolog.core.annotations.AutoLogPerformance;
import com.github.maximevw.autolog.core.logger.LogLevel;
import com.github.maximevw.autolog.spring.aspects.AutoLogPerformanceSpringAspect;

/**
 * This class annotated with {@link AutoLogPerformance} contains methods specially designed to test
 * {@link AutoLogPerformanceSpringAspect}.
 */
@AutoLogPerformance
public class MethodPerformanceAnnotatedTestClass {

	/**
	 * Test non-annotated method in an annotated class.
	 * <p>
	 *     {@literal @}AutoLogPerformance at class level with default parameters.
	 * </p>
	 */
	public void testMethodToMonitor() {
		// Do nothing: for test purpose only.
	}

	/**
	 * Test annotated method in an annotated class.
	 * <p>
	 *     {@literal @}AutoLogPerformance at method level with specific parameters:
	 *     <ul>
	 *         <li>Specific name</li>
	 *         <li>Static comments</li>
	 *         <li>Logging level is INFO</li>
	 *     </ul>
	 * </p>
	 */
	@AutoLogPerformance(name = "methodPerformance", comments = {"comment1", "comment2"}, level = LogLevel.INFO)
	public void testAnnotatedMethodToMonitor() {
		// Do nothing: for test purpose only.
	}

	/**
	 * Test non-annotated method calling another method annotated with @AutoLogPerformance in an annotated class.
	 * <p>
	 *     {@literal @}AutoLogPerformance at class level with default parameters.
	 * </p>
	 *
	 * @param notAnnotatedTestClass Instance of test class not annotated with {@link AutoLogPerformance} containing
	 *                              a public method with a such annotation.
	 */
	public void testMethodCallingAnotherOne(final MethodPerformanceNotAnnotatedTestClass notAnnotatedTestClass) {
		notAnnotatedTestClass.testMethodToMonitor();
	}

	/**
	 * Test annotated method calling another method annotated with @AutoLogPerformance and throwing exception in an
	 * annotated class.
	 * <p>
	 *     {@literal @}AutoLogPerformance at class level with default parameters.AutoLogPerformance at method level with
	 *     specific parameters:
	 *     <ul>
	 *         <li>Don't dump the full stack of calls.</li>
	 *     </ul>
	 * </p>
	 *
	 * @param arg0 Fake integer argument for test purpose.
	 */
	@AutoLogPerformance(dumpCallsStack = false)
	public void testMethodToMonitorThrowingException(final int arg0) {
		final MethodPerformanceNotAnnotatedTestClass notAnnotatedTestClass =
			new MethodPerformanceNotAnnotatedTestClass();
		notAnnotatedTestClass.testMethodToMonitor();
		throw new IllegalArgumentException("Wrong value: " + arg0);
	}
}
