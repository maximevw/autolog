/*-
 * #%L
 * Autolog core module
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

package com.github.maximevw.autolog.core.annotations.processors;

import org.apache.commons.lang3.StringUtils;

import javax.annotation.processing.Messager;
import javax.lang.model.element.Element;
import javax.tools.Diagnostic;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;

/**
 * Utilities for annotations processors.
 */
final class AnnotationsProcessingUtils {

	private AnnotationsProcessingUtils() {
		// Private constructor to hide it externally.
	}

	/**
	 * Checks if a message template defined in an annotation parameter contains the expected number of placeholders
	 * (symbolized by <code>{}</code>).
	 *
	 * @param element 							The processed element.
	 * @param messager							The messager of the annotation processor.
	 * @param checkedAnnotation					The annotation class on which the validation is performed.
	 * @param expectedPlaceholdersByParameter	The expected number of placeholders for each parameter defined as key
	 *                                          of the map.
	 * @param <A>								Type of the annotation to validate.
	 */
	static <A extends Annotation> void checkPlaceholders(final Element element, final Messager messager,
														 final Class<A> checkedAnnotation,
														 final Map<String, Integer> expectedPlaceholdersByParameter) {
		final A annotationInstance = element.getAnnotation(checkedAnnotation);
		expectedPlaceholdersByParameter.forEach((parameterName, expectedNumber) -> {
			final String parameterValue = (String) getParameterValue(annotationInstance, parameterName, messager);
			final int found = StringUtils.countMatches(parameterValue, "{}");
			if (found != expectedNumber) {
				messager.printMessage(Diagnostic.Kind.ERROR,
					String.format("Parameter '%s' of @%s must contain exactly %d placeholder(s) but found: %d.",
						parameterName, checkedAnnotation.getSimpleName(), expectedNumber, found), element);
			}
		});
	}

	/**
	 * Gets the value of a given parameter in a given annotation instance.
	 *
	 * @param annotationInstance	The annotation instance.
	 * @param parameterName			The name of the parameter for which the value is retrieved.
	 * @param messager				The messager of the annotation processor.
	 * @param <A>					Type of the annotation.
	 * @return The value of the given parameter in the given annotation instance or {@code null} when the given
	 * 		   parameter could not be retrieved.
	 */
	static <A extends Annotation> Object getParameterValue(final A annotationInstance,
														   final String parameterName,
														   final Messager messager) {
		try {
			final Method parameterMethod = annotationInstance.getClass().getMethod(parameterName);
			return parameterMethod.invoke(annotationInstance);
		} catch (final NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
			messager.printMessage(Diagnostic.Kind.ERROR,
				String.format("Unable to get parameter on annotation @%s: %s",
					annotationInstance.getClass().getSimpleName(), e.getMessage()));
			return null;
		}
	}

	/**
	 * Checks if some annotation parameters (P) have kept their default values when a specific parameter has a given
	 * value causing the non-processing of the values defined in the set of parameters P.
	 *
	 * @param element 				The processed element.
	 * @param messager				The messager of the annotation processor.
	 * @param checkedAnnotation		The annotation class on which the validation is performed.
	 * @param whenParameterName		The parameter name for which a specific value causes the non-processing of other
	 *                              annotation parameters.
	 * @param whenValue				The specific value of parameter {@code whenParameterName} causing the non-processing
	 *                              of other annotation parameters.
	 * @param ignoredParameters		The parameter names ignored when the parameter {@code whenParameterName} is equal
	 *                              to {@code whenValue}.
	 * @param <A>					Type of the annotation to validate.
	 */
	static <A extends Annotation> void checkIgnoredParameters(final Element element, final Messager messager,
															  final Class<A> checkedAnnotation,
															  final String whenParameterName,
															  final Object whenValue,
															  final String... ignoredParameters) {
		final A annotationInstance = element.getAnnotation(checkedAnnotation);
		final String checkedAnnotationName = checkedAnnotation.getSimpleName();
		final Object actualParameterValue = getParameterValue(annotationInstance, whenParameterName, messager);
		if (Objects.equals(actualParameterValue, whenValue)) {
			Arrays.stream(ignoredParameters).forEach(ignoredParameter -> {
				try {
					final Object defaultValue = checkedAnnotation.getMethod(ignoredParameter).getDefaultValue();
					final Object actualIgnoredValue = getParameterValue(annotationInstance, ignoredParameter, messager);

					if (!isDefaultValue(actualIgnoredValue, defaultValue)) {
						messager.printMessage(Diagnostic.Kind.WARNING,
							String.format("Parameter '%s' of @%s will be ignored since parameter '%s' is set to %s.",
								ignoredParameter, checkedAnnotationName, whenParameterName, whenValue), element);
					}
				} catch (final NoSuchMethodException e) {
					messager.printMessage(Diagnostic.Kind.ERROR,
						String.format("Unable to get parameter on annotation @%s: %s", checkedAnnotationName,
							e.getMessage()));
				}
			});
		}
	}

	/**
	 * Checks whether a parameter value is equals to its default value.
	 * <p>This method takes into account all types of parameters including the arrays.</p>
	 *
	 * @param actualValue	The actual parameter value.
	 * @param defaultValue	The default parameter value.
	 * @return {@code true} when the parameter value is equal to its default value, {@code false} otherwise.
	 */
	private static boolean isDefaultValue(final Object actualValue, final Object defaultValue) {
		if (actualValue != null && defaultValue != null
			&& actualValue.getClass().isArray() && defaultValue.getClass().isArray()) {
			return Arrays.equals((Object[]) actualValue, (Object[]) defaultValue);
		}

		return Objects.equals(actualValue, defaultValue);
	}
}
