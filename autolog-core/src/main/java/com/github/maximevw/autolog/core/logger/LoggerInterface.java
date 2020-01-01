/*-
 * #%L
 * Autolog core module
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

package com.github.maximevw.autolog.core.logger;

import org.apiguardian.api.API;

/**
 * Interface wrapping a real logger instance.
 * <p>
 *     Any class implementing this interface should implement the singleton pattern since the {@link LoggerManager}
 *     accepts to register only one instance of each available implementation of {@code LoggerInterface}.
 * </p>
 * <p>
 *     <i>Note: </i> The message template syntax used in the arguments {@code format} of the methods described in this
 *     interface is the same as the one used by SLF4J.
 * </p>
 * @see org.slf4j.Logger
 * @see org.slf4j.helpers.MessageFormatter
 */
@API(status = API.Status.STABLE, since = "1.0.0")
public interface LoggerInterface {

	/**
	 * Logs a message at the TRACE level according to the specified format and arguments.
	 *
	 * @param format    The format string.
	 * @param arguments The list of arguments.
	 */
	void trace(String format, Object... arguments);

	/**
	 * Logs a message at the DEBUG level according to the specified format and arguments.
	 *
	 * @param format    The format string.
	 * @param arguments The list of arguments.
	 */
	void debug(String format, Object... arguments);

	/**
	 * Logs a message at the INFO level according to the specified format and arguments.
	 *
	 * @param format    The format string.
	 * @param arguments The list of arguments.
	 */
	void info(String format, Object... arguments);

	/**
	 * Logs a message at the WARN level according to the specified format and arguments.
	 *
	 * @param format    The format string.
	 * @param arguments The list of arguments.
	 */
	void warn(String format, Object... arguments);

	/**
	 * Logs a message at the ERROR level according to the specified format and arguments.
	 *
	 * @param format    The format string.
	 * @param arguments The list of arguments.
	 */
	void error(String format, Object... arguments);

}
