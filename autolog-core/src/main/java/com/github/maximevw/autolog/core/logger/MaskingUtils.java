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
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apiguardian.api.API;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Internal class providing utilities for masking of values.
 */
@API(status = API.Status.INTERNAL, consumers = "com.github.maximevw.autolog.core.*")
public final class MaskingUtils {

	/**
	 * Regular expression used to verify if a characters preservation expression is equivalent to a full mask
	 * expression.
	 */
	private static final String FULL_MASK_EQUIVALENT_REGEX = ",|\\[]|\\d?:0";

	private MaskingUtils() {
		// Private constructor to hide it externally.
	}

	/**
	 * Builds a masked value according to the parameters defined into the given {@link Mask} annotation.
	 *
	 * @param value				The value to mask.
	 * @param maskParameters	The annotation containing the masking parameters.
	 * @return The masked value if applicable to the given value, otherwise the input value itself.
	 */
	static Object maskValue(final Object value, final Mask maskParameters) {
		// Check if the value has a valid type for masking.
		if (value instanceof String || value instanceof Number) {
			final String preservedCharacters = maskParameters.preservedCharacters();
			final String valueAsString = value.toString();

			// Specific case of full mask (with or without a fixed-sized masking string).
			if (isFullMask(preservedCharacters)) {
				int maskedLength = valueAsString.length();
				if (maskParameters.fixedLength() > 0) {
					maskedLength = maskParameters.fixedLength();
				}
				return StringUtils.repeat(maskParameters.character(), maskedLength);
			}

			// Parse the preservation rules, then apply the mask to the input value.
			final PreservationRules preservationRules = parsePreservedCharacters(preservedCharacters);
			final char[] result = valueAsString.toCharArray();
			final int valueLength = result.length;
			int maskedCharacters = 0;

			for (int i = 0; i < valueLength; i++) {
				if (!preservationRules.getPreservedCharacters().contains(result[i])
					&& !preservationRules.isPreservedPosition(i, valueLength)) {
					result[i] = maskParameters.character();
					maskedCharacters++;
				}
			}

			// Check that at least one character is masked according to the rules. If not, mask at least one character.
			if (maskedCharacters == 0) {
				final int forcedMaskPosition = preservationRules.computeForcedMaskPosition(valueLength);
				result[forcedMaskPosition] = maskParameters.character();
			}

			return String.valueOf(result);
		}

		return value;
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

	/**
	 * Parses an expression from the parameter {@link Mask#preservedCharacters()}.
	 *
	 * @param preservedCharactersExpression The expression to parse.
	 * @return The rules of characters preservation based on the parsed expression.
	 */
	private static PreservationRules parsePreservedCharacters(final String preservedCharactersExpression) {
		final String[] elements = preservedCharactersExpression.split(",");
		final PreservationRules preservationRules = new PreservationRules();

		Arrays.stream(elements)
			.map(String::trim)
			.collect(Collectors.partitioningBy(element -> element.matches("\\d*:\\d*")))
			.forEach((isPositionAndLength, values) -> {
				if (isPositionAndLength) {
					values.forEach(positionAndLength -> {
						final int separatorPosition = positionAndLength.indexOf(':');
						final String position = positionAndLength.substring(0, separatorPosition);
						final String length = positionAndLength.substring(separatorPosition + 1);
						preservationRules.addPreservedRange(NumberUtils.toInt(position, -1),
							NumberUtils.toInt(length, -1));
					});
				} else {
					values.stream()
						.filter(value -> value.matches("\\[.*]"))
						// Remove brackets to only keep the characters to preserve.
						.map(value -> value.substring(1, value.length() - 1))
						.forEach(setOfChars ->
							setOfChars.chars().forEach(character ->
								preservationRules.addPreservedCharacter((char) character))
						);
				}
			});

		return preservationRules;
	}

	/**
	 * Rules for preservation of characters in a value to mask.
	 */
	@Getter
	@Setter
	@NoArgsConstructor
	private static class PreservationRules {
		/**
		 * The set of characters to preserve.
		 */
		private Set<Character> preservedCharacters = new HashSet<>();

		/**
		 * The ranges of characters to preserve.
		 * <p>
		 *     The key is the start position (the value -1 means the range starts at the end of the value to mask).
		 *     The value is the length of the range of characters (the value -1 means until the last (or the first if
		 *     the key is -1) character of the value to mask. Keep in mind that at least one character will be masked
		 *     even if the defined range implies that all characters should be preserved.
		 * </p>
		 */
		private Map<Integer, Integer> preservedRanges = new HashMap<>();

		/**
		 * Adds a character to preserve.
		 *
		 * @param character The character.
		 */
		public void addPreservedCharacter(final char character) {
			this.preservedCharacters.add(character);
		}

		/**
		 * Adds a range to preserve.
		 *
		 * @param startPosition The start position. The value -1 means the range starts at the end of the value to mask.
		 * @param length		The length of the range to preserve. The value -1 means until the last (or the first if
		 *                      the start position is -1) character of the value to mask.
		 */
		public void addPreservedRange(final int startPosition, final int length) {
			this.preservedRanges.put(startPosition, length);
		}

		/**
		 * Gets whether the given position in the value to mask should be preserved.
		 *
		 * @param position    The position.
		 * @param totalLength The length of the value to mask.
		 * @return {@code true} if the character at the given position should be preserved, {@code false} otherwise.
		 */
		public boolean isPreservedPosition(final int position, final int totalLength) {
			if (this.preservedRanges.containsKey(-1)
				&& (this.preservedRanges.get(-1) == -1 || position >= totalLength - this.preservedRanges.get(-1))) {
				return true;
			}

			return this.preservedRanges.keySet().stream()
					.filter(key -> key != -1)
					.anyMatch(startPosition -> position <= startPosition + this.preservedRanges.get(startPosition) - 1
						&& position >= startPosition);
		}

		/**
		 * Computes the position of the character to imperatively mask in a value where all the characters should be
		 * preserved according to these preservation rules.
		 *
		 * @param totalLength The length of the value to mask.
		 * @return The position of the character to mask.
		 */
		public int computeForcedMaskPosition(final int totalLength) {
			final Optional<Integer> potentialPosition = this.preservedRanges.keySet().stream()
					.filter(key -> key != -1)
					.min(Integer::compare);
			if (potentialPosition.isPresent() && this.preservedRanges.get(potentialPosition.get()) < totalLength) {
				return this.preservedRanges.get(potentialPosition.get());
			} else {
				return totalLength - 1;
			}
		}
	}
}
