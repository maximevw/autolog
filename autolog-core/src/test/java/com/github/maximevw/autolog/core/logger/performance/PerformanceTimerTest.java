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

import org.apache.commons.lang3.time.StopWatch;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the classes {@link PerformanceTimer} and {@link PerformanceTimerContext}.
 */
@ExtendWith(MockitoExtension.class)
class PerformanceTimerTest {

	/**
	 * Initializes the {@link PerformanceTimerContext} for each new test case.
	 */
	@BeforeEach
	void init() {
		// Initialize current timer context.
		PerformanceTimerContext.setCurrent(initPerformanceTimerForTesting(true));
	}

	/**
	 * Initializes a new {@link PerformanceTimer}.
	 *
	 * @param running Whether the timer is running (and if yes, started) at initialization time.
	 * @return A performance timer instance.
	 */
	private static PerformanceTimer initPerformanceTimerForTesting(final boolean running) {
		final PerformanceTimer timer = new PerformanceTimer();
		timer.setTimer(new StopWatch());
		timer.setRunning(running);
		if (running) {
			timer.getTimer().start();
		}
		return timer;
	}

	/**
	 * Verifies that setting a null timer to the current context removes the current timer.
	 */
	@Test
	void givenNullTimer_whenSetCurrentTimerContext_removesCurrentTimer() {
		assertNotNull(PerformanceTimerContext.current());
		// Remove current timer context.
		PerformanceTimerContext.setCurrent(null);
		assertNull(PerformanceTimerContext.current());
	}

	/**
	 * Verifies that stopping a timer without parent clears the current context.
	 */
	@Test
	void givenTimerWithoutParent_whenStop_clearsCurrentTimerContext() {
		final PerformanceTimer sut = PerformanceTimerContext.current();
		sut.stop();
		assertFalse(sut.isRunning());
		assertNull(PerformanceTimerContext.current());
	}

	/**
	 * Verifies that stopping a timer with a running parent updates the current context with the parent timer.
	 */
	@Test
	void givenTimerWithRunningParent_whenStop_updatesCurrentTimerContext() {
		final PerformanceTimer sut = PerformanceTimerContext.current();
		final PerformanceTimer parentTimer = initPerformanceTimerForTesting(true);

		sut.setParent(parentTimer);
		sut.stop();

		assertFalse(sut.isRunning());
		assertNotNull(PerformanceTimerContext.current());
		assertThat(PerformanceTimerContext.current(), is(parentTimer));
	}

	/**
	 * Verifies that stopping a timer with "zombie" parent clears the current context.
	 */
	@Test
	void givenTimerWithZombieParent_whenStop_clearsCurrentTimerContext() {
		final PerformanceTimer sut = PerformanceTimerContext.current();
		final PerformanceTimer parentTimer = initPerformanceTimerForTesting(false);

		sut.setParent(parentTimer);
		sut.stop();

		assertFalse(sut.isRunning());
		assertNull(PerformanceTimerContext.current());
	}
}
