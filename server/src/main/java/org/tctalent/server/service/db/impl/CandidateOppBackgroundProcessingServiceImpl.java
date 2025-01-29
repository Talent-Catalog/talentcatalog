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

import java.util.ArrayList;
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
 * Service for background processing of Candidate Opportunities
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
   * updated from Salesforce. Can also be called from {@code SystemAdminApi.sfSyncOpenCases()}
   */
  @Scheduled(cron = "0 0 23 * * ?", zone = "GMT")
  @SchedulerLock(
      name = "CandidateOppBackgroundProcessingService_updateOpenCases",
      lockAtLeastFor = "PT23H",
      lockAtMostFor = "PT23H"
  )
  public void initiateBackgroundCaseUpdate() {
    try {
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

      // Fetch Salesforce equivalents in batches - necessary because there's a 4,000-character limit
      // on single strings in a SOQL WHERE clause, which the concatenated sfIds might exceed.
      int totalItems = sfIds.size();
      int batchSize = 100;
      List<Opportunity> sfOpps = new ArrayList<>();

      for (int i = 0; i < totalItems; i += batchSize) {
        List<String> batch = sfIds.subList(i, Math.min(i + batchSize, totalItems));
        sfOpps.addAll(salesforceService.fetchOpportunitiesById(batch, OpportunityType.CANDIDATE));
      }

      // Add any opps that were reopened on Salesforce
      sfOpps.addAll(salesforceService.fetchOpportunitiesByOpenOnSF(OpportunityType.CANDIDATE));

      LogBuilder.builder(log)
          .action("UpdateCasesFromSf")
          .message(
              "Fetched " + sfOpps.size() + " Candidate Opps from Salesforce, including " +
                  (sfOpps.size() - sfIds.size())
                  + " that were closed on the TC but reopened by a user on "
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

    } catch (Exception e) {
      LogBuilder.builder(log)
          .action("CandidateOppBackgroundProcessingService_updateOpenCases")
          .message("Failed to update open cases")
          .logError(e);
    }
  }

  private BackProcessor<IdContext> createCaseUpdateBackProcessor(List<Opportunity> sfOpps) {
    BackProcessor<IdContext> backProcessor = new BackProcessor<>() {
      @Override
      public boolean process(IdContext ctx) {
        long firstIndex =
            ctx.getLastProcessedId() == null ? 0 : ctx.getLastProcessedId() + 1;
        long lastIndex =
            firstIndex + ctx.getNumToProcess() < sfOpps.size() ?
                firstIndex + ctx.getNumToProcess() - 1 : sfOpps.size() - 1;

        // sublist method's 2nd param 'toIndex' is exclusive, so we add 1 to the last index.
        List<Opportunity> nextBatch = sfOpps.subList((int) firstIndex, (int) lastIndex + 1);

        // Delegate the actual processing to the appropriate service
        candidateOpportunityService.processCaseUpdateBatch(nextBatch);

        // Log progress
        LogBuilder.builder(log)
            .action("UpdateCasesFromSf")
            .message("Completed processing " + (lastIndex + 1) + " of " + sfOpps.size() +
                " Candidate Opps from Salesforce")
            .logInfo();

        ctx.setLastProcessedId(lastIndex);

        // Has the last element been processed?
        boolean isComplete = lastIndex == sfOpps.size() - 1;

        return isComplete;
      }
    };

    return backProcessor;
  }
}
