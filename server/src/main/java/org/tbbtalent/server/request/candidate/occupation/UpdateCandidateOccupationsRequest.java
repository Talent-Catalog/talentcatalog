package org.tbbtalent.server.request.candidate.occupation;

import java.util.List;

public class UpdateCandidateOccupationsRequest {

   List<UpdateCandidateOccupationRequest> updates;

    public List<UpdateCandidateOccupationRequest> getUpdates() {
        return updates;
    }

    public void setUpdates(List<UpdateCandidateOccupationRequest> updates) {
        this.updates = updates;
    }
}


