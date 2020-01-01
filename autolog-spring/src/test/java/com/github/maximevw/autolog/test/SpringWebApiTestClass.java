/*-
 * #%L
 * Autolog Spring integration module
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

import com.github.maximevw.autolog.core.annotations.AutoLogPerformance;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

/**
 * Class using Spring Web Framework to declare API endpoints for test purpose.
 */
@RequestMapping("/apiTest")
@AutoLogPerformance
public class SpringWebApiTestClass {

	public void testApiBadEndpointMethod() {
		// Do nothing: for test purpose only.
	}

	@GetMapping("/otherTest")
	@AutoLogPerformance(apiEndpointsAutoConfig = false, showClassName = false)
	public void testApiEndpointMethodNoAutoconfig() {
		// Do nothing: for test purpose only.
	}

	@GetMapping("/test")
	public void testApiGETEndpointMethod() {
		// Do nothing: for test purpose only.
	}

	@PostMapping("/test")
	public void testApiPOSTEndpointMethod() {
		// Do nothing: for test purpose only.
	}

	@PutMapping("/test")
	public void testApiPUTEndpointMethod() {
		// Do nothing: for test purpose only.
	}

	@DeleteMapping("/test")
	public void testApiDELETEEndpointMethod() {
		// Do nothing: for test purpose only.
	}

	@PatchMapping("/test")
	public void testApiPATCHEndpointMethod() {
		// Do nothing: for test purpose only.
	}

	@RequestMapping(value = "/test", method = RequestMethod.OPTIONS)
	public void testApiOPTIONSEndpointMethod() {
		// Do nothing: for test purpose only.
	}

	@RequestMapping(path = "/test", method = RequestMethod.HEAD)
	public void testApiHEADEndpointMethod() {
		// Do nothing: for test purpose only.
	}

}
