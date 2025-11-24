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

import java.util.Iterator;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.item.ItemReader;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.model.db.SavedSearch;
import org.tctalent.server.request.PagedSearchRequest;
import org.tctalent.server.request.candidate.SavedListGetRequest;
import org.tctalent.server.request.candidate.SavedSearchGetRequest;
import org.tctalent.server.request.candidate.SearchCandidateRequest;
import org.tctalent.server.service.db.CandidateService;
import org.tctalent.server.service.db.SavedSearchService;

/**
 * Standard CandidateReader for CandidateJob batches as created by CandidateJobFactory.
 *
 * @author John Cameron
 */
@Slf4j
public class CandidateReader implements ItemReader<Candidate>, StepExecutionListener {
    private final int chunkSize;
    private SavedList savedList;
    private SavedSearch savedSearch;
    private SearchCandidateRequest searchCandidateRequest;
    private CandidateService candidateService;
    private SavedSearchService savedSearchService;

    private int currentPage = 0;
    private int totalPages = 1; // Initialise with 1 to start fetching
    private Iterator<Candidate> batchIterator;

    public CandidateReader(
        @NonNull SavedList savedList, int chunkSize, CandidateService candidateService) {
        this.savedList = savedList;
        this.chunkSize = chunkSize;
        this.candidateService = candidateService;
    }

    public CandidateReader(
        @NonNull SavedSearch savedSearch, int chunkSize, SavedSearchService savedSearchService) {
        this.savedSearch = savedSearch;
        this.chunkSize = chunkSize;
        this.savedSearchService = savedSearchService;
    }

    public CandidateReader(
        @NonNull SearchCandidateRequest searchCandidateRequest, int chunkSize,
        SavedSearchService savedSearchService) {
        this.searchCandidateRequest = searchCandidateRequest;
        this.chunkSize = chunkSize;
        this.savedSearchService = savedSearchService;
    }

    @Nullable
    @Override
    public Candidate read() throws Exception {
        try {
            if (batchIterator == null || !batchIterator.hasNext()) {
                fetchNextBatch();
            }
            return batchIterator != null && batchIterator.hasNext() ? batchIterator.next() : null;
        } catch (Exception e) {
            throw new Exception("Failed to read Candidate", e);
        }
    }

    private void fetchNextBatch() {
        PagedSearchRequest request;
        if (savedSearch != null) {
            request = new SavedSearchGetRequest();
        } else if (savedList != null) {
            request = new SavedListGetRequest();
        } else if (searchCandidateRequest != null) {
            request = searchCandidateRequest;
        } else {
            throw new RuntimeException("No saved search or saved list specified");
        }

        //Set page number
        request.setPageNumber(currentPage);
        //Set page size
        request.setPageSize(chunkSize);

        if (currentPage < totalPages) {
            // Fetch next batch
            Page<Candidate> page;
            if (savedSearch != null) {
                assert request instanceof SavedSearchGetRequest;
                page = savedSearchService.searchCandidates(
                    savedSearch.getId(), (SavedSearchGetRequest) request);
            } else if (searchCandidateRequest != null) {
                page = savedSearchService.searchCandidates(searchCandidateRequest);
            } else if (savedList != null) {
                assert request instanceof SavedListGetRequest;
                page = candidateService.getSavedListCandidates(
                    savedList, (SavedListGetRequest) request);
            } else {
                throw new RuntimeException("No saved search or saved list specified");
            }

            final List<Candidate> currentBatch = page.getContent();
            totalPages = page.getTotalPages();
            batchIterator = currentBatch.iterator();
            currentPage++;
        }
    }

    /** Resets the reader state before a step starts */
    @Override
    public void beforeStep(@NonNull StepExecution stepExecution) {
        LogBuilder.builder(log)
            .action("Resetting CandidateReader before step execution")
            .logInfo();

        this.currentPage = 0;
        this.totalPages = 1;
        this.batchIterator = null;
    }

    @Override
    public ExitStatus afterStep(@NonNull StepExecution stepExecution) {
        return ExitStatus.COMPLETED;
    }
}
