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

package com.github.maximevw.autolog.core.logger;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.github.maximevw.autolog.core.annotations.AutoLogMethodInOut;
import com.github.maximevw.autolog.core.configuration.MethodInputLoggingConfiguration;
import com.github.maximevw.autolog.core.configuration.PrettyDataFormat;
import lombok.NonNull;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.apiguardian.api.API;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Internal class providing logging utilities.
 */
@API(status = API.Status.INTERNAL, consumers = "com.github.maximevw.autolog.core.*")
public final class LoggingUtils {

	/**
	 * Default category for loggers used by Autolog.
	 */
	public static final String AUTOLOG_DEFAULT_TOPIC = "Autolog";

	/**
	 * Comma delimiter for string representation of list of values.
	 */
	static final String LIST_ITEMS_DELIMITER = ", ";

	/**
	 * Formatter to auto-generate method argument name when the original name is not available.
	 * This formatter contains a placeholder for the argument index in the list of method parameters.
	 */
	static final String AUTO_GENERATED_ARG_NAME_FORMATTER = "$arg%d";
	/**
	 * The regular expression matching auto-generated arguments names.
	 * @see #AUTO_GENERATED_ARG_NAME_FORMATTER
	 */
	private static final String AUTO_GENERATED_ARG_NAME_REGEX = "^\\$arg\\d+$";

	private static final String UNKNOWN_METHOD_NAME = "anonymous";
	private static final String METHOD_NAME_FORMATTER = "%s.%s";
	private static final String ARG_VALUE_FORMATTER = "%s=%s";

	private static final String DURATION_PATTERN_DAYS = "d' day(s) 'H' h 'm' m 's' s 'S' ms'";
	private static final String DURATION_PATTERN_HOURS = "H' h 'm' m 's' s 'S' ms'";
	private static final String DURATION_PATTERN_MINUTES = "m' m 's' s 'S' ms'";
	private static final String DURATION_PATTERN_SECONDS = "s' s 'S' ms'";
	private static final String DURATION_PATTERN_MILLISECONDS = "S' ms'";

	/**
	 * Default {@code ObjectMapper} instance used to prettify logged data in JSON format.
	 */
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();
	/**
	 * Default {@code XmlMapper} instance used to prettify logged data in XML format.
	 */
	private static final XmlMapper XML_MAPPER = new XmlMapper();

	private LoggingUtils() {
		// Private constructor to hide it externally.
	}

	/**
	 * Logs a debug message in the standard output.
	 *
	 * @param message The message to log.
	 */
	public static void report(final String message) {
		report(message, LogLevel.DEBUG);
	}

	/**
	 * Logs a message prefixed with a specific log level in the standard output.
	 *
	 * @param message 	The message to log.
	 * @param level		The log level to use.
	 */
	public static void report(final String message, final LogLevel level) {
		System.out.println(String.format("[%s] Autolog: %s", level.name(), message));
	}

	/**
	 * Logs an error in the error output.
	 *
	 * @param message	The message to log.
	 */
	public static void reportError(final String message) {
		reportError(message, null);
	}

	/**
	 * Logs a {@link Throwable} in the error output.
	 *
	 * @param message	The message to log.
	 * @param throwable	The throwable to log.
	 */
	public static void reportError(final String message, final Throwable throwable) {
		System.err.println(String.format("[ERROR] Autolog: %s", message));
		if (throwable != null) {
			throwable.printStackTrace();
		}
	}

	/**
	 * Gets the qualified (if requested) method name.
	 *
	 * @param method                The method for which the name has to be retrieved.
	 * @param prefixWithClassName   Whether the method name must be prefixed by the declaring class name.
	 * @return The qualified method name if {@code prefixWithClassName} is {@code true}, the simple method name
	 *         otherwise.
	 */
	static String getMethodName(final Method method, final boolean prefixWithClassName) {
		final String methodName = StringUtils.defaultString(method.getName(), UNKNOWN_METHOD_NAME);
		return getMethodName(methodName, method.getDeclaringClass(), prefixWithClassName);
	}

