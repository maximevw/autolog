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
import com.github.maximevw.autolog.core.annotations.AutoLogMethodOutput;
import org.apiguardian.api.API;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Annotation processor for {@link AutoLogMethodInOut}.
 * <p>
 *     It checks:
 *     <ul>
 *         <li>if there are annotations {@link AutoLogMethodInput} and/or {@link AutoLogMethodOutput} at the same level
 *         and generates a warning if such annotations are found.</li>
 *         <li>if the values of parameters {@link AutoLogMethodInOut#inputMessageTemplate()},
 *         {@link AutoLogMethodInOut#outputMessageTemplate()} and {@link AutoLogMethodInOut#voidOutputMessageTemplate()}
 *         are correct, i.e. contains the expected number of placeholders.</li>
 *         <li>if the values of parameters {@link AutoLogMethodInOut#prettify()},
 *         {@link AutoLogMethodInOut#inputMessageTemplate()}, {@link AutoLogMethodInOut#outputMessageTemplate()} and
 *         {@link AutoLogMethodInOut#voidOutputMessageTemplate()} are those by default when
 *         {@link AutoLogMethodInOut#structuredMessage()} is {@code true} since they will be ignored in this case.</li>
 *     </ul>
 * </p>
 */
@API(status = API.Status.INTERNAL)
@SupportedAnnotationTypes("com.github.maximevw.autolog.core.annotations.AutoLogMethodInOut")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class AutoLogMethodInOutProcessor extends AbstractProcessor {

	@Override
	public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
		final Messager messager = processingEnv.getMessager();

		annotations.forEach(typeElement -> {
			roundEnv.getElementsAnnotatedWith(typeElement).forEach(element -> {
				checkConflicts(element, messager);
				AnnotationsProcessingUtils.checkPlaceholders(element, messager, AutoLogMethodInOut.class,
					Map.of("inputMessageTemplate", 2, "outputMessageTemplate", 2, "voidOutputMessageTemplate", 1));
				AnnotationsProcessingUtils.checkIgnoredParameters(element, messager, AutoLogMethodInOut.class,
					"structuredMessage", true, "inputMessageTemplate", "outputMessageTemplate",
					"voidOutputMessageTemplate", "prettify");
			});
		});

		return false;
	}

	/**
	 * Checks potential conflicts between annotations {@link AutoLogMethodInOut}, {@link AutoLogMethodInput} and
	 * {@link AutoLogMethodOutput} at the same level (class or method).
	 *
	 * @param element   The processed element.
	 * @param messager	The messager of the annotation processor.
	 */
	private static void checkConflicts(final Element element, final Messager messager) {
		final String elementKind = element.getKind().name().toLowerCase();
		final String elementName = element.getSimpleName().toString();

		// Check existence of specific @SuppressWarnings "AutoLogMethodAnnotationsConflict".
		final SuppressWarnings suppressWarnings = Optional.ofNullable(element.getAnnotation(SuppressWarnings.class))
			.orElse(element.getEnclosingElement().getAnnotation(SuppressWarnings.class));
		final boolean ignoreConflictWarning = suppressWarnings != null
			&& Arrays.asList(suppressWarnings.value()).contains("AutoLogMethodAnnotationsConflict");

		if (element.getAnnotation(AutoLogMethodInput.class) != null && !ignoreConflictWarning) {
			messager.printMessage(Diagnostic.Kind.WARNING,
				String.format("Annotation @AutoLogMethodInput on %s %s will be ignored: already annotated with "
					+ "@AutoLogMethodInOut.", elementKind, elementName), element);
		}

		if (element.getAnnotation(AutoLogMethodOutput.class) != null && !ignoreConflictWarning) {
			messager.printMessage(Diagnostic.Kind.WARNING,
				String.format("Annotation @AutoLogMethodOutput on %s %s will be ignored: already annotated with "
					+ "@AutoLogMethodInOut.", elementKind, elementName), element);
		}
	}

}
