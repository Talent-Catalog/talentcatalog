package org.tbbtalent.server.model;

import javax.persistence.Entity;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "translation")
@SequenceGenerator(name = "seq_gen", sequenceName = "translation_id_seq", allocationSize = 1)
public class Translation extends AbstractAuditableDomainObject<Long> {

    private Long objectId;
    private String objectType;
    private String language;
    private String value;

    public Translation() {
    }

    public Translation(User createdBy, Long objectId, String objectType, String language, String value) {
        super(createdBy);
        this.objectId = objectId;
        this.objectType = objectType;
        this.language = language;
        this.value = value;
    }

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

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Translation{" +
                "objectId=" + objectId +
                ", objectType='" + objectType + '\'' +
                ", language='" + language + '\'' +
                ", value='" + value + '\'' +
                '}';
    }
}
