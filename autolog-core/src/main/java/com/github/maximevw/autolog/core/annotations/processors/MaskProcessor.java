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

package com.github.maximevw.autolog.core.annotations.processors;

import com.github.maximevw.autolog.core.annotations.Mask;
import org.apache.commons.lang3.ClassUtils;
import org.apache.commons.lang3.StringUtils;
import org.apiguardian.api.API;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;
import java.util.Set;

/**
 * Annotation processor for {@link Mask}.
 * <p>
 *     It checks:
 *     <ul>
 *         <li>if the annotation is applied to a {@link String} or numeric (i.e. inherited from {@link Number})
 *         argument.</li>
 *         <li>if the value of {@link Mask#preservedCharacters()} is valid.</li>
 *         <li>if the value of {@link Mask#fixedLength()} is equal to 0 when the value of
 *         {@link Mask#preservedCharacters()} is different of {@link Mask#FULL_MASK} (or equivalent values).</li>
 *     </ul>
 * </p>
 */
@API(status = API.Status.INTERNAL)
@SupportedAnnotationTypes("com.github.maximevw.autolog.core.annotations.Mask")
@SupportedSourceVersion(SourceVersion.RELEASE_11)
public class MaskProcessor extends AbstractProcessor {

	private static final String PRESERVED_CHARS_REGEX = "^$|^(\\d*:\\d*|\\[.*])(,\\s?(\\d*:\\d*|\\[.*]))*$";
	private static final String FULL_MASK_EQUIVALENT_REGEX = ",|\\[]|\\d?:0";

	@Override
	public boolean process(final Set<? extends TypeElement> annotations, final RoundEnvironment roundEnv) {
		final Messager messager = processingEnv.getMessager();

		annotations.forEach(typeElement ->
			roundEnv.getElementsAnnotatedWith(typeElement)
				.forEach(element -> {
					checkMaskedArgumentType(element, messager);
					checkPreservedCharactersExpression(element, messager);
					checkFixedLengthValue(element, messager);
				}));

		return false;
	}

	/**
	 * Checks whether the type of the argument to which the annotation is applied is {@link String} or a numeric one
	 * (i.e. inherited from {@link Number}).
	 *
	 * @param element   The processed element.
	 * @param messager	The messager of the annotation processor.
	 */
	private static void checkMaskedArgumentType(final Element element, final Messager messager) {
		final Mask maskAnnotation = element.getAnnotation(Mask.class);
		final TypeMirror elementType = element.asType();
		Class<?> elementClass = null;
		try {
			if (elementType.getKind().isPrimitive()) {
				elementClass = ClassUtils.primitiveToWrapper(ClassUtils.getClass(elementType.toString()));
			} else {
				elementClass = Class.forName(elementType.toString());
			}
		} catch (final ClassNotFoundException e) {
			messager.printMessage(Diagnostic.Kind.WARNING,
				String.format("Unable to process @Mask on method argument %s due to ClassNotFoundException: %s",
					element.getSimpleName(), e.getMessage()));
		}

		if (maskAnnotation != null && elementClass != null
			&& !(String.class.isAssignableFrom(elementClass) || Number.class.isAssignableFrom(elementClass))) {
			messager.printMessage(Diagnostic.Kind.WARNING,
				String.format("@Mask is applied to a method argument %s which is neither a String nor a numeric value. "
					+ "It will be ignored.", element.getSimpleName()), element);
		}
	}

	/**
	 * Checks whether the value of {@link Mask#preservedCharacters()} is a valid expression (i.e. matches
	 * {@value PRESERVED_CHARS_REGEX}).
	 *
	 * @param element   The processed element.
	 * @param messager	The messager of the annotation processor.
	 */
	private static void checkPreservedCharactersExpression(final Element element, final Messager messager) {
		final Mask maskAnnotation = element.getAnnotation(Mask.class);
		if (maskAnnotation != null && !maskAnnotation.preservedCharacters().matches(PRESERVED_CHARS_REGEX)) {
			messager.printMessage(Diagnostic.Kind.ERROR,
				String.format("Parameter 'preservedCharacters' of @Mask on method argument %s is invalid. Please check "
					+ "the format of the expression.", element.getSimpleName()), element);
		}
	}

	/**
	 * Checks whether the value of {@link Mask#fixedLength()} is equal to 0 when the value of
	 * {@link Mask#preservedCharacters()} is different of {@link Mask#FULL_MASK} (or equivalent values).
	 *
	 * @param element   The processed element.
	 * @param messager	The messager of the annotation processor.
	 */
	private static void checkFixedLengthValue(final Element element, final Messager messager) {
		final Mask maskAnnotation = element.getAnnotation(Mask.class);
		if (maskAnnotation != null && maskAnnotation.fixedLength() > 0
			&& !isFullMask(maskAnnotation.preservedCharacters())) {
			messager.printMessage(Diagnostic.Kind.WARNING,
				String.format("Parameter 'fixedLength' of @Mask on method argument %s should be equal to 0 since the "
					+ "parameter 'preservedCharacters' is not a full mask expression.", element.getSimpleName()),
				element);
		}
	}

	/**
	 * Checks whether the given expression is equivalent to {@link Mask#FULL_MASK}.
	 *
	 * @param preservedCharacters The characters preservation expression to test.
	 * @return {@code true} if the given expression is equivalent to {@link Mask#FULL_MASK}, {@code false} otherwise.
	 */
	private static boolean isFullMask(final String preservedCharacters) {
		if (preservedCharacters == null) {
			return true;
		}
		return preservedCharacters.replaceAll(FULL_MASK_EQUIVALENT_REGEX, StringUtils.EMPTY).trim().isEmpty();
	}
}
