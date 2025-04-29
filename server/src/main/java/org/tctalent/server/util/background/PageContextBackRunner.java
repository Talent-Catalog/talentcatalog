// Copyright 2009 Cameron Edge Pty Ltd. All rights reserved.
// Reproduction in whole or in part in any form or medium without express
// written permission of Cameron Edge Pty Ltd is strictly prohibited.

package org.tctalent.server.util.background;

import java.time.Duration;
import java.util.concurrent.ScheduledFuture;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.TaskScheduler;
import org.tctalent.server.util.background.logging.BackLogger;

/**
 * Simplified BackRunner for PageContext's
 *
 * @author John Cameron
 */
public class PageContextBackRunner extends BackRunner<PageContext> {

    public ScheduledFuture<?> start(
        TaskScheduler taskScheduler,
        BackProcessor<PageContext> backProcessor,
        @Nullable BackLogger backLogger,
        int percentageCPU
    ) {
        return super.start(
            taskScheduler,
            backProcessor,
            backLogger,
            new PageContext(null),
            percentageCPU
        );
    }

    public ScheduledFuture<?> start(
        TaskScheduler taskScheduler,
        BackProcessor<PageContext> backProcessor,
        @Nullable BackLogger backLogger,
        Duration delay
    ) {
        return super.start(
            taskScheduler,
            backProcessor,
            backLogger,
            new PageContext(null),
            delay
        );
    }
}
