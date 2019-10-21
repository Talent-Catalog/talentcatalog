package org.tbbtalent.server.model;

import java.io.Serializable;

import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;

@MappedSuperclass
@Translatable(value = "name", translation = "translatedName")
public abstract class AbstractTranslatableDomainObject<IdType extends Serializable> extends AbstractDomainObject<IdType> {

    private String name;

    @Transient
    private String translatedName;

    protected AbstractTranslatableDomainObject() {
    }

    protected AbstractTranslatableDomainObject(String translatedName) {
        this.translatedName = translatedName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTranslatedName() {
        if (StringUtils.isBlank(translatedName)) return name;
        return translatedName;
    }

    public void setTranslatedName(String translatedName) {
        this.translatedName = translatedName;
    }

}
