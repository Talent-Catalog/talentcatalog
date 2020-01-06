package org.tbbtalent.server.service;

import org.tbbtalent.server.model.CandidateShortlistItem;
import org.tbbtalent.server.request.shortlist.CreateCandidateShortlistRequest;
import org.tbbtalent.server.request.shortlist.UpdateCandidateShortlistRequest;

public interface CandidateShortlistService {

    CandidateShortlistItem createCandidateShortlist(CreateCandidateShortlistRequest request);

    CandidateShortlistItem updateCandidateShortlist(long id, UpdateCandidateShortlistRequest request);


    CandidateShortlistItem getCandidateShortlistItem(long id);
}
