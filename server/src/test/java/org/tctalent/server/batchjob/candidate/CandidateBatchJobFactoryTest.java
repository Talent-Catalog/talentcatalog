/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.batchjob.candidate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.transaction.PlatformTransactionManager;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.SavedSearch;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.request.candidate.SearchCandidateRequest;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.SavedSearchService;

@ExtendWith(MockitoExtension.class)
class CandidateBatchJobFactoryTest {

  @Mock
  private PlatformTransactionManager transactionManager;

  @Mock
  private JobRepository jobRepository;

  @Mock
  private CandidateRepository candidateRepository;

  @Mock
  private CandidateService candidateService;

  @Mock
  private SavedSearchService savedSearchService;

  @Mock
  private SavedSearch savedSearch;

  @Mock
  private SavedList savedList;

  private CandidateBatchJobFactory factory;

  private final ItemProcessor<Candidate, Candidate> processor = candidate -> candidate;

  @BeforeEach
  void setUp() {
    factory = new CandidateBatchJobFactory(
        transactionManager,
        jobRepository,
        candidateRepository,
        candidateService,
        savedSearchService
    );
  }

  @Test
  void outerBuilderWithSavedSearchReturnsInnerBuilderAndBuildsJob() {
    CandidateBatchJobFactory.CandidateBatchJobBuilder builder =
        factory.builder("savedSearchJob", savedSearch, processor);

    Job job = builder.build();

    assertNotNull(builder);
    assertNotNull(job);
    assertEquals("savedSearchJob", job.getName());
  }

  @Test
  void outerBuilderWithSavedListReturnsInnerBuilderAndBuildsJob() {
    CandidateBatchJobFactory.CandidateBatchJobBuilder builder =
        factory.builder("savedListJob", savedList, processor);

    Job job = builder.build();

    assertNotNull(builder);
    assertNotNull(job);
    assertEquals("savedListJob", job.getName());
  }

  @Test
  void outerBuilderWithSearchCandidateRequestReturnsInnerBuilderAndBuildsJob() {
    SearchCandidateRequest request = new SearchCandidateRequest();

    CandidateBatchJobFactory.CandidateBatchJobBuilder builder =
        factory.builder("requestJob", request, processor);

    Job job = builder.build();

    assertNotNull(builder);
    assertNotNull(job);
    assertEquals("requestJob", job.getName());
  }

  @Test
  void innerBuilderSavedSearchConstructorBuildsJob() {
    CandidateBatchJobFactory.CandidateBatchJobBuilder builder =
        factory.new CandidateBatchJobBuilder(
            "innerSavedSearchJob",
            savedSearch,
            processor
        );

    Job job = builder.build();

    assertNotNull(job);
    assertEquals("innerSavedSearchJob", job.getName());
  }

  @Test
  void innerBuilderSavedListConstructorBuildsJob() {
    CandidateBatchJobFactory.CandidateBatchJobBuilder builder =
        factory.new CandidateBatchJobBuilder(
            "innerSavedListJob",
            savedList,
            processor
        );

    Job job = builder.build();

    assertNotNull(job);
    assertEquals("innerSavedListJob", job.getName());
  }

  @Test
  void innerBuilderSearchCandidateRequestConstructorBuildsJob() {
    SearchCandidateRequest request = new SearchCandidateRequest();

    CandidateBatchJobFactory.CandidateBatchJobBuilder builder =
        factory.new CandidateBatchJobBuilder(
            "innerRequestJob",
            request,
            processor
        );

    Job job = builder.build();

    assertNotNull(job);
    assertEquals("innerRequestJob", job.getName());
  }

  @Test
  void fluentOptionsReturnSameBuilderAndBuildUsesCustomValues() {
    CandidateBatchJobFactory.CandidateBatchJobBuilder builder =
        factory.builder("customOptionsJob", savedList, processor);

    assertSame(builder, builder.chunkSize(25));
    assertSame(builder, builder.maxDelayMs(5000));
    assertSame(builder, builder.percentageOfCpu(75));

    Job job = builder.build();

    assertNotNull(job);
    assertEquals("customOptionsJob", job.getName());
  }

  @Test
  void buildThrowsWhenPercentageOfCpuIsZero() {
    CandidateBatchJobFactory.CandidateBatchJobBuilder builder =
        factory.builder("badCpuZeroJob", savedList, processor)
            .percentageOfCpu(0);

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        builder::build
    );

    assertEquals(
        "Percentage of CPU must be greater than 0 and less than or equal to 100",
        exception.getMessage()
    );
  }

  @Test
  void buildThrowsWhenPercentageOfCpuIsNegative() {
    CandidateBatchJobFactory.CandidateBatchJobBuilder builder =
        factory.builder("badCpuNegativeJob", savedList, processor)
            .percentageOfCpu(-1);

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        builder::build
    );

    assertEquals(
        "Percentage of CPU must be greater than 0 and less than or equal to 100",
        exception.getMessage()
    );
  }

  @Test
  void buildThrowsWhenPercentageOfCpuIsGreaterThanOneHundred() {
    CandidateBatchJobFactory.CandidateBatchJobBuilder builder =
        factory.builder("badCpuHighJob", savedList, processor)
            .percentageOfCpu(101);

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        builder::build
    );

    assertEquals(
        "Percentage of CPU must be greater than 0 and less than or equal to 100",
        exception.getMessage()
    );
  }

  @Test
  void buildThrowsWhenNoCandidateSourceIsSpecified() {
    CandidateBatchJobFactory.CandidateBatchJobBuilder builder =
        factory.new CandidateBatchJobBuilder(
            "missingSourceJob",
            (SavedList) null,
            processor
        );

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        builder::build
    );

    assertEquals("No saved search or saved list specified", exception.getMessage());
  }
}