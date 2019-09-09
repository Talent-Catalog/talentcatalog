package org.tbbtalent.server.exception;

public class CandidateDeactivatedException extends RuntimeException {

    public CandidateDeactivatedException() {
        super("This account has been deactivated");
    }

}
