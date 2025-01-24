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
   * updated from Salesforce.
   */
  @Scheduled(cron = "0 18 12 * * ?")
//  @Scheduled(cron = "0 0 23 * * ?", zone = "GMT")
  // TODO reinstate ☝️👇
//  @SchedulerLock(
//      name = "CandidateOpportunityService_updateOpenCases",
//      lockAtLeastFor = "PT23H",
//      lockAtMostFor = "PT23H"
//  )
  @Override
  public void updateOpenCasesFromSf() {
    try {
//      TODO delete if not needed
//      Find all open cases on the TC
//      SearchCandidateOpportunityRequest request = new SearchCandidateOpportunityRequest();
//      request.setSfOppClosed(false);
//
//      Page<CandidateOpportunity> casePage =
//          candidateOpportunityService.searchCandidateOpportunities(request);
//
//      List<String> sfIds = new ArrayList<>();
//
//      // Collect sfIds
//      do {
//        List<CandidateOpportunity> caseList = casePage.getContent();
//        sfIds.addAll(
//            caseList.stream()
//                .map(CandidateOpportunity::getSfId)
//                .toList()
//        );
//      } while (casePage.hasNext());

      List<String> sfIds = candidateOpportunityService.findAllNonNullSfIdsByClosedFalse();

      // Now initiate their update from Salesforce
      initiateBackgroundCaseUpdate(sfIds);

    } catch (Exception e) {
      LogBuilder.builder(log)
          .action("CandidateOpportunityService.updateOpenCases")
          .message("Failed to update open cases")
          .logError(e);
    }
  }

  public void initiateBackgroundCaseUpdate(List<String> sfIds) {
    if (!sfIds.isEmpty()) {
      LogBuilder.builder(log)
          .action("UpdateCasesFromSf")
          .message("Updating Candidate Opps from their Salesforce equivalents")
          .logInfo();
    }

    List<Opportunity> sfOpps = salesforceService.fetchOpportunitiesByIdOrOpenOnSF(
        sfIds, OpportunityType.CANDIDATE
    );

    LogBuilder.builder(log)
        .action("UpdateCasesFromSf")
        .message("Loaded " + sfOpps.size() + " Candidate Opps from Salesforce")
        .logInfo();

    // Create BackProcessor
    BackProcessor<IdContext> backProcessor = createCaseUpdateBackProcessor(sfOpps);

    // Schedule background processing
    BackRunner<IdContext> backRunner = new BackRunner<>();

    ScheduledFuture<?> scheduledFuture = backRunner.start(taskScheduler, backProcessor,
        new IdContext(null, 200), 20);
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

        // Log completion and return true when the last item has been processed
        if (lastIndex == sfOpps.size() - 1) {
          LogBuilder.builder(log)
              .action("UpdateCasesFromSf")
              .message("Completed processing " + sfOpps.size() + " Candidate Opps from Salesforce")
              .logInfo();

          return true;
        }
        return false;
      }
    };

    return backProcessor;
  }
}
