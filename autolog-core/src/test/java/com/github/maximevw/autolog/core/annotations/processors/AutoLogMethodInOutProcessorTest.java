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
 * Unit tests for annotation processor {@link AutoLogMethodInOutProcessor}.
 */
class AutoLogMethodInOutProcessorTest {

	/**
	 * Verifies that compilation warnings are generated for a Java file containing annotations conflicts
	 * between {@link AutoLogMethodInOut}, {@link AutoLogMethodInput} and {@link AutoLogMethodOutput}.
	 */
	@Test
	void givenJavaSourceWithConflictingAnnotations_whenCompile_generatesCompilationWarnings() {
		final JavaFileObject compilationTestClassFile = JavaFileObjects.forResource("WronglyAnnotatedTestClass.java");
		final Compilation compilation =
			javac()
				.withProcessors(new AutoLogMethodInOutProcessor())
				.compile(compilationTestClassFile);
		assertThat(compilation).succeeded();
		assertThat(compilation).hadWarningCount(4);
		assertUnexpectedAnnotation(compilation, compilationTestClassFile, "class", "WronglyAnnotatedTestClass",
			"AutoLogMethodInput");
		assertUnexpectedAnnotation(compilation, compilationTestClassFile, "class", "WronglyAnnotatedTestClass",
			"AutoLogMethodOutput");
		assertUnexpectedAnnotation(compilation, compilationTestClassFile, "method", "badlyAnnotatedMethod",
			"AutoLogMethodInput");
		assertUnexpectedAnnotation(compilation, compilationTestClassFile, "method", "badlyAnnotatedMethod",
			"AutoLogMethodOutput");
	}

	/**
	 * Asserts that the compilation results contain a warning relative to a conflict between the annotations
	 * {@link AutoLogMethodInOut}, {@link AutoLogMethodInput} and {@link AutoLogMethodOutput}.
	 *
	 * @param compilation			The compilation result.
	 * @param compiledSourceFile	The compiled file for which a warning must be found.
	 * @param elementType			The type (class or method) of the element for which a warning must be found.
	 * @param elementOnError		The element name for which a warning must be found.
	 * @param unexpectedAnnotation	The name of the unexpected annotation.
	 */
	private void assertUnexpectedAnnotation(final Compilation compilation, final JavaFileObject compiledSourceFile,
											final String elementType, final String elementOnError,
											final String unexpectedAnnotation) {
		assertThat(compilation)
			.hadWarningContaining(String.format("Annotation @%s on %s %s will be ignored: already annotated with "
				+ "@AutoLogMethodInOut.", unexpectedAnnotation, elementType, elementOnError))
			.inFile(compiledSourceFile)
			.onLineContaining(elementOnError);
	}

	/**
	 * Verifies that compilation succeeds for a Java file without annotations conflicts between
	 * {@link AutoLogMethodInOut}, {@link AutoLogMethodInput} and {@link AutoLogMethodOutput}.
	 */
	@Test
	void givenJavaSourceWithNoConflictingAnnotations_whenCompile_succeedsWithoutWarnings() {
		final JavaFileObject compilationTestClassFile = JavaFileObjects.forResource("CorrectlyAnnotatedTestClass.java");
		final Compilation compilation =
			javac()
				.withProcessors(new AutoLogMethodInOutProcessor())
				.compile(compilationTestClassFile);
		assertThat(compilation).succeededWithoutWarnings();
	}

	/**
	 * Verifies that compilation succeeds without warnings for a Java file with annotations conflicts between
	 * {@link AutoLogMethodInOut}, {@link AutoLogMethodInput} and {@link AutoLogMethodOutput} and using
	 * {@link SuppressWarnings}(of type {@code AutoLogMethodAnnotationsConflict}).
	 */
	@Test
	void givenJavaSourceWithConflictingAnnotationsAndSuppressWarnings_whenCompile_succeedsWithoutWarnings() {
		final JavaFileObject compilationTestClassFile =
			JavaFileObjects.forResource("IgnoreWarningsWronglyAnnotatedTestClass.java");
		final Compilation compilation =
			javac()
				.withProcessors(new AutoLogMethodInOutProcessor())
				.compile(compilationTestClassFile);
		assertThat(compilation).succeededWithoutWarnings();
	}
}
