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
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.sf.Opportunity;
import org.tctalent.server.model.sf.Opportunity.OpportunityType;
import org.tctalent.server.service.db.CandidateOppBackgroundProcessingService;
import org.tctalent.server.service.db.CandidateOpportunityService;
import org.tctalent.server.service.db.SalesforceService;
import org.tctalent.server.util.background.BackProcessor;
import org.tctalent.server.util.background.BackRunner;
import org.tctalent.server.util.background.IdContext;

/**
 * Service for background processing methods
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class CandidateOppBackgroundProcessingServiceImpl
    implements CandidateOppBackgroundProcessingService {
  private final TaskScheduler taskScheduler;
  private final CandidateOpportunityService candidateOpportunityService;
  private final SalesforceService salesforceService;

  /**
   * Scheduled daily at 2300 GMT to keep TC data accurate in cases where Candidate Opps have been
   * updated from Salesforce. Can also be called from {@code SystemAdminApi.sfSyncOpenCases}
   */
  @Scheduled(cron = "0 0 23 * * ?", zone = "GMT")
  @SchedulerLock(
      name = "CandidateOpportunityService_updateOpenCases",
      lockAtLeastFor = "PT23H",
      lockAtMostFor = "PT23H"
  )
  public void initiateBackgroundCaseUpdate() {
    List<String> sfIds = candidateOpportunityService.findAllNonNullSfIdsByClosedFalse();

    if (!sfIds.isEmpty()) {
      LogBuilder.builder(log)
          .action("UpdateCasesFromSf")
          .message(
              "Found " + sfIds.size() + " TC Candidate Opps which will be synced with their "
              + "Salesforce equivalents"
          )
          .logInfo();
    }

    List<Opportunity> sfOpps = salesforceService.fetchOpportunitiesByIdOrOpenOnSF(
        sfIds, OpportunityType.CANDIDATE
    );

    LogBuilder.builder(log)
        .action("UpdateCasesFromSf")
        .message(
            "Loaded " + sfOpps.size() + " Candidate Opps from Salesforce, including " +
            (sfOpps.size() - sfIds.size()) + " that were closed on the TC but reopened by a user on "
                + "Salesforce.")
        .logInfo();

    if (!sfOpps.isEmpty()) {
      // Create BackProcessor
      BackProcessor<IdContext> backProcessor = createCaseUpdateBackProcessor(sfOpps);

      // Schedule background processing
      BackRunner<IdContext> backRunner = new BackRunner<>();

      ScheduledFuture<?> scheduledFuture = backRunner.start(taskScheduler, backProcessor,
          new IdContext(null, 200), 20);
    }
  }

  public BackProcessor<IdContext> createCaseUpdateBackProcessor(List<Opportunity> sfOpps) {
    BackProcessor<IdContext> backProcessor = new BackProcessor<>() {
      @Override
      public boolean process(IdContext ctx) {
        long firstIndex =
            ctx.getLastProcessedId() == null ? 0 : ctx.getLastProcessedId() + 1;
        long lastIndex =
            firstIndex + ctx.getNumToProcess() < sfOpps.size() ?
                firstIndex + ctx.getNumToProcess() - 1 : sfOpps.size() - 1;

        // sublist 2nd param toIndex is exclusive, so we add 1 to the last index we want to process.
        List<Opportunity> nextBatch = sfOpps.subList((int) firstIndex, (int) lastIndex + 1);

        // Delegate the actual processing to the appropriate service
        candidateOpportunityService.processCaseUpdateBatch(nextBatch);

        ctx.setLastProcessedId(lastIndex);

        // Has the last element been processed?
        boolean isComplete = lastIndex == sfOpps.size() - 1;

        if (isComplete) {
          LogBuilder.builder(log)
              .action("UpdateCasesFromSf")
              .message("Completed processing " + sfOpps.size() + " Candidate Opps from Salesforce")
              .logInfo();
        }

        return isComplete;
      }
    };

    return backProcessor;
  }
}
