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

import com.fasterxml.jackson.annotation.JsonInclude;
import com.github.maximevw.autolog.core.annotations.AutoLogPerformance;
import com.github.maximevw.autolog.core.logger.performance.AdditionalDataProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apiguardian.api.API;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The loggable performance data of a method invocation.
 */
@API(status = API.Status.INTERNAL, consumers = "com.github.maximevw.autolog.core.*")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class MethodPerformanceLogEntry {

	/**
	 * The name of the invoked method (or API endpoint).
	 * @see AutoLogPerformance#apiEndpointsAutoConfig()
	 */
	private String invokedMethod;

	/**
	 * The HTTP method used to invoke the method when it corresponds to an API endpoint.
	 * @see AutoLogPerformance#apiEndpointsAutoConfig()
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String httpMethod;

	/**
	 * The total execution time of the invoked method (in milliseconds).
	 */
	private long executionTimeInMs;

	/**
	 * The start time of the invocation.
	 */
	private LocalDateTime startTime;

	/**
	 * The end time of the invocation.
	 */
	private LocalDateTime endTime;

	/**
	 * The optional number of items processed by the invoked method.
	 * @see AdditionalDataProvider
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Integer processedItems;

	/**
	 * The average execution time (in milliseconds) by processed item (only defined if the number of processed items is
	 * defined).
	 */
	@Builder.Default
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private long averageExecutionTimeByItemInMs = 0;

	/**
	 * Whether the invoked method has failed (an exception has been thrown for example).
	 */
	@Builder.Default
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private boolean failed = false;

	/**
	 * The optional comments.
	 */
	@Builder.Default
	@JsonInclude(JsonInclude.Include.NON_DEFAULT)
	private List<String> comments = new ArrayList<>();

	/**
	 * Gets the total execution time of the invoked method formatted to be human readable.
	 *
	 * @return The formatted total execution time of the invoked method.
	 */
	public String getExecutionTime() {
		return LoggingUtils.formatDuration(executionTimeInMs);
	}

	/**
	 * Gets the average execution time by processed item (only defined if the number of processed items is defined)
	 * formatted to be human readable.
	 *
	 * @return The formatted average execution time by processed item or {@code null} if the number of processed items
	 *         is not defined.
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	public String getAverageExecutionTimeByItem() {
		if (averageExecutionTimeByItemInMs > 0) {
			return LoggingUtils.formatDuration(averageExecutionTimeByItemInMs);
		}
		return null;
	}

	/**
	 * Adds comments to log.
	 *
	 * @param comments The comments to add.
	 */
	public void addComments(final String... comments) {
		this.comments.addAll(Arrays.asList(comments));
	}

	/**
	 * Whether the log entry has comments.
	 *
	 * @return {@code true} if the log entry has at least one comment, {@code false} otherwise.
	 */
	public boolean hasComments() {
		return comments != null && !comments.isEmpty();
	}

	/**
	 * Calculates the average duration of processing for one item (when the number of processed items is defined).
	 */
	public void computeAverageExecutionTime() {
		if (processedItems != null && processedItems > 0) {
			averageExecutionTimeByItemInMs = executionTimeInMs / processedItems;
		}
	}

}
