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

import com.github.maximevw.autolog.core.annotations.AutoLogMethodInOut;
import com.github.maximevw.autolog.core.annotations.AutoLogMethodInput;
import com.github.maximevw.autolog.core.annotations.AutoLogMethodOutput;
import com.github.maximevw.autolog.core.configuration.MethodInputLoggingConfiguration;
import com.github.maximevw.autolog.core.configuration.MethodOutputLoggingConfiguration;
import com.github.maximevw.autolog.core.logger.LoggerManager;
import com.github.maximevw.autolog.core.logger.MethodCallLogger;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;

/**
 * Spring AOP aspect allowing to automatically log the input and output data of public methods annotated with
 * {@link AutoLogMethodInOut}, {@link AutoLogMethodInput} and/or {@link AutoLogMethodOutput} or included in classes
 * annotated with such annotations.
 * <p>
 * Be careful with usage of this aspect and please read
 * <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#aop">Spring AOP documentation</a>
 * and especially the section about <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#aop-understanding-aop-proxies">
 * proxy-based aspects</a> to be aware of the limitations implied by Spring AOP.
 * </p>
 */
@Aspect
@Component
public class AutoLogMethodInOutAspect {

	private MethodCallLogger methodCallLogger;

	/**
	 * Constructor.
	 * <p>
	 *     It autowires a bean of type {@link LoggerManager}.
	 * </p>
	 *
	 * @param loggerManager The logger manager instance used by the aspect.
	 */
	@Autowired
	public AutoLogMethodInOutAspect(final LoggerManager loggerManager) {
		this.methodCallLogger = new MethodCallLogger(loggerManager);
	}

	/**
	 * Pointcut checking that the method is annotated with {@link AutoLogMethodInOut} in a class annotated with
	 * {@link AutoLogMethodInOut}, {@link AutoLogMethodInput} or {@link AutoLogMethodOutput}.
	 * <p>In this case, the generated log will use the parameters of the annotation at the method level.</p>
	 *
	 * @param pjp          		The proceeding join point (here the method).
	 * @param annotationInOut	The annotation indicating that the input and output data have to be logged for this
	 *                          method.
	 * @return The execution result of the invoked method.
	 * @throws Throwable when the invoked method throws an exception.
	 */
	@Around(value = "(@within(com.github.maximevw.autolog.core.annotations.AutoLogMethodInOut) || "
		+ "@within(com.github.maximevw.autolog.core.annotations.AutoLogMethodInput) || "
		+ "@within(com.github.maximevw.autolog.core.annotations.AutoLogMethodOutput))"
		+ "&& @annotation(annotationInOut) && execution(* *(..))",
		argNames = "annotationInOut")
	public Object aroundDataInOutLoggableClassAndMethod(final ProceedingJoinPoint pjp,
														final AutoLogMethodInOut annotationInOut) throws Throwable {
		return handleAroundDataInOutLoggableMethod(pjp, annotationInOut);
	}

	/**
	 * Pointcut checking that the executed method has no {@code AutoLogMethod*} annotation and is included in a class
	 * annotated with {@link AutoLogMethodInOut}.
	 *
	 * @param pjp          		The proceeding join point (here the method).
	 * @param annotationInOut	The annotation indicating that the input and output data have to be logged for this
	 *                          method.
	 * @return The execution result of the invoked method.
	 * @throws Throwable when the invoked method throws an exception.
	 */
	@Around(value = "@within(annotationInOut) "
		+ "&& !@annotation(com.github.maximevw.autolog.core.annotations.AutoLogMethodInOut) "
		+ "&& !@annotation(com.github.maximevw.autolog.core.annotations.AutoLogMethodInput)"
		+ "&& !@annotation(com.github.maximevw.autolog.core.annotations.AutoLogMethodOutput) && execution(* *(..))",
		argNames = "annotationInOut")
	public Object aroundDataInOutLoggableClass(final ProceedingJoinPoint pjp, final AutoLogMethodInOut annotationInOut)
		throws Throwable {
		return handleAroundDataInOutLoggableMethod(pjp, annotationInOut);
	}

	/**
	 * Pointcut checking that the executed method is annotated with {@link AutoLogMethodInOut}.
	 *
	 * @param pjp          		The proceeding join point (here the method).
	 * @param annotationInOut	The annotation indicating that the input and output data have to be logged for this
	 *                          method.
	 * @return The execution result of the invoked method.
	 * @throws Throwable when the invoked method throws an exception.
	 */
	@Around(value = "!within(@com.github.maximevw.autolog.core.annotations.AutoLogMethodInOut *) "
		+ "&& @annotation(annotationInOut) && execution(* *(..))",
		argNames = "annotationInOut")
	public Object aroundDataInOutLoggableMethod(final ProceedingJoinPoint pjp, final AutoLogMethodInOut annotationInOut)
		throws Throwable {
		return handleAroundDataInOutLoggableMethod(pjp, annotationInOut);
	}

