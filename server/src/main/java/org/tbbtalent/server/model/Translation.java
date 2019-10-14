package org.tbbtalent.server.model;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "system_language")
@SequenceGenerator(name = "seq_gen", sequenceName = "system_language_id_seq", allocationSize = 1)
public class Translation extends AbstractAuditableDomainObject<Long> {

    private Long objectId;
    private String objectType;
    private String language;

    public Long getObjectId() {
        return objectId;
    }

    public void setObjectId(Long objectId) {
        this.objectId = objectId;
    }

    public String getObjectType() {
        return objectType;
    }

    public void setObjectType(String objectType) {
        this.objectType = objectType;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

}
