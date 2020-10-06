/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db.impl;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.CandidateCitizenship;
import org.tbbtalent.server.model.db.Nationality;
import org.tbbtalent.server.repository.db.CandidateCitizenshipRepository;
import org.tbbtalent.server.repository.db.NationalityRepository;
import org.tbbtalent.server.request.candidate.CandidateIntakeData;
import org.tbbtalent.server.service.db.CandidateCitizenshipService;

/**
 * Manage candidate citizenships
 *
 * @author John Cameron
 */
@Service
public class CandidateCitizenshipServiceImpl implements CandidateCitizenshipService {
    private final CandidateCitizenshipRepository candidateCitizenshipRepository;
    private final NationalityRepository nationalityRepository;

    public CandidateCitizenshipServiceImpl(
            CandidateCitizenshipRepository candidateCitizenshipRepository, 
            NationalityRepository nationalityRepository) {
        this.candidateCitizenshipRepository = candidateCitizenshipRepository;
        this.nationalityRepository = nationalityRepository;
    }

    @Override
    public void updateIntakeData(
            Long nationalityId, @NonNull Candidate candidate, CandidateIntakeData data) 
            throws NoSuchObjectException {
        if (nationalityId != null) {
            Nationality nationality = nationalityRepository.findById(nationalityId)
                    .orElseThrow(() -> new NoSuchObjectException(Nationality.class, nationalityId));
            CandidateCitizenship cc = candidateCitizenshipRepository.findByNationalityId(nationalityId);
            if (cc == null) {
                //Create
                cc = new CandidateCitizenship();
            }
            cc.populateIntakeData(candidate, nationality, data);
            candidateCitizenshipRepository.save(cc);
        }
    }
}