	/**
	 * Pointcut checking that the method is annotated with {@link AutoLogMethodInput} in a class annotated with
	 * {@link AutoLogMethodInOut} or {@link AutoLogMethodInput}.
	 * <p>In this case, the generated log will use the parameters of the annotation at the method level.</p>
	 *
	 * @param jp          		The join point (here the method).
	 * @param annotationInput	The annotation indicating that the input data have to be logged for this method.
	 * @throws Throwable when the invoked method throws an exception.
	 */
	@Before(value = "(@within(com.github.maximevw.autolog.core.annotations.AutoLogMethodInput) || "
		+ "@within(com.github.maximevw.autolog.core.annotations.AutoLogMethodInOut))"
		+ "&& @annotation(annotationInput) && execution(* *(..))",
		argNames = "annotationInput")
	public void beforeDataInputLoggableClassAndMethod(final JoinPoint jp, final AutoLogMethodInput annotationInput)
		throws Throwable {
		handleBeforeDataInputLoggableMethod(jp, annotationInput);
	}

	/**
	 * Pointcut checking that the executed method has no {@link AutoLogMethodInput} annotation and is included in a
	 * class annotated with {@link AutoLogMethodInput}.
	 *
	 * @param jp          		The join point (here the method).
	 * @param annotationInput	The annotation indicating that the input data have to be logged for this method.
	 * @throws Throwable when the invoked method throws an exception.
	 */
	@Before(value = "@within(annotationInput) "
		+ "&& !@annotation(com.github.maximevw.autolog.core.annotations.AutoLogMethodInput) && execution(* *(..))",
		argNames = "annotationInput")
	public void beforeDataInputLoggableClass(final JoinPoint jp, final AutoLogMethodInput annotationInput)
		throws Throwable {
		handleBeforeDataInputLoggableMethod(jp, annotationInput);
	}

	/**
	 * Pointcut checking that the executed method is annotated with {@link AutoLogMethodInput} but not with
	 * {@link AutoLogMethodInOut}.
	 *
	 * @param jp          		The join point (here the method).
	 * @param annotationInput	The annotation indicating that the input data have to be logged for this method.
	 * @throws Throwable when the invoked method throws an exception.
	 */
	@Before(value = "!within(@com.github.maximevw.autolog.core.annotations.AutoLogMethodInput *) "
		+ "&& !@annotation(com.github.maximevw.autolog.core.annotations.AutoLogMethodInOut)"
		+ "&& @annotation(annotationInput) && execution(* *(..))",
		argNames = "annotationInput")
	public void beforeDataInputLoggableMethod(final JoinPoint jp, final AutoLogMethodInput annotationInput)
		throws Throwable {
		handleBeforeDataInputLoggableMethod(jp, annotationInput);
	}

	/**
	 * Pointcut checking that the method is annotated with {@link AutoLogMethodOutput} in a class annotated with
	 * {@link AutoLogMethodInOut} or {@link AutoLogMethodOutput}.
	 * <p>In this case, the generated log will use the parameters of the annotation at the method level.</p>
	 *
	 * @param pjp          		The proceeding join point (here the method).
	 * @param annotationOutput	The annotation indicating that the output data have to be logged for this method.
	 * @return The execution result of the invoked method.
	 * @throws Throwable when the invoked method throws an exception.
	 */
	@Around(value = "(@within(com.github.maximevw.autolog.core.annotations.AutoLogMethodOutput) || "
		+ "@within(com.github.maximevw.autolog.core.annotations.AutoLogMethodInOut))"
		+ "&& @annotation(annotationOutput) && execution(* *(..))",
		argNames = "annotationOutput")
	public Object aroundDataOutputLoggableClassAndMethod(final ProceedingJoinPoint pjp,
														 final AutoLogMethodOutput annotationOutput) throws Throwable {
		return handleAroundDataOutputLoggableMethod(pjp, annotationOutput);
	}

	/**
	 * Pointcut checking that the executed method has no {@link AutoLogMethodOutput} annotation and is included in a
	 * class annotated with {@link AutoLogMethodOutput}.
	 *
	 * @param pjp          		The proceeding join point (here the method).
	 * @param annotationOutput	The annotation indicating that the output data have to be logged for this method.
	 * @return The execution result of the invoked method.
	 * @throws Throwable when the invoked method throws an exception.
	 */
	@Around(value = "@within(annotationOutput) "
		+ "&& !@annotation(com.github.maximevw.autolog.core.annotations.AutoLogMethodOutput) && execution(* *(..))",
		argNames = "annotationOutput")
	public Object aroundDataOutputLoggableClass(final ProceedingJoinPoint pjp,
												final AutoLogMethodOutput annotationOutput) throws Throwable {
		return handleAroundDataOutputLoggableMethod(pjp, annotationOutput);
	}

