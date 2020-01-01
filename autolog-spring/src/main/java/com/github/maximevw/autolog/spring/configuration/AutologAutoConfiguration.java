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

package com.github.maximevw.autolog.spring.configuration;

import com.github.maximevw.autolog.core.logger.LoggerInterface;
import com.github.maximevw.autolog.core.logger.LoggerManager;
import com.github.maximevw.autolog.core.logger.adapters.SystemOutAdapter;
import org.apiguardian.api.API;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Objects;

/**
 * Spring auto-configuration for Autolog.
 */
@API(status = API.Status.STABLE, since = "1.0.0")
@Configuration
@ComponentScan("com.github.maximevw.autolog.spring")
@EnableConfigurationProperties(AutologProperties.class)
@Import(LoggerInterfaceConverter.class)
public class AutologAutoConfiguration {

	@Autowired
	private AutologProperties autologProperties;

	/**
	 * The {@link LoggerManager} bean.
	 * <p>
	 *     By default, if the provided properties don't contain any specific or valid loggers the default logger
	 *     interface used by Autolog will be {@link SystemOutAdapter}.
	 * </p>
	 *
	 * @return An instance of {@link LoggerManager} based on the Spring application properties.
	 */
	@Bean
	public LoggerManager loggerManager() {
		final LoggerManager loggerManager = new LoggerManager();
		final List<LoggerInterface> loggersToRegister = autologProperties.getLoggers();

		// Try to register the loggers listed in configuration.
		// If no specific logger is configured or if all the configured ones are invalid (null), register standard
		// output as the default one.
		if (loggersToRegister != null) {
			loggersToRegister.stream()
				.filter(Objects::nonNull)
				.forEach(loggerManager::register);
		}

		if (loggerManager.getRegisteredLoggers().isEmpty()) {
			loggerManager.register(SystemOutAdapter.getInstance());
		}

		return loggerManager;
	}
}
