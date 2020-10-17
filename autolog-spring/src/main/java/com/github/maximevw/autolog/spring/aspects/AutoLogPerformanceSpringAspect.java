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

package com.github.maximevw.autolog.spring.aspects;

import com.github.maximevw.autolog.aspectj.aspects.AutoLogPerformanceAspect;
import com.github.maximevw.autolog.core.annotations.AutoLogPerformance;
import com.github.maximevw.autolog.core.logger.LoggerManager;
import org.apiguardian.api.API;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Spring AOP aspect allowing to automatically monitor and log the performance information of public methods annotated
 * with {@link AutoLogPerformance} or included in classes annotated with such annotations.
 * <p>
 * Be careful with usage of this aspect and please read
 * <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#aop">Spring AOP documentation</a>
 * and especially the section about <a href="https://docs.spring.io/spring/docs/current/spring-framework-reference/core.html#aop-understanding-aop-proxies">
 * proxy-based aspects</a> to be aware of the limitations implied by Spring AOP.
 * </p>
 */
@API(status = API.Status.STABLE, since = "1.0.0")
@Component
public class AutoLogPerformanceSpringAspect extends AutoLogPerformanceAspect {

	/**
	 * Constructor.
	 * <p>
	 *     It autowires a bean of type {@link LoggerManager}.
	 * </p>
	 *
	 * @param loggerManager The logger manager instance used by the aspect.
	 */
	@Autowired
	public AutoLogPerformanceSpringAspect(final LoggerManager loggerManager) {
		super(loggerManager);
	}

}
