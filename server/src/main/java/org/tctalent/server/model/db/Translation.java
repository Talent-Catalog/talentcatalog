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

import jakarta.persistence.Entity;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;

/**
 * This table stores translations for the english values of other tables
 * (typically used in drop downs) eg
 * country, nationality, occupation, education_level etc.
 *
 */
@Entity
@Table(name = "translation")
@SequenceGenerator(name = "seq_gen", sequenceName = "translation_id_seq", allocationSize = 1)
public class Translation extends AbstractAuditableDomainObject<Long> {

    /**
     * This refers to the id of the object being translated.
     * So for example, if objectType is "country" this will be the id of
     * country in the "country" table.
     */
    private Long objectId;

    /**
     * This refers to the name of the associated table that this entry is
     * translating - eg "country"
     */
    private String objectType;

    /**
     * Two character language identifier - eg "ar" for Arabic.
     */
    private String language;

    /**
     * This is the translation of the given objectType with the given objectId
     * to the given language.
     */
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
