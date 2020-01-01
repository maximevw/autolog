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
import lombok.Getter;
import lombok.Setter;
import org.apiguardian.api.API;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

/**
 * Spring configuration properties used to configure Autolog.
 */
@API(status = API.Status.STABLE, since = "1.0.0")
@Getter
@Setter
@ConfigurationProperties(prefix = "autolog")
public class AutologProperties {

	/**
	 * The list of classes implementing {@link LoggerInterface} to register as loggers in the {@link LoggerManager}
	 * bean.
	 * <p>
	 *     Each listed class should be designated by its fully qualified name, except those from the package
	 *     {@code com.github.maximevw.autolog.core.logger.adapters}.
	 * </p>
	 * <p>
	 *     Example of value for the property {@code autolog.loggers}:
	 *     <code>
	 *         Slf4jAdapter,com.foo.bar.MyCustomLoggerAdapter
	 *     </code>
	 * </p>
	 */
	private List<LoggerInterface> loggers;

}
