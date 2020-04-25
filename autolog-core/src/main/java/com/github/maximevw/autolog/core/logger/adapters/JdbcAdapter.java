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
import com.github.maximevw.autolog.core.configuration.adapters.JdbcAdapterConfiguration;
import com.github.maximevw.autolog.core.logger.ConfigurableLoggerInterface;
import com.github.maximevw.autolog.core.logger.LogLevel;
import com.github.maximevw.autolog.core.logger.LoggingUtils;
import org.apache.commons.lang3.StringUtils;
import org.apiguardian.api.API;
import org.slf4j.helpers.FormattingTuple;
import org.slf4j.helpers.MessageFormatter;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;

/**
 * This class, designed to be used by Autolog, persists log events into a database using JDBC.
 *
 * <p>
 *     This adapter requires an additional configuration to define the {@link DataSource} used to store the log events.
 * </p>
 * <p>
 *     By default, the log events are stored into a table named {@code LOG_EVENTS} and having the following columns:
 *     <table border="1" summary="Columns of default table LOG_EVENTS">
 *         <tr>
 *             <th>Column name</th>
 *             <th>Type</th>
 *         </tr>
 *         <tr>
 *             <td>EVENT_TIMESTAMP</td>
 *             <td>TIMESTAMP</td>
 *         </tr>
 *         <tr>
 *             <td>TOPIC</td>
 *             <td>VARCHAR(min. 8 charactrers)</td>
 *         </tr>
 *         <tr>
 *             <td>LOG_LEVEL</td>
 *             <td>VARCHAR(min. 5 characters)</td>
 *         </tr>
 *         <tr>
 *             <td>MESSAGE</td>
 *             <td>VARCHAR(or LONGVARCHAR)</td>
 *         </tr>
 *     </table>
 *     <br>
 *     The name of the table can be prefixed by setting the property {@code tablePrefix} in
 *     {@link JdbcAdapterConfiguration}.
 * </p>
 * @see JdbcAdapterConfiguration
 */
@API(status = API.Status.EXPERIMENTAL, since = "1.1.0")
public class JdbcAdapter implements ConfigurableLoggerInterface {

	/**
	 * The table prefix pattern to replace in SQL queries.
	 */
	private static final String TABLE_PREFIX_PATTERN = "%PREFIX%";

	/**
	 * The default SQL statement used to persist a log event in database.
	 */
	private static final String SQL_STATEMENT_INSERT_LOG =
		"INSERT INTO " + TABLE_PREFIX_PATTERN + "LOG_EVENTS"
			+ "(EVENT_TIMESTAMP, TOPIC, LOG_LEVEL, MESSAGE) "
			+ "VALUES (?, ?, ?, ?)";

	private static final int IDX_EVENT_TIMESTAMP = 1;
	private static final int IDX_TOPIC = 2;
	private static final int IDX_LOG_LEVEL = 3;
	private static final int IDX_MESSAGE = 4;

	private DataSource dataSource;
	private String tablePrefix;

	private JdbcAdapter() {
		// Private constructor to force usage of singleton instance via the method
		// getInstance(LoggerInterfaceConfiguration).
	}

	/**
	 * Gets an instance of JDBC logger for Autolog.
	 *
	 * @param configuration The configuration required by this adapter.
	 * @return A singleton instance of JDBC logger.
	 */
	public static JdbcAdapter getInstance(final LoggerInterfaceConfiguration configuration) {
		final JdbcAdapter jdbcAdapter = JdbcAdapterInstanceHolder.INSTANCE;
		jdbcAdapter.configure(configuration);
		return jdbcAdapter;
	}

	@Override
	public void trace(final String format, final Object... arguments) {
		log(LogLevel.TRACE, format, arguments);
	}

	@Override
	public void debug(final String format, final Object... arguments) {
		log(LogLevel.DEBUG, format, arguments);
	}

	@Override
	public void info(final String format, final Object... arguments) {
		log(LogLevel.INFO, format, arguments);
	}

	@Override
	public void warn(final String format, final Object... arguments) {
		log(LogLevel.WARN, format, arguments);
	}

	@Override
	public void error(final String format, final Object... arguments) {
		log(LogLevel.ERROR, format, arguments);
	}

	@Override
	public void configure(final LoggerInterfaceConfiguration configuration) {
		if (configuration instanceof JdbcAdapterConfiguration) {
			final JdbcAdapterConfiguration jdbcAdapterConfiguration = (JdbcAdapterConfiguration) configuration;
			this.dataSource = jdbcAdapterConfiguration.getDataSource();
			// Set the table prefix as empty by default.
			this.tablePrefix = StringUtils.EMPTY;
			if (StringUtils.isNotBlank(jdbcAdapterConfiguration.getTablePrefix())) {
				this.tablePrefix = StringUtils.appendIfMissing(jdbcAdapterConfiguration.getTablePrefix(), "_");
			}
		} else {
			throw new IllegalArgumentException("JdbcAdapter configuration is invalid.");
		}
	}

	private void log(final LogLevel logLevel, final String format, final Object... arguments) {
		if (dataSource == null) {
			LoggingUtils.report("No data source configured: unable to persist log event.", LogLevel.WARN);
		} else {
			try (Connection connection = dataSource.getConnection()) {
				final FormattingTuple formattingTuple = MessageFormatter.arrayFormat(format, arguments);
				final String insertSqlQuery = SQL_STATEMENT_INSERT_LOG.replace(TABLE_PREFIX_PATTERN, this.tablePrefix);
				final PreparedStatement insertStmt = connection.prepareStatement(insertSqlQuery);
				insertStmt.setTimestamp(IDX_EVENT_TIMESTAMP, Timestamp.from(Instant.now()));
				insertStmt.setString(IDX_TOPIC, "Autolog");
				insertStmt.setString(IDX_LOG_LEVEL, logLevel.name());
				insertStmt.setString(IDX_MESSAGE, formattingTuple.getMessage());
				insertStmt.execute();
				connection.commit();
			} catch (final SQLException e) {
				LoggingUtils.reportError("Unable to persist log event in the specified database.", e);
			}
		}
	}

	private static class JdbcAdapterInstanceHolder {
		private static final JdbcAdapter INSTANCE = new JdbcAdapter();
	}

}
