package org.tbbtalent.server.exception;

public class CandidateDeactivatedException extends ServiceException {

    public CandidateDeactivatedException() {
        super("candidate_deactivated", "This account has been deactivated");
    }

}
