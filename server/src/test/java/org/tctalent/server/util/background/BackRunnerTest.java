/*
 * Copyright (c) 2024 Talent Catalog.
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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

class BackRunnerTest {

    private BackRunner<IdContext> backRunner;
    private BackProcessor<IdContext> backProcessor;
    ThreadPoolTaskScheduler taskScheduler;

    @BeforeEach
    void setUp() {
        taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.initialize();
        backRunner = new BackRunner<>();
        backProcessor = new BackProcessor<>() {
            @Override
            public boolean process(IdContext ctx) {
                long startId = ctx.getLastProcessedId() == null ? 0 : ctx.getLastProcessedId()+1;
                System.out.println("Processing " + ctx.getNumToProcess() + " ids starting from "
                    + (startId == 0 ? "beginning " : startId) );
                long lastProcessed = startId + ctx.getNumToProcess() - 1;
                ctx.setLastProcessedId(lastProcessed);
                return lastProcessed+1 >= 50;
            }
        };
    }

    @Test
    void testFixedScheduling() throws InterruptedException {
        ScheduledFuture<?> scheduledFuture =
            backRunner.start(taskScheduler, backProcessor, new IdContext(null, 10, null),
                Duration.ofSeconds(1));
        assertFalse(scheduledFuture.isDone());
        Thread.sleep(5000);
        assertTrue(scheduledFuture.isDone());
    }

    @Test
    void testVariableScheduling() throws InterruptedException {
        ScheduledFuture<?> scheduledFuture =
            backRunner.start(taskScheduler, backProcessor, new IdContext(null, 10, null),
                50);
        assertFalse(scheduledFuture.isDone());
        Thread.sleep(5000);
        assertTrue(scheduledFuture.isDone());
    }
}
