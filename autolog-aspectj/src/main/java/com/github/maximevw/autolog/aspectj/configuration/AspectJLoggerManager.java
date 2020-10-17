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

import com.github.maximevw.autolog.core.logger.LoggerManager;
import com.github.maximevw.autolog.core.logger.adapters.SystemOutAdapter;
import org.apiguardian.api.API;

/**
 * This class is a singleton providing a {@link LoggerManager} instance used by AspectJ for automation of logging
 * based on Autolog annotations.
 *
 * @see LoggerManager
 */
@API(status = API.Status.STABLE, since = "1.2.0")
public final class AspectJLoggerManager {

	private LoggerManager loggerManager;

	private AspectJLoggerManager() {
		// Private constructor to force usage of singleton instance via the method getInstance().
		// By default, at least register standard output in the default logger manager.
		this.loggerManager = new LoggerManager();
		this.loggerManager.register(SystemOutAdapter.getInstance());
	}

	/**
	 * Gets an instance of AspectJLoggerManager.
	 *
	 * @return A singleton instance of AspectJLoggerManager.
	 */
	public static AspectJLoggerManager getInstance() {
		return AspectJLoggerManager.AspectJLoggerManagerInstanceHolder.INSTANCE;
	}

	/**
	 * Defines the instance of {@link LoggerManager} provided by the AspectJLoggerManager singleton.
	 *
	 * @param loggerManager The logger manager.
	 */
	public void init(final LoggerManager loggerManager) {
		this.loggerManager = loggerManager;
	}

	/**
	 * Gets the {@link LoggerManager} instance to use for logging.
	 *
	 * @return The {@link LoggerManager} instance to use for logging.
	 */
	public LoggerManager getLoggerManager() {
		return this.loggerManager;
	}

	private static class AspectJLoggerManagerInstanceHolder {
		private static final AspectJLoggerManager INSTANCE = new AspectJLoggerManager();
	}
}
