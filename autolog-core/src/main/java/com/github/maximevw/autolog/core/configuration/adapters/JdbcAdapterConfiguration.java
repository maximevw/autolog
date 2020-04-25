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

package com.github.maximevw.autolog.core.configuration.adapters;

import com.github.maximevw.autolog.core.configuration.LoggerInterfaceConfiguration;
import com.github.maximevw.autolog.core.logger.ConfigurableLoggerInterface;
import com.github.maximevw.autolog.core.logger.adapters.JdbcAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apiguardian.api.API;

import javax.sql.DataSource;

/**
 * Configuration for {@link JdbcAdapter} implementation of {@link ConfigurableLoggerInterface}.
 */
@API(status = API.Status.EXPERIMENTAL, since = "1.1.0")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class JdbcAdapterConfiguration implements LoggerInterfaceConfiguration {

	/**
	 * The data source corresponding to the database into which the log events will be persisted.
	 */
	private DataSource dataSource;

	/**
	 * The prefix of the table name used to store the log events.
	 * <p>
	 *     For exemple, if the prefix is {@code "APP"} or {@code "APP_"}, the table name will be {@code APP_LOG_EVENTS}.
	 * </p>
	 */
	private String tablePrefix;
}
