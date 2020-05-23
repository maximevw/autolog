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

package com.github.maximevw.autolog.core.logger.adapters;

import com.github.maximevw.autolog.core.logger.LoggerInterface;
import com.github.maximevw.autolog.core.logger.LoggingUtils;
import org.apache.commons.lang3.StringUtils;
import org.apiguardian.api.API;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * An abstract class for implementations of {@link LoggerInterface} wrapping loggers obtained through a logger factory.
 * <p>
 *     The loggers are returned by the method {@link #getLogger(String)} automatically setting the value
 *     {@value LoggingUtils#AUTOLOG_DEFAULT_TOPIC} if the provided name is {@code null} or blank.
 *     Subclasses have to implement the method {@link #getLoggerInstance(String)} to get a real instance of a logger
 *     of the parameterized type {@code L}.
 * </p>
 * <p>
 *     The loggers instances are obtained from the factory once, then stored into a "cache" map.
 * </p>
 *
 * @param <L> The class of logger returned by the method {@link #getLogger(String)}.
 */
@API(status = API.Status.INTERNAL, since = "1.2.0", consumers = "com.github.maximevw.autolog.core.logger.adapters.*")
abstract class LoggerFactoryBasedAdapter<L> {

	private final Map<String, L> loggersCache = new HashMap<>();

	/**
	 * Gets an instance of logger of type {@code L} (defined as parameterized type) with the given name.
	 * <p>
	 *     If the given name is {@code null} or blank, the default value {@value LoggingUtils#AUTOLOG_DEFAULT_TOPIC}
	 *     will be used.
	 * </p>
	 *
	 * @param topic The logger name.
	 * @return The configured instance of logger.
	 */
	L getLogger(final String topic) {
		final String safeTopic = StringUtils.defaultIfBlank(topic, LoggingUtils.AUTOLOG_DEFAULT_TOPIC);
		return Optional.ofNullable(loggersCache.get(safeTopic)).orElseGet(() -> {
			final L logger = getLoggerInstance(safeTopic);
			loggersCache.put(safeTopic, logger);
			return logger;
		});
	}

	/**
	 * Gets an instance of logger of type {@code L} (defined as parameterized type) with the given name.
	 *
	 * @param topic The logger name.
	 * @return The configured instance of logger.
	 */
	abstract L getLoggerInstance(String topic);

}
