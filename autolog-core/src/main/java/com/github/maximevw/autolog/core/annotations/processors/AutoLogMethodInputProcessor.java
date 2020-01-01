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

import com.github.maximevw.autolog.core.annotations.AutoLogMethodInOut;
import com.github.maximevw.autolog.core.annotations.AutoLogMethodInput;
import org.apiguardian.api.API;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

/**
 * Annotation processor for {@link AutoLogMethodInput}.
 * <p>
 *     It checks:
 *     <ul>
 *         <li>if the value of the parameter {@link AutoLogMethodInput#messageTemplate()} is correct, i.e. contains the
 *         expected number of placeholders.</li>
 *         <li>if the value of the parameter {@link AutoLogMethodInput#prettify()} doesn't contain values reserved to
 *         {@link AutoLogMethodInOut#prettify()} parameter: {@link AutoLogMethodInOut#ALL_DATA},
 *         {@link AutoLogMethodInOut#INPUT_DATA} or {@link AutoLogMethodInOut#OUTPUT_DATA}.</li>
 *         <li>if the values of parameters {@link AutoLogMethodInput#prettify()},
 *         {@link AutoLogMethodInput#messageTemplate()} are those by default when
 *         {@link AutoLogMethodInput#structuredMessage()} is {@code true} since they will be ignored in this case.</li>
 *     </ul>
 * </p>
 */
@API(status = API.Status.INTERNAL)
@SupportedAnnotationTypes("com.github.maximevw.autolog.core.annotations.AutoLogMethodInput")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class AutoLogMethodInputProcessor extends AbstractProcessor {

	@Override
	public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
		final Messager messager = processingEnv.getMessager();

		annotations.forEach(typeElement -> {
			roundEnv.getElementsAnnotatedWith(typeElement).forEach(element -> {
				AnnotationsProcessingUtils.checkPlaceholders(element, messager, AutoLogMethodInput.class,
					Map.of("messageTemplate", 2));

				AnnotationsProcessingUtils.checkIgnoredParameters(element, messager, AutoLogMethodInput.class,
					"structuredMessage", true, "messageTemplate", "prettify");

				// Check that the "prettify" parameter doesn't contain unexpected values reserved to AutoLogMethodInOut
				// annotation.
				final String[] prettifyParameterValues =
					Optional.ofNullable((String[]) AnnotationsProcessingUtils.getParameterValue(
						element.getAnnotation(AutoLogMethodInput.class), "prettify", messager)).orElse(new String[]{});

				Stream.of(AutoLogMethodInOut.ALL_DATA, AutoLogMethodInOut.INPUT_DATA, AutoLogMethodInOut.OUTPUT_DATA)
					.filter(unexpectedValue -> Set.of(prettifyParameterValues).contains(unexpectedValue))
					.forEach(unexpectedValue -> messager.printMessage(Diagnostic.Kind.WARNING,
						String.format("Parameter 'prettify' of @AutoLogMethodInput contains unexpected value: %s. It "
							+ "will be ignored.", unexpectedValue), element)
					);
			});
		});

		return false;
	}

}
