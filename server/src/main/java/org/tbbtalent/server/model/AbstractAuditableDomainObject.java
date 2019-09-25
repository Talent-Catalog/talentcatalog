package org.tbbtalent.server.model;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@MappedSuperclass
public abstract class AbstractAuditableDomainObject<IdType extends Serializable>  extends AbstractDomainObject<IdType> {

    @Column(name = "created_date")
    private LocalDateTime createdDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by")
    private User createdBy;

    @Column(name = "updated_date")
    private LocalDateTime updatedDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by")
    private User updatedBy;

    protected AbstractAuditableDomainObject() {
    }

    protected AbstractAuditableDomainObject(User createdBy) {
        setAuditFields(createdBy);
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public User getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(User createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getUpdatedDate() {
        return updatedDate;
    }

    public void setUpdatedDate(LocalDateTime updatedDate) {
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
            this.createdDate = LocalDateTime.now();
        }
        this.updatedBy = modifiedBy;
        this.updatedDate = LocalDateTime.now();
    }
}
