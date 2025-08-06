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

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemWriter;
import org.springframework.transaction.PlatformTransactionManager;
import org.tctalent.server.batchjob.LoggingChunkListener;
import org.tctalent.server.batchjob.LoggingJobExecutionListener;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.service.db.SavedSearchService;

/**
 * Factory for creating simple, single step candidate jobs which process all candidates
 * specified in a candidate source.
 * <p>
 *     The jobs created here are designed to be called using BatchJobService.launch.
 * </p>
 *
 * @author John Cameron
 */
@Slf4j
public class CandidateJobFactory {

    private final ItemWriter<Candidate> candidateWriter;
    private final LoggingChunkListener loggingChunkListener;
    private final LoggingJobExecutionListener loggingJobExecutionListener;
    private final PlatformTransactionManager transactionManager;
    private final JobRepository jobRepository;
    private final SavedSearchService savedSearchService;

    public CandidateJobFactory(
        ItemWriter<Candidate> candidateWriter, LoggingChunkListener loggingChunkListener,
        LoggingJobExecutionListener loggingJobExecutionListener,
        PlatformTransactionManager transactionManager, JobRepository jobRepository,
        SavedSearchService savedSearchService) {
        this.candidateWriter = candidateWriter;
        this.loggingChunkListener = loggingChunkListener;
        this.loggingJobExecutionListener = loggingJobExecutionListener;
        this.transactionManager = transactionManager;
        this.jobRepository = jobRepository;
        this.savedSearchService = savedSearchService;
    }

    /**
     * Creates a simple single step Spring batch job with the given name which calls the
     * given candidateProcessor on all candidates returned by the given search.
     * <p>
     *     If the processor updates the candidate it is passed, it should return the candidate
     *     that it has updated. However, if the processor does not update the candidate it should
     *     return null in which case the candidate will not be updated on the database.
     *     So processors can be used just to scan data without making any changes.
     * </p>
     * @param jobName Name for job
     * @param searchId Id of search called to return candidates
     * @param chunkSize Number of candidates to process at a time. This translates to the page size
     *                  in the search query
     * @param candidateProcessor Processor to execute on each candidate.
     * @return the created batch up. It can be run by calling BatchJobService.launchJob
     */
    public Job createCandidateJob(
        String jobName, long searchId, int chunkSize,
        ItemProcessor<Candidate, Candidate> candidateProcessor) {

        CandidateReader candidateReader = new CandidateReader(searchId, chunkSize, savedSearchService);

        Step candidateStep =
            new StepBuilder("candidateStep", jobRepository)
                .<Candidate, Candidate>chunk(chunkSize, transactionManager)
                .reader(candidateReader)
                .processor(candidateProcessor)
                .writer(candidateWriter)
                .listener(loggingChunkListener)
                .faultTolerant()
                .build();

        return new JobBuilder(jobName, jobRepository)
            .listener(loggingJobExecutionListener)
            .start(candidateStep)
            .build();
    }
}
