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

package com.github.maximevw.autolog.spring.configuration.converters;

import com.github.maximevw.autolog.core.configuration.LoggerInterfaceConfiguration;
import com.github.maximevw.autolog.core.logger.ConfigurableLoggerInterface;
import com.github.maximevw.autolog.core.logger.LogLevel;
import com.github.maximevw.autolog.core.logger.LoggerInterface;
import com.github.maximevw.autolog.core.logger.LoggingUtils;
import com.github.maximevw.autolog.core.logger.adapters.JdbcAdapter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.WordUtils;
import org.apiguardian.api.API;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Properties;

/**
 * A converter of objects of type {@link String} to instances of {@link LoggerInterface}.
 * <p>
 *     For classes implementing {@link ConfigurableLoggerInterface} and requiring an additional configuration, the
 *     latter is automatically converted into an instance of the appropriate implementation of
 *     {@link LoggerInterfaceConfiguration} using a Spring-managed {@link Converter} bean named as following:
 *     <pre>
 *         {class name of implementation of ConfigurableLoggerInterface}ConfigurationConverter
 *     </pre>
 *     For example, the class {@link JdbcAdapter} implementing {@link LoggerInterfaceConfiguration} has the class
 *     {@link JdbcAdapterConfigurationConverter} as a such converter.
 * </p>
 * <p>
 *     The mentioned additional configurations are specified between parentheses immediately after the logger adapter
 *     class name converted into a instance of {@link LoggerInterface} by this class and formatted as a
 *     semi-colon-separated list of properties using the format {@code key=value}. Note that the line feed character
 *     can also be used to separate the items of this list. These values are parsed as {@link Properties} to be
 *     converted into instances of {@link LoggerInterfaceConfiguration} thanks to the appropriate converter as
 *     explained above.
 * </p>
 *
 * @see Converter
 */
@API(status = API.Status.INTERNAL, since = "1.0.0")
@Component
@ConfigurationPropertiesBinding
public class LoggerInterfaceConverter implements Converter<String, LoggerInterface> {

	private static final String DEFAULT_ADAPTERS_PACKAGE = "com.github.maximevw.autolog.core.logger.adapters.";
	private static final String DEFAULT_GET_INSTANCE_METHOD = "getInstance";
	private static final String LOGGER_INTERFACE_REGEX = "^[a-zA-Z0-9.]*(\\((.*\n*)*\\))?$";
	private static final String CONFIGURATION_CONVERTER_CLASS_NAME_FORMAT = "%sConfigurationConverter";

	@Autowired
	private ApplicationContext applicationContext;

	@Override
	public LoggerInterface convert(@NonNull final String source) {
		if (StringUtils.isEmpty(source)) {
			LoggingUtils.report("Empty class name for logger interface, it will be skipped.", LogLevel.WARN);
			return null;
		}

		if (!source.matches(LOGGER_INTERFACE_REGEX)) {
			LoggingUtils.reportError("Invalid syntax for logger interface definition: " + source);
			return null;
		}

		// Try to get the Java class from the given name.
		// If the class name is followed by an additional configuration (between parentheses), separate it from the
		// configuration (which will be parse later).
		String loggerInterfaceClassName = source;
		String configurationExpression = null;
		final int configurationStartPosition = source.indexOf("(");
		if (configurationStartPosition > 0) {
			loggerInterfaceClassName = source.substring(0, configurationStartPosition);
			configurationExpression = source.substring(configurationStartPosition + 1, source.length() - 1);
		}
		if (!loggerInterfaceClassName.contains(".")) {
			loggerInterfaceClassName = StringUtils.prependIfMissing(loggerInterfaceClassName, DEFAULT_ADAPTERS_PACKAGE);
		}

		try {
			final Class<?> loggerInterfaceClass = Class.forName(loggerInterfaceClassName);
			if (LoggerInterface.class.isAssignableFrom(loggerInterfaceClass)) {
				final Method getInstanceMethod;

				// Manage configuration for loggers requiring additional configuration (i.e. implementing
				// ConfigurableLoggerInterface) separately.
				final LoggerInterfaceConfiguration configuration;
				if (ConfigurableLoggerInterface.class.isAssignableFrom(loggerInterfaceClass)) {
					getInstanceMethod = loggerInterfaceClass.getMethod(DEFAULT_GET_INSTANCE_METHOD,
						LoggerInterfaceConfiguration.class);
					configuration = parseLoggerInterfaceConfiguration(loggerInterfaceClass, configurationExpression);
					return (LoggerInterface) getInstanceMethod.invoke(LoggerInterfaceConfiguration.class,
						configuration);
				} else {
					getInstanceMethod = loggerInterfaceClass.getMethod(DEFAULT_GET_INSTANCE_METHOD);
					return (LoggerInterface) getInstanceMethod.invoke(null);
				}
			} else {
				LoggingUtils.report(loggerInterfaceClassName + " is not a valid logger: it doesn't implement "
					+ "LoggerInterface.", LogLevel.WARN);
			}
		} catch (final ClassNotFoundException e) {
			LoggingUtils.reportError("Unable to find logger interface: " + loggerInterfaceClassName, e);
		} catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
			LoggingUtils.reportError("Unable to get an instance of logger interface: " + loggerInterfaceClassName, e);
		}

		return null;
	}

	/**
	 * Converts a {@link String} containing the additional configuration of an implementation of
	 * {@link ConfigurableLoggerInterface} into the corresponding implementation of
	 * {@link LoggerInterfaceConfiguration}.
	 *
	 * @param loggerInterface 			The class of the {@link ConfigurableLoggerInterface} implementation.
	 * @param configurationExpression 	The expression containing the additional configuration to convert.
	 * @param <C> 						Type of the target implementation of {@link LoggerInterfaceConfiguration}.
	 * @return The implementation of {@link LoggerInterfaceConfiguration} corresponding to the provided configuration
	 * 		   expression for the given implementation of {@link ConfigurableLoggerInterface}.
	 */
	@SuppressWarnings("unchecked")
	private <C extends LoggerInterfaceConfiguration> C parseLoggerInterfaceConfiguration(
		final Class<?> loggerInterface, final String configurationExpression) {
		// Define the name of the Converter bean used to converted parsed properties to the expected type of
		// LoggerInterfaceConfiguration.
		final String converterBeanName = String.format(CONFIGURATION_CONVERTER_CLASS_NAME_FORMAT,
			WordUtils.uncapitalize(loggerInterface.getSimpleName()));
		final Converter<Properties, C> converter = this.applicationContext.getBean(converterBeanName, Converter.class);

		final Properties parsedProperties = new Properties();
		if (StringUtils.isNotBlank(configurationExpression)) {
			try {
				parsedProperties.load(new StringReader(configurationExpression.replaceAll(";", "\n")));
			} catch (final IOException e) {
				LoggingUtils.reportError("Unable to parse the provided configuration for logger interface: "
					+ loggerInterface.getName(), e);
			}
		}

		return converter.convert(parsedProperties);
	}
}
