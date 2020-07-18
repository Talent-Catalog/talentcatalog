package org.tbbtalent.server.service;

import org.tbbtalent.server.model.db.CandidateReviewStatusItem;
import org.tbbtalent.server.request.reviewstatus.CreateCandidateReviewStatusRequest;
import org.tbbtalent.server.request.reviewstatus.UpdateCandidateReviewStatusRequest;

public interface CandidateReviewStatusService {

    CandidateReviewStatusItem createCandidateReviewStatusItem(CreateCandidateReviewStatusRequest request);

    CandidateReviewStatusItem updateCandidateReviewStatusItem(long id, UpdateCandidateReviewStatusRequest request);


    CandidateReviewStatusItem getCandidateReviewStatusItem(long id);
}
