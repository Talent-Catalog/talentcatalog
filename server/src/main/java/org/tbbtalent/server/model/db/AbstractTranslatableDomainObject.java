/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.model.db;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;

@MappedSuperclass
@Translatable(value = "name", translation = "translatedName")
public abstract class AbstractTranslatableDomainObject<IdType extends Serializable> 
        extends AbstractDomainObject<IdType> 
        implements Comparable<AbstractTranslatableDomainObject<IdType>> {

    private String name;

    @Transient
    private Long translatedId;

    @Transient
    private String translatedName;

    protected AbstractTranslatableDomainObject() {
    }

    protected AbstractTranslatableDomainObject(String translatedName) {
        this.translatedName = translatedName;
    }

    /**
     * This supports a "nulls first" alphabetical ascending sort order on 
     * anything with a name.
     */
    @Override
    public int compareTo(AbstractTranslatableDomainObject<IdType> other) {
        if (this.name == null) {
            return other.getName() == null ? 0 : -1;
        }
        
        if (other.getName() == null) {
            return 1;
        }
        
        return this.name.compareTo(other.getName());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getTranslatedId() {
        return translatedId;
    }

    public void setTranslatedId(Long translatedId) {
        this.translatedId = translatedId;
    }

    public String getTranslatedName() {
        if (StringUtils.isBlank(translatedName)) return name;
        return translatedName;
    }

    public void setTranslatedName(String translatedName) {
        this.translatedName = translatedName;
    }

    @Override
    public String toString() {
        return name + ": " + super.toString();
    }
}
