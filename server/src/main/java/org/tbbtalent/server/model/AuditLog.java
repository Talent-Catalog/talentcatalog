package org.tbbtalent.server.model;


import org.tbbtalent.server.service.audit.AuditAction;
import org.tbbtalent.server.service.audit.AuditType;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "audit_log")
@SequenceGenerator(name = "seq_gen", sequenceName = "audit_log_id_seq", allocationSize = 1)
public class AuditLog extends AbstractDomainObject<Long> {

    private static final long serialVersionUID = 4733031439317778347L;

    @Column(name = "event_date")
    private LocalDateTime eventDate;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private AuditType type;
    
    @Column(name = "action")
    @Enumerated(EnumType.STRING)
    private AuditAction action;
    
    @Column(name = "object_ref")
    private String objectRef;

    @Column(name = "description")
    private String description;

    public AuditLog() {
    }



    public AuditLog(LocalDateTime eventDate, Long userId, AuditType type, AuditAction action, String objectRef,
                    String description) {
        this.eventDate = eventDate;
        this.userId = userId;
        this.type = type;
        this.action = action;
        this.objectRef = objectRef;
        this.description = description;
    }

    public LocalDateTime getEventDate() {
        return eventDate;
    }

    public void setEventDate(LocalDateTime eventDate) {
        this.eventDate = eventDate;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public AuditType getType() {
        return type;
    }

    public void setType(AuditType type) {
        this.type = type;
    }

    public AuditAction getAction() {
        return action;
    }

    public void setAction(AuditAction action) {
        this.action = action;
    }

    public String getObjectRef() {
        return objectRef;
    }

    public void setObjectRef(String objectRef) {
        this.objectRef = objectRef;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
