/*-
 * #%L
 * Autolog core module
 * %%
 * Copyright (C) 2019 - 2020 Maxime WIEWIORA
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

package com.github.maximevw.autolog.core.logger.performance;

import com.github.maximevw.autolog.core.logger.MethodPerformanceLogEntry;
import org.apache.commons.lang3.StringUtils;
import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.runtime.RuntimeConstants;
import org.apache.velocity.runtime.log.NullLogChute;
import org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader;
import org.apache.velocity.runtime.resource.loader.FileResourceLoader;
import org.apache.velocity.runtime.resource.loader.StringResourceLoader;
import org.apache.velocity.runtime.resource.util.StringResourceRepository;
import org.apiguardian.api.API;

import java.io.StringWriter;

/**
 * The processor of Velocity templates used to log performance information.
 */
@API(status = API.Status.INTERNAL, since = "1.2.0",
	consumers = "com.github.maximevw.autolog.core.logger.MethodPerformanceLogger")
public final class PerformanceMessageTemplateProcessor {

	private static final String FILE_TEMPLATE_PREFIX = "file:";

	private final VelocityEngine velocityEngine;

	/**
	 * Constructor.
	 * <p>
	 *     It initializes the Velocity engine to accept files, classpath resources and strings as templates resources.
	 * </p>
	 */
	private PerformanceMessageTemplateProcessor() {
		this.velocityEngine = new VelocityEngine();
		// Do not generate logs for Velocity.
		this.velocityEngine.addProperty(RuntimeConstants.RUNTIME_LOG_LOGSYSTEM_CLASS, NullLogChute.class.getName());
		// Enable and setup multiple resource loaders: from filesystem, classpath or directly a string.
		// Note: by default the default path used by FileResourceLoader is the current directory, so define the property
		// 'file.resource.loader.path' to an empty string to allow any absolute or relative path.
		this.velocityEngine.addProperty(RuntimeConstants.RESOURCE_LOADER, "file, class, string");
		this.velocityEngine.addProperty("file.resource.loader.class", FileResourceLoader.class.getName());
		this.velocityEngine.addProperty("file.resource.loader.path", StringUtils.EMPTY);
		this.velocityEngine.addProperty("class.resource.loader.class", ClasspathResourceLoader.class.getName());
		this.velocityEngine.addProperty("string.resource.loader.class", StringResourceLoader.class.getName());
		this.velocityEngine.init();
	}

	/**
	 * Gets an instance of {@code PerformanceMessageTemplateProcessor}.
	 *
	 * @return A singleton instance of {@code PerformanceMessageTemplateProcessor}.
	 */
	public static PerformanceMessageTemplateProcessor getInstance() {
		return PerformanceMessageTemplateProcessor.PerformanceMessageTemplateProcessorInstanceHolder.INSTANCE;
	}

	/**
	 * Generates a log message using the defined Velocity template and the data included into the given log entry.
	 *
	 * @param messageTemplate		The message template to use (it can be a reference to a file or classpath resource).
	 * @param performanceLogEntry	The performance log entry.
	 * @return The message to log.
	 */
	public String generateMessage(final String messageTemplate, final MethodPerformanceLogEntry performanceLogEntry) {
		// Define the message template.
		final Template velocityTemplate;
		if (messageTemplate.startsWith(FILE_TEMPLATE_PREFIX)) {
			velocityTemplate = this.velocityEngine.getTemplate(
				messageTemplate.substring(FILE_TEMPLATE_PREFIX.length()));
		} else {
			final StringResourceRepository stringResourceRepository = StringResourceLoader.getRepository();
			stringResourceRepository.putStringResource("customTemplate", messageTemplate);
			velocityTemplate = this.velocityEngine.getTemplate("customTemplate");
		}

		// Fill the context with the data from the performance log entry.
		final VelocityContext context = new VelocityContext();
		context.put("isFailed", performanceLogEntry.isFailed());
		context.put("invokedMethod", performanceLogEntry.getInvokedMethod());
		context.put("httpMethod", performanceLogEntry.getHttpMethod());
		context.put("executionTime", performanceLogEntry.getExecutionTime());
		context.put("processedItems", performanceLogEntry.getProcessedItems());
		context.put("averageExecutionTimeByItem", performanceLogEntry.getAverageExecutionTimeByItem());
		context.put("startTime", performanceLogEntry.getStartTime());
		context.put("endTime", performanceLogEntry.getEndTime());
		context.put("comments", performanceLogEntry.getComments());

		final StringWriter writer = new StringWriter();
		velocityTemplate.merge(context, writer);
		// Trim the result to remove potential useless whitespaces introduced by the formatting of the template.
		return writer.toString().trim();
	}

	private static class PerformanceMessageTemplateProcessorInstanceHolder {
		private static final PerformanceMessageTemplateProcessor INSTANCE = new PerformanceMessageTemplateProcessor();
	}
}
