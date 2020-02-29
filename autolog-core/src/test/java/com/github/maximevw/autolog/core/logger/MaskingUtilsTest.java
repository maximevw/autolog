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

package com.github.maximevw.autolog.core.logger;

import com.github.maximevw.autolog.core.annotations.Mask;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for the class {@link MaskingUtils}.
 */
class MaskingUtilsTest {

	/**
	 * Builds an instance of {@link Mask} annotation with default parameters for testing purpose.
	 *
	 * @return The instance of {@link Mask} annotation with the default parameters.
	 */
	static Mask getMaskAnnotationInstance() {
		return getMaskAnnotationInstance(Mask.DEFAULT_MASKING_CHAR, 0, StringUtils.EMPTY);
	}

	/**
	 * Builds an instance of {@link Mask} annotation with a specific pattern for characters preservation for testing
	 * purpose.
	 *
	 * @param preservedCharacters	The value of annotation parameter {@link Mask#preservedCharacters()}.
	 * @return The instance of {@link Mask} annotation with the given parameters.
	 */
	static Mask getMaskAnnotationInstance(final String preservedCharacters) {
		return getMaskAnnotationInstance(Mask.DEFAULT_MASKING_CHAR, 0, preservedCharacters);
	}

	/**
	 * Builds an instance of {@link Mask} annotation for testing purpose.
	 *
	 * @param character				The value of annotation parameter {@link Mask#character()}.
	 * @param fixedLength			The value of annotation parameter {@link Mask#fixedLength()}.
	 * @param preservedCharacters	The value of annotation parameter {@link Mask#preservedCharacters()}.
	 * @return The instance of {@link Mask} annotation with the given parameters.
	 */
	static Mask getMaskAnnotationInstance(final char character, final int fixedLength,
										  final String preservedCharacters) {
		return new Mask() {
			@Override
			public Class<? extends Annotation> annotationType() {
				return Mask.class;
			}

			@Override
			public char character() {
				return character;
			}

			@Override
			public int fixedLength() {
				return fixedLength;
			}

			@Override
			public String preservedCharacters() {
				return preservedCharacters;
			}
		};
	}

	/**
	 * Verifies that the given value is masked (or not if it's not applciable) according to the given masking rules.
	 * @param valueToMask			The value to mask.
	 * @param appliedMaskingRules	The masking rules defined into an instance of {@link Mask} annotation.
	 * @param expectedResult		The expected value returned by the call to
	 * 								{@link MaskingUtils#maskValue(Object, Mask)}.
	 */
	@ParameterizedTest
	@MethodSource("provideMaskingRulesAndValues")
	void givenValue_whenMaskValue_returnsExpectedMaskedValue(final Object valueToMask, final Mask appliedMaskingRules,
															 final Object expectedResult) {
		assertEquals(expectedResult, MaskingUtils.maskValue(valueToMask, appliedMaskingRules));
	}

	/**
	 * Builds arguments for the parameterized test relative to the masking of values:
	 * {@link #givenValue_whenMaskValue_returnsExpectedMaskedValue(Object, Mask, Object)}.
	 *
	 * @return The value to mask, the masking rules and the expected result for the parameterized tests.
	 */
	private static Stream<Arguments> provideMaskingRulesAndValues() {
		final Object notMaskableObject = new Object();
		final List<String> notMaskableList = Collections.singletonList("test");

		return Stream.of(
			// Check the masking is only applied to String and numeric values.
			Arguments.of(notMaskableObject, getMaskAnnotationInstance(), notMaskableObject),
			Arguments.of(notMaskableList, getMaskAnnotationInstance(), notMaskableList),
			Arguments.of("value", getMaskAnnotationInstance(), "*****"),
			Arguments.of(10, getMaskAnnotationInstance(), "**"),
			Arguments.of(10.125d, getMaskAnnotationInstance(), "******"),
			Arguments.of(1234L, getMaskAnnotationInstance(), "****"),
			// Check masking with different patterns of "full mask" without a fixed-size mask.
			Arguments.of("value", getMaskAnnotationInstance(null), "*****"),
			Arguments.of("value", getMaskAnnotationInstance("0:0"), "*****"),
			Arguments.of("value", getMaskAnnotationInstance(":0"), "*****"),
			Arguments.of("value", getMaskAnnotationInstance("[]"), "*****"),
			Arguments.of("value", getMaskAnnotationInstance("0:0,[]"), "*****"),
			Arguments.of("value", getMaskAnnotationInstance("[],:0,[]"), "*****"),
			// Check masking with a fixed-size mask.
			Arguments.of("value", getMaskAnnotationInstance(Mask.DEFAULT_MASKING_CHAR, 3, StringUtils.EMPTY), "***"),
			Arguments.of("value", getMaskAnnotationInstance('#', 6, StringUtils.EMPTY), "######"),
			// Check the fixed-size mask is ignored if a pattern different of "full mask" is set.
			Arguments.of("value", getMaskAnnotationInstance(Mask.DEFAULT_MASKING_CHAR, 3, "0:1"), "v****"),
			// Check the preservation of characters.
			Arguments.of("value", getMaskAnnotationInstance(Mask.DEFAULT_MASKING_CHAR, 0, ":"), "valu*"),
			Arguments.of("value", getMaskAnnotationInstance(Mask.DEFAULT_MASKING_CHAR, 0, ":4"), "*alue"),
			Arguments.of("value", getMaskAnnotationInstance(Mask.DEFAULT_MASKING_CHAR, 0, ":1"), "****e"),
			Arguments.of("value", getMaskAnnotationInstance(Mask.DEFAULT_MASKING_CHAR, 0, ":1, [a]"), "*a**e"),
			Arguments.of("value", getMaskAnnotationInstance(Mask.DEFAULT_MASKING_CHAR, 0, "2:8"), "**lue"),
			Arguments.of("value", getMaskAnnotationInstance(Mask.DEFAULT_MASKING_CHAR, 0, "0:8"), "valu*"),
			Arguments.of("value", getMaskAnnotationInstance(Mask.DEFAULT_MASKING_CHAR, 0, "1:3"), "*alu*"),
			Arguments.of("value", getMaskAnnotationInstance(Mask.DEFAULT_MASKING_CHAR, 0, "0:2, :5"), "va*ue"),
			Arguments.of("value", getMaskAnnotationInstance(Mask.DEFAULT_MASKING_CHAR, 0, "0:1, [alue]"), "v*lue"),
			Arguments.of("value", getMaskAnnotationInstance(Mask.DEFAULT_MASKING_CHAR, 0, "0:1, :2, [a]"), "va*ue"),
			Arguments.of(10.125d, getMaskAnnotationInstance(Mask.DEFAULT_MASKING_CHAR, 0, "[.]"), "**.***"),
			Arguments.of(123445, getMaskAnnotationInstance(Mask.DEFAULT_MASKING_CHAR, 0, "[24]"), "*2*44*")
		);
	}

}
