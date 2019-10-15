package org.tbbtalent.server.model;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;

@MappedSuperclass
public abstract class AbstractAuditableDomainObject<IdType extends Serializable>  extends AbstractDomainObject<IdType> {

    @Column(name = "created_date")
    private OffsetDateTime createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "updated_date")
    private OffsetDateTime updatedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    protected AbstractAuditableDomainObject() {
    }

    protected AbstractAuditableDomainObject(User createdBy) {
        setAuditFields(createdBy);
    }

    public OffsetDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(OffsetDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public OffsetDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(OffsetDateTime updatedDate) {
        this.updatedDate = updatedDate;
    }

    public User getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(User updatedBy) {
        this.updatedBy = updatedBy;
    }

    public void setAuditFields(User modifiedBy){
        if(createdBy == null){
            this.createdBy = modifiedBy;
        }
        if(createdDate == null){
            this.createdDate = OffsetDateTime.now();
        }
        this.updatedBy = modifiedBy;
        this.updatedDate = OffsetDateTime.now();
    }
}
