/*-
 * #%L
 * Autolog AspectJ integration module
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

package com.github.maximevw.autolog.aspectj.configuration;

import com.github.maximevw.autolog.core.logger.LoggerInterface;
import com.github.maximevw.autolog.core.logger.LoggerManager;
import com.github.maximevw.autolog.core.logger.adapters.Log4j2Adapter;
import com.github.maximevw.autolog.core.logger.adapters.Slf4jAdapter;
import com.github.maximevw.autolog.core.logger.adapters.SystemOutAdapter;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.instanceOf;

/**
 * Unit tests for the singleton class {@link AspectJLoggerManager}.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class AspectJLoggerManagerTest {

	/**
	 * Verifies that the default logger ({@link SystemOutAdapter}) is registered by default in the {@link LoggerManager}
	 * provided by the {@link AspectJLoggerManager} singleton when it is not initialized.
	 */
	@Test
	// Always run it first to be sure that the singleton has not been already initialized.
	@Order(0)
	public void givenNoSpecificLoggerManager_whenGetLoggerManager_returnDefaultLoggerManager() {
		final LoggerManager loggerManager = AspectJLoggerManager.getInstance().getLoggerManager();
		final List<LoggerInterface> registeredLoggers = loggerManager.getRegisteredLoggers();
		assertThat(registeredLoggers, hasSize(1));
		assertThat(registeredLoggers, hasItem(instanceOf(SystemOutAdapter.class)));
	}

	/**
	 * Verifies that the  {@link LoggerManager} provided by the {@link AspectJLoggerManager} singleton is the expected
	 * one.
	 */
	@Test
	@Order(1)
	public void givenSpecificLoggerManager_whenGetLoggerManager_returnExpectedLoggerManager() {
		final LoggerManager loggerManager = new LoggerManager();
		loggerManager.register(Log4j2Adapter.getInstance());
		loggerManager.register(Slf4jAdapter.getInstance());
		AspectJLoggerManager.getInstance().init(loggerManager);

		final List<LoggerInterface> registeredLoggers = AspectJLoggerManager.getInstance().getLoggerManager()
			.getRegisteredLoggers();
		assertThat(registeredLoggers, hasSize(2));
		assertThat(registeredLoggers, hasItem(instanceOf(Log4j2Adapter.class)));
		assertThat(registeredLoggers, hasItem(instanceOf(Slf4jAdapter.class)));
	}

}
