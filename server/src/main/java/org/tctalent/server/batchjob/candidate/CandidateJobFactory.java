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

import kotlin.NotImplementedError;
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
    private final JobRepository jobRepository;
    private final SavedSearchService savedSearchService;

    public CandidateJobFactory(
        PlatformTransactionManager transactionManager,
        JobRepository jobRepository,
        CandidateRepository candidateRepository,
        SavedSearchService savedSearchService) {
        this.transactionManager = transactionManager;
        this.candidateRepository = candidateRepository;
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
     * //TODO JC Update this for builder
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
    public class CandidateJobBuilder {
        private final String name;
        private SavedList savedList;
        private SavedSearch savedSearch;
        private final ItemProcessor<Candidate, Candidate> processor;
        private int chunkSize = 100;
        private long maxDelayMs = 30000;

        public CandidateJobBuilder(
            String name, SavedSearch savedSearch, ItemProcessor<Candidate, Candidate> processor) {
            this.name = name;
            this.savedSearch = savedSearch;
            this.processor = processor;
        }

        public CandidateJobBuilder(
            String name, SavedList savedList, ItemProcessor<Candidate, Candidate> processor) {
            this.name = name;
            this.savedList = savedList;
            this.processor = processor;
        }

        public CandidateJobBuilder chunkSize(int chunkSize){
            this.chunkSize = chunkSize;
            return this;
        }

        public CandidateJobBuilder maxDelayMs(long maxDelayMs) {
            this.maxDelayMs = maxDelayMs;
            return this;
        }

        public Job build() {
            CandidateReader candidateReader;
            if (savedList != null) {
                //TODO JC
                throw  new NotImplementedError("SavedList not yet implemented");
            } else if (savedSearch != null) {
                candidateReader = new CandidateReader(savedSearch.getId(), chunkSize, savedSearchService);
            } else {
               throw new IllegalArgumentException("No saved search or saved list specified");
            }
            CandidateWriter candidateWriter = new CandidateWriter(candidateRepository);

            RepeatTemplate repeatTemplate = new RepeatTemplate();
            repeatTemplate.setCompletionPolicy(new SimpleCompletionPolicy(chunkSize));
            SimpleChunkProvider<Candidate> chunkProvider =
                new SimpleChunkProvider<>(candidateReader, repeatTemplate);
            SimpleChunkProcessor<Candidate, Candidate> chunkProcessor =
                new SimpleChunkProcessor<>(processor, candidateWriter);

            Tasklet adaptiveTasklet =
                new AdaptiveDelayTasklet<>(chunkProvider, chunkProcessor, maxDelayMs);

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
