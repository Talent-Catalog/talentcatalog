/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db.impl;

import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.CandidateCitizenship;
import org.tbbtalent.server.model.db.Nationality;
import org.tbbtalent.server.repository.db.CandidateCitizenshipRepository;
import org.tbbtalent.server.repository.db.CandidateRepository;
import org.tbbtalent.server.repository.db.NationalityRepository;
import org.tbbtalent.server.request.candidate.CandidateIntakeDataUpdate;
import org.tbbtalent.server.request.candidate.citizenship.CreateCandidateCitizenshipRequest;
import org.tbbtalent.server.service.db.CandidateCitizenshipService;

/**
 * Manage candidate citizenships
 *
 * @author John Cameron
 */
@Service
public class CandidateCitizenshipServiceImpl implements CandidateCitizenshipService {
    private final CandidateCitizenshipRepository candidateCitizenshipRepository;
    private final CandidateRepository candidateRepository;
    private final NationalityRepository nationalityRepository;

    public CandidateCitizenshipServiceImpl(
            CandidateCitizenshipRepository candidateCitizenshipRepository,
            CandidateRepository candidateRepository, 
            NationalityRepository nationalityRepository) {
        this.candidateCitizenshipRepository = candidateCitizenshipRepository;
        this.candidateRepository = candidateRepository;
        this.nationalityRepository = nationalityRepository;
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
            Nationality nationality = nationalityRepository.findById(nationalityId)
                    .orElseThrow(() -> new NoSuchObjectException(Nationality.class, nationalityId));
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
            Nationality nationality = nationalityRepository.findById(nationalityId)
                    .orElseThrow(() -> new NoSuchObjectException(Nationality.class, nationalityId));

            CandidateCitizenship cc;
            Long id = data.getCitizenId();
            if (id == null) {
                //Create
                cc = new CandidateCitizenship();
            } else {
                //Update
                cc = candidateCitizenshipRepository.findById(id)
                        .orElseThrow(() -> new NoSuchObjectException(CandidateCitizenship.class, id));
            }
            cc.populateIntakeData(candidate, nationality, data);
            candidateCitizenshipRepository.save(cc);
        }
    }
}
