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
 * Replace the normal Tasklet so that we can insert delays between processing of chunks.
 *
 * @author John Cameron
 */
public class AdaptiveDelayTasklet<I> implements Tasklet {
  private final ChunkProvider<I> chunkProvider;
  private final ChunkProcessor<I> chunkProcessor;
  private final int percentageOfCpu;
  private final long maxDelayMs;

  public AdaptiveDelayTasklet(
      ChunkProvider<I> chunkProvider,
      ChunkProcessor<I> chunkProcessor,
      int percentageOfCpu,
      long maxDelayMs) {
    this.chunkProvider = chunkProvider;
    this.chunkProcessor = chunkProcessor;
    this.maxDelayMs = maxDelayMs;
    this.percentageOfCpu = percentageOfCpu;
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

    long processingTime = System.currentTimeMillis() - start;
    long delay = Math.min(computeDelay(processingTime), maxDelayMs);

    System.out.printf("Processed chunk in %d ms. Sleeping for %d ms.%n", processingTime, delay);

    Thread.sleep(delay);

    return RepeatStatus.CONTINUABLE;
  }

  private long computeDelay(long processingTime) {
      /*
        As an example, if desired percentageOfCPU is 50% then the delay will match the processing
        time. (100-50)/50 = 1.

        The minimum percentage is 1%, which means a long delay 99 times the processing time.
        (100-1)/1 = 99.

        The maximum percentage is 100%, in which case there will be no delay. (100-100)/100 = 0.

        So the delay ranges from 0 to 99 times the last processing time.
       */
      return processingTime * (100 - percentageOfCpu) /percentageOfCpu;
  }

}
