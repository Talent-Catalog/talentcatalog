/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Matching;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.repository.db.MatchingRepository;
import org.tctalent.server.repository.db.SalesforceJobOppRepository;
import org.tctalent.server.service.db.MatchingService;

/**
 * Implementation of {@link MatchingService}
 */
@Service
@RequiredArgsConstructor
public class MatchingServiceImpl implements MatchingService {

    private final MatchingRepository matchingRepository;
    private final SalesforceJobOppRepository salesforceJobOppRepository;

    @Override
    @NonNull
    @Transactional
    public Matching createMatching(@Nullable String matchingDescription) {
        return matchingRepository.save(new Matching(matchingDescription));
    }

    @Override
    @NonNull
    public Matching getMatching(long id) throws NoSuchObjectException {
        return matchingRepository.findById(id)
            .orElseThrow(() -> new NoSuchObjectException(Matching.class, id));
    }

    @Override
    @NonNull
    @Transactional
    public Matching updateMatchingDescription(long id, @Nullable String matchingDescription)
        throws NoSuchObjectException {
        Matching matching = getMatching(id);
        if (matching.updateMatchingDescription(matchingDescription)) {
            matching = matchingRepository.save(matching);
        }
        return matching;
    }

    @Override
    @NonNull
    @Transactional
    public Matching getOrCreateJobMatching(
        @NonNull SalesforceJobOpp job, @Nullable String preferredDescription) {
        Matching matching = job.getMatching();
        if (matching == null) {
            String description = Matching.hasTextContent(preferredDescription)
                ? preferredDescription : JobServiceHelper.extractJobText(job);
            //Flushed so that the job can reference it in the update below.
            matching = matchingRepository.saveAndFlush(new Matching(description));
            if (salesforceJobOppRepository.setMatchingIfNone(job.getId(), matching.getId()) == 0) {
                //Another request has just given the job its Matching - use that one instead.
                matchingRepository.delete(matching);
                final long jobMatchingId = salesforceJobOppRepository
                    .findMatchingIdByJobId(job.getId()).orElseThrow();
                matching = getMatching(jobMatchingId);
            }
            //The job's matching attribute is read only (see SalesforceJobOpp.matching), so this
            //only updates the in memory job.
            job.setMatching(matching);
        } else if (matching.getMatchingDescription() == null
            && matching.updateMatchingDescription(preferredDescription)) {
            matching = matchingRepository.save(matching);
        }
        return matching;
    }
}
