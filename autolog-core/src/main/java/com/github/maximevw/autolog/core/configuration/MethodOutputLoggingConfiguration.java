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
import com.github.maximevw.autolog.core.annotations.AutoLogMethodOutput;
import com.github.maximevw.autolog.core.logger.LogLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apiguardian.api.API;

import java.util.Arrays;
import java.util.Objects;

/**
 * Configuration for auto-logging of output value of methods calls.
 */
@API(status = API.Status.STABLE, since = "1.0.0")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class MethodOutputLoggingConfiguration {

    /**
     * Default message template used to log exceptions and errors thrown during a method call.
     */
    public static final String THROWABLE_MESSAGE_TEMPLATE = "{} thrown: {}";

    /**
     * The level of log to use. By default: {@code INFO}.
     */
    @Builder.Default
    private LogLevel logLevel = LogLevel.INFO;

    /**
     * The message template will be used to log the method output value. By default:
	 * {@value AutoLogMethodInOut#OUTPUT_DEFAULT_MESSAGE_TEMPLATE}.
     * <p>
     *     The defined template must contain two placeholders (symbolized by "<code>{}</code>"):
     *     <ul>
     *         <li>the first one will be replaced by the called method name;</li>
     *         <li>the second one will be replaced by the method output value.</li>
     *     </ul>
     * </p>
	 * <p>
	 *     <i>Note:</i> The message template syntax is the same as the one used by SLF4J.
	 * </p>
	 * @see org.slf4j.Logger
	 * @see org.slf4j.helpers.MessageFormatter
     */
    @Builder.Default
    private String messageTemplate = AutoLogMethodInOut.OUTPUT_DEFAULT_MESSAGE_TEMPLATE;

    /**
     * The message template will be used to log the method exiting for methods having return type {@code void}.
     * By default: {@value AutoLogMethodInOut#VOID_OUTPUT_DEFAULT_MESSAGE_TEMPLATE}.
     * <p>
     *     The defined template must contain a placeholder (symbolized by "<code>{}</code>") will be replaced by the
     *     called method name.
     * </p>
	 * <p>
	 *     <i>Note:</i> The message template syntax is the same as the one used by SLF4J.
	 * </p>
	 * @see org.slf4j.Logger
	 * @see org.slf4j.helpers.MessageFormatter
     */
    @Builder.Default
    private String voidOutputMessageTemplate = AutoLogMethodInOut.VOID_OUTPUT_DEFAULT_MESSAGE_TEMPLATE;

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
     * The format used to prettify the output value (when it exists).
     * By default: {@link PrettyDataFormat#JSON}.
     */
    @Builder.Default
    private PrettyDataFormat prettyFormat = PrettyDataFormat.JSON;

    /**
     * Whether any thrown exception or error must be logged before being re-thrown. The log level used to log
     * the {@link Throwable} is {@link LogLevel#ERROR}. By default: {@code false}.
     */
    @Builder.Default
    private boolean throwableLogged = false;

	/**
	 * Whether the format of the logged message is fully structured (using the format defined by the parameter
	 * {@link #getPrettyFormat()}) and not "human readable" (that's to say using the message templates defined by the
	 * parameters {@link #getMessageTemplate()} and {@link #getVoidOutputMessageTemplate()}).
	 * <p>
	 *     <i>Note: </i>When the structured format is active, the values of the following parameters are ignored:
	 *     {@link #getMessageTemplate()} and {@link #getVoidOutputMessageTemplate()}.
	 * </p>
	 *
	 * @see com.github.maximevw.autolog.core.logger.MethodOutputLogEntry
	 */
	@Builder.Default
	private boolean structuredMessage = false;

    /**
     * Builds a new instance of configuration for auto-logging of output value of methods calls based on an annotation
     * {@link AutoLogMethodInOut}.
     *
     * @param autoLogMethodInOut An annotation {@link AutoLogMethodInOut}.
     * @return A new instance of {@code MethodOutputLoggingConfiguration}.
     */
    public static MethodOutputLoggingConfiguration from(final AutoLogMethodInOut autoLogMethodInOut) {
        final boolean prettifyOutput = Arrays.stream(autoLogMethodInOut.prettify())
                .anyMatch(argName -> Objects.equals(argName, AutoLogMethodInOut.OUTPUT_DATA)
                        || Objects.equals(argName, AutoLogMethodInOut.ALL_DATA));
        PrettyDataFormat prettyDataFormat = null;
        if (prettifyOutput) {
			prettyDataFormat = autoLogMethodInOut.prettyFormat();
		}

        return MethodOutputLoggingConfiguration.builder()
                .logLevel(autoLogMethodInOut.level())
                .messageTemplate(autoLogMethodInOut.outputMessageTemplate())
                .voidOutputMessageTemplate(autoLogMethodInOut.voidOutputMessageTemplate())
                .classNameDisplayed(autoLogMethodInOut.showClassName())
                .collectionsAndMapsExpanded(autoLogMethodInOut.expandCollectionsAndMaps())
                .prettyFormat(prettyDataFormat)
                .throwableLogged(autoLogMethodInOut.logThrowable())
				.structuredMessage(autoLogMethodInOut.structuredMessage())
                .build();
    }

    /**
     * Builds a new instance of configuration for auto-logging of output value of methods calls based on an annotation
     * {@link AutoLogMethodOutput}.
     *
     * @param autoLogMethodOutput An annotation {@link AutoLogMethodOutput}.
     * @return A new instance of {@code MethodOutputLoggingConfiguration}.
     */
    public static MethodOutputLoggingConfiguration from(final AutoLogMethodOutput autoLogMethodOutput) {
        return MethodOutputLoggingConfiguration.builder()
                .logLevel(autoLogMethodOutput.level())
                .messageTemplate(autoLogMethodOutput.messageTemplate())
                .voidOutputMessageTemplate(autoLogMethodOutput.voidOutputMessageTemplate())
                .classNameDisplayed(autoLogMethodOutput.showClassName())
                .collectionsAndMapsExpanded(autoLogMethodOutput.expandCollectionsAndMaps())
                .prettyFormat(autoLogMethodOutput.prettyFormat())
                .throwableLogged(autoLogMethodOutput.logThrowable())
				.structuredMessage(autoLogMethodOutput.structuredMessage())
                .build();
    }
}
