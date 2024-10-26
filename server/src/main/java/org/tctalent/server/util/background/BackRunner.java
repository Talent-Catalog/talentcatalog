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

package org.tctalent.server.util.background;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.Getter;
import lombok.Setter;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.Trigger;
import org.springframework.scheduling.support.PeriodicTrigger;

/**
 * This is intended to run long tasks in the background without consuming too much CPU.
 * It does this by using Spring's scheduling to do a bit of work on a task, then wait a while,
 * then do more work, until the task is complete.
 * <p/>
 * Tasks to be completed must implement {@link BackProcessor} - which just needs to implement a
 * single method called "process" which takes a single parameter defining the CONTEXT.
 * CONTEXT is used by the task to keep track of where it is upto in its processing.
 * The task does some processing, then updates the context object recording where it got up to.
 * The next time the process method is called it can continue where it left off.
 * <p/>
 * process returns false if there is still processing to do, in which case it will be called again
 * by the scheduler after a certain delay.
 * When the task has finished all processing, it can return true from the process method. That
 * will cancel the scheduling so that process is not called again.
 *
 *
 * @author John Cameron
 */
@Getter
@Setter
public class BackRunner<CONTEXT> implements Runnable {
    private TaskScheduler taskScheduler;
    private BackProcessor<CONTEXT> backProcessor;
    private Trigger trigger;

    /**
     * This defines the "context" for keeping track of where a {@link BackProcessor}
     * is up to in its processing as managed by a {@link BackRunner}.
     * <p/>
     * For example the context object might just contain a Long representing the page number of
     * candidates from a search, that we are processing a page at a time.
     */
    private CONTEXT batchContext;

    private ScheduledFuture<?> scheduledFuture;

    @Override
    public void run() {
        boolean complete = backProcessor.process(batchContext);
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
     * @param backProcessor Processor called to do the processing
     * @param batchContext Context object used to keep track of processing - initialized to its
     *                     beginning value - ie indicating where processing should start.
     * @param period Delay between each processing call
     * @param timeUnit Seconds etc
     * @return ScheduledFuture which can be used to query the state of the scheduling.
     */
    public ScheduledFuture<?> start(TaskScheduler taskScheduler,
        BackProcessor<CONTEXT> backProcessor, CONTEXT batchContext, long period, TimeUnit timeUnit) {
        this.trigger = new PeriodicTrigger(period, timeUnit);
        return start(taskScheduler, backProcessor, batchContext, trigger);
    }

    /**
     * Starts batch processing using the given processor, initialised with the given context,
     * and configured to adjust the delay between processing calls to work within a given maximum
     * CPU usage.
     * <p/>
     * @param backProcessor Processor called to do the processing
     * @param batchContext Context object used to keep track of processing - initialized to its
     *                     beginning value - ie indicating where processing should start.
     * @param percentageCPU Desired maximum CPU load
     * @return ScheduledFuture which can be usd to query the state of the scheduling.
     */
    public ScheduledFuture<?> start(TaskScheduler taskScheduler,
        BackProcessor<CONTEXT> backProcessor, CONTEXT batchContext, int percentageCPU) {
        this.trigger = new VariableTrigger(percentageCPU);
        return start(taskScheduler, backProcessor, batchContext, trigger);
    }

    private ScheduledFuture<?> start(TaskScheduler taskScheduler,
        BackProcessor<CONTEXT> backProcessor, CONTEXT batchContext, Trigger trigger) {
        this.batchContext = batchContext;
        this.backProcessor = backProcessor;
        scheduledFuture = taskScheduler.schedule(this, trigger);
        return scheduledFuture;
    }
}