	/**
	 * Gets the qualified (if requested) method name.
	 *
	 * @param methodName            The method name.
	 * @param clazz                 The declaring class corresponding to the method name.
	 * @param prefixWithClassName   Whether the method name must be prefixed by the declaring class name.
	 * @return The qualified method name if {@code prefixWithClassName} is {@code true}, the simple method name
	 *         otherwise.
	 */
	static String getMethodName(@NonNull final String methodName, final Class<?> clazz,
								final boolean prefixWithClassName) {
		if (prefixWithClassName) {
			return String.format(METHOD_NAME_FORMATTER, clazz.getSimpleName(), methodName);
		}
		return methodName;
	}

	/**
	 * Computes the logger name to use according to the given configuration.
	 * <p>
	 *     If the custom logger name is left blank, the default value {@value AUTOLOG_DEFAULT_TOPIC} will be returned
	 *     except if the caller class name must used. Otherwise, the custom logger name will be returned.
	 * </p>
	 *
	 * @param callerClass			The caller class.
	 * @param topic					The custom logger name (can be {@code null} or blank).
	 * @param useCallerClassName	Whether the caller class name must be used as logger name if no custom name is
	 *                              specified.
	 * @return The logger name to use.
	 */
	static String computeTopic(@NonNull final Class<?> callerClass, final String topic,
							   final boolean useCallerClassName) {
		if (useCallerClassName && StringUtils.isBlank(topic)) {
			return Optional.ofNullable(callerClass.getCanonicalName()).orElse(callerClass.getName());
		} else {
			return StringUtils.defaultIfBlank(topic, AUTOLOG_DEFAULT_TOPIC);
		}
	}

	/**
	 * Formats a collection of method input arguments.
	 * <p>
	 *     The output format is: <code>arg0=value0, arg1=value1, ...</code> (see {@link #ARG_VALUE_FORMATTER}).
	 * </p>
	 *
	 * @param argsList      The list of method arguments: each item is a pair where the key is the argument name.
	 * @param configuration The configuration used for logging.
	 * @return The comma-separated list of method input arguments.
	 */
	static String formatMethodArguments(final List<Pair<String, Object>> argsList,
										final MethodInputLoggingConfiguration configuration) {
		return argsList.stream()
			.filter(entry -> isLoggableArgument(entry.getKey(), configuration))
			.map(entry -> String.format(ARG_VALUE_FORMATTER, entry.getKey(),
				formatMethodArgumentIfRequired(entry.getKey(), entry.getValue(), configuration)))
			.collect(Collectors.joining(LIST_ITEMS_DELIMITER));
	}

	/**
	 * Formats and maps a collection of method input arguments.
	 *
	 * @param argsList		The list of method arguments: each item is a pair where the key is the argument name.
	 * @param configuration	The configuration used for logging.
	 * @return A map of arguments where the keys are the arguments names and the values the formatted arguments values.
	 */
	static Map<String, String> mapMethodArguments(final List<Pair<String, Object>> argsList,
												  final MethodInputLoggingConfiguration configuration) {
		return argsList.stream()
			.filter(entry -> isLoggableArgument(entry.getKey(), configuration))
			.collect(Collectors.toMap(
				Pair::getKey,
				entry -> formatData(entry.getValue(), configuration.getPrettyFormat(),
					configuration.isCollectionsAndMapsExpanded()))
			);
	}

	/**
	 * Checks whether the given argument can be logged based on the given configuration.
	 * <p>
	 *     If the argument name matches the auto-generated format ({@value #AUTO_GENERATED_ARG_NAME_FORMATTER}, used
	 *     when the original argument name is not available), the exclusion and restriction rules are ignored for this
	 *     argument.
	 * </p>
	 *
	 * @param argName       The argument name.
	 * @param configuration The configuration used for logging.
	 * @return {@code true} if the argument can be logged, {@code false} otherwise.
	 */
	private static boolean isLoggableArgument(final String argName,
											  final MethodInputLoggingConfiguration configuration) {
		final Set<String> excludedArguments = configuration.getExcludedArguments();
		final Set<String> onlyLoggedArguments = configuration.getOnlyLoggedArguments();

		return argName.matches(AUTO_GENERATED_ARG_NAME_REGEX)
			|| (!excludedArguments.contains(argName)
				&& (onlyLoggedArguments.isEmpty() || onlyLoggedArguments.contains(argName)));
	}

