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
import com.google.testing.compile.Compilation;
import com.google.testing.compile.JavaFileObjects;
import org.junit.jupiter.api.Test;

import javax.tools.JavaFileObject;

import static com.google.testing.compile.CompilationSubject.assertThat;
import static com.google.testing.compile.Compiler.javac;

/**
 * Unit tests for annotation processor {@link AutoLogPerformanceProcessor}.
 */
class AutoLogPerformanceProcessorTest {

	/**
	 * Verifies that compilation errors are thrown for a Java file containing annotation {@link AutoLogPerformance}
	 * with a bad definition of {@link AdditionalDataProvider}.
	 */
	@Test
	void givenJavaSourceWithWrongAdditionalDataProvider_whenCompile_generatesCompilationError() {
		final JavaFileObject compilationTestClassFile =
			JavaFileObjects.forResource("WrongAdditionalDataProviderTestClass.java");
		final Compilation compilation =
			javac()
				.withProcessors(new AutoLogPerformanceProcessor())
				.compile(compilationTestClassFile);
		assertThat(compilation).failed();
		assertThat(compilation).hadErrorCount(2);
		assertThat(compilation)
			.hadErrorContaining("The additional data provider field wrongAdditionalDataProvider must be an instance "
				+ "of AdditionalDataProvider.")
			.inFile(compilationTestClassFile)
			.onLineContaining("methodWithWrongAdditionalDataProvider");
		assertThat(compilation)
			.hadErrorContaining("The additional data provider field anotherWrongAdditionalDataProvider must be an "
				+ "instance of AdditionalDataProvider.")
			.inFile(compilationTestClassFile)
			.onLineContaining("WrongAdditionalDataProviderTestClass");
	}
}
