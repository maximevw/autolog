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

package com.github.maximevw.autolog.core.configuration;

import com.github.maximevw.autolog.core.annotations.AutoLogMethodInOut;
import com.github.maximevw.autolog.core.annotations.AutoLogMethodInput;
import com.github.maximevw.autolog.core.logger.LogLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.logstash.logback.argument.StructuredArguments;
import org.apache.logging.log4j.CloseableThreadContext;
import org.apiguardian.api.API;
import org.slf4j.MDC;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Configuration for auto-logging of input data of methods calls.
 */
@API(status = API.Status.STABLE, since = "1.0.0")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class MethodInputLoggingConfiguration {

    /**
     * The level of log to use. By default: {@code INFO}.
     */
    @Builder.Default
    private LogLevel logLevel = LogLevel.INFO;

    /**
     * The message template will be used to log the method input arguments. By default:
     * {@value AutoLogMethodInOut#INPUT_DEFAULT_MESSAGE_TEMPLATE}.
     * <p>
     *     The defined template must contain two placeholders (symbolized by "<code>{}</code>"):
     *     <ul>
     *         <li>the first one will be replaced by the called method name;</li>
     *         <li>the second one will be replaced by the list of the method input arguments.</li>
     *     </ul>
     * </p>
	 * <p>
	 *     <i>Note:</i> The message template syntax is the same as the one used by SLF4J.
	 * </p>
	 * @see org.slf4j.Logger
	 * @see org.slf4j.helpers.MessageFormatter
     */
    @Builder.Default
    private String messageTemplate = AutoLogMethodInOut.INPUT_DEFAULT_MESSAGE_TEMPLATE;

    /**
     * The list of input arguments excluded from auto-logging. By default, there is no excluded argument.
     * <p>
     *     <i>Important note:</i> This parameter will only work if your application is compiled in debug mode.
     *     Use {@code javac} with the {@code -g} argument to include debug information.
     * </p>
     */
    @Builder.Default
    private Set<String> excludedArguments = new HashSet<>();

    /**
     * The list of the only input arguments must be logged. By default, the values of all the input arguments
     * are logged.
     * <p>
     *     <i>Important note:</i> This parameter will only work if your application is compiled in debug mode.
     *     Use {@code javac} with the {@code -g} argument to include debug information.
     * </p>
     */
    @Builder.Default
    private Set<String> onlyLoggedArguments = new HashSet<>();

    /**
     * Whether the method name must be prefixed with the name of its enclosing class. By default: {@code true}.
     */
    @Builder.Default
    private boolean classNameDisplayed = true;

    /**
     * Whether the collections and maps data must be fully logged. If set to {@code false}, only the size of the
     * collections or maps is logged. By default: {@code false}.
     */
    @Builder.Default
    private boolean collectionsAndMapsExpanded = false;

    /**
     * The list of the arguments values to prettify in the generated logs using the format defined by the parameter
     * {@link #getPrettyFormat()}. By default, nothing is prettified.
     * <p>
     *     <i>Note 1:</i> If a value cannot be prettified, the value will be simply logged with a call to
     *     {@link Object#toString()}.
     * </p>
     * <p>
     *     <i>Note 2:</i> The arguments names set into this list will be taken into account only if your application is
     *     compiled in debug mode. Use {@code javac} with the {@code -g} argument to include debug information.
     * </p>
     */
    @Builder.Default
    private Set<String> prettifiedValues = new HashSet<>();

    /**
     * The format used to prettify the arguments values listed in the parameter {@link #getPrettifiedValues()}.
     * By default: {@link PrettyDataFormat#JSON}.
     */
    @Builder.Default
    private PrettyDataFormat prettyFormat = PrettyDataFormat.JSON;

	/**
	 * Whether the format of the logged message is fully structured (using the format defined by the parameter
	 * {@link #getPrettyFormat()}) and not "human readable" (that's to say using the message template defined by the
	 * parameter {@link #getMessageTemplate()}). By default: {@code false}.
	 * <p>
	 *     <i>Note: </i>When the structured format is active, the values of the following parameters are ignored:
	 *     {@link #getMessageTemplate()} and {@link #getPrettifiedValues()}.
	 * </p>
	 *
	 * @see com.github.maximevw.autolog.core.logger.MethodInputLogEntry
	 */
	@Builder.Default
	private boolean structuredMessage = false;

	/**
	 * Whether the input data should also be logged into the log context when it is possible (i.e. using {@link MDC}
	 * for SLF4J implementations, {@link StructuredArguments} for Logback with Logstash encoder or
	 * {@link CloseableThreadContext} for Log4j2). By default: {@code false}.
	 * <p>
	 *     The data stored in the log context are:
	 *     <ul>
	 *         <li>method name (property {@code invokedMethod})</li>
	 *         <li>the values of the method arguments in accordance with the rules defined by the other parameters of
	 *         the configuration.</li>
	 *     </ul>
	 *  </p>
	 */
	@API(status = API.Status.STABLE, since = "1.1.0")
	@Builder.Default
	private boolean dataLoggedInContext = false;

    /**
     * Builds a new instance of configuration for auto-logging of input data of methods calls based on an annotation
     * {@link AutoLogMethodInOut}.
     *
     * @param autoLogMethodInOut An annotation {@link AutoLogMethodInOut}.
     * @return A new instance of {@code MethodInputLoggingConfiguration}.
     */
    public static MethodInputLoggingConfiguration from(final AutoLogMethodInOut autoLogMethodInOut) {
        return MethodInputLoggingConfiguration.builder()
                .logLevel(autoLogMethodInOut.level())
                .messageTemplate(autoLogMethodInOut.inputMessageTemplate())
                .excludedArguments(Set.of(autoLogMethodInOut.exclude()))
                .onlyLoggedArguments(Set.of(autoLogMethodInOut.restrict()))
                .classNameDisplayed(autoLogMethodInOut.showClassName())
                .collectionsAndMapsExpanded(autoLogMethodInOut.expandCollectionsAndMaps())
                .prettifiedValues(Arrays.stream(autoLogMethodInOut.prettify())
                        .filter(argName -> !argName.equals(AutoLogMethodInOut.OUTPUT_DATA))
                        .collect(Collectors.toUnmodifiableSet()))
                .prettyFormat(autoLogMethodInOut.prettyFormat())
				.structuredMessage(autoLogMethodInOut.structuredMessage())
				.dataLoggedInContext(autoLogMethodInOut.logDataInContext())
                .build();
    }

    /**
     * Builds a new instance of configuration for auto-logging of input data of methods calls based on an annotation
     * {@link AutoLogMethodInput}.
     *
     * @param autoLogMethodInput An annotation {@link AutoLogMethodInput}.
     * @return A new instance of {@code MethodInputLoggingConfiguration}.
     */
    public static MethodInputLoggingConfiguration from(final AutoLogMethodInput autoLogMethodInput) {
        return MethodInputLoggingConfiguration.builder()
                .logLevel(autoLogMethodInput.level())
                .messageTemplate(autoLogMethodInput.messageTemplate())
                .excludedArguments(Set.of(autoLogMethodInput.exclude()))
                .onlyLoggedArguments(Set.of(autoLogMethodInput.restrict()))
                .classNameDisplayed(autoLogMethodInput.showClassName())
                .collectionsAndMapsExpanded(autoLogMethodInput.expandCollectionsAndMaps())
                .prettifiedValues(Set.of(autoLogMethodInput.prettify()))
                .prettyFormat(autoLogMethodInput.prettyFormat())
				.structuredMessage(autoLogMethodInput.structuredMessage())
				.dataLoggedInContext(autoLogMethodInput.logDataInContext())
                .build();
    }
}
