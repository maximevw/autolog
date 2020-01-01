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

/**
 * Available log levels.
 */
public enum LogLevel {
	/**
	 * The {@code TRACE} level designates finest-grained informational events.
	 */
	TRACE,
	/**
	 * The {@code DEBUG} level designates fine-grained informational events useful to debug an application.
	 */
	DEBUG,
	/**
	 * The {@code INFO} level designates coarse-grained informational messages highlighting the progress of the
	 * application.
	 */
	INFO,
	/**
	 * The {@code WARN} level designates potentially harmful events.
	 */
	WARN,
	/**
	 * The {@code ERROR} level designates any errors occurring during the execution of an application.
	 */
	ERROR
}
