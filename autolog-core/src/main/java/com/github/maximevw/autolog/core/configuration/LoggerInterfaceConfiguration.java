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

package com.github.maximevw.autolog.core.configuration;

import com.github.maximevw.autolog.core.logger.ConfigurableLoggerInterface;
import org.apiguardian.api.API;

/**
 * Interface for any additional configuration required by the implementations of {@link ConfigurableLoggerInterface}.
 */
@API(status = API.Status.EXPERIMENTAL, since = "1.1.0")
public interface LoggerInterfaceConfiguration {

	// This interface currently doesn't specify any method.

}
