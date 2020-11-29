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
import org.apache.kafka.clients.producer.ProducerConfig;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.autoconfigure.kafka.KafkaProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Properties;

import static com.github.maximevw.autolog.core.configuration.adapters.KafkaAdapterConfiguration.AUTOLOG_DEFAULT_KAFKA_CLIENT_ID;
import static com.github.maximevw.autolog.core.logger.LoggingUtils.AUTOLOG_DEFAULT_TOPIC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the converter class {@link KafkaAdapterConfigurationConverter}.
 */
@ExtendWith(MockitoExtension.class)
class KafkaAdapterConfigurationConverterTest {

	private static final ByteArrayOutputStream STD_ERR = new ByteArrayOutputStream();

	@Spy
	private GenericApplicationContext applicationContext;

	@InjectMocks
	private KafkaAdapterConfigurationConverter sut;

	/**
	 * Initializes the context for all the tests in this class.
	 */
	@BeforeAll
	static void init() {
		// Redirect standard error output.
		final PrintStream errStream = new PrintStream(STD_ERR);
		System.setErr(errStream);
	}

	/**
	 * Initializes the context for each new test case: refresh the used Spring {@link ApplicationContext}.
	 */
	@BeforeEach
	void initEachTest() {
		this.applicationContext.refresh();
	}

	/**
	 * Verifies that an error is logged and an instance of {@link KafkaAdapterConfiguration} is returned when no
	 * specific producer configuration is specified in the given {@link Properties} and no bean of type
	 * {@link KafkaProperties} is available in the Spring {@link ApplicationContext}.
	 */
	@Test
	public void givenPropertiesWithoutProducerConfigurationAndNoKafkaPropertiesBeanInContext_whenConvert_logsError() {
		final KafkaAdapterConfiguration kafkaAdapterConfiguration = sut.convert(new Properties());

		assertDefaultKafkaAdapterConfiguration(kafkaAdapterConfiguration);
		assertThat(STD_ERR.toString(), containsString("[ERROR] Autolog: Unable to retrieve a KafkaProperties bean."));
	}

	/**
	 * Verifies that an error is logged and an instance of {@link KafkaAdapterConfiguration} is returned when a specific
	 * producer configuration is specified in the given {@link Properties} but there is no matching bean of type
	 * {@link KafkaProperties} available in the Spring {@link ApplicationContext}.
	 */
	@Test
	public void givenPropertiesWithProducerConfigAndNoMatchingKafkaPropertiesBeanInContext_whenConvert_logsError() {
		final Properties propertiesToConvert = new Properties();
		propertiesToConvert.setProperty("producerConfiguration", "badProducerConfig");
		applicationContext.registerBean("defaultKafkaProperties", KafkaProperties.class);

		final KafkaAdapterConfiguration kafkaAdapterConfiguration = sut.convert(propertiesToConvert);

		assertDefaultKafkaAdapterConfiguration(kafkaAdapterConfiguration);
		assertThat(STD_ERR.toString(), containsString("[ERROR] Autolog: Unable to retrieve a KafkaProperties bean "
			+ "matching name: badProducerConfig"));
	}

	/**
	 * Verifies that an instance of {@link KafkaAdapterConfiguration} is returned when a specific producer
	 * configuration is specified in the given {@link Properties} and a bean of type {@link KafkaProperties} with this
	 * name is available in the Spring {@link ApplicationContext}.
	 */
	@Test
	public void givenPropertiesWithProducerConfigAndMatchingKafkaPropertiesBeanInContext_whenConvert_returnsConfig() {
		final Properties propertiesToConvert = new Properties();
		propertiesToConvert.setProperty("producerConfiguration", "validProducerConfig");
		applicationContext.registerBean("validProducerConfig", KafkaProperties.class);

		final KafkaAdapterConfiguration kafkaAdapterConfiguration = sut.convert(propertiesToConvert);

		assertNotNull(kafkaAdapterConfiguration);
		assertNotNull(kafkaAdapterConfiguration.getProducerConfiguration());
	}

