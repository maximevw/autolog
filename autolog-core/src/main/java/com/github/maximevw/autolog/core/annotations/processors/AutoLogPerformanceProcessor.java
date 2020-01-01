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

import com.github.maximevw.autolog.core.annotations.AutoLogPerformance;
import com.github.maximevw.autolog.core.logger.performance.AdditionalDataProvider;
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
import java.util.List;
import java.util.Set;

/**
 * Annotation processor for {@link AutoLogPerformance}.
 * <p>
 *     It checks:
 *     <ul>
 *         <li>if the type of the field defined in the parameter {@link AutoLogPerformance#additionalDataProvider()}
 *         is effectively an instance of {@link AdditionalDataProvider}.</li>
 *     </ul>
 * </p>
 */
@API(status = API.Status.INTERNAL)
@SupportedAnnotationTypes("com.github.maximevw.autolog.core.annotations.AutoLogPerformance")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class AutoLogPerformanceProcessor extends AbstractProcessor {

	@Override
	public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
		final Messager messager = processingEnv.getMessager();

		annotations.forEach(typeElement ->
			roundEnv.getElementsAnnotatedWith(typeElement)
				.forEach(element -> checkAdditionalDataProvider(element, messager)));

		return false;
	}

	/**
	 * Checks the if the parameter {@link AutoLogPerformance#additionalDataProvider()} effectively references an
	 * instance of {@link AdditionalDataProvider}.
	 *
	 * @param element   The processed element.
	 * @param messager	The messager of the annotation processor.
	 */
	private static void checkAdditionalDataProvider(final Element element, final Messager messager) {
		final AutoLogPerformance checkedAnnotation = element.getAnnotation(AutoLogPerformance.class);

		switch (element.getKind()) {
			case METHOD:
				// If the annotation @AutoLogPerformance is located on a method, retrieve the field defined in the
				// attribute "additionalDataProvider" in the enclosing class to check its type.
				final Element enclosingClassElement = element.getEnclosingElement();
				if (enclosingClassElement != null) {
					final List<? extends Element> enclosedElements = enclosingClassElement.getEnclosedElements();
					checkAdditionalDataProviderType(enclosedElements, checkedAnnotation.additionalDataProvider(),
						element, messager);
				}
				break;
			case CLASS:
				// If the annotation @AutoLogPerformance is located on a class, retrieve the field defined in the
				// attribute "additionalDataProvider" directly in this class to check its type.
				checkAdditionalDataProviderType(element.getEnclosedElements(),
					checkedAnnotation.additionalDataProvider(), element, messager);
				break;
			default:
				// Do nothing.
				break;
		}
	}

	/**
	 * Checks if there is a field of type {@link AdditionalDataProvider} having the name defined in the annotation
	 * {@link AutoLogPerformance} in the class annotated with this annotation or in the enclosing class of the annotated
	 * method. If not, it will generate an error at the compilation time.
	 *
	 * @param classElements              The list of elements in the class to check.
	 * @param additionalDataProviderName The name of the field defined in the annotation.
	 * @param element                    The processed element (class or method).
	 * @param messager                   The messager of the annotation processor.
	 */
	private static void checkAdditionalDataProviderType(final List<? extends Element> classElements,
														final String additionalDataProviderName,
														final Element element, final Messager messager) {
		classElements.forEach(enclosedElement -> {
			if (enclosedElement.getKind().isField()
				&& enclosedElement.getSimpleName().toString().equals(additionalDataProviderName)
				&& !AdditionalDataProvider.class.getCanonicalName().equals(enclosedElement.asType().toString())) {
				messager.printMessage(Diagnostic.Kind.ERROR,
					String.format("The additional data provider field %s must be an instance of "
						+ "AdditionalDataProvider.", additionalDataProviderName), element);
			}
		});
	}

}
