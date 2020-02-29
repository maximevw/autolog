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
import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

/**
 * Unit tests for annotation processor {@link MaskProcessor}.
 */
public class MaskProcessorTest {

	/**
	 * Verifies that compilation warnings and errors are generated for a Java file containing {@link Mask} annotations
	 * with conditions generating such warnings and errors.
	 */
	@Test
	void givenJavaSourceWithMaskAnnotations_whenCompile_generatesCompilationWarningsAndErrors() {
		final JavaFileObject compilationTestClassFile = JavaFileObjects.forResource("MaskAnnotationTestClass.java");
		final Compilation compilation =
			javac()
				.withProcessors(new MaskProcessor())
				.compile(compilationTestClassFile);
		//		assertThat(compilation).failed();
		assertThat(compilation).hadErrorCount(1);
		assertThat(compilation).hadWarningCount(2);

		// Assert that the annotation on an argument with an invalid type (i.e. not String or numeric type) generates a
		// warning.
		assertThat(compilation)
			.hadWarningContaining("@Mask is applied to a method argument argObj which is neither a String nor a "
				+ "numeric value. It will be ignored.")
			.inFile(compilationTestClassFile)
			.onLineContaining("maskOnInvalidType");

		// Assert that the annotation with a fixed length greater than 0 and preserved characters expression which is
		// not a "full mask" expression generates a warning.
		assertThat(compilation)
			.hadWarningContaining("Parameter 'fixedLength' of @Mask on method argument argStrInvalidFixedLength should "
				+ "be equal to 0 since the parameter 'preservedCharacters' is not a full mask expression.")
			.inFile(compilationTestClassFile)
			.onLineContaining("argStrInvalidFixedLength");

		// Assert that the annotation with an invalid expression for preserved characters generates an error.
		assertThat(compilation)
			.hadErrorContaining("Parameter 'preservedCharacters' of @Mask on method argument argStrInvalidMask is "
				+ "invalid. Please check the format of the expression.")
			.inFile(compilationTestClassFile)
			.onLineContaining("argStrInvalidMask");
	}

}
