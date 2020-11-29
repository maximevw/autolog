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

package com.github.maximevw.autolog.core.configuration.adapters;

import com.github.maximevw.autolog.core.configuration.LoggerInterfaceConfiguration;
import com.github.maximevw.autolog.core.logger.ConfigurableLoggerInterface;
import com.github.maximevw.autolog.core.logger.LogLevel;
import com.github.maximevw.autolog.core.logger.LoggingUtils;
import com.github.maximevw.autolog.core.logger.adapters.KafkaAdapter;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apiguardian.api.API;

import java.util.Collections;
import java.util.List;
import java.util.Properties;

/**
 * Configuration for {@link KafkaAdapter} implementation of {@link ConfigurableLoggerInterface}.
 */
@API(status = API.Status.EXPERIMENTAL, since = "1.3.0")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class KafkaAdapterConfiguration implements LoggerInterfaceConfiguration {

	/**
	 * Default client ID passed to the server when making requests.
	 */
	public static final String AUTOLOG_DEFAULT_KAFKA_CLIENT_ID = "AutologKafkaProducer";

	/**
	 * The strategy used to define the topic(s) in which the log events will be published. By default:
	 * {@link TopicStrategy#UNIQUE_TOPIC}.
	 */
	@Builder.Default
	private TopicStrategy topicStrategy = TopicStrategy.UNIQUE_TOPIC;

	/**
	 * The Kafka topic into which the log events will be published if the defined {@link TopicStrategy} (property
	 * {@link #getTopicStrategy()}) is {@link TopicStrategy#UNIQUE_TOPIC}. By default:
	 * {@value LoggingUtils#AUTOLOG_DEFAULT_TOPIC}.
	 */
	@Builder.Default
	private String topic = LoggingUtils.AUTOLOG_DEFAULT_TOPIC;

	/**
	 * A set of properties related to the configuration of a Kafka producer.
	 * <p>
	 *     If not defined, the producer will be setup using the properties {@link #getBootstrapServers()} and
	 *     {@link #getClientId()} as minimal configuration.
	 * </p>
	 *
	 * @see KafkaProducer
	 * @see ProducerConfig
	 */
	private Properties producerConfiguration;

	/**
	 * The list of {@code host:port} pairs used to establish the initial connections to the Kafka cluster.
	 * <p>
	 *     <i>Note:</i> If the property {@link #getProducerConfiguration()} doesn't contain any key
	 *     {@value ProducerConfig#BOOTSTRAP_SERVERS_CONFIG}, the value of this property will be used.
	 * </p>
	 */
	@Builder.Default
	private List<String> bootstrapServers = Collections.emptyList();

	/**
	 * The client ID passed to the server when making requests. By default, {@value #AUTOLOG_DEFAULT_KAFKA_CLIENT_ID}.
	 * <p>
	 *     <i>Note:</i> If the property {@link #getProducerConfiguration()} doesn't contain any key
	 *     {@value ProducerConfig#CLIENT_ID_CONFIG}, the value of this property will be used.
	 * </p>
	 */
	@Builder.Default
	private String clientId = AUTOLOG_DEFAULT_KAFKA_CLIENT_ID;

	/**
	 * Strategies used to define the Kafka topic(s) in which the log events will be published.
	 */
	public enum TopicStrategy {
		/**
		 * Use {@code UNIQUE_TOPIC} to publish all the log events into a unique Kafka topic.
		 *
		 * @see #getTopicStrategy()
		 * @see #getTopic()
		 */
		UNIQUE_TOPIC,
		/**
		 * Use {@code SPECIFIED_TOPICS} to use the configured logger name for each log event as the target Kafka topic.
		 */
		SPECIFIED_TOPICS,
		/**
		 * Use {@code BY_LEVEL_TOPICS} to publish the log events into a Kafka topic corresponding to the log level.
		 *
		 * @see LogLevel
		 */
		BY_LEVEL_TOPICS
	}
}
