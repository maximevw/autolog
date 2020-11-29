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
import com.github.maximevw.autolog.core.logger.LogLevel;
import com.github.maximevw.autolog.core.logger.LoggingUtils;
import com.salesforce.kafka.test.junit5.SharedKafkaTestResource;
import com.salesforce.kafka.test.listeners.PlainListener;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Properties;
import java.util.stream.Stream;

import static com.github.maximevw.autolog.core.configuration.adapters.KafkaAdapterConfiguration.TopicStrategy.BY_LEVEL_TOPICS;
import static com.github.maximevw.autolog.core.configuration.adapters.KafkaAdapterConfiguration.TopicStrategy.SPECIFIED_TOPICS;
import static com.github.maximevw.autolog.core.configuration.adapters.KafkaAdapterConfiguration.TopicStrategy.UNIQUE_TOPIC;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doCallRealMethod;
import static org.mockito.Mockito.mock;

/**
 * Unit tests for the class {@link KafkaAdapter}.
 */
class KafkaAdapterTest {

	private static final ByteArrayOutputStream STD_OUT = new ByteArrayOutputStream();
	private static final ByteArrayOutputStream STD_ERR = new ByteArrayOutputStream();

	private static final String CUSTOM_LOG_CATEGORY = "customCategory";
	private static final String CUSTOM_UNIQUE_TOPIC = "customTopic";

	private static final KafkaAdapterConfiguration baseKafkaAdapterConfig = KafkaAdapterConfiguration.builder()
		.bootstrapServers(Collections.singletonList("localhost:9092"))
		.build();

	@RegisterExtension
	static final SharedKafkaTestResource sharedKafkaTestResource = new SharedKafkaTestResource()
		// Start the cluster on port 9092.
		.registerListener(new PlainListener().onPorts(9092))
		// Disable topic auto-creation. The topics requested for the tests are initialized in the method init().
		.withBrokerProperty("auto.create.topics.enable", "false");

	/**
	 * Initializes the context for all the tests in this class.
	 */
	@BeforeAll
	static void init() {
		// Redirect standard system output and standard error output.
		final PrintStream outStream = new PrintStream(STD_OUT);
		final PrintStream errStream = new PrintStream(STD_ERR);
		System.setOut(outStream);
		System.setErr(errStream);

		// Setup topics in the Kafka cluster.
		sharedKafkaTestResource.getKafkaTestUtils().createTopic(CUSTOM_UNIQUE_TOPIC, 1, (short) 1);
		sharedKafkaTestResource.getKafkaTestUtils().createTopic(CUSTOM_LOG_CATEGORY, 1, (short) 1);
		sharedKafkaTestResource.getKafkaTestUtils().createTopic(LogLevel.TRACE.name(), 1, (short) 1);
		sharedKafkaTestResource.getKafkaTestUtils().createTopic(LogLevel.DEBUG.name(), 1, (short) 1);
		sharedKafkaTestResource.getKafkaTestUtils().createTopic(LogLevel.INFO.name(), 1, (short) 1);
		sharedKafkaTestResource.getKafkaTestUtils().createTopic(LogLevel.WARN.name(), 1, (short) 1);
		sharedKafkaTestResource.getKafkaTestUtils().createTopic(LogLevel.ERROR.name(), 1, (short) 1);
	}

	/**
	 * Verifies that an invalid configuration throws an {@link IllegalArgumentException}.
	 *
	 * @see KafkaAdapter#configure(LoggerInterfaceConfiguration)
	 */
	@Test
	void givenInvalidConfiguration_whenConfigureAdapter_throwsException() {

		final KafkaAdapter sut = KafkaAdapter.getInstance(baseKafkaAdapterConfig);
		final IllegalArgumentException illegalArgumentException = assertThrows(IllegalArgumentException.class,
			() -> sut.configure(mock(LoggerInterfaceConfiguration.class)));
		assertEquals("KafkaAdapter configuration is invalid.", illegalArgumentException.getMessage());
	}

	/**
	 * Verifies that a warning message is logged and nothing is done when the producer is {@code null}.
	 */
	@Test
	void givenNullProducer_whenLogEvent_logsWarningAndDoesNothing() {
		final KafkaAdapter sut = mock(KafkaAdapter.class);
		doCallRealMethod().when(sut).debug(anyString(), anyString());
		sut.debug(LoggingUtils.AUTOLOG_DEFAULT_TOPIC, "Test logging a message without Kafka producer.");
		assertThat(STD_OUT.toString(),
			containsString("[WARN] Autolog: No Kafka producer configured: unable to publish log event."));
	}

