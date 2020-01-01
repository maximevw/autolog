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

package com.github.maximevw.autolog.test;

import com.github.maximevw.autolog.core.configuration.MethodPerformanceLoggingConfiguration;
import com.github.maximevw.autolog.core.logger.MethodCallLogger;
import com.github.maximevw.autolog.core.logger.MethodPerformanceLogger;
import com.github.maximevw.autolog.core.logger.performance.AdditionalDataProvider;
import com.github.maximevw.autolog.core.logger.performance.PerformanceTimer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.OPTIONS;
import javax.ws.rs.PATCH;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import java.util.Collection;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * This class contains methods specially designed to test {@link MethodCallLogger} and {@link MethodPerformanceLogger}.
 */
@Slf4j
public class LogTestingClass {

	/**
	 * Test method without neither arguments nor returned value.
	 */
	public void noOp() {
		// Method for testing purpose only.
	}

	/**
	 * Test method with several input arguments and without returned value.
	 *
	 * @param argInt	Fake integer argument for test purpose.
	 * @param argStr	Fake string argument for test purpose.
	 * @param argBool	Fake boolean argument for test purpose.
	 */
	public void methodInputData(final int argInt, final String argStr, final boolean argBool) {
		// Method for testing purpose only.
	}

	/**
	 * Test method with several input arguments simulating generated names (when the real names are not available after
	 * compilation without {@code -g} argument) and without returned value.
	 *
	 * @param $arg0	Fake string argument for test purpose.
	 * @param $arg1	Fake long argument for test purpose.
	 */
	public void methodSimulatingGeneratedArgNames(final String $arg0, final long $arg1) {
		// Method for testing purpose only.
	}

	/**
	 * Test method with object input argument and without returned value.
	 *
	 * @param data	Fake object argument for test purpose.
	 */
	public void methodInputComplexData(final TestObject data) {
		// Method for testing purpose only.
	}

	/**
	 * Test method with collection and map input arguments and without returned value.
	 *
	 * @param collection	Fake collection argument for test purpose.
	 * @param map			Fake map argument for test purpose.
	 */
	public void methodInputCollectionAndMap(final Collection<String> collection, final Map<String, Integer> map) {
		// Method for testing purpose only.
	}

	/**
	 * Test method without neither arguments nor returned value.
	 */
	public void methodReturningVoid() {
		// Method for testing purpose only.
	}

	/**
	 * Test method without input arguments and returning a value.
	 *
	 * @return A string value.
	 */
	public String methodOutputData() {
		// Method for testing purpose only.
		return "test";
	}

	/**
	 * Test method without input arguments and throwing a {@link Throwable}.
	 *
	 * @return Nothing since a {@link Throwable} is always thrown.
	 * @throws Throwable for test purpose.
	 */
	public String methodThrowingThrowable() throws Throwable {
		// Method for testing purpose only.
		throw new Throwable("Test throwable");
	}

	/**
	 * Test method using an {@link AdditionalDataProvider}.
	 * <p>
	 *     <i>Warning: </i>this method is using {@link Thread#sleep(long)} to simulate a quick processing of few
	 *     milliseconds.
	 * </p>
	 *
	 * @param processedItems	The number of processed items set into the {@link AdditionalDataProvider}.
	 * @return The used {@link AdditionalDataProvider} instance.
	 * @throws InterruptedException if the processing is interrupted.
	 */
	public AdditionalDataProvider methodUsingAdditionalDataProvider(final int processedItems)
		throws InterruptedException {
		// Method for testing purpose only.
		final AdditionalDataProvider additionalDataProvider = new AdditionalDataProvider();
		for (int i = 0; i < processedItems; i++) {
			Thread.sleep(1);
		}
		additionalDataProvider.setProcessedItems(processedItems);
		additionalDataProvider.addComments("additionalDataProviderUsed");
		return additionalDataProvider;
	}

	/**
	 * Test method calling another monitored method.
	 *
	 * @param performanceLogger	The instance of performance logger passed to the called method.
	 * @param configuration		The performance logging configuration passed to the called method.
	 * @param throwException	Whether the called method must throw an exception.
	 * @throws Exception if the called method throws an exception.
	 */
	public void methodCallingAnotherMonitoredOne(final MethodPerformanceLogger performanceLogger,
												 final MethodPerformanceLoggingConfiguration configuration,
												 final boolean throwException) throws Exception {
		// Method for testing purpose only.
		methodCalledFromAnother(performanceLogger, configuration, throwException);
	}

