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

import com.github.maximevw.autolog.core.annotations.AutoLogMethodOutput;
import org.apiguardian.api.API;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import java.util.Map;
import java.util.Set;

/**
 * Annotation processor for {@link AutoLogMethodOutput}.
 * <p>
 *     It checks:
 *     <ul>
 *         <li>if the values of the parameters {@link AutoLogMethodOutput#messageTemplate()} and
 *         {@link AutoLogMethodOutput#voidOutputMessageTemplate()} are correct, i.e. contains the expected number of
 *         placeholders.</li>
 *         <li>if the values of parameters {@link AutoLogMethodOutput#messageTemplate()},
 *         {@link AutoLogMethodOutput#voidOutputMessageTemplate()} ()} are those by default when
 *         {@link AutoLogMethodOutput#structuredMessage()} is {@code true} since they will be ignored in this case.</li>
 *     </ul>
 * </p>
 */
@API(status = API.Status.INTERNAL)
@SupportedAnnotationTypes("com.github.maximevw.autolog.core.annotations.AutoLogMethodOutput")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class AutoLogMethodOutputProcessor extends AbstractProcessor {

	@Override
	public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
		final Messager messager = processingEnv.getMessager();

		annotations.forEach(typeElement -> {
			roundEnv.getElementsAnnotatedWith(typeElement).forEach(element -> {
				AnnotationsProcessingUtils.checkPlaceholders(element, messager, AutoLogMethodOutput.class,
					Map.of("messageTemplate", 2, "voidOutputMessageTemplate", 1));
				AnnotationsProcessingUtils.checkIgnoredParameters(element, messager, AutoLogMethodOutput.class,
					"structuredMessage", true, "messageTemplate", "voidOutputMessageTemplate");
			});
		});

		return false;
	}

}
