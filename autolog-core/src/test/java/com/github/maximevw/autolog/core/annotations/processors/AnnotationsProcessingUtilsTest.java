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
import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

/**
 * Unit tests for the class {@link AnnotationsProcessingUtils}.
 */
class AnnotationsProcessingUtilsTest {

	/**
	 * Verifies that compilation errors are thrown for a Java file containing annotations {@link AutoLogMethodInOut},
	 * {@link AutoLogMethodInput} and {@link AutoLogMethodOutput} with invalid placeholders in message templates.
	 */
	@Test
	void givenJavaSourceWithAnnotationsWithWrongPlaceholders_whenCompile_throwsCompilationErrors() {
		final JavaFileObject compilationTestClassFile =
			JavaFileObjects.forResource("AnnotationsParametersTestClass.java");
		final Compilation compilation =
			javac()
				.withProcessors(new AutoLogMethodInOutProcessor(), new AutoLogMethodInputProcessor(),
					new AutoLogMethodOutputProcessor())
				.compile(compilationTestClassFile);
		assertThat(compilation).failed();
		assertThat(compilation).hadErrorCount(6);
		assertWrongPlaceholders(compilation, compilationTestClassFile, "inOutWrongPlaceholders()",
			"inputMessageTemplate", "AutoLogMethodInOut", 2, 1);
		assertWrongPlaceholders(compilation, compilationTestClassFile, "inOutWrongPlaceholders()",
			"outputMessageTemplate", "AutoLogMethodInOut", 2, 1);
		assertWrongPlaceholders(compilation, compilationTestClassFile, "inOutWrongPlaceholders()",
			"voidOutputMessageTemplate", "AutoLogMethodInOut", 1, 0);
		assertWrongPlaceholders(compilation, compilationTestClassFile, "inputAndOutputAnnotatedMethod()",
			"messageTemplate", "AutoLogMethodInput", 2, 1);
		assertWrongPlaceholders(compilation, compilationTestClassFile, "inputAndOutputAnnotatedMethod()",
			"messageTemplate", "AutoLogMethodOutput", 2, 1);
		assertWrongPlaceholders(compilation, compilationTestClassFile, "inputAndOutputAnnotatedMethod()",
			"voidOutputMessageTemplate", "AutoLogMethodOutput", 1, 0);
	}

	/**
	 * Asserts that the compilation results contain an error relative to bad placeholders in message template for
	 * the annotations {@link AutoLogMethodInOut}, {@link AutoLogMethodInput} and {@link AutoLogMethodOutput}.
	 *
	 * @param compilation			The compilation result.
	 * @param compiledSourceFile	The compiled file for which an error must be found.
	 * @param methodOnError			The method for which an error must be found.
	 * @param parameter				The annotation parameter name containing an error.
	 * @param annotation			The annotation name containing an error.
	 * @param expectedPlaceholders	The expected number of placeholders.
	 * @param foundPlaceholders		The found number of placeholders.
	 */
	private void assertWrongPlaceholders(final Compilation compilation, final JavaFileObject compiledSourceFile,
										 final String methodOnError, final String parameter, final String annotation,
										 final int expectedPlaceholders, final int foundPlaceholders) {
		assertThat(compilation)
			.hadErrorContaining(String.format("Parameter '%s' of @%s must contain exactly %d placeholder(s) but "
				+ "found: %d", parameter, annotation, expectedPlaceholders, foundPlaceholders))
			.inFile(compiledSourceFile)
			.onLineContaining(methodOnError);
	}

