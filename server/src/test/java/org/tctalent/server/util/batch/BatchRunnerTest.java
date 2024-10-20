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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
class BatchRunnerTest {

    private BatchRunner batchRunner;
    private BatchProcessor batchProcessor;

    @BeforeEach
    void setUp() {
        ThreadPoolTaskScheduler taskScheduler = new ThreadPoolTaskScheduler();
        taskScheduler.initialize();
        batchProcessor = new BatchProcessor() {
            @Override
            public boolean process(BatchContext context) {
                Long page = (Long) context.getContext();
                System.out.println("Processing page " + page);
                page++;
                context.setContext(page);
                return page >= 5;
            }
        };
        batchRunner = new BatchRunner( taskScheduler );
    }

    @Test
    void testBatchExport() throws InterruptedException {
        ScheduledFuture<?> scheduledFuture =
            batchRunner.start(batchProcessor, new PagingBatchContext(0L), Duration.ofSeconds(1));
        assertFalse(scheduledFuture.isDone());
        Thread.sleep(5000);
        assertTrue(scheduledFuture.isDone());
    }
}
