package org.tbbtalent.server.service;

import org.tbbtalent.server.model.CandidateEducation;
import org.tbbtalent.server.request.candidate.education.CreateCandidateEducationRequest;
import org.tbbtalent.server.request.candidate.education.UpdateCandidateEducationRequest;

import java.util.List;

public interface CandidateEducationService {

    CandidateEducation createCandidateEducation(CreateCandidateEducationRequest request);

    CandidateEducation updateCandidateEducation(UpdateCandidateEducationRequest request);
    
    CandidateEducation updateCandidateEducation(Long id, UpdateCandidateEducationRequest request);

    CandidateEducation createCandidateEducation(long id, CreateCandidateEducationRequest request);

    List<CandidateEducation> list(long id);

    void deleteCandidateEducation(Long id);
}
