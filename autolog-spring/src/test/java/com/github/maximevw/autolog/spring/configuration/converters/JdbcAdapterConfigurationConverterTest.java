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

import com.github.maximevw.autolog.core.configuration.adapters.JdbcAdapterConfiguration;
import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.GenericApplicationContext;

import javax.sql.DataSource;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Properties;
import java.util.function.Supplier;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Unit tests for the converter class {@link JdbcAdapterConfigurationConverter}.
 */
@ExtendWith(MockitoExtension.class)
class JdbcAdapterConfigurationConverterTest {

	private static final ByteArrayOutputStream STD_ERR = new ByteArrayOutputStream();

	@Spy
	private GenericApplicationContext applicationContext;

	@InjectMocks
	private JdbcAdapterConfigurationConverter sut;

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
	 * Verifies that an error is logged and an instance of {@link JdbcAdapterConfiguration} is returned when no specific
	 * data source is specified in the given {@link Properties} and no bean of type {@link DataSource} is available in
	 * the Spring {@link ApplicationContext}.
	 */
	@Test
	public void givenPropertiesWithoutDataSourceAndNoDataSourceBeanInContext_whenConvert_logsError() {
		final JdbcAdapterConfiguration jdbcAdapterConfiguration = sut.convert(new Properties());

		assertNotNull(jdbcAdapterConfiguration);
		assertNull(jdbcAdapterConfiguration.getDataSource());
		assertNull(jdbcAdapterConfiguration.getTablePrefix());
		assertThat(STD_ERR.toString(), containsString("[ERROR] Autolog: Unable to retrieve a DataSource bean."));
	}

	/**
	 * Verifies that an error is logged and an instance of {@link JdbcAdapterConfiguration} is returned when a specific
	 * data source is specified in the given {@link Properties} but there is no matching bean of type {@link DataSource}
	 * available in the Spring {@link ApplicationContext}.
	 */
	@Test
	public void givenPropertiesWithDataSourceAndNoMatchingDataSourceBeanInContext_whenConvert_logsError() {
		final Properties propertiesToConvert = new Properties();
		propertiesToConvert.setProperty("dataSource", "badDataSource");
		applicationContext.registerBean("defaultDataSource", DataSource.class,
			(Supplier<DataSource>) HikariDataSource::new);

		final JdbcAdapterConfiguration jdbcAdapterConfiguration = sut.convert(propertiesToConvert);

		assertNotNull(jdbcAdapterConfiguration);
		assertNull(jdbcAdapterConfiguration.getDataSource());
		assertNull(jdbcAdapterConfiguration.getTablePrefix());
		assertThat(STD_ERR.toString(), containsString("[ERROR] Autolog: Unable to retrieve a DataSource bean matching "
			+ "name: badDataSource"));
	}

	/**
	 * Verifies that an instance of {@link JdbcAdapterConfiguration} is returned when no specific data source is
	 * specified in the given {@link Properties} but it exists a bean of type {@link DataSource} available in the Spring
	 * {@link ApplicationContext}.
	 */
	@Test
	public void givenPropertiesWithoutDataSourceAndDefaultDataSourceBeanInContext_whenConvert_returnsConfiguration() {
		applicationContext.registerBean(DataSource.class, (Supplier<DataSource>) HikariDataSource::new);

		final JdbcAdapterConfiguration jdbcAdapterConfiguration = sut.convert(new Properties());

		assertNotNull(jdbcAdapterConfiguration);
		assertNotNull(jdbcAdapterConfiguration.getDataSource());
		assertNull(jdbcAdapterConfiguration.getTablePrefix());
	}
}
