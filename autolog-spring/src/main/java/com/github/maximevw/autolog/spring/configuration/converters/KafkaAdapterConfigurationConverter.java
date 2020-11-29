/*-
 * #%L
 * Autolog Spring integration module
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

package com.github.maximevw.autolog.spring.configuration.converters;

import com.github.maximevw.autolog.core.configuration.adapters.KafkaAdapterConfiguration;
import com.github.maximevw.autolog.core.logger.LoggingUtils;
import com.github.maximevw.autolog.core.logger.adapters.KafkaAdapter;
import org.apache.commons.lang3.StringUtils;
import org.apiguardian.api.API;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesBinding;
import org.springframework.context.ApplicationContext;
import org.springframework.core.convert.converter.Converter;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * A converter of objects of type {@link Properties} to instances of {@link KafkaAdapterConfiguration}.
 * <p>
 *     Only the fields from {@link KafkaAdapterConfiguration} matching keys in the provided {@link Properties} are taken
 *     into account.
 * </p>
 * <p>
 *     If a property {@code producerConfiguration} is defined, the bean of type {@link KafkaProperties} (or any class
 *     extending it) with the name provided into this property will be used into {@link KafkaAdapterConfiguration}.
 *     If no name is provided, any bean of type {@link KafkaProperties} returned by the method
 *     {@link ApplicationContext#getBean(Class)} will be used. If no such bean exists, the property
 *     {@code bootstrapServers} must at least be defined to allow the usage of {@link KafkaAdapter}.
 * </p>
 *
 * @see Converter
 * @see LoggerInterfaceConverter
 */
@API(status = API.Status.EXPERIMENTAL, since = "1.3.0")
@Component
@ConfigurationPropertiesBinding
public class KafkaAdapterConfigurationConverter implements Converter<Properties, KafkaAdapterConfiguration> {

	@Autowired
	private ApplicationContext applicationContext;

	@Override
	public KafkaAdapterConfiguration convert(final Properties source) {
		final KafkaAdapterConfiguration kafkaAdapterConfiguration = new KafkaAdapterConfiguration();

		// Try to retrieve the Kafka producer configuration if defined:
		// - either a bean KafkaProperties (or any class extending it) with the given name in the ApplicationContext.
		// - or any KafkaProperties bean available in the ApplicationContext (generally using the configuration defined
		// into the properties 'spring.kafka.bootstrap-servers' and/or 'spring.kafka.producer.bootstrap-servers').
		final String producerConfigPropertyValue = source.getProperty("producerConfiguration");
		final Properties producerConfig = new Properties();
		try {
			final KafkaProperties kafkaProperties;
			if (StringUtils.isNotBlank(producerConfigPropertyValue)) {
				kafkaProperties = this.applicationContext.getBean(producerConfigPropertyValue, KafkaProperties.class);
			} else {
				kafkaProperties = this.applicationContext.getBean(KafkaProperties.class);
			}
			producerConfig.putAll(kafkaProperties.buildProducerProperties());
			kafkaAdapterConfiguration.setProducerConfiguration(producerConfig);
		} catch (final Exception e) {
			String errorMessage = "Unable to retrieve a KafkaProperties bean.";
			if (StringUtils.isNotBlank(producerConfigPropertyValue)) {
				errorMessage = "Unable to retrieve a KafkaProperties bean matching name: "
					+ producerConfigPropertyValue;
			}
			LoggingUtils.reportError(errorMessage, e);
		}

		// Parse the other valid properties for the KafkaAdapterConfiguration.
		final String bootstrapServersPropertyValue = source.getProperty("bootstrapServers");
		List<String> bootstrapServers = Collections.emptyList();
		if (StringUtils.isNotBlank(bootstrapServersPropertyValue)) {
			bootstrapServers = Arrays.stream(bootstrapServersPropertyValue.split(LoggingUtils.COMMA))
				.map(String::trim)
				.collect(Collectors.toList());
		}
		kafkaAdapterConfiguration.setBootstrapServers(bootstrapServers);

		final String clientIdPropertyValue = source.getProperty("clientId");
		if (StringUtils.isNotBlank(clientIdPropertyValue)) {
			kafkaAdapterConfiguration.setClientId(clientIdPropertyValue);
		}

		final String topicStrategyPropertyValue = source.getProperty("topicStrategy");
		if (StringUtils.isNotBlank(topicStrategyPropertyValue)) {
			kafkaAdapterConfiguration.setTopicStrategy(
				KafkaAdapterConfiguration.TopicStrategy.valueOf(topicStrategyPropertyValue));
		}

		final String topicPropertyValue = source.getProperty("topic");
		if (StringUtils.isNotBlank(topicPropertyValue)) {
			kafkaAdapterConfiguration.setTopic(topicPropertyValue);
		}

		return kafkaAdapterConfiguration;
	}

}
