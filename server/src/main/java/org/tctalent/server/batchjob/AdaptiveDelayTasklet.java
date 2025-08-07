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

import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.item.ChunkProcessor;
import org.springframework.batch.core.step.item.ChunkProvider;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
public class AdaptiveDelayTasklet<I> implements Tasklet {
  private final ChunkProvider<I> chunkProvider;
  private final ChunkProcessor<I> chunkProcessor;
  private final long maxDelayMs;

  public AdaptiveDelayTasklet(
      ChunkProvider<I> chunkProvider,
      ChunkProcessor<I> chunkProcessor,
      long maxDelayMs) {
    this.chunkProvider = chunkProvider;
    this.chunkProcessor = chunkProcessor;
    this.maxDelayMs = maxDelayMs;
  }

  @Nullable
  @Override
  public RepeatStatus execute(
      @NonNull StepContribution contribution, @NonNull ChunkContext chunkContext) throws Exception {

    var chunk = chunkProvider.provide(contribution);
    if (chunk.isEmpty()) {
      return RepeatStatus.FINISHED;
    }

    // Read + process one chunk
    long start = System.currentTimeMillis();

    chunkProcessor.process(contribution, chunk);
    chunkProvider.postProcess(contribution, chunk);

    long duration = System.currentTimeMillis() - start;
    //TODO JC Better logic - see Trigger
    long delay = Math.min((long) (duration * 1.5), maxDelayMs);

    System.out.printf("Processed chunk in %d ms. Sleeping for %d ms.%n", duration, delay);

    Thread.sleep(delay); // synchronous, no session loss

    return RepeatStatus.CONTINUABLE;
  }
}
