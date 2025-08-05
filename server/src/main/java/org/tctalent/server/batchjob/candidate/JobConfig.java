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

package org.tctalent.server.batchjob.candidate;

import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;
import org.tctalent.server.batchjob.LoggingChunkListener;
import org.tctalent.server.batchjob.LoggingJobExecutionListener;
import org.tctalent.server.model.db.Candidate;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
@Configuration
@RequiredArgsConstructor
public class JobConfig {

    @Bean
    public Job candidateJob(JobRepository jobRepository,
        LoggingJobExecutionListener listener,
        Step candidateStep) {
        return new JobBuilder("candidateJob", jobRepository)
            .listener(listener)
            .start(candidateStep)
            .build();

    }

    @Bean
    public Step candidateStep(JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        ItemReader<Candidate> candidateReader,
        ItemProcessor<Candidate, Candidate> candidateProcessor,
        ItemWriter<Candidate> candidateWriter,
        LoggingChunkListener loggingChunkListener) {

        return new StepBuilder("candidateStep", jobRepository)
            .<Candidate, Candidate>chunk(
                100, //todo jc config
                transactionManager)
            .reader(candidateReader)
            .processor(candidateProcessor)
            .writer(candidateWriter)
            .listener(loggingChunkListener)
            .faultTolerant()
            .build();
    }

}
