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

package com.github.maximevw.autolog.core.logger;

import com.github.maximevw.autolog.core.configuration.LoggerInterfaceConfiguration;
import org.apiguardian.api.API;

/**
 * Interface wrapping a real logger instance with a required additional configuration.
 * <p>
 *     Any class implementing this interface should implement the singleton pattern since the {@link LoggerManager}
 *     accepts to register only one instance of each available implementation of {@code LoggerInterface}. The method
 *     to implement in order to get a singleton instance is {@code getInstance(LoggerInterfaceConfiguration)}.
 * </p>
 * @see LoggerInterface
 */
@API(status = API.Status.EXPERIMENTAL, since = "1.1.0")
public interface ConfigurableLoggerInterface extends LoggerInterface {

	/**
	 * Setup the instance of {@code ConfigurableLoggerInterface} with the given configuration.
	 *
	 * @param configuration The additional configuration to apply.
	 */
	void configure(LoggerInterfaceConfiguration configuration);

}
