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
import java.util.EnumSet;
import java.util.List;
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
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.SavedSearch;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.request.candidate.SearchCandidateRequest;
import org.tctalent.server.request.list.SearchSavedListRequest;
import org.tctalent.server.request.partner.SearchPartnerRequest;
import org.tctalent.server.request.search.SearchSavedSearchRequest;
import org.tctalent.server.service.db.BackgroundProcessingService;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.PartnerService;
import org.tctalent.server.service.db.SavedListService;
import org.tctalent.server.service.db.SavedSearchService;
import org.tctalent.server.service.db.util.PagedCandidateBackProcessor;
import org.tctalent.server.service.db.util.PagedPartnerBackProcessor;
import org.tctalent.server.service.db.util.PagedSavedListBackProcessor;
import org.tctalent.server.service.db.util.PagedSavedSearchBackProcessor;
import org.tctalent.server.util.background.BackProcessor;
import org.tctalent.server.util.background.BackRunner;
import org.tctalent.server.util.background.PageContext;
import org.tctalent.server.util.background.PageContextBackRunner;
import org.tctalent.server.util.listener.BatchListeningLogger;

/**
 * Service for background processing methods
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class BackgroundProcessingServiceImpl implements BackgroundProcessingService {
  private final CandidateService candidateService;
  private final CandidateRepository candidateRepository;
  private final PartnerService partnerService;
  private final SavedListService savedListService;
  private final SavedSearchService savedSearchService;
  private final TaskScheduler taskScheduler;
  private final BatchListeningLogger batchListeningLogger;

  public BackProcessor<PageContext> createSfSyncBackProcessor(
      List<CandidateStatus> statuses, long totalNoOfPages
  ) {
    BackProcessor<PageContext> backProcessor = new BackProcessor<>() {
      @Override
      public boolean process(PageContext ctx) throws SalesforceException, WebClientException {
        int page =
            ctx.getLastProcessedPage() == null ? 0 : ctx.getLastProcessedPage() + 1;

        // Delegate page processing to the service, which will open a transaction
        candidateService.processSfCandidateSyncPage(page, statuses);

        // Log completed page
        LogBuilder.builder(log)
            .action("processSfCandidateSyncPage")
            .message("Processed page " + (page + 1) + " of " + totalNoOfPages)
            .logInfo();

        // Set last processed page
        ctx.setLastProcessedPage(page);

        // Log if processing complete
        if (page + 1 >= totalNoOfPages) {
          LogBuilder.builder(log)
              .action("Sync Candidates to Salesforce")
              .message("SF candidate sync complete!")
              .logInfo();
        }

        // Return true if complete - ends processing
        return page + 1 >= totalNoOfPages;
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
        int page =
            ctx.getLastProcessedPage() == null ? 0 : ctx.getLastProcessedPage() + 1;

        // Fetch new page of candidates
        Page<Candidate> candidatePage = candidateRepository.findByIdIn(
            candidateIds,
            PageRequest.of(page, 200, Sort.by("id").ascending())
        );

        // Delegate page processing to the service, which will open a transaction
        candidateService.processPotentialDuplicatePage(candidatePage);

        // Set last processed page
        ctx.setLastProcessedPage(page);

        // Return true if complete - ends processing
        return page + 1 >= candidatePage.getTotalPages();
      }
    };

    return backProcessor;
  }

  @Override
  @Scheduled(cron = "0 0 21 * * ?", zone = "GMT")
  @SchedulerLock(name = "BackgroundProcessingService_processPotentialDuplicateCandidates",
      lockAtLeastFor = "PT2H", lockAtMostFor = "PT2H")
  public void processPotentialDuplicateCandidates() {
    candidateService.cleanUpResolvedDuplicates();
    initiateDuplicateProcessing();
  }

  @Override
  public void initiateDuplicateProcessing() {
    // Obtain list of IDs of candidates who meet potential duplicate criteria but not yet identified
    List<Long> potentialDupeIds =
        candidateRepository.findIdsOfPotentialDuplicateCandidates(false);

    // Implement background processing
    BackProcessor<PageContext> backProcessor =
        createPotentialDuplicatesBackProcessor(potentialDupeIds);

    // Schedule background processing
    BackRunner<PageContext> backRunner = new BackRunner<>();
    backRunner.addListener(batchListeningLogger);

    backRunner.start(
        taskScheduler,
        backProcessor,
        new PageContext(null),
        20,
        "Potential duplicate candidate processing"
    );
  }

  @Override
  public void setCandidatePublicIds() {

    //Process all candidates except deleted or withdrawn
    SearchCandidateRequest searchCandidateRequest = new SearchCandidateRequest();
    EnumSet<CandidateStatus> includedStatuses = EnumSet.complementOf(
        EnumSet.of(CandidateStatus.deleted, CandidateStatus.withdrawn));
    searchCandidateRequest.setStatuses(new ArrayList<>(includedStatuses));

    //Set page size
    searchCandidateRequest.setPageSize(100);

    //Create the processor, passing in the request and services it needs
    PagedCandidateBackProcessor backProcessor =
        new PagedCandidateBackProcessor( "setCandidatePublicIds",
            searchCandidateRequest, candidateService, savedSearchService) {
          @Override
          protected void processCandidates(
              CandidateService candidateService, List<Candidate> candidates) {
            //The actual processing of the candidates happens in the candidate service.
            candidateService.setPublicIds(candidates);
          }
        };

    //Start the processing - only consuming 20% of the CPU
    PageContextBackRunner runner = new PageContextBackRunner();
    runner.start(taskScheduler, backProcessor, 20, "Set candidate public IDs");
  }

  @Override
  public void setPartnerPublicIds() {

    //Process all - so empty search
    SearchPartnerRequest searchPartnerRequest = new SearchPartnerRequest();

    //Set page size
    searchPartnerRequest.setPageSize(100);

    //Create the processor, passing in the request and services it needs
    PagedPartnerBackProcessor backProcessor =
        new PagedPartnerBackProcessor( "setPartnerPublicIds",
            searchPartnerRequest, partnerService) {
          @Override
          protected void processPartners(
              PartnerService partnerService, List<PartnerImpl> partners) {
            //The actual processing of the partners happens in the partner service.
            partnerService.setPublicIds(partners);
          }
        };

    //Start the processing - only consuming 20% of the CPU
    PageContextBackRunner runner = new PageContextBackRunner();
    runner.start(taskScheduler, backProcessor, 20, "Set partner public IDs");
  }

  @Override
  public void setSavedListPublicIds() {

    //Process all - so empty search
    SearchSavedListRequest searchSavedListRequest = new SearchSavedListRequest();

    //Set page size
    searchSavedListRequest.setPageSize(100);

    //Create the processor, passing in the request and services it needs
    PagedSavedListBackProcessor backProcessor =
        new PagedSavedListBackProcessor( "setSavedListPublicIds",
            searchSavedListRequest, savedListService) {
          @Override
          protected void processSavedLists(
              SavedListService savedListService, List<SavedList> savedLists) {
            //The actual processing of the savedLists happens in the saved list service.
            savedListService.setPublicIds(savedLists);
          }
        };

    //Start the processing - only consuming 20% of the CPU
    PageContextBackRunner runner = new PageContextBackRunner();
    runner.start(taskScheduler, backProcessor, 20, "Set saved list public IDs");
  }

  @Override
  public void setSavedSearchPublicIds() {

    //Process all - so empty search
    SearchSavedSearchRequest searchSavedSearchRequest = new SearchSavedSearchRequest();

    //Set page size
    searchSavedSearchRequest.setPageSize(100);

    //Create the processor, passing in the request and services it needs
    PagedSavedSearchBackProcessor backProcessor =
        new PagedSavedSearchBackProcessor( "setSavedSearchPublicIds",
            searchSavedSearchRequest, savedSearchService) {
          @Override
          protected void processSavedSearches(
              SavedSearchService savedSearchService, List<SavedSearch> savedSearches) {
            savedSearchService.setPublicIds(savedSearches);
          }
        };

    //Start the processing - only consuming 20% of the CPU
    PageContextBackRunner runner = new PageContextBackRunner();
    runner.start(taskScheduler, backProcessor, 20, "Set saved search public IDs");
  }
}