	/**
	 * Test monitored method called from another monitored one.
	 *
	 * @param performanceLogger	The instance of performance logger to use.
	 * @param configuration		The performance logging configuration to use.
	 * @param throwException	Whether the method must throw an exception.
	 * @throws Exception if required by parameter {@code throwException}.
	 */
	public void methodCalledFromAnother(final MethodPerformanceLogger performanceLogger,
										final MethodPerformanceLoggingConfiguration configuration,
										final boolean throwException) throws Exception {
		// Method for testing purpose only.
		final PerformanceTimer timer = performanceLogger.start(configuration, "calledMethod");

		assertNotNull(timer);
		assertNotNull(timer.getParent());
		assertThat(timer.getParent().getChildren().size(), is(1));
		assertNull(timer.getChildren());
		assertTrue(timer.isRunning());
		assertTrue(timer.getParent().isRunning());

		if (throwException) {
			timer.getPerformanceLogEntry().setFailed(true);
			performanceLogger.stopAndLog(configuration, timer);

			assertFalse(timer.isRunning());
			assertTrue(timer.getParent().isRunning());

			throw new Exception("Test performance with exception.");
		} else {
			performanceLogger.stopAndLog(configuration, timer);

			assertFalse(timer.isRunning());
			assertTrue(timer.getParent().isRunning());
		}
	}

	/**
	 * Nested class using JAX-RS to declare API endpoints for test purpose.
	 */
	@Path("/apiTest")
	public static class JaxRsApiTestClass {
		/**
		 * Test GET endpoint in JAX-RS API class.
		 */
		@GET
		@Path("/test")
		public void httpGetEndpointMethod() {
			// Method for testing purpose only.
		}

		/**
		 * Test POST endpoint in JAX-RS API class.
		 */
		@POST
		public void httpPostEndpointMethod() {
			// Method for testing purpose only.
		}

		/**
		 * Test PUT endpoint in JAX-RS API class.
		 */
		@PUT
		public void httpPutEndpointMethod() {
			// Method for testing purpose only.
		}

		/**
		 * Test PATCH endpoint in JAX-RS API class.
		 */
		@PATCH
		public void httpPatchEndpointMethod() {
			// Method for testing purpose only.
		}

		/**
		 * Test DELETE endpoint in JAX-RS API class.
		 */
		@DELETE
		public void httpDeleteEndpointMethod() {
			// Method for testing purpose only.
		}

		/**
		 * Test OPTIONS endpoint in JAX-RS API class.
		 */
		@OPTIONS
		public void httpOptionsEndpointMethod() {
			// Method for testing purpose only.
		}

		/**
		 * Test HEAD endpoint in JAX-RS API class.
		 */
		@HEAD
		public void httpHeadEndpointMethod() {
			// Method for testing purpose only.
		}
	}

	/**
	 * Nested class using Spring Web Framework to declare API endpoints for test purpose.
	 */
	@RequestMapping("/apiTest")
	public static class SpringWebApiTestClass {
		/**
		 * Test GET endpoint in Spring Web API class.
		 */
		@GetMapping(path = "/test")
		public void httpGetEndpointMethod() {
			// Method for testing purpose only.
		}

		/**
		 * Test POST endpoint in Spring Web API class.
		 */
		@PostMapping
		public void httpPostEndpointMethod() {
			// Method for testing purpose only.
		}

		/**
		 * Test PUT endpoint in Spring Web API class.
		 */
		@PutMapping
		public void httpPutEndpointMethod() {
			// Method for testing purpose only.
		}

		/**
		 * Test PATCH endpoint in Spring Web API class.
		 */
		@PatchMapping
		public void httpPatchEndpointMethod() {
			// Method for testing purpose only.
		}

		/**
		 * Test DELETE endpoint in Spring Web API class.
		 */
		@DeleteMapping
		public void httpDeleteEndpointMethod() {
			// Method for testing purpose only.
		}

		/**
		 * Test OPTIONS endpoint in Spring Web API class.
		 */
		@RequestMapping(method = RequestMethod.OPTIONS, path = "/test")
		public void httpOptionsEndpointMethod() {
			// Method for testing purpose only.
		}

		/**
		 * Test HEAD endpoint in Spring Web API class.
		 */
		@RequestMapping(method = RequestMethod.HEAD, value = "/test")
		public void httpHeadEndpointMethod() {
			// Method for testing purpose only.
		}
	}
}
