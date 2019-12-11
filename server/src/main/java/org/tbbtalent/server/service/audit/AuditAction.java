package org.tbbtalent.server.service.audit;

public enum AuditAction {
    ADD("created"),
    UPDATE("updated"),
    DELETE("removed"),
    VERIFY("verified"),
    ;
    
    private String name;

    private AuditAction(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}
