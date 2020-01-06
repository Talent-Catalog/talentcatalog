package org.tbbtalent.server.service.audit;

public enum AuditType {
    CANDIDATE_OCCUPATION("Candidate Occupation"),

    ;
    
    private String name;

    private AuditType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
    
    
}
