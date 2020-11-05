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

package com.github.maximevw.autolog.core.logger.adapters;

import com.github.maximevw.autolog.core.logger.LoggerInterface;
import com.google.common.flogger.AbstractLogger;
import com.google.common.flogger.FluentLogger;
import com.google.common.flogger.LogContext;
import com.google.common.flogger.LoggingApi;
import com.google.common.flogger.MetadataKey;
import com.google.common.flogger.backend.LoggerBackend;
import com.google.common.flogger.backend.Platform;
import com.google.common.flogger.parser.DefaultPrintfMessageParser;
import com.google.common.flogger.parser.MessageParser;
import org.apiguardian.api.API;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.util.Map;
import java.util.logging.Level;

/**
 * This class wraps an instance of Flogger-based logger to be used by Autolog.
 */
@API(status = API.Status.EXPERIMENTAL, since = "1.3.0")
public class FloggerAdapter extends LoggerFactoryBasedAdapter<AbstractLogger<FluentLogger.Api>>
	implements LoggerInterface {

	private FloggerAdapter() {
		// Private constructor to force usage of singleton instance via the method getInstance().
	}

	/**
	 * Gets an instance of the Flogger-based logger wrapper for Autolog.
	 *
	 * @return A singleton instance of the Flogger-based logger wrapper.
	 */
	public static FloggerAdapter getInstance() {
		return FloggerAdapter.FloggerAdapterInstanceHolder.INSTANCE;
	}

	@Override
	public void trace(final String topic, final String format, final Object... arguments) {
		getLogger(topic).atFinest().log(formatLog(format, arguments));
	}

	@Override
	public void trace(final String topic, final String format, final Map<String, String> contextualData,
					  final Object... arguments) {
		addContextualDataToLogger(getLogger(topic).atFinest(), contextualData).log(formatLog(format, arguments));
	}

	@Override
	public void debug(final String topic, final String format, final Object... arguments) {
		getLogger(topic).atFine().log(formatLog(format, arguments));
	}

	@Override
	public void debug(final String topic, final String format, final Map<String, String> contextualData,
					  final Object... arguments) {
		addContextualDataToLogger(getLogger(topic).atFine(), contextualData).log(formatLog(format, arguments));
	}

	@Override
	public void info(final String topic, final String format, final Object... arguments) {
		getLogger(topic).atInfo().log(formatLog(format, arguments));
	}

	@Override
	public void info(final String topic, final String format, final Map<String, String> contextualData,
					 final Object... arguments) {
		addContextualDataToLogger(getLogger(topic).atInfo(), contextualData).log(formatLog(format, arguments));
	}

	@Override
	public void warn(final String topic, final String format, final Object... arguments) {
		getLogger(topic).atWarning().log(formatLog(format, arguments));
	}

	@Override
	public void warn(final String topic, final String format, final Map<String, String> contextualData,
					 final Object... arguments) {
		addContextualDataToLogger(getLogger(topic).atWarning(), contextualData).log(formatLog(format, arguments));
	}

	@Override
	public void error(final String topic, final String format, final Object... arguments) {
		getLogger(topic).atSevere().log(formatLog(format, arguments));
	}

	@Override
	public void error(final String topic, final String format, final Map<String, String> contextualData,
					  final Object... arguments) {
		addContextualDataToLogger(getLogger(topic).atSevere(), contextualData).log(formatLog(format, arguments));
	}

	private String formatLog(final String format, final Object... arguments) {
		final FormattingTuple formattingTuple = MessageFormatter.arrayFormat(format, arguments);
		return formattingTuple.getMessage();
	}

	/**
	 * Adds the provided data as contextual metadata to a {@link FluentLogger.Api} instance.
	 *
	 * @param loggerApi 	 The logger API used by Flogger.
	 * @param contextualData The data to store into the log context.
	 * @return A {@link FluentLogger.Api} instance with the contextual metadata.
	 */
	private FluentLogger.Api addContextualDataToLogger(final FluentLogger.Api loggerApi,
													   final Map<String, String> contextualData) {
		FluentLogger.Api api = loggerApi;
		for (final Map.Entry<String, String> contextValue : contextualData.entrySet()) {
			api = api.with(MetadataKey.single(contextValue.getKey(), String.class), contextValue.getValue());
		}
		return api;
	}

	/**
	 * Gets an instance of Flogger-based logger ({@link CustomNameFluentLogger}) with the given name.
	 *
	 * @param topic The logger name.
	 * @return The configured instance of Flogger-based logger ({@link CustomNameFluentLogger}).
	 */
	AbstractLogger<FluentLogger.Api> getLoggerInstance(final String topic) {
		return CustomNameFluentLogger.withLoggerName(topic);
	}

	private static class FloggerAdapterInstanceHolder {
		private static final FloggerAdapter INSTANCE = new FloggerAdapter();
	}

	/**
	 * A custom implementation of {@link AbstractLogger} based on the {@link FluentLogger} one but allowing the
	 * definition of a custom logger name.
	 *
	 * @see FluentLogger
	 */
	static class CustomNameFluentLogger extends AbstractLogger<FluentLogger.Api> {

		/**
		 * Singleton instance of the no-op API.
 		 */
		static final NoOp NO_OP = new NoOp();

		/**
		 * Constructs a new logger for the specified backend.
		 *
		 * @param backend The logger backend.
		 */
		CustomNameFluentLogger(final LoggerBackend backend) {
			super(backend);
		}

		/**
		 * Returns a new logger instance with the given name.
		 *
		 * @param topic The logger name.
		 * @return A logger instance with the given name.
		 */
		public static CustomNameFluentLogger withLoggerName(final String topic) {
			return new CustomNameFluentLogger(Platform.getBackend(topic));
		}

		@Override
		public FluentLogger.Api at(final Level level) {
			final boolean isLoggable = isLoggable(level);
			final boolean isForced = Platform.shouldForceLogging(getName(), level, isLoggable);
			if (isLoggable || isForced) {
				return new Context(level, isForced);
			} else {
				return NO_OP;
			}
		}

		/**
		 * The non-wildcard, fully specified, no-op API implementation. This is required to provide a
		 * no-op implementation whose type is compatible with this logger's API.
		 */
		private static final class NoOp extends LoggingApi.NoOp<FluentLogger.Api> implements FluentLogger.Api { }

		/**
		 * Logging context implementing the fully specified API for this logger.
		 */
		final class Context extends LogContext<CustomNameFluentLogger, FluentLogger.Api> implements FluentLogger.Api {
			private Context(final Level level, final boolean isForced) {
				super(level, isForced);
			}

			@Override
			protected CustomNameFluentLogger getLogger() {
				return CustomNameFluentLogger.this;
			}

			@Override
			protected FluentLogger.Api api() {
				return this;
			}

			@Override
			protected FluentLogger.Api noOp() {
				return NO_OP;
			}

			@Override
			protected MessageParser getMessageParser() {
				return DefaultPrintfMessageParser.getInstance();
			}
		}
	}
}
