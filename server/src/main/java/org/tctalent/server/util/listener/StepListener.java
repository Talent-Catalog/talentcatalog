/*
 * Copyright (c) 2025 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.util.listener;

/**
 * A listener interface for monitoring the lifecycle of a batch processing step.
 *
 * <p>Implementations can override any of the default methods (which do nothing) to hook into specific
 * points of the step execution: before the step starts, after it completes or when it fails.
 *
 * <p>This interface is designed to allow decoupled monitoring and handling logic, such as logging
 * or alerting, without tying it to the core batch processing logic.</p>
 */
public interface StepListener {
    default void beforeStep(String jobName) {}
    default void afterStep(String jobName) {}
    default void onStepFailure(String jobName, Exception exception) {}
}
