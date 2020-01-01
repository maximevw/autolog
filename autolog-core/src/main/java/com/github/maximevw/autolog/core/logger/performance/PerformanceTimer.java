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

package com.github.maximevw.autolog.core.logger.performance;

import com.github.maximevw.autolog.core.logger.MethodPerformanceLogEntry;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.time.StopWatch;
import org.apiguardian.api.API;

import java.util.ArrayList;
import java.util.List;

/**
 * This class encapsulates the timer to monitor a method invocation and the data relative to the performance of this
 * invocation.
 */
@API(status = API.Status.STABLE, since = "1.0.0")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class PerformanceTimer {

	/**
	 * The timer measuring the execution time.
	 */
	private StopWatch timer;

	/**
	 * The loggable data relative to the measured performance.
	 */
	private MethodPerformanceLogEntry performanceLogEntry;

	/**
	 * The parent performance timer (or {@code null} if no parent timer is running) for monitoring of chained
	 * invocations.
	 */
	@Setter(AccessLevel.NONE)
	private PerformanceTimer parent;

	/**
	 * The list of children performance timers (or {@code null} if no children timers are running) for monitoring of
	 * chained invocations.
	 */
	private List<PerformanceTimer> children;

	/**
	 * Whether the process to monitor is running yet.
	 * <p>
	 *    For example, if a method invocation is ended but the caller method is running yet, this value is set to
	 *    {@code true}.
	 * </p>
	 */
	@Builder.Default
	private boolean running = false;

	/**
	 * Stops the performance timer.
	 */
	public void stop() {
		timer.stop();
		running = false;

		// Set the running parent timer as the current one.
		// If the parent is no more running: it is in a inconsistent state ("zombie" timer), so do not use it anymore.
		if (parent != null && parent.isRunning()) {
			PerformanceTimerContext.setCurrent(parent);
		} else {
			// If the parent timer is no more running (has been stopped): it cannot be a valid parent.
			// So help the garbage collector by removing all the references to this timer.
			PerformanceTimerContext.removeCurrent();
		}
	}

	/**
	 * Sets this timer's parent.
	 * <p>
	 *     This method is automatically called when the timer is created and/or started, you should not invoke it except
	 *     if you are using multi-threading. In a such situation, the {@link PerformanceTimerContext} will not be able
	 *     to see the parent/children relationship and you have to set this relationship manually.
	 * </p>
	 *
	 * @param parentTimer The parent timer.
	 */
	public void setParent(final PerformanceTimer parentTimer) {
		this.parent = parentTimer;
		if (parent != null) {
			// Check that the parent is running (not stopped).
			if (!parent.isRunning()) {
				// If the parent is no more running: it is in a inconsistent state ("zombie" timer), so do not use it
				// anymore because it cannot be a valid parent.
				// Help the garbage collector by removing all the references from this timer and do not create the
				// parent/child relationship.
				parent.children = null;
				parent = null;
			} else {
				// Add this child to the parent timer.
				if (parent.children == null) {
					parent.children = new ArrayList<>();
				}
				parent.children.add(this);
			}
		}
	}

}
