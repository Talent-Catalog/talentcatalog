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
import lombok.Getter;
import lombok.Setter;

/**
 * This names a type of form that a candidate may fill out.
 * <p>
 *     Note that the actual detail of the form is defined in Entity classes which subclass
 *     {@link CandidateFormInstance}.
 * </p>
 */
@Getter
@Setter
@Entity
@Table(name = "candidate_form")
@SequenceGenerator(name = "seq_gen", sequenceName = "candidate_form_id_seq", allocationSize = 1)
public class CandidateForm extends AbstractDomainObject<Long> {

    /**
     * Name of the form
     */
    private String name;

    /**
     * Description of the form. This can be displayed to the candidate when they are filling out the
     * form.
     */
    private String description;
}
