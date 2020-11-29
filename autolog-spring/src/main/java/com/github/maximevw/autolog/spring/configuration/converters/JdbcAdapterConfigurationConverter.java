/*-
 * #%L
 * Autolog Spring integration module
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

package com.github.maximevw.autolog.spring.configuration.converters;

import com.github.maximevw.autolog.core.configuration.adapters.JdbcAdapterConfiguration;
import com.github.maximevw.autolog.core.logger.LoggingUtils;
import org.apache.commons.lang3.StringUtils;
import org.apiguardian.api.API;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * A converter of objects of type {@link Properties} to instances of {@link JdbcAdapterConfiguration}.
 * <p>
 *     Only the fields from {@link JdbcAdapterConfiguration} matching keys in the provided {@link Properties} are taken
 *     into account.
 * </p>
 * <p>
 *     If a property {@code dataSource} is defined, the bean of type {@link DataSource} with the name provided into this
 *     property will be used into {@link JdbcAdapterConfiguration}. If no name is provided, any bean of type
 *     {@link DataSource} returned by the method {@link ApplicationContext#getBean(Class)} will be used.
 * </p>
 *
 * @see Converter
 * @see LoggerInterfaceConverter
 */
@API(status = API.Status.EXPERIMENTAL, since = "1.1.0")
@Component
@ConfigurationPropertiesBinding
public class JdbcAdapterConfigurationConverter implements Converter<Properties, JdbcAdapterConfiguration> {

	@Autowired
	private ApplicationContext applicationContext;

	@Override
	public JdbcAdapterConfiguration convert(final Properties source) {
		// Try to retrieve the DataSource bean with the given name in the ApplicationContext or any DataSource bean
		// available in the ApplicationContext.
		final String dataSourcePropertyValue = source.getProperty("dataSource");
		DataSource dataSourceBean = null;
		try {
			if (StringUtils.isNotBlank(dataSourcePropertyValue)) {
				dataSourceBean = this.applicationContext.getBean(dataSourcePropertyValue, DataSource.class);
			} else {
				dataSourceBean = this.applicationContext.getBean(DataSource.class);
			}
		} catch (final Exception e) {
			String errorMessage = "Unable to retrieve a DataSource bean.";
			if (StringUtils.isNotBlank(dataSourcePropertyValue)) {
				errorMessage = "Unable to retrieve a DataSource bean matching name: " + dataSourcePropertyValue;
			}
			LoggingUtils.reportError(errorMessage, e);
		}

		// Build an instance of JdbcAdapterConfiguration.
		return JdbcAdapterConfiguration.builder()
			.dataSource(dataSourceBean)
			.tablePrefix(source.getProperty("tablePrefix"))
			.build();
	}

}
