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

package com.github.maximevw.autolog.core.logger.performance;

import com.github.maximevw.autolog.core.annotations.AutoLogPerformance;
import org.apiguardian.api.API;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class allows a method monitored with {@link AutoLogPerformance} to update some data to log during its execution:
 * <ul>
 *     <li>add dynamic comments relative to the method execution</li>
 *     <li>set a number of processed items to get an average execution time by processed item</li>
 * </ul>
 *
 * <p>
 *     This class is {@link ThreadLocal}-based. Getting values through a call to the methods {@link #getComments()} and
 *     {@link #getProcessedItems()} will automatically remove the values from the {@link ThreadLocal}.
 * </p>
 */
@API(status = API.Status.STABLE, since = "1.0.0")
public final class AdditionalDataProvider {

	private ThreadLocal<List<String>> threadLocalComments;
	private ThreadLocal<Integer> threadLocalProcessedItems;

	/**
	 * Default constructor.
	 *
	 * <p>
	 *     It instantiates the comments to a new {@link ArrayList} instance and the number of processed items to
	 *     {@code null}.
	 * </p>
	 */
	public AdditionalDataProvider() {
		threadLocalComments = ThreadLocal.withInitial(ArrayList::new);
		threadLocalProcessedItems = ThreadLocal.withInitial(() -> null);
	}

	/**
	 * Adds a list of comments to log to the current list.
	 *
	 * @param commentsToAdd The additional comments to log.
	 */
	public void addComments(final String... commentsToAdd) {
		final List<String> comments = threadLocalComments.get();
		comments.addAll(Arrays.asList(commentsToAdd));
		threadLocalComments.set(comments);
	}

	/**
	 * Sets the number of processed items.
	 *
	 * @param processedItems The number of processed items to log.
	 */
	public void setProcessedItems(final int processedItems) {
		threadLocalProcessedItems.set(processedItems);
	}

	/**
	 * Gets the additional comments to log.
	 *
	 * @return The additional comments to log.
	 */
	public List<String> getComments() {
		final List<String> comments = threadLocalComments.get();
		threadLocalComments.remove();
		return comments;
	}

	/**
	 * Gets the number of items processed by the monitored method.
	 *
	 * @return The number of items processed by the monitored method.
	 */
	public Integer getProcessedItems() {
		final Integer processedItems = threadLocalProcessedItems.get();
		threadLocalProcessedItems.remove();
		return processedItems;
	}
}
