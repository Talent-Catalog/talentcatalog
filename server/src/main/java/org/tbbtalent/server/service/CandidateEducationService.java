package org.tbbtalent.server.service;

import org.tbbtalent.server.model.CandidateEducation;
import org.tbbtalent.server.request.candidate.education.CreateCandidateEducationRequest;
import org.tbbtalent.server.request.candidate.education.UpdateCandidateEducationRequest;

import java.util.List;

public interface CandidateEducationService {

    CandidateEducation createCandidateEducation(CreateCandidateEducationRequest request);

    CandidateEducation updateCandidateEducation(CreateCandidateEducationRequest request);

    CandidateEducation createCandidateEducationAdmin(long id, CreateCandidateEducationRequest request);

    CandidateEducation updateCandidateEducationAdmin(long id, UpdateCandidateEducationRequest request);

    List<CandidateEducation> list(long id);

}
