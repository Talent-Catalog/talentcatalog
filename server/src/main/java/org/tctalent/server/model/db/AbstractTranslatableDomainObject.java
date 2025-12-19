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

import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import java.io.Serializable;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
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

    public String getTranslatedName() {
        if (StringUtils.isBlank(translatedName)) return name;
        return translatedName;
    }

    @Override
    public String toString() {
        return name + ": " + super.toString();
    }
}