	/**
	 * Pointcut checking that the executed method is annotated with {@link AutoLogMethodOutput} but not with
	 * {@link AutoLogMethodInOut}.
	 *
	 * @param pjp          		The proceeding join point (here the method).
	 * @param annotationOutput	The annotation indicating that the output data have to be logged for this method.
	 * @return The execution result of the invoked method.
	 * @throws Throwable when the invoked method throws an exception.
	 */
	@Around(value = "!within(@com.github.maximevw.autolog.core.annotations.AutoLogMethodOutput *) "
		+ "&& !@annotation(com.github.maximevw.autolog.core.annotations.AutoLogMethodInOut)"
		+ "&& @annotation(annotationOutput) && execution(* *(..))",
		argNames = "annotationOutput")
	public Object aroundDataOutputLoggableMethod(final ProceedingJoinPoint pjp,
												 final AutoLogMethodOutput annotationOutput) throws Throwable {
		return handleAroundDataOutputLoggableMethod(pjp, annotationOutput);
	}

	/**
	 * Handles an advice to log output data of the invoked method.
	 *
	 * @param jp          			The join point (here the method).
	 * @param autoLogMethodInput 	The annotation indicating that the input data have to be logged for this method.
	 * @throws Throwable when the invoked method throws an exception.
	 */
	private void handleBeforeDataInputLoggableMethod(final JoinPoint jp, final AutoLogMethodInput autoLogMethodInput)
		throws Throwable {
		handleAroundDataInOutLoggableMethod(jp, MethodInputLoggingConfiguration.from(autoLogMethodInput), null);
	}

	/**
	 * Handles an advice to log output data of the invoked method.
	 *
	 * @param pjp          			The proceeding join point (here the method).
	 * @param autoLogMethodOutput 	The annotation indicating that the output data have to be logged for this method.
	 * @return The execution result of the invoked method.
	 * @throws Throwable when the invoked method throws an exception.
	 */
	private Object handleAroundDataOutputLoggableMethod(final ProceedingJoinPoint pjp,
														final AutoLogMethodOutput autoLogMethodOutput)
		throws Throwable {
		return handleAroundDataInOutLoggableMethod(pjp, null,
			MethodOutputLoggingConfiguration.from(autoLogMethodOutput));
	}

	/**
	 * Handles an advice to log input and output data of the invoked method.
	 *
	 * @param pjp          			The proceeding join point (here the method).
	 * @param autoLogMethodInOut 	The annotation indicating that the input and output data have to be logged for this
	 *                              method.
	 * @return The execution result of the invoked method.
	 * @throws Throwable when the invoked method throws an exception.
	 */
	private Object handleAroundDataInOutLoggableMethod(final ProceedingJoinPoint pjp,
													   final AutoLogMethodInOut autoLogMethodInOut) throws Throwable {
		return handleAroundDataInOutLoggableMethod(pjp, MethodInputLoggingConfiguration.from(autoLogMethodInOut),
			MethodOutputLoggingConfiguration.from(autoLogMethodInOut));
	}

	/**
	 * Handles an advice to log input and/or output data of the invoked method.
	 *
	 * @param jp							The join point (here the method).
	 * @param inputLoggingConfiguration		The method input logging configuration extracted from the handled
	 *                                      annotation. It can be {@code null} if only the output data must be logged.
	 * @param outputLoggingConfiguration    The method output logging configuration extracted from the handled
	 *                                      annotation. It can be {@code null} if only the input data must be logged.
	 * @return The execution result of the invoked method or {@code null} if only the input data must be logged and the
	 *         type of the handled advice is "before execution".
	 * @throws Throwable when the invoked method throws an exception.
	 */
	private Object handleAroundDataInOutLoggableMethod(
		final JoinPoint jp, final MethodInputLoggingConfiguration inputLoggingConfiguration,
		final MethodOutputLoggingConfiguration outputLoggingConfiguration) throws Throwable {
		final Method method = ((MethodSignature) jp.getSignature()).getMethod();

		// Log input data.
		if (inputLoggingConfiguration != null) {
			methodCallLogger.logMethodInput(inputLoggingConfiguration, method, jp.getArgs());
		}

		if (jp instanceof ProceedingJoinPoint && outputLoggingConfiguration != null) {
			// Execute the method.
			final Object output;
			try {
				output = ((ProceedingJoinPoint) jp).proceed();
			} catch (final Throwable throwable) {
				if (outputLoggingConfiguration.isThrowableLogged()) {
					methodCallLogger.logThrowable(outputLoggingConfiguration, method, throwable);
				}
				throw throwable;
			}

			// Log output data.
			methodCallLogger.logMethodOutput(outputLoggingConfiguration, method, output);

			return output;
		}

		return null;
	}
}