	/**
	 * Verifies that an instance of {@link KafkaAdapterConfiguration} is returned when no specific producer
	 * configuration is specified in the given {@link Properties} but it exists a bean of type {@link KafkaProperties}
	 * available in the Spring {@link ApplicationContext}.
	 */
	@Test
	public void givenPropertiesWithoutProducerConfigAndKafkaPropertiesInContext_whenConvert_returnsConfiguration() {
		final KafkaProperties kafkaPropertiesBean = new KafkaProperties();
		applicationContext.registerBean(KafkaProperties.class, () -> kafkaPropertiesBean);

		final KafkaAdapterConfiguration kafkaAdapterConfiguration = sut.convert(new Properties());

		assertNotNull(kafkaAdapterConfiguration);
		final Properties producerConfiguration = kafkaAdapterConfiguration.getProducerConfiguration();
		assertNotNull(producerConfiguration);
		assertEquals(kafkaPropertiesBean.getBootstrapServers(),
			producerConfiguration.get(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG));
		assertEquals(kafkaPropertiesBean.getProducer().getKeySerializer(),
			producerConfiguration.get(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG));
		assertEquals(kafkaPropertiesBean.getProducer().getValueSerializer(),
			producerConfiguration.get(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG));
	}

	/**
	 * Verifies that an instance of {@link KafkaAdapterConfiguration} is returned with the values specified in the
	 * given {@link Properties}.
	 */
	@Test
	public void givenPropertiesWithoutProducerConfig_whenConvert_returnsConfiguration() {
		final Properties propertiesToConvert = new Properties();
		propertiesToConvert.setProperty("bootstrapServers", "localhost:9092, localhost:9093");
		propertiesToConvert.setProperty("clientId", "AutologTestClient");
		propertiesToConvert.setProperty("topic", "TestTopic");
		propertiesToConvert.setProperty("topicStrategy",
			KafkaAdapterConfiguration.TopicStrategy.SPECIFIED_TOPICS.name());

		final KafkaAdapterConfiguration kafkaAdapterConfiguration = sut.convert(propertiesToConvert);

		assertNotNull(kafkaAdapterConfiguration);
		assertNull(kafkaAdapterConfiguration.getProducerConfiguration());
		assertThat(kafkaAdapterConfiguration.getBootstrapServers(),
			Matchers.hasItems("localhost:9092", "localhost:9093"));
		assertEquals("AutologTestClient", kafkaAdapterConfiguration.getClientId());
		assertEquals("TestTopic", kafkaAdapterConfiguration.getTopic());
		assertEquals(KafkaAdapterConfiguration.TopicStrategy.SPECIFIED_TOPICS,
			kafkaAdapterConfiguration.getTopicStrategy());
	}

	/**
	 * Asserts that the given {@link KafkaAdapterConfiguration} instance is the default one.
	 *
	 * @param kafkaAdapterConfiguration The configuration to verify.
	 */
	private static void assertDefaultKafkaAdapterConfiguration(
		final KafkaAdapterConfiguration kafkaAdapterConfiguration) {
		assertNotNull(kafkaAdapterConfiguration);
		assertNull(kafkaAdapterConfiguration.getProducerConfiguration());
		assertNotNull(kafkaAdapterConfiguration.getBootstrapServers());
		assertThat(kafkaAdapterConfiguration.getBootstrapServers(), Matchers.hasSize(0));
		assertEquals(AUTOLOG_DEFAULT_KAFKA_CLIENT_ID, kafkaAdapterConfiguration.getClientId());
		assertEquals(AUTOLOG_DEFAULT_TOPIC, kafkaAdapterConfiguration.getTopic());
		assertEquals(KafkaAdapterConfiguration.TopicStrategy.UNIQUE_TOPIC,
			kafkaAdapterConfiguration.getTopicStrategy());
	}

}
