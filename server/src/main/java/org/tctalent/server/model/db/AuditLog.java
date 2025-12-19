/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.model.db;


import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import org.tctalent.server.service.db.audit.AuditAction;
import org.tctalent.server.service.db.audit.AuditType;

@Entity
@Table(name = "audit_log")
@SequenceGenerator(name = "seq_gen", sequenceName = "audit_log_id_seq", allocationSize = 1)
public class AuditLog extends AbstractDomainObject<Long> {

    private static final long serialVersionUID = 4733031439317778347L;

    @Column(name = "event_date")
    private OffsetDateTime eventDate;

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



    public AuditLog(OffsetDateTime eventDate, Long userId, AuditType type, AuditAction action, String objectRef,
                    String description) {
        this.eventDate = eventDate;
        this.userId = userId;
        this.type = type;
        this.action = action;
        this.objectRef = objectRef;
        this.description = description;
    }

    public OffsetDateTime getEventDate() {
        return eventDate;
    }

    public void setEventDate(OffsetDateTime eventDate) {
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
