/*-
 * #%L
 * Autolog Spring integration module
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

package com.github.maximevw.autolog.spring.aspects;

import com.github.maximevw.autolog.core.annotations.AutoLogPerformance;
import com.github.maximevw.autolog.core.configuration.MethodPerformanceLoggingConfiguration;
import com.github.maximevw.autolog.core.logger.LogLevel;
import com.github.maximevw.autolog.core.logger.LoggerManager;
import com.github.maximevw.autolog.core.logger.LoggingUtils;
import com.github.maximevw.autolog.core.logger.MethodPerformanceLogEntry;
import com.github.maximevw.autolog.core.logger.MethodPerformanceLogger;
import com.github.maximevw.autolog.core.logger.performance.AdditionalDataProvider;
import com.github.maximevw.autolog.core.logger.performance.PerformanceTimer;
import org.apache.commons.lang3.StringUtils;
import org.apiguardian.api.API;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Spring AOP aspect allowing to automatically monitor and log the performance information of public methods annotated
 * with {@link AutoLogPerformance} or included in classes annotated with such annotations.
 * <p>
 * Be careful with usage of this aspect and please read
 * <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#aop">Spring AOP documentation</a>
 * and especially the section about <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#aop-understanding-aop-proxies">
 * proxy-based aspects</a> to be aware of the limitations implied by Spring AOP.
 * </p>
 */
@API(status = API.Status.STABLE, since = "1.0.0")
@Aspect
@Component
public class AutoLogPerformanceAspect {

	private MethodPerformanceLogger methodPerformanceLogger;

	/**
	 * Constructor.
	 * <p>
	 *     It autowires a bean of type {@link LoggerManager}.
	 * </p>
	 *
	 * @param loggerManager The logger manager instance used by the aspect.
	 */
	@Autowired
	public AutoLogPerformanceAspect(final LoggerManager loggerManager) {
		this.methodPerformanceLogger = new MethodPerformanceLogger(loggerManager);
	}

	/**
	 * Pointcut checking that the method is annotated with {@link AutoLogPerformance} in a class also annotated with
	 * {@link AutoLogPerformance}.
	 * <p>In this case, the generated log will use the parameters of the annotation at the method level.</p>
	 *
	 * @param pjp            		The proceeding join point (here the method).
	 * @param annotationPerformance The annotation indicating that the performance information have to be logged for
	 *                              this method.
	 * @return The execution result of the invoked method.
	 * @throws Throwable when the invoked method throws an exception.
	 */
	@Around(value = "@within(com.github.maximevw.autolog.core.annotations.AutoLogPerformance) && "
		+ "@annotation(annotationPerformance) && execution(* *(..))",
		argNames = "annotationPerformance")
	public Object aroundPerformanceLoggableClassAndMethod(final ProceedingJoinPoint pjp,
														  final AutoLogPerformance annotationPerformance)
		throws Throwable {
		return handleAroundPerformanceLoggableMethod(pjp,
			MethodPerformanceLoggingConfiguration.from(annotationPerformance));
	}

	/**
	 * Pointcut checking that the executed method is included in a class annotated with {@link AutoLogPerformance}.
	 *
	 * @param pjp            		The proceeding join point (here the method).
	 * @param annotationPerformance The annotation indicating that the performance information have to be logged for
	 *                              this method.
	 * @return The execution result of the invoked method.
	 * @throws Throwable when the invoked method throws an exception.
	 */
	@Around(value = "@within(annotationPerformance) && "
		+ "!@annotation(com.github.maximevw.autolog.core.annotations.AutoLogPerformance) && execution(* *(..))",
		argNames = "annotationPerformance")
	public Object aroundPerformanceLoggableClass(final ProceedingJoinPoint pjp,
												 final AutoLogPerformance annotationPerformance) throws Throwable {
		return handleAroundPerformanceLoggableMethod(pjp,
			MethodPerformanceLoggingConfiguration.from(annotationPerformance));
	}

