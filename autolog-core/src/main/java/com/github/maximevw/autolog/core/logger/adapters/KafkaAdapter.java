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

import com.github.maximevw.autolog.core.configuration.LoggerInterfaceConfiguration;
import com.github.maximevw.autolog.core.configuration.adapters.KafkaAdapterConfiguration;
import com.github.maximevw.autolog.core.logger.ConfigurableLoggerInterface;
import com.github.maximevw.autolog.core.logger.LogLevel;
import com.github.maximevw.autolog.core.logger.LoggingUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apiguardian.api.API;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import java.util.Properties;

/**
 * This class, designed to be used by Autolog, publishes log events into Kafka topics.
 *
 * <p>
 *     This adapter requires an additional configuration to define where and how the log events are published.
 * </p>
 * <p>
 *     Be sure that the topic(s) into which the log events will be published exist(s) on the target Kafka server(s).
 *     You can set the property {@code auto.create.topics.enable} to {@code true} in your Kafka server configuration
 *     but it is not really recommended. So, preferentially adapt the topic strategy (property
 *     {@link KafkaAdapterConfiguration#getTopicStrategy()} to match your Kafka configuration.
 * </p>
 * <p>
 *     <i>Note:</i> This adapter currently doesn't support transactions, even if the configured Kafka producer accepts
 *     them.
 * </p>
 *
 * @see KafkaAdapterConfiguration
 */
@API(status = API.Status.EXPERIMENTAL, since = "1.3.0")
public class KafkaAdapter implements ConfigurableLoggerInterface {

	private KafkaProducer<String, String> kafkaProducer;
	private KafkaAdapterConfiguration.TopicStrategy topicStrategy;
	private String uniqueTopicName;

	private KafkaAdapter() {
		// Private constructor to force usage of singleton instance via the method
		// getInstance(LoggerInterfaceConfiguration).
	}

	/**
	 * Gets an instance of Kafka logger for Autolog.
	 *
	 * @param configuration The configuration required by this adapter.
	 * @return A singleton instance of Kafka logger.
	 */
	public static KafkaAdapter getInstance(final LoggerInterfaceConfiguration configuration) {
		final KafkaAdapter kafkaAdapter = KafkaAdapterInstanceHolder.INSTANCE;
		kafkaAdapter.configure(configuration);
		return kafkaAdapter;
	}

	@Override
	public void trace(final String topic, final String format, final Object... arguments) {
		log(topic, LogLevel.TRACE, format, arguments);
	}

	@Override
	public void debug(final String topic, final String format, final Object... arguments) {
		log(topic, LogLevel.DEBUG, format, arguments);
	}

	@Override
	public void info(final String topic, final String format, final Object... arguments) {
		log(topic, LogLevel.INFO, format, arguments);
	}

	@Override
	public void warn(final String topic, final String format, final Object... arguments) {
		log(topic, LogLevel.WARN, format, arguments);
	}

	@Override
	public void error(final String topic, final String format, final Object... arguments) {
		log(topic, LogLevel.ERROR, format, arguments);
	}

	@Override
	public void configure(final LoggerInterfaceConfiguration configuration) {
		if (configuration instanceof KafkaAdapterConfiguration) {
			final KafkaAdapterConfiguration kafkaAdapterConfiguration = (KafkaAdapterConfiguration) configuration;
			Properties kafkaProducerProperties = new Properties();

			// Setup the Kafka producer.
			if (kafkaAdapterConfiguration.getProducerConfiguration() != null) {
				kafkaProducerProperties = kafkaAdapterConfiguration.getProducerConfiguration();
			}
			kafkaProducerProperties.putIfAbsent(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,
				kafkaAdapterConfiguration.getBootstrapServers());
			kafkaProducerProperties.putIfAbsent(ProducerConfig.CLIENT_ID_CONFIG,
				kafkaAdapterConfiguration.getClientId());
			kafkaProducerProperties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG,
				StringSerializer.class.getName());
			kafkaProducerProperties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG,
				StringSerializer.class.getName());
			this.kafkaProducer = new KafkaProducer<>(kafkaProducerProperties);

			// Setup the topic strategy.
			this.topicStrategy = kafkaAdapterConfiguration.getTopicStrategy();
			if (this.topicStrategy == KafkaAdapterConfiguration.TopicStrategy.UNIQUE_TOPIC) {
				this.uniqueTopicName = kafkaAdapterConfiguration.getTopic();
			}
		} else {
			throw new IllegalArgumentException("KafkaAdapter configuration is invalid.");
		}
	}

	private void log(final String topic, final LogLevel logLevel, final String format, final Object... arguments) {
		if (kafkaProducer == null) {
			LoggingUtils.report("No Kafka producer configured: unable to publish log event.", LogLevel.WARN);
		} else {
			// Define the topic into which the log event will be published.
			final String targetTopic;
			switch (this.topicStrategy) {
				case UNIQUE_TOPIC:
					targetTopic = this.uniqueTopicName;
					break;
				case SPECIFIED_TOPICS:
					targetTopic = topic;
					break;
				case BY_LEVEL_TOPICS:
					targetTopic = logLevel.name();
					break;
				default:
					LoggingUtils.reportError(String.format("Invalid topic strategy for publishing to Kafka: %s",
						this.topicStrategy));
					return;
			}

			// Prepend the log level to the message.
			final String formatWithLogLevel = StringUtils.prependIfMissing(format,
				String.format("[%s] ", logLevel));
			final FormattingTuple formattingTuple = MessageFormatter.arrayFormat(formatWithLogLevel, arguments);

			// Send the log event to Kafka server(s).
			try {
				this.kafkaProducer.send(new ProducerRecord<>(targetTopic, formattingTuple.getMessage()));
			} catch (final Exception e) {
				LoggingUtils.reportError("Unable to publish log event in the specified Kafka topic.", e);
			}
		}
	}

	private static class KafkaAdapterInstanceHolder {
		private static final KafkaAdapter INSTANCE = new KafkaAdapter();
	}
}
