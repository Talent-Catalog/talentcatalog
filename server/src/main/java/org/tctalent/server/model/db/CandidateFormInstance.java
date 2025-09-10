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

import jakarta.persistence.CascadeType;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.MapsId;
import jakarta.persistence.SequenceGenerator;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;


/**
 * <p>
 *     This records the fact that a given candidate and has filled out a form of a given type.
 *     Forms can be filled out and then subsequently updated.
 * </p>
 * <p>
 *     The actual values entered into the form are not stored here.
 *     They will have been stored either directly in explicit candidate-related fields in the
 *     database or else in candidate properties.
 * </p>
 */
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@MappedSuperclass
@SequenceGenerator(name = "seq_gen", sequenceName = "candidate_form_instance_id_seq", allocationSize = 1)
public class CandidateFormInstance {

    @EqualsAndHashCode.Include
    @EmbeddedId
    CandidateFormInstanceKey id;

    //We want any changes made to associated candidate to be updated.
    @ManyToOne(cascade = CascadeType.ALL)
    @MapsId("candidateId")
    @JoinColumn(name = "candidate_id")
    @NonNull
    private Candidate candidate;

    @NonNull
    @ManyToOne
    @MapsId("formId")
    @JoinColumn(name = "form_id")
    private CandidateForm candidateForm;

    /**
     * Time that the form was first filled out.
     */
    private OffsetDateTime createdDate;

    /**
     * Time that the form was last filled out.
     * Note that forms can be updated after they were initially completed.
     */
    private OffsetDateTime updatedDate;

    /**
     * Return value of given property name from candidate properties.
     * @param propertyName Name of property
     * @return Value of property - null if not found
     */
    protected String getProperty(String propertyName) {
        String value = null;
        Map<String,CandidateProperty> properties = candidate.getCandidateProperties();
        if (properties != null) {
            CandidateProperty property = properties.get(propertyName);
            if (property != null) {
                value = property.getValue();
            }
        }
        return value;
    }

    /**
     * Set value of given property name from candidate properties.
     * @param propertyName Name of property
     * @param value New property value - may be null. Replaces any previous value.
     */
    protected void setProperty(String propertyName, String value) {
        Map<String,CandidateProperty> properties = candidate.getCandidateProperties();
        if (properties == null) {
            //TODO JC Create properties
            properties = new HashMap<>();
            candidate.setCandidateProperties(properties);
        }
        CandidateProperty property = properties.get(propertyName);
        if (property == null) {
            property = new CandidateProperty();
            properties.put(propertyName, property);

            property.setName(propertyName);
            property.setCandidateId(candidate.getId());
        }
        property.setValue(value);
    }
}