	/**
	 * Formats the method input argument value if required by the auto-logging configuration.
	 *
	 * @param argName       The argument name.
	 * @param argValue      The raw argument value.
	 * @param configuration The configuration used for logging.
	 * @return The formatted input argument.
	 */
	private static String formatMethodArgumentIfRequired(final String argName, final Object argValue,
														 final MethodInputLoggingConfiguration configuration) {
		PrettyDataFormat prettyFormat = null;
		if (configuration.getPrettifiedValues().contains(argName)
			|| configuration.getPrettifiedValues().contains(AutoLogMethodInOut.INPUT_DATA)
			|| configuration.getPrettifiedValues().contains(AutoLogMethodInOut.ALL_DATA)) {
			prettyFormat = configuration.getPrettyFormat();
		}
		return formatData(argValue, prettyFormat, configuration.isCollectionsAndMapsExpanded());
	}

	/**
	 * Formats an object to properly log it.
	 * <p>
	 *     By default, the method {@link Object#toString()} is applied except if the pretty formatting is required.
	 * </p>
	 * <p>
	 *     If the data to log is a collection or a map, only the size is logged, except if the logging of the full data
	 *     is explicitly required.
	 * </p>
	 *
	 * @param data                     The data to log.
	 * @param prettyFormat             The pretty format to apply when required. This value is nullable: when set to
	 *                                 {@code null}, no pretty formatting is applied.
	 * @param expandCollectionsAndMaps Whether the full data of collections and maps or only their size is logged.
	 * @return The formatted data to log.
	 */
	static String formatData(final Object data, final PrettyDataFormat prettyFormat,
							 final boolean expandCollectionsAndMaps) {
		if (data == null) {
			return "null";
		} else {
			final String formattedData = prettify(data, prettyFormat);

			// Deal with collections and maps.
			if (data instanceof Collection) {
				final int collectionSize = ((Collection<?>) data).size();
				String expandedCollection = StringUtils.EMPTY;
				if (expandCollectionsAndMaps && collectionSize > 0) {
					expandedCollection = String.format(": %s", formattedData);
				}
				return String.format("%d item(s)%s", collectionSize, expandedCollection);
			} else if (data instanceof Map) {
				final int mapSize = ((Map<?, ?>) data).size();
				String expandedMap = StringUtils.EMPTY;
				if (expandCollectionsAndMaps && mapSize > 0) {
					expandedMap = String.format(": %s", formattedData);
				}
				return String.format("%d key-value pair(s)%s", mapSize, expandedMap);
			} else {
				return formattedData;
			}
		}
	}

	/**
	 * Formats an object according to the given {@link PrettyDataFormat}.
	 * <p>
	 *     By default, the method {@link Object#toString()} is applied if no format is specified.
	 * </p>
	 *
	 * @param data		The data to format.
	 * @param format	The format to apply.
	 * @return The formatted data.
	 */
	static String prettify(final Object data, final PrettyDataFormat format) {
		if (PrettyDataFormat.JSON.equals(format)) {
			try {
				return OBJECT_MAPPER.writeValueAsString(data);
			} catch (final JsonProcessingException e) {
				report(String.format("Error during formatting of [%s] in JSON: %s", data.toString(),
					e.getMessage()), LogLevel.WARN);
			}
		} else if (PrettyDataFormat.XML.equals(format)) {
			try {
				return XML_MAPPER.writeValueAsString(data);
			} catch (final JsonProcessingException e) {
				report(String.format("Error during formatting of [%s] in XML: %s", data.toString(),
					e.getMessage()), LogLevel.WARN);
			}
		}

		return data.toString();
	}

