/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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

package org.tctalent.server.util.batch;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Component;

/**
 * This is intended to run long tasks in the background without consuming too much CPU.
 * It does this by using Spring's scheduling to do a bit of work on a task, then wait a while,
 * then do more work, until the task is complete.
 * <p/>
 * Tasks to be completed must implement {@link BatchProcessor} - which just needs to implement a
 * single method called "process" which takes a single parameter {@link BatchContext}.
 * BatchContext is used by the task to keep track of where it is upto in its processing.
 * The task does some processing, then updates the context object recording where it got up to.
 * The next time the process method is called it can continue where it left off.
 * <p/>
 * process returns false if there is still processing to do, in which case it will be called again
 * by the scheduler after a certain delay.
 * When the task has finished all processing, it can return true from the process method. That
 * will cancel the scheduling so that process is not called again.
 *
 * @author John Cameron
 */
//todo May be better not to make BatchRunner a component. Then can user <T> logic.
//Then need to inject Task Scheduler.
@Component
@Scope("prototype")
@RequiredArgsConstructor
public class BatchRunner implements Runnable {
    private final TaskScheduler taskScheduler;
    private BatchProcessor batchProcessor;
    private BatchContext batchContext;

    private ScheduledFuture<?> scheduledFuture;

    @Override
    public void run() {
        boolean complete = batchProcessor.process(batchContext);
        if (complete) {
            //todo The runner could be configured to notify by email once processing is complete.
            scheduledFuture.cancel(true);
        }
    }

    /**
     * Starts batch processing using the given processor, initialised with the given context,
     * and configured to wait for the given delay between processing calls.
     * <p/>
     * The demand on the TC can be managed by configuring the batch processor to process small
     * amounts at a time, and also by configuring a decent delay between processing calls.
     * @param batchProcessor Processor called to do the processing
     * @param batchContext Context object used to keep track of processing - initialized to its
     *                     beginning value - ie indicating where processing should start.
     * @param delay Delay between each processing call
     * @return ScheduledFuture which can be used to query the state of the scheduling.
     */
    public ScheduledFuture<?> start(
        BatchProcessor batchProcessor, BatchContext batchContext, Duration delay) {
        this.batchContext = batchContext;
        this.batchProcessor = batchProcessor;
        scheduledFuture = taskScheduler.scheduleWithFixedDelay(this, delay);
        return scheduledFuture;
    }
}
