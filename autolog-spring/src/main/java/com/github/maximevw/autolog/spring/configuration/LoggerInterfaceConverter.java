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

import com.github.maximevw.autolog.core.logger.LogLevel;
import com.github.maximevw.autolog.core.logger.LoggerInterface;
import com.github.maximevw.autolog.core.logger.LoggingUtils;
import org.apache.commons.lang3.StringUtils;
import org.apiguardian.api.API;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * A converter of objects of type {@link String} to instances of {@link LoggerInterface}.
 *
 * @see Converter
 */
@API(status = API.Status.INTERNAL, since = "1.0.0")
@Component
@ConfigurationPropertiesBinding
public class LoggerInterfaceConverter implements Converter<String, LoggerInterface> {

	private static final String DEFAULT_ADAPTERS_PACKAGE = "com.github.maximevw.autolog.core.logger.adapters.";
	private static final String DEFAULT_GET_INSTANCE_METHOD = "getInstance";

	@Override
	public LoggerInterface convert(@NonNull final String from) {
		if (StringUtils.isEmpty(from)) {
			LoggingUtils.report("Empty class name for logger interface, it will be skipped.", LogLevel.WARN);
			return null;
		}

		// Try to get the Java class from the given name.
		String loggerInterfaceClassName = from;
		if (!from.contains(".")) {
			loggerInterfaceClassName = StringUtils.prependIfMissing(from, DEFAULT_ADAPTERS_PACKAGE);
		}

		try {
			final Class<?> loggerInterfaceClass = Class.forName(loggerInterfaceClassName);
			if (LoggerInterface.class.isAssignableFrom(loggerInterfaceClass)) {
				final Method getInstanceMethod = loggerInterfaceClass.getMethod(DEFAULT_GET_INSTANCE_METHOD);
				return (LoggerInterface) getInstanceMethod.invoke(null);
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
}
