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
import org.springframework.batch.core.step.item.ChunkOrientedTasklet;
import org.springframework.batch.core.step.item.SimpleChunkProcessor;
import org.springframework.batch.core.step.item.SimpleChunkProvider;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.repeat.policy.SimpleCompletionPolicy;
import org.springframework.batch.repeat.support.RepeatTemplate;
import org.springframework.lang.NonNull;
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
 * <p>
 * Factory for creating simple, single step candidate batch jobs which process all candidates
 * specified in a candidate source.
 * </p>
 * <p>
 *     The jobs created here are designed to be called using BatchJobService.launchJob.
 * </p>
 * <p>
 *     Example:
 * </p>
 * <p>
 *     Just create a processor with the code you want to apply to each candidate...
 * </p>
 * <pre>{@code
 *         //Create an anonymous class instance of an ItemProcessor.
 *         ItemProcessor<Candidate, Candidate> myCandidateProcessor =
 *             candidate -> {
 *                 //Do something with the candidate
 *                 candidate.updateText();
 *
 *                 //Return the candidate if you want to update it, or else return null.
 *                 return candidate;
 *             };
 * }</pre>
 * <p>
 *     Then create a job to run on candidates in a given saved search...
 * </p>
 * <pre>{@code
 *         Job myJob = candidateBatchJobFactory
 *             .builder("MyJob", savedSearch, myCandidateProcessor)
 *             .build();
 * }</pre>
 * <p>
 *     Then start the batch running...
 * </p>
 * <pre>{@code
 *         //batchJobService is a standard TC service
 *         batchJobService.launchJob(myJob, false);
 * }</pre>
 * <p>
 *     You could also create a normal ItemProcessor class like...
 * </p>
 * <pre>{@code
 * public class MyProcessorClass implements ItemProcessor<Candidate,Candidate> {
 *     public Candidate process(Candidate candidate) throws Exception {
 *         candidate.updateText();
 *         return candidate;
 *     }
 * }
 * }</pre>
 * <p>
 *     ...and configure in an instance of that class.
 * </p>
 * <p>
 *     There are also a number of options you can specify when building the job...
 * </p>
 * <pre>{@code
 *    Job myJob = candidateBatchJobFactory
 *       .builder("MyJob", savedSearch, myCandidateProcessor)
 *       .percentageOfCpu(25) //25% of cpu
 *       .chunkSize(50)       //Process 50 candidates at a time
 *       .maxDelayMs(60000)   //Maximum delay between processing chunks is a minute
 *       .build();
 * }</pre>
 * <p>
 *     Defaults are 50% cpu, chunk size of 100 and max delay of 30 seconds.
 * </p>
 * @author John Cameron
 */
@Slf4j
@Component
public class CandidateBatchJobFactory {

    private final PlatformTransactionManager transactionManager;
    private final CandidateRepository candidateRepository;
    private final CandidateService candidateService;
    private final JobRepository jobRepository;
    private final SavedSearchService savedSearchService;

    public CandidateBatchJobFactory(
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

    public CandidateBatchJobBuilder builder(String name, SavedSearch search, ItemProcessor<Candidate, Candidate> processor) {
        return new CandidateBatchJobBuilder(name, search, processor );
    }

    public CandidateBatchJobBuilder builder(String name, SavedList savedList, ItemProcessor<Candidate, Candidate> processor) {
        return new CandidateBatchJobBuilder(name, savedList, processor );
    }

    /**
     * Builder used to Create a simple single step Spring batch job which runs a processor over
     * all candidates returned by a candidate source: saved list or search.
     */
    public class CandidateBatchJobBuilder {
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
        public CandidateBatchJobBuilder(
            String name, SavedSearch savedSearch, ItemProcessor<Candidate, Candidate> processor) {
            this.name = name;
            this.savedSearch = savedSearch;
            this.processor = processor;
        }

        /**
         * See comments for above constructor - except that the source of candidates is a SavedList.
         */
        public CandidateBatchJobBuilder(
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
        public CandidateBatchJobBuilder chunkSize(int chunkSize){
            this.chunkSize = chunkSize;
            return this;
        }

        /**
         * Maximum delay in milliseconds between running chunks.
         * <p>
         * Defaults to 30,000 milliseconds.
         * </p>
         */
        public CandidateBatchJobBuilder maxDelayMs(long maxDelayMs) {
            this.maxDelayMs = maxDelayMs;
            return this;
        }

        /**
         * Desired maximum CPU load
         * <p>
         * Defaults to 50. Must be greater than 0 and less than or equal to 100.
         * </p>
         */
        public CandidateBatchJobBuilder percentageOfCpu(int percentageOfCpu) {
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

            final Tasklet adaptiveTasklet = getAdaptiveTasklet(candidateReader, candidateWriter);

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

        @NonNull
        private Tasklet getAdaptiveTasklet(
            CandidateReader candidateReader, CandidateWriter candidateWriter) {

            RepeatTemplate repeatTemplate = new RepeatTemplate();
            repeatTemplate.setCompletionPolicy(new SimpleCompletionPolicy(chunkSize));
            SimpleChunkProvider<Candidate> chunkProvider =
                new SimpleChunkProvider<>(candidateReader, repeatTemplate);
            SimpleChunkProcessor<Candidate, Candidate> chunkProcessor =
                new SimpleChunkProcessor<>(processor, candidateWriter);
            ChunkOrientedTasklet<Candidate> chunkOrientedTasklet =
                new ChunkOrientedTasklet<>(chunkProvider, chunkProcessor);

            return new AdaptiveDelayTasklet<>(chunkOrientedTasklet, percentageOfCpu, maxDelayMs);
        }
    }
}
