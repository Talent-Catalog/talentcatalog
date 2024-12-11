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

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateCitizenship;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.repository.db.CandidateCitizenshipRepository;
import org.tctalent.server.repository.db.CandidateRepository;
import org.tctalent.server.repository.db.CountryRepository;
import org.tctalent.server.request.candidate.CandidateIntakeDataUpdate;
import org.tctalent.server.request.candidate.citizenship.CreateCandidateCitizenshipRequest;
import org.tctalent.server.service.db.CandidateCitizenshipService;

/**
 * Manage candidate citizenships
 *
 * @author John Cameron
 */
@Service
public class CandidateCitizenshipServiceImpl implements CandidateCitizenshipService {
    private final CandidateCitizenshipRepository candidateCitizenshipRepository;
    private final CandidateRepository candidateRepository;
    private final CountryRepository countryRepository;

    public CandidateCitizenshipServiceImpl(
            CandidateCitizenshipRepository candidateCitizenshipRepository,
            CandidateRepository candidateRepository,
            CountryRepository countryRepository) {
        this.candidateCitizenshipRepository = candidateCitizenshipRepository;
        this.candidateRepository = candidateRepository;
        this.countryRepository = countryRepository;
    }

    @Override
    public CandidateCitizenship createCitizenship(
            long candidateId, CreateCandidateCitizenshipRequest request)
            throws NoSuchObjectException {

        Candidate candidate = candidateRepository.findById(candidateId)
                    .orElseThrow(() -> new NoSuchObjectException(Candidate.class, candidateId));

        CandidateCitizenship cc = new CandidateCitizenship();
        cc.setCandidate(candidate);

        final Long nationalityId = request.getNationalityId();
        if (nationalityId != null) {
            Country nationality = countryRepository.findById(nationalityId)
                    .orElseThrow(() -> new NoSuchObjectException(Country.class, nationalityId));
            cc.setNationality(nationality);
        }
        cc.setHasPassport(request.getHasPassport());
        cc.setNotes(request.getNotes());

        return candidateCitizenshipRepository.save(cc);
    }

    @Override
    public boolean deleteCitizenship(long citizenshipId)
            throws EntityReferencedException, InvalidRequestException {
        candidateCitizenshipRepository.deleteById(citizenshipId);
        return true;
    }

    @Override
    public void updateIntakeData(
            Long nationalityId, @NonNull Candidate candidate, CandidateIntakeDataUpdate data)
            throws NoSuchObjectException {
        if (nationalityId != null) {
            Country nationality = countryRepository.findById(nationalityId)
                    .orElseThrow(() -> new NoSuchObjectException(Country.class, nationalityId));

            CandidateCitizenship cc;
            Long id = data.getCitizenId();
            cc = candidateCitizenshipRepository.findById(id)
                    .orElseThrow(() -> new NoSuchObjectException(CandidateCitizenship.class, id));
            cc.populateIntakeData(candidate, nationality, data);
            candidateCitizenshipRepository.save(cc);
        }
    }
}
