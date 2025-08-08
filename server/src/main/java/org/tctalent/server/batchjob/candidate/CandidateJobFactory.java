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
import org.springframework.batch.core.step.item.SimpleChunkProcessor;
import org.springframework.batch.core.step.item.SimpleChunkProvider;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy;
import org.springframework.batch.repeat.support.RepeatTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.tctalent.server.batchjob.AdaptiveDelayTasklet;
import org.tctalent.server.batchjob.LoggingChunkListener;
import org.tctalent.server.batchjob.LoggingJobExecutionListener;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.SavedSearch;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.service.db.CandidateService;
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
@Component
public class CandidateJobFactory {

    private final PlatformTransactionManager transactionManager;
    private final CandidateRepository candidateRepository;
    private final CandidateService candidateService;
    private final JobRepository jobRepository;
    private final SavedSearchService savedSearchService;

    public CandidateJobFactory(
        PlatformTransactionManager transactionManager,
        JobRepository jobRepository,
        CandidateRepository candidateRepository, CandidateService candidateService,
        SavedSearchService savedSearchService) {
        this.transactionManager = transactionManager;
        this.candidateRepository = candidateRepository;
        this.candidateService = candidateService;
        this.jobRepository = jobRepository;
        this.savedSearchService = savedSearchService;
    }

    public CandidateJobBuilder builder(String name, SavedSearch search, ItemProcessor<Candidate, Candidate> processor) {
        return new CandidateJobBuilder(name, search, processor );
    }

    public CandidateJobBuilder builder(String name, SavedList savedList, ItemProcessor<Candidate, Candidate> processor) {
        return new CandidateJobBuilder(name, savedList, processor );
    }

    /**
     * Builder used to Create a simple single step Spring batch job which runs a processor over
     * all candidates returned by a candidate source: saved list or search.
     */
    public class CandidateJobBuilder {
        private final String name;
        private SavedList savedList;
        private SavedSearch savedSearch;
        private final ItemProcessor<Candidate, Candidate> processor;

        //Default values
        private int chunkSize = 100;
        private long maxDelayMs = 30000;
        private int percentageOfCpu = 50;

        /**
         * Builder which can be used to creates a simple single step Spring batch job with the given
         * name which calls the given processor on all candidates returned by the given search.
         * @param name Name for job
         * @param savedSearch Search called to return candidates
         * @param processor Processor to execute on each candidate.
         *    If the processor updates the candidate it is passed, it should return the candidate
         *    that it has updated. However, if the processor does not update the candidate,
         *    it should return null, in which case the candidate will not be updated on the database.
         *    So processors can be used just to scan data without making any changes.
         */
        public CandidateJobBuilder(
            String name, SavedSearch savedSearch, ItemProcessor<Candidate, Candidate> processor) {
            this.name = name;
            this.savedSearch = savedSearch;
            this.processor = processor;
        }

        /**
         * See comments for above constructor - except that the source of candidates is a SavedList.
         */
        public CandidateJobBuilder(
            String name, SavedList savedList, ItemProcessor<Candidate, Candidate> processor) {
            this.name = name;
            this.savedList = savedList;
            this.processor = processor;
        }

        /**
         * Number of candidates to process at a time.
         * This translates to the page size in the query.
         * <p>
         * Defaults to 100 candidates.
         * </p>
         */
        public CandidateJobBuilder chunkSize(int chunkSize){
            this.chunkSize = chunkSize;
            return this;
        }

        /**
         * Maximum delay in milliseconds between running chunks.
         * <p>
         * Defaults to 30,000 milliseconds.
         * </p>
         */
        public CandidateJobBuilder maxDelayMs(long maxDelayMs) {
            this.maxDelayMs = maxDelayMs;
            return this;
        }

        /**
         * Desired maximum CPU load
         * <p>
         * Defaults to 50. Must be greater than 0 and less than or equal to 100.
         * </p>
         */
        public CandidateJobBuilder percentageOfCpu(int percentageOfCpu) {
            this.percentageOfCpu = percentageOfCpu;
            return this;
        }

        public Job build() {
            CandidateReader candidateReader;
            if (savedList != null) {
                candidateReader = new CandidateReader(savedList, chunkSize, candidateService);
            } else if (savedSearch != null) {
                candidateReader = new CandidateReader(savedSearch, chunkSize, savedSearchService);
            } else {
               throw new IllegalArgumentException("No saved search or saved list specified");
            }

            if (percentageOfCpu <= 0 || percentageOfCpu > 100) {
                throw new IllegalArgumentException(
                    "Percentage of CPU must be greater than 0 and less than or equal to 100");
            }

            CandidateWriter candidateWriter = new CandidateWriter(candidateRepository);

            RepeatTemplate repeatTemplate = new RepeatTemplate();
            repeatTemplate.setCompletionPolicy(new SimpleCompletionPolicy(chunkSize));
            SimpleChunkProvider<Candidate> chunkProvider =
                new SimpleChunkProvider<>(candidateReader, repeatTemplate);
            SimpleChunkProcessor<Candidate, Candidate> chunkProcessor =
                new SimpleChunkProcessor<>(processor, candidateWriter);

            Tasklet adaptiveTasklet =
                new AdaptiveDelayTasklet<>(chunkProvider, chunkProcessor, percentageOfCpu, maxDelayMs);

            Step candidateStep =
                new StepBuilder("candidateStep", jobRepository)
                    .tasklet(adaptiveTasklet, transactionManager)
                    .listener(new LoggingChunkListener())
                    .build();

            return new JobBuilder(name, jobRepository)
                .listener(new LoggingJobExecutionListener())
                .start(candidateStep)
                .build();
        }
    }
}
