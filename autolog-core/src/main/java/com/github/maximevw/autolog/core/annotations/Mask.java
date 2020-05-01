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

package com.github.maximevw.autolog.core.annotations;

import org.apache.commons.lang3.StringUtils;
import org.apiguardian.api.API;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation identifies a method argument to mask when it is automatically logged thanks to the annotations
 * {@link AutoLogMethodInput} or {@link AutoLogMethodInOut}.
 * <p>
 *     <i>Note:</i> This annotation can only be applied to {@link String} and numeric arguments (classes inherited from
 *     {@link Number}).
 * </p>
 */
@API(status = API.Status.STABLE, since = "1.1.0")
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Mask {

	/**
	 * Constant value for the default masking character.
	 */
	char DEFAULT_MASKING_CHAR = '*';

	/**
	 * Constant value usable in {@link #preservedCharacters()} parameter to indicate that none of the characters of the
	 * original value must be preserved.
	 * <p>
	 *     Example: {@code sensitive} will be masked by: {@code *********}.
	 * </p>
	 */
	String FULL_MASK = StringUtils.EMPTY;

	/**
	 * Constant value usable in {@link #preservedCharacters()} parameter to indicate that only the first and the last
	 * characters of the original value must be preserved.
	 * <p>
	 *     Example: {@code sensitive} will be masked by: {@code s*******e}.
	 * </p>
	 */
	String KEEP_FIRST_AND_LAST_CHARS = "0:1, :1";

	/**
	 * @return The character used to mask the parameter value. By default: {@value #DEFAULT_MASKING_CHAR}.
	 */
	char character() default DEFAULT_MASKING_CHAR;

	/**
	 * @return The mask length when a fixed-size mask must be used. If set to 0, the mask length corresponds to the
     * 		   original length of the parameter value. By default: 0.
	 * <p>
	 *     <i>Note:</i> this parameter has no effect if the value of {@link #preservedCharacters()} is different of
	 *     {@link #FULL_MASK} (or equivalent values).
	 * </p>
	 * <p>
	 *     For example, if the value of {@link #preservedCharacters()} is {@link #FULL_MASK}:
	 *     <ul>
	 *         <li>if {@code fixedLength = 0}, the value {@code password} will be masked by {@code ********}.</li>
	 *         <li>if {@code fixedLength = 3}, the value {@code password} will be masked by {@code ***}.</li>
	 *     </ul>
	 * </p>
	 */
	int fixedLength() default 0;

	/**
	 * @return The set of characters to preserve in the value to mask.
	 * <h1>Syntax</h1>
	 * <p>
	 *     The expression must respect the following regular expression:
	 *     <pre>^$|^(\d*:\d*|\[.*])(,\s?(\d*:\d*|\[.*]))*$</pre>
	 *     This expression is a comma-separated list of:
	 *     <ul>
	 *         <li>values {@code P:N} representing the number <b>N</b> of preserved characters from the position
	 *         <b>P</b> included. The position starts at index 0. If the position is left empty, the number of
	 *         characters to preserve is counted from the end of the value to mask. If the number N is left empty, all
	 *         the characters are preserved until the end of the value to mask (according to the direction defined by
	 *         the position P).</li>
	 *         <li>sets of preserved characters, enumerated between brackets. For example: {@code [ -]} means that
	 *         spaces and dashes will not be masked.</li>
	 *     </ul>
	 * </p>
	 * <h1>Restrictions</h1>
	 * <p>
	 *     If the number of preserved characters is equal or greater than the length of the value to mask, the rules of
	 *     preservation will be altered to ensure that at least 1 character is masked.
	 * </p>
	 * <h1>Examples</h1>
	 * <p>
	 *     Assuming the value to mask is {@code "password"}, the following table shows the results obtained by applying
	 *     different rules of preservation:
	 *     <table border="1" summary="Examples of rules applied to the value 'password'">
	 *         <tr>
	 *             <th>Expressions</th>
	 *             <th>Result</th>
	 *         </tr>
	 *         <tr>
	 *             <td>empty or {@code 0:0} or {@code :0} or {@code []}</td>
	 *             <td>********</td>
	 *         </tr>
	 *         <tr>
	 *             <td>{@code :1}</td>
	 *             <td>*******d</td>
	 *         </tr>
	 *         <tr>
	 *             <td>{@code 0:1, :1}</td>
	 *             <td>p******d</td>
	 *         </tr>
	 *         <tr>
	 *             <td>{@code 0:2, :1}</td>
	 *             <td>pa*****d</td>
	 *         </tr>
	 *         <tr>
	 *             <td>{@code 0:1, :3}</td>
	 *             <td>p****ord</td>
	 *         </tr>
	 *         <tr>
	 *             <td>{@code 0:1, :1,[s]}</td>
	 *             <td>p*ss***d</td>
	 *         </tr>
	 *         <tr>
	 *             <td>{@code 0:1, :7}</td>
	 *             <td>p*ssword</td>
	 *         </tr>
	 *         <tr>
	 *             <td>{@code 0:3, :1,[orsw]}</td>
	 *             <td>pas*word</td>
	 *         </tr>
	 *     </table>
	 * </p>
	 */
	String preservedCharacters() default StringUtils.EMPTY;

}
