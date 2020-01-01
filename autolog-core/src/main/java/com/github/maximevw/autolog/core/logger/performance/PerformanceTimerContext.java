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

import org.apiguardian.api.API;

/**
 * This class maintains the stack of calls of the running {@link PerformanceTimer} linked to each running threads.
 *
 * @see ThreadLocal
 */
@API(status = API.Status.INTERNAL, consumers = "com.github.maximevw.autolog.*")
public final class PerformanceTimerContext {

	/**
	 * The current running {@link PerformanceTimer}.
	 */
	private static final ThreadLocal<PerformanceTimer> THREAD_LOCAL_PERFORMANCE_TIMER = new ThreadLocal<>();

	private PerformanceTimerContext() {
		// Private constructor to hide it externally.
	}

	/**
	 * @return The current {@link PerformanceTimer} or {@code null} if none is running.
	 */
	public static PerformanceTimer current() {
		return THREAD_LOCAL_PERFORMANCE_TIMER.get();
	}

	/**
	 * Set the current {@link PerformanceTimer} in the stack.
	 *
	 * @param performanceTimer The current {@link PerformanceTimer} to set (or {@code null} to remove the current
	 *                         {@link PerformanceTimer}).
	 */
	public static void setCurrent(final PerformanceTimer performanceTimer) {
		if (performanceTimer == null) {
			removeCurrent();
		} else {
			THREAD_LOCAL_PERFORMANCE_TIMER.set(performanceTimer);
		}
	}

	/**
	 * Removes the current {@link PerformanceTimer}.
	 */
	public static void removeCurrent() {
		THREAD_LOCAL_PERFORMANCE_TIMER.remove();
	}

}