	/**
	 * Pointcut checking that the executed method is annotated with {@link AutoLogPerformance}.
	 *
	 * @param pjp            		The proceeding join point (here the method).
	 * @param annotationPerformance The annotation indicating that the performance information have to be logged for
	 *                              this method.
	 * @return The execution result of the invoked method.
	 * @throws Throwable when the invoked method throws an exception.
	 */
	@Around(value = "!within(@com.github.maximevw.autolog.core.annotations.AutoLogPerformance *) && "
		+ "@annotation(annotationPerformance) && execution(* *(..))",
		argNames = "annotationPerformance")
	public Object aroundPerformanceLoggableMethod(final ProceedingJoinPoint pjp,
												  final AutoLogPerformance annotationPerformance) throws Throwable {
		return handleAroundPerformanceLoggableMethod(pjp,
			MethodPerformanceLoggingConfiguration.from(annotationPerformance));
	}

	/**
	 * Handles an advice to log performance data of the invoked method.
	 *
	 * @param pjp								The proceeding join point (here the method).
	 * @param performanceLoggingConfiguration	The performance configuration extracted from the handled annotation.
	 * @return The execution result of the invoked method.
	 * @throws Throwable when the invoked method throws an exception.
	 */
	private Object handleAroundPerformanceLoggableMethod(final ProceedingJoinPoint pjp,
														 final MethodPerformanceLoggingConfiguration
															 performanceLoggingConfiguration) throws Throwable {
		// Start the performance timer for the method.
		final Method method = ((MethodSignature) pjp.getSignature()).getMethod();
		final PerformanceTimer performanceTimer = methodPerformanceLogger.start(performanceLoggingConfiguration,
			method);

		// Execute the method.
		final Object result;
		try {
			result = pjp.proceed();
		} catch (final Throwable throwable) {
			performanceTimer.getPerformanceLogEntry().setFailed(true);
			throw throwable;
		} finally {
			// Update data of the performance timer if the method set some additional data during its execution.
			if (StringUtils.isNotBlank(performanceLoggingConfiguration.getAdditionalDataProvider())) {
				performanceTimer.setPerformanceLogEntry(retrieveAdditionalData(pjp, performanceLoggingConfiguration,
					performanceTimer.getPerformanceLogEntry()));
			}

			// Stop the performance timer for the method and log performance.
			methodPerformanceLogger.stopAndLog(performanceLoggingConfiguration, performanceTimer);
		}

		return result;
	}

	/**
	 * Retrieves additional data related to the execution of the method to monitor from the
	 * {@link AdditionalDataProvider} defined in the annotation {@link AutoLogPerformance} and enriches an existing
	 * {@link MethodPerformanceLogEntry} with them.
	 *
	 * @param pjp            				  The proceeding join point (here the method).
	 * @param performanceLoggingConfiguration The performance configuration extracted from the handled annotation.
	 * @param performanceLogEntry			  The performance log entry to enrich with additional data.
	 * @return The performance log entry enriched with the additional data related to the execution of the method to
	 *         monitor.
	 */
	private MethodPerformanceLogEntry retrieveAdditionalData(final ProceedingJoinPoint pjp,
															 final MethodPerformanceLoggingConfiguration
																 performanceLoggingConfiguration,
															 final MethodPerformanceLogEntry performanceLogEntry) {
		final MethodPerformanceLogEntry enrichedPerformanceLogEntry = performanceLogEntry.toBuilder().build();
		final String additionalDataProviderName = performanceLoggingConfiguration.getAdditionalDataProvider();

		try {
			final Class<?> declaringClass = pjp.getSourceLocation().getWithinType();
			final Field additionalDataProviderField = declaringClass.getDeclaredField(additionalDataProviderName);

			// The given variable must be assignable from type AdditionalDataProvider.
			if (additionalDataProviderField.getType().isAssignableFrom(AdditionalDataProvider.class)) {
				// Ensure the field is accessible.
				additionalDataProviderField.setAccessible(true);

				// Retrieve the additional data from the given provider and enrich the current log entry.
				final AdditionalDataProvider additionalDataProvider =
					(AdditionalDataProvider) additionalDataProviderField.get(pjp.getTarget());
				enrichedPerformanceLogEntry.addComments(additionalDataProvider.getComments().toArray(new String[0]));
				enrichedPerformanceLogEntry.setProcessedItems(additionalDataProvider.getProcessedItems());
			}
		} catch (final NoSuchFieldException | SecurityException | IllegalAccessException | IllegalArgumentException e) {
			LoggingUtils.report(
				String.format("Unable to retrieve additional data through provider %s for method %s: %s.",
					additionalDataProviderName, pjp.getSignature().getName(), e.getMessage()), LogLevel.WARN);
			return enrichedPerformanceLogEntry;
		}

		return enrichedPerformanceLogEntry;
	}
}