	/**
	 * Verifies that compilation warnings are generated for a Java file containing annotations
	 * {@link AutoLogMethodInOut}, {@link AutoLogMethodInput} and {@link AutoLogMethodOutput} with ignored parameters.
	 */
	@Test
	void givenJavaSourceWithAnnotationsWithUnexpectedParamValues_whenCompile_generatesCompilationWarnings() {
		final JavaFileObject compilationTestClassFile =
			JavaFileObjects.forResource("AnnotationsParametersTestClass.java");
		final Compilation compilation =
			javac()
				.withProcessors(new AutoLogMethodInOutProcessor(), new AutoLogMethodInputProcessor(),
					new AutoLogMethodOutputProcessor())
				.compile(compilationTestClassFile);
		assertThat(compilation).hadWarningCount(11);

		// Check unexpected prettify values.
		assertUnexpectedPrettify(compilation, compilationTestClassFile, "inputAndOutputAnnotatedMethod()",
			AutoLogMethodInOut.ALL_DATA);
		assertUnexpectedPrettify(compilation, compilationTestClassFile, "inputAndOutputAnnotatedMethod()",
			AutoLogMethodInOut.INPUT_DATA);
		assertUnexpectedPrettify(compilation, compilationTestClassFile, "inputAndOutputAnnotatedMethod()",
			AutoLogMethodInOut.OUTPUT_DATA);

		// Check unexpected values on ignored parameters when structuredMessage is true.
		assertIgnoredParameterWhenStructuredMessage(compilation, compilationTestClassFile,
			"inOutWithIgnoredParameters()", "AutoLogMethodInOut", "prettify");
		assertIgnoredParameterWhenStructuredMessage(compilation, compilationTestClassFile,
			"inOutWithIgnoredParameters()", "AutoLogMethodInOut", "inputMessageTemplate");
		assertIgnoredParameterWhenStructuredMessage(compilation, compilationTestClassFile,
			"inOutWithIgnoredParameters()", "AutoLogMethodInOut", "outputMessageTemplate");
		assertIgnoredParameterWhenStructuredMessage(compilation, compilationTestClassFile,
			"inOutWithIgnoredParameters()", "AutoLogMethodInOut", "voidOutputMessageTemplate");
		assertIgnoredParameterWhenStructuredMessage(compilation, compilationTestClassFile,
			"inputWithIgnoredParameters()", "AutoLogMethodInput", "prettify");
		assertIgnoredParameterWhenStructuredMessage(compilation, compilationTestClassFile,
			"inputWithIgnoredParameters()", "AutoLogMethodInput", "messageTemplate");
		assertIgnoredParameterWhenStructuredMessage(compilation, compilationTestClassFile,
			"outputWithIgnoredParameters()", "AutoLogMethodOutput", "messageTemplate");
		assertIgnoredParameterWhenStructuredMessage(compilation, compilationTestClassFile,
			"outputWithIgnoredParameters()", "AutoLogMethodOutput", "voidOutputMessageTemplate");
	}

	/**
	 * Asserts that the compilation results contain a warning relative to an unexpected value in {@code prettify}
	 * parameter for the annotations {@link AutoLogMethodInOut}, {@link AutoLogMethodInput} and
	 * {@link AutoLogMethodOutput}.
	 *
	 * @param compilation			The compilation result.
	 * @param compiledSourceFile	The compiled file for which a warning must be found.
	 * @param methodOnError			The method for which a warning must be found.
	 * @param unexpectedValue		The unexpected parameter value.
	 */
	private void assertUnexpectedPrettify(final Compilation compilation, final JavaFileObject compiledSourceFile,
										  final String methodOnError, final String unexpectedValue) {
		assertThat(compilation)
			.hadWarningContaining(String.format("Parameter 'prettify' of @AutoLogMethodInput contains unexpected "
				+ "value: %s. It will be ignored.", unexpectedValue))
			.inFile(compiledSourceFile)
			.onLineContaining(methodOnError);
	}

	/**
	 * Asserts that the compilation results contain a warning relative to an ignored parameter when the parameter
	 * {@code structuredMessage} parameter is {@code true} for the annotations {@link AutoLogMethodInOut},
	 * {@link AutoLogMethodInput} and {@link AutoLogMethodOutput}.
	 *
	 * @param compilation			The compilation result.
	 * @param compiledSourceFile	The compiled file for which a warning must be found.
	 * @param methodOnError			The method for which a warning must be found.
	 * @param annotation			The annotation name containing a warning.
	 * @param ignoredParameter		The name of the ignored parameter.
	 */
	private void assertIgnoredParameterWhenStructuredMessage(final Compilation compilation,
															 final JavaFileObject compiledSourceFile,
															 final String methodOnError, final String annotation,
															 final String ignoredParameter) {
		assertThat(compilation)
			.hadWarningContaining(String.format("Parameter '%s' of @%s will be ignored since parameter "
				+ "'structuredMessage' is set to true.", ignoredParameter, annotation))
			.inFile(compiledSourceFile)
			.onLineContaining(methodOnError);
	}

}
