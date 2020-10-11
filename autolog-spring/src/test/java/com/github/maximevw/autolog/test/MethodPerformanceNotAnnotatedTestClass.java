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
import com.github.maximevw.autolog.core.configuration.PrettyDataFormat;
import com.github.maximevw.autolog.core.logger.performance.AdditionalDataProvider;
import com.github.maximevw.autolog.spring.aspects.AutoLogPerformanceAspect;

import java.util.List;

/**
 * This class contains methods annotated with {@link AutoLogPerformance} specially designed to test
 * {@link AutoLogPerformanceAspect}.
 */
public class MethodPerformanceNotAnnotatedTestClass {

	private AdditionalDataProvider methodToMonitorDataProvider = new AdditionalDataProvider();
	private Object methodToMonitorWrongTypeDataProvider = new Object();

	/**
	 * Test annotated method in a non-annotated class.
	 * <p>
	 *     {@literal @}AutoLogPerformance at method level with specific parameters:
	 *     <ul>
	 *         <li>Don't log the individual timer of the method.</li>
	 *     </ul>
	 * </p>
	 */
	@AutoLogPerformance(logEachTimer = false)
	public void testMethodToMonitor() {
		// Do nothing: for test purpose only.
	}

	/**
	 * Test annotated method in a non-annotated class.
	 * <p>
	 *     {@literal @}AutoLogPerformance at method level with specific parameters:
	 *     <ul>
	 *         <li>The class name of the method is not logged.</li>
	 *         <li>Use an additional data provider with data set during the execution of the method.</li>
	 *     </ul>
	 * </p>
	 *
	 * @param arg0	Additional comment set into the {@link AdditionalDataProvider}.
	 * @param arg1	The number of processed items set into the {@link AdditionalDataProvider}.
	 */
	@AutoLogPerformance(additionalDataProvider = "methodToMonitorDataProvider", showClassName = false)
	public void testMethodToMonitorWithAdditionalDataProvider(final String arg0, final List<String> arg1) {
		methodToMonitorDataProvider.setProcessedItems(arg1.size());
		methodToMonitorDataProvider.addComments("additional comment", arg0);
	}

	/**
	 * Test annotated method in a non-annotated class.
	 * <p>
	 *     {@literal @}AutoLogPerformance at method level with specific parameters:
	 *     <ul>
	 *         <li>Use a non-existing additional data provider.</li>
	 *     </ul>
	 * </p>
	 */
	@AutoLogPerformance(additionalDataProvider = "methodToMonitorWrongDataProvider")
	public void testMethodToMonitorWithWrongAdditionalDataProvider() {
		// Do nothing: for test purpose only.
	}

	/**
	 * Test annotated method in a non-annotated class.
	 * <p>
	 *     {@literal @}AutoLogPerformance at method level with specific parameters:
	 *     <ul>
	 *          <li>Log using JSON structured format.</li>
	 *     </ul>
	 * </p>
	 */
	@AutoLogPerformance(structuredMessage = true)
	public void testMethodToMonitorJsonStructuredMessage() {
		// Do nothing: for test purpose only.
	}

	/**
	 * Test annotated method in a non-annotated class.
	 * <p>
	 *     {@literal @}AutoLogPerformance at method level with specific parameters:
	 *     <ul>
	 *         <li>Log using XML structured format.</li>
	 *     </ul>
	 * </p>
	 */
	@AutoLogPerformance(structuredMessage = true, prettyFormat = PrettyDataFormat.XML)
	public void testMethodToMonitorXmlStructuredMessage() {
		// Do nothing: for test purpose only.
	}

	/**
	 * Test annotated method in a non-annotated class using custom logger.
	 * <p>
	 *     {@literal @}AutoLogPerformance at method level with specific parameters:
	 *     <ul>
	 *         <li>Custom logger named {@code custom-logger}.</li>
	 *     </ul>
	 * </p>
	 */
	@AutoLogPerformance(topic = "custom-logger")
	public void testMethodWithCustomLogger() {
		// Do nothing: for test purpose only.
	}

	/**
	 * Test annotated method in a non-annotated class using custom logger.
	  <p>
	 *     {@literal @}AutoLogPerformance at method level with specific parameters:
	 *     <ul>
	 *         <li>Use caller class as logger name.</li>
	 *     </ul>
	 * </p>
	 */
	@AutoLogPerformance(callerClassAsTopic = true)
	public void testMethodWithCallerClassLogger() {
		// Do nothing: for test purpose only.
	}

	/**
	 * Test annotated method in a non-annotated class using custom message template.
	 * <p>
	 *     {@literal @}AutoLogPerformance at method level with specific parameters:
	 *     <ul>
	 *         <li>Custom message template.</li>
	 *     </ul>
	 * </p>
	 */
	@AutoLogPerformance(messageTemplate = "Method $invokedMethod finished in $executionTime")
	public void testMethodWithCustomMessageTemplate() {
		// Do nothing: for test purpose only.
	}
}