	/**
	 * Formats a duration in a human readable way.
	 *
	 * @param duration The duration to format in milliseconds.
	 * @return The formatted duration.
	 */
	static String formatDuration(final long duration) {
		String pattern = DURATION_PATTERN_DAYS;
		if (duration < Duration.ofSeconds(1).toMillis()) {
			pattern = DURATION_PATTERN_MILLISECONDS;
		} else if (duration < Duration.ofMinutes(1).toMillis()) {
			pattern = DURATION_PATTERN_SECONDS;
		} else if (duration < Duration.ofHours(1).toMillis()) {
			pattern = DURATION_PATTERN_MINUTES;
		} else if (duration < Duration.ofDays(1).toMillis()) {
			pattern = DURATION_PATTERN_HOURS;
		}

		return DurationFormatUtils.formatDuration(duration, pattern);
	}

	/**
	 * Retrieves the path of an API endpoint corresponding to a monitored method.
	 * <p>
	 *     The path is built from the values of the annotations {@link Path} or {@link RequestMapping} (and the
	 *     variants) on the method and its enclosing class.
	 * </p>
	 *
	 * @param method				The method for which the path of an API endpoint has to be retrieved.
	 * @param fallbackName 			The fallback value to use if the path cannot be retrieved. If {@code null} or empty,
	 *                              the method name will be used as fallback value.
	 * @param prefixWithClassName 	Whether the method name must be prefixed by the declaring class name if the method
	 *                              name is the fallback value.
	 * @return The path of the API endpoint or the fallback value.
	 */
	static String retrieveApiPath(final Method method, final String fallbackName, final boolean prefixWithClassName) {
		final Class<?> enclosingClass = method.getDeclaringClass();
		final String apiPath = StringUtils.EMPTY
			.concat(retrieveApiPathOnElement(enclosingClass, Path.class, RequestMapping.class))
			.concat(retrieveApiPathOnElement(method, Path.class, RequestMapping.class, GetMapping.class,
				PostMapping.class, PutMapping.class, DeleteMapping.class, PatchMapping.class));

		if (StringUtils.isNotBlank(apiPath)) {
			// Prevent '//' at the beginning of the path (when the root path "/" is defined at class level).
			return apiPath.replaceAll("//", "/");
		} else if (StringUtils.isNotBlank(fallbackName)) {
			// If there is no API endpoint path, return the fallback name or, if empty, the method name.
			return fallbackName;
		}
		return LoggingUtils.getMethodName(method, prefixWithClassName);
	}

	/**
	 * Gets the API endpoint path from the JAX-RS or Spring Web annotations present on a class or a method.
	 *
	 * @param element		The annotated element (class or method).
	 * @param annotations	The acceptable annotations to retrieve on the element.
	 * @return The API endpoint path defined for the given element or an empty string if the path cannot be retrieved.
	 */
	@SafeVarargs
	private static String retrieveApiPathOnElement(final AnnotatedElement element,
												   final Class<? extends Annotation>... annotations) {
		final Class<? extends Annotation> annotation = atLeastOneAnnotationPresent(element, annotations);

		if (annotation != null) {
			String invokedAnnotationParameter = "value";

			try {
				final Annotation annotationInstance = element.getAnnotation(annotation);

				if (annotationInstance == null) {
					return StringUtils.EMPTY;
				}

				if (annotation.isAssignableFrom(Path.class)) {
					// For JAX-RS Path annotation.
					return (String) annotation.getMethod(invokedAnnotationParameter).invoke(annotationInstance);
				} else if (RequestMapping.class.getPackageName().equals(annotation.getPackageName())) {
					// For Spring Web annotations, the value()/path() annotation parameter is defined as String[].
					final String[] annotationValue = (String[]) annotation.getMethod(invokedAnnotationParameter)
						.invoke(annotationInstance);
					invokedAnnotationParameter = "path";
					final String[] annotationPathValue = (String[]) annotation.getMethod(invokedAnnotationParameter)
						.invoke(annotationInstance);
					if (annotationValue.length > 0) {
						return annotationValue[0];
					} else if (annotationPathValue.length > 0) {
						return annotationPathValue[0];
					}
				}
			} catch (final IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
				LoggingUtils.reportError(String.format("Unable to invoke %s() on annotation: %s.",
					invokedAnnotationParameter, annotation.getSimpleName()), e);
			}
		}

		return StringUtils.EMPTY;
	}

