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
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.MappedSuperclass;
import java.io.Serializable;
import org.springframework.data.annotation.Id;

@MappedSuperclass
public abstract class AbstractDomainObject<IdType extends Serializable>  implements Serializable {


    /*
       See https://stackoverflow.com/questions/4560813/specifying-distinct-sequence-per-table-in-hibernate-on-subclasses
       Unfortunate side effect is that it generates "Duplicate generator name seq_gen" warnings
       for each entity at start up.
       Hibernate has a JPA_ID_GENERATOR_GLOBAL_SCOPE_COMPLIANCE setting which can be set false
       but haven't figured out how to set this yet.
     */
    @Id
    @jakarta.persistence.Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "seq_gen")
    @Column(name = "id")
    private IdType id;

    protected AbstractDomainObject() {
    }

    public IdType getId() {
        return id;
    }

    public void setId(IdType id) {
        this.id = id;
    }

    /*
      For good discussion on hashCode and equals for entities see
      https://web.archive.org/web/20170710132916/http://www.onjava.com/pub/a/onjava/2006/09/13/dont-let-hibernate-steal-your-identity.html

      The key problem is that entity objects only get an id once they are
      persisted. If you are using those objects before persisting them
      the absence of an id can lead to peculiar results - for example all
      object instances looking like they are equal.
     */

    @Override
    public int hashCode() {
        if (id != null) {
            return id.hashCode();
        } else {
            return super.hashCode();
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;

        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        AbstractDomainObject<?> other = (AbstractDomainObject<?>) obj;

        //If id is missing assume that it is not equal to other instance.
        //(Previous version of this code treated all instances with null
        //ids as equal).
        if (id == null) return false;

        //Equivalence by id
        return id.equals(other.id);
    }

    @Override
    public String toString() {
        return this.getClass().getName() + "[id=" + id + "]";
    }
}
