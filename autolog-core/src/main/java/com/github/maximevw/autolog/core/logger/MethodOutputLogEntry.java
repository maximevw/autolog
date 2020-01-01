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

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apiguardian.api.API;

import java.util.List;

/**
 * The loggable output data of a method call.
 */
@API(status = API.Status.INTERNAL, consumers = "com.github.maximevw.autolog.core.*")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class MethodOutputLogEntry {

	/**
	 * The name of the called method.
	 */
	private String calledMethod;

	/**
	 * The output value of the called method.
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Object outputValue;

	/**
	 * The class of the {@link Throwable} thrown by the called method (if any).
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private Class<? extends Throwable> thrown;

	/**
	 * The message of the {@link Throwable} thrown by the called method (if any).
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private String thrownMessage;

	/**
	 * The stack trace when a {@link Throwable} is thrown by the called method.
	 */
	@JsonInclude(JsonInclude.Include.NON_NULL)
	private List<String> stackTrace;

}