	/**
	 * Gets the first available annotation present on the given element among the acceptable annotations.
	 *
	 * @param element 		The annotated element (class or method).
	 * @param annotations	The acceptable annotations to retrieve on the element.
	 * @return The class of first available annotation present on the given element or {@code null} if none of the
	 *         acceptable annotations is present.
	 */
	@SafeVarargs
	private static Class<? extends Annotation> atLeastOneAnnotationPresent(
		final AnnotatedElement element, final Class<? extends Annotation>... annotations) {
		return Arrays.stream(annotations).filter(element::isAnnotationPresent).findFirst().orElse(null);
	}

	/**
	 * Retrieves the HTTP method of an API endpoint corresponding to a monitored method.
	 * <p>
	 *     It is based on :
	 *     <ul>
	 *         <li>the {@code javax.ws.rs} annotations ({@link GET}, {@link POST}, {@link PUT}, {@link DELETE},
	 * 	       {@link OPTIONS}, {@link HEAD} and {@link PATCH})</li>
	 * 	       <li>or the value of annotation parameter {@link RequestMapping#method()}</li>
	 * 	       <li>or the Spring Web annotations {@link GetMapping}, {@link PostMapping}, {@link PutMapping},
	 * 	       {@link DeleteMapping} and {@link PatchMapping}.</li>
	 *     </ul>
	 * </p>
	 *
	 * @param method The monitored method corresponding to an API endpoint.
	 * @return The HTTP method of the API endpoint or {@code null} if not defined.
	 */
	static String retrieveHttpMethod(final Method method) {
		if (isHttpMethodAnnotationPresent(method, RequestMethod.GET, GET.class, GetMapping.class)) {
			return "GET";
		} else if (isHttpMethodAnnotationPresent(method, RequestMethod.POST, POST.class, PostMapping.class)) {
			return "POST";
		} else if (isHttpMethodAnnotationPresent(method, RequestMethod.PUT, PUT.class, PutMapping.class)) {
			return "PUT";
		} else if (isHttpMethodAnnotationPresent(method, RequestMethod.DELETE, DELETE.class, DeleteMapping.class)) {
			return "DELETE";
		} else if (isHttpMethodAnnotationPresent(method, RequestMethod.PATCH, PATCH.class, PatchMapping.class)) {
			return "PATCH";
		} else if (isHttpMethodAnnotationPresent(method, RequestMethod.OPTIONS, OPTIONS.class)) {
			return "OPTIONS";
		} else if (isHttpMethodAnnotationPresent(method, RequestMethod.HEAD, HEAD.class)) {
			return "HEAD";
		}
		return null;
	}

	/**
	 * Checks whether one of the specified annotations or {@link RequestMapping} with given {@link RequestMethod} is
	 * present on the given method.
	 *
	 * @param method		The method on the which the annotations are checked.
	 * @param requestMethod The HTTP method to retrieve on {@link RequestMapping} if present.
	 * @param annotations	The annotations to check on the method.
	 * @return {@code true} if one of the specified annotations or {@link RequestMapping} with given
	 * {@link RequestMethod} is present on the given method, {@code false} otherwise.
	 */
	@SafeVarargs
	private static boolean isHttpMethodAnnotationPresent(final Method method,
														 final RequestMethod requestMethod,
														 final Class<? extends Annotation>... annotations) {
		return Arrays.stream(annotations).anyMatch(method::isAnnotationPresent)
			|| method.isAnnotationPresent(RequestMapping.class)
			&& ArrayUtils.contains(method.getAnnotation(RequestMapping.class).method(), requestMethod);
	}
}
