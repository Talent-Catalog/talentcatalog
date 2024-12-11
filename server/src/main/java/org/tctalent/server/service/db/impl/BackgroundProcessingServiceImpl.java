/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.tctalent.server.service.db.impl;

import java.util.List;
import java.util.concurrent.ScheduledFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.javacrumbs.shedlock.spring.annotation.SchedulerLock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientException;
import org.tctalent.server.exception.SalesforceException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.service.db.BackgroundProcessingService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.util.background.BackProcessor;
import org.tctalent.server.util.background.BackRunner;
import org.tctalent.server.util.background.PageContext;

/**
 * Service for background processing methods
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BackgroundProcessingServiceImpl implements BackgroundProcessingService {
  private final CandidateService candidateService;
  private final CandidateRepository candidateRepository;
  private final TaskScheduler taskScheduler;

  public BackProcessor<PageContext> createSfSyncBackProcessor(
      List<CandidateStatus> statuses, long totalNoOfPages
  ) {
    BackProcessor<PageContext> backProcessor = new BackProcessor<>() {
      @Override
      public boolean process(PageContext ctx) throws SalesforceException, WebClientException {
        long startPage =
            ctx.getLastProcessedPage() == null ? 0 : ctx.getLastProcessedPage() + 1;

        // Delegate page processing to the service, which will open a transaction
        candidateService.processSfCandidateSyncPage(startPage, statuses);

        // Log completed page
        LogBuilder.builder(log)
            .action("processSfCandidateSyncPage")
            .message("Processed page " + (startPage + 1) + " of " + totalNoOfPages)
            .logInfo();

        // Set last processed page
        long lastProcessed = startPage + ctx.getNumToProcess() - 1;
        ctx.setLastProcessedPage(lastProcessed);

        // Log if processing complete
        if (startPage + ctx.getNumToProcess() >= totalNoOfPages) {
          LogBuilder.builder(log)
              .action("Sync Candidates to Salesforce")
              .message("SF candidate sync complete!")
              .logInfo();
        }

        // Return true if complete - ends processing
        return startPage + ctx.getNumToProcess() >= totalNoOfPages;
      }
    };

    return backProcessor;
  }

  public BackProcessor<PageContext> createPotentialDuplicatesBackProcessor(
      List<Long> candidateIds
  ) {
    BackProcessor<PageContext> backProcessor = new BackProcessor<>() {
      @Override
      public boolean process(PageContext ctx) {
        long startPage =
            ctx.getLastProcessedPage() == null ? 0 : ctx.getLastProcessedPage() + 1;

        // Fetch new page of candidates
        Page<Candidate> candidatePage = candidateRepository.findByIdIn(
            candidateIds,
            PageRequest.of((int) startPage, 200, Sort.by("id").ascending())
        );

        // Delegate page processing to the service, which will open a transaction
        candidateService.processPotentialDuplicatePage(candidatePage);

        // Set last processed page
        long lastProcessed = startPage + ctx.getNumToProcess() - 1;
        ctx.setLastProcessedPage(lastProcessed);

        // Return true if complete - ends processing
        return startPage + ctx.getNumToProcess() >= candidatePage.getTotalPages();
      }
    };

    return backProcessor;
  }

  @Override
  @Scheduled(cron = "0 0 21 * * ?", zone = "GMT")
  @SchedulerLock(name = "BackgroundProcessingService_processPotentialDuplicateCandidates",
      lockAtLeastFor = "PT2H", lockAtMostFor = "PT2H")
  public void processPotentialDuplicateCandidates() {
    this.candidateService.cleanUpResolvedDuplicates();
    initiateDuplicateProcessing();
  }

  @Override
  public void initiateDuplicateProcessing() {
    // Obtain list of IDs of candidates who meet potential duplicate criteria but not yet identified
    List<Long> potentialDupeIds =
        this.candidateRepository.findIdsOfPotentialDuplicateCandidates(false);

    // Implement background processing
    BackProcessor<PageContext> backProcessor =
        createPotentialDuplicatesBackProcessor(potentialDupeIds);

    // Schedule background processing
    BackRunner<PageContext> backRunner = new BackRunner<>();

    ScheduledFuture<?> scheduledFuture = backRunner.start(taskScheduler, backProcessor,
        new PageContext(null, 1), 20);
  }

}
