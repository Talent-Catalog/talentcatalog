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

package org.tctalent.server.batchjob;

import java.time.Instant;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.item.ChunkOrientedTasklet;
import org.springframework.batch.core.step.item.ChunkProcessor;
import org.springframework.batch.core.step.item.ChunkProvider;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.TaskScheduler;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
public class AdaptiveScheduledTasklet<I> implements Tasklet {

  private final ChunkOrientedTasklet<I> delegate;
  private final TaskScheduler scheduler;
  private final long maxDelayMs;

  public AdaptiveScheduledTasklet(
      ChunkProvider<I> chunkProvider,
      ChunkProcessor<I> chunkProcessor,
      TaskScheduler scheduler, long maxDelayMs) {

    this.delegate = new ChunkOrientedTasklet<>(chunkProvider, chunkProcessor);
    this.scheduler = scheduler;
    this.maxDelayMs = maxDelayMs;
  }

  @Nullable
  @Override
  public RepeatStatus execute(
      @NonNull StepContribution contribution, @NonNull ChunkContext chunkContext) throws Exception {

    long start = System.currentTimeMillis();

    RepeatStatus status = delegate.execute(contribution, chunkContext);

    long duration = System.currentTimeMillis() - start;

    //TODO JC Better logic - see Trigger
    long nextDelay = Math.min(duration * 2, maxDelayMs);

    if (status == RepeatStatus.FINISHED) {
      return RepeatStatus.FINISHED;
    }

    scheduler.schedule(() -> {
        try {
          delegate.execute(contribution, chunkContext);
        } catch (Exception e) {
          throw new RuntimeException(e);
        }
      }, Instant.now().plusMillis(nextDelay)
    );

    return RepeatStatus.CONTINUABLE;
  }
}