	/**
	 * Verifies that an error is logged if the publication of a log event to a Kafka topic throws an {@link Exception}.
	 */
	@Test
	void givenInvalidConfiguration_whenConfigureAdapter_logsError() {
		// Since the Kafka topic cannot be null, force it to null (with topic strategy set to UNIQUE_TOPIC) to simulate
		// an exception during the message publication.
		baseKafkaAdapterConfig.setTopic(null);
		final KafkaAdapter sut = KafkaAdapter.getInstance(baseKafkaAdapterConfig);

		sut.debug(LoggingUtils.AUTOLOG_DEFAULT_TOPIC, "Test logging a message without connection.");
		assertThat(STD_ERR.toString(),
			containsString("[ERROR] Autolog: Unable to publish log event in the specified Kafka topic."));
	}

	/**
	 * Verifies that any log event is published in the configured Kafka topic.
	 *
	 * @param logLevel	  	  The log level to use for the test.
	 * @param topicStrategy	  The topic strategy to use for the test.
	 * @param expectedTopic	  The expected topic into which the log event should be published.
	 * @param expectedValue	  The expected log message to retrieve into the topic.
	 */
	@ParameterizedTest
	@MethodSource("provideKafkaAdapterTestConfigurations")
	void givenValidConfiguration_whenLogEvent_publishesLogEventIntoKafkaTopics(
		final LogLevel logLevel, final KafkaAdapterConfiguration.TopicStrategy topicStrategy,
		final String expectedTopic, final String expectedValue) {
		// Configure the KafkaAdapter.
		final Properties producerConfig = new Properties();
		producerConfig.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
		final KafkaAdapter sut = KafkaAdapter.getInstance(
			KafkaAdapterConfiguration.builder()
				.producerConfiguration(producerConfig)
				.topicStrategy(topicStrategy)
				.topic(CUSTOM_UNIQUE_TOPIC)
				.build()
			);

		// Publish the log event.
		log(sut, logLevel);

		// Verify the message has been correctly published into the expected topic.
		final List<ConsumerRecord<String, String>> records = sharedKafkaTestResource.getKafkaTestUtils()
			.consumeAllRecordsFromTopic(expectedTopic, StringDeserializer.class, StringDeserializer.class);
		assertThat(records, hasItem(new HasConsumerRecordValue(expectedValue)));
	}

	/**
	 * Builds arguments for the parameterized tests relative to the logging by the class {@link KafkaAdapter}:
	 * {@link #givenValidConfiguration_whenLogEvent_publishesLogEventIntoKafkaTopics(LogLevel,
	 * KafkaAdapterConfiguration.TopicStrategy, String, String)}.
	 *
	 * @return The arguments to execute the different test cases.
	 */
	private static Stream<Arguments> provideKafkaAdapterTestConfigurations() {
		return Stream.of(
			Arguments.of(LogLevel.TRACE, UNIQUE_TOPIC, CUSTOM_UNIQUE_TOPIC, "[TRACE] This is a test: TRACE"),
			Arguments.of(LogLevel.DEBUG, UNIQUE_TOPIC, CUSTOM_UNIQUE_TOPIC, "[DEBUG] This is a test: DEBUG"),
			Arguments.of(LogLevel.INFO, SPECIFIED_TOPICS, CUSTOM_LOG_CATEGORY, "[INFO] This is a test: INFO"),
			Arguments.of(LogLevel.WARN, BY_LEVEL_TOPICS, LogLevel.WARN.name(), "[WARN] This is a test: WARN"),
			Arguments.of(LogLevel.ERROR, BY_LEVEL_TOPICS, LogLevel.ERROR.name(), "[ERROR] This is a test: ERROR")
		);
	}

	private static void log(final KafkaAdapter sut, final LogLevel logLevel) {
		// For each log level, publishes a log event using a custom log category (it can be ignored depending on the
		// defined topic strategy).
		switch (logLevel) {
			case TRACE:
				sut.trace(CUSTOM_LOG_CATEGORY, "This is a test: {}", logLevel);
				break;
			case DEBUG:
				sut.debug(CUSTOM_LOG_CATEGORY, "This is a test: {}", logLevel);
				break;
			case INFO:
				sut.info(CUSTOM_LOG_CATEGORY, "This is a test: {}", logLevel);
				break;
			case WARN:
				sut.warn(CUSTOM_LOG_CATEGORY, "This is a test: {}", logLevel);
				break;
			case ERROR:
				sut.error(CUSTOM_LOG_CATEGORY, "This is a test: {}", logLevel);
				break;
			default:
				fail("Unknown level of log: " + logLevel);
		}
	}

	/**
	 * Specific matcher for {@link ConsumerRecord#value()}.
	 */
	static class HasConsumerRecordValue extends BaseMatcher<ConsumerRecord<String, String>> {
		private final String valueToMatch;

		public HasConsumerRecordValue(final String valueToMatch) {
			this.valueToMatch = valueToMatch;
		}

		@SuppressWarnings("unchecked")
		@Override
		public boolean matches(Object item) {
			return Objects.equals(((ConsumerRecord<String, String>) item).value(), valueToMatch);
		}

		@Override
		public void describeTo(Description description) {
			description.appendText("a record with value ").appendValue(valueToMatch);
		}
	}
}
