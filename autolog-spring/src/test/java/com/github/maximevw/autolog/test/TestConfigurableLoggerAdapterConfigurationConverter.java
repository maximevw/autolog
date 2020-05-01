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

package com.github.maximevw.autolog.test;

import com.github.maximevw.autolog.core.configuration.LoggerInterfaceConfiguration;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.util.Properties;

/**
 * A converter of objects of type {@link Properties} to instances of {@link LoggerInterfaceConfiguration} designed for
 * the class {@link TestConfigurableLoggerAdapter} used in unit tests of Autolog Spring auto-configuration.
 */
@Component
@ConfigurationPropertiesBinding
public class TestConfigurableLoggerAdapterConfigurationConverter
	implements Converter<Properties, LoggerInterfaceConfiguration> {

	@Override
	public LoggerInterfaceConfiguration convert(@NonNull final Properties source) {
		return new LoggerInterfaceConfiguration() {
			// No specific implementation: for test purpose only.
		};
	}
}
