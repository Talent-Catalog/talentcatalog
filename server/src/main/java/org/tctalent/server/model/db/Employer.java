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
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

/**
 * This is an employer who can be associated with job opportunities .
 * It is backed up by a corresponding Salesforce Account object.
 *
 * @author John Cameron
 */
@Getter
@Setter
@Entity
@Table(name = "employer")
@SequenceGenerator(name = "seq_gen", sequenceName = "employer_id_seq", allocationSize = 1)
public class Employer extends AbstractSalesforceObject {

    /**
     * References country object on database
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    @Nullable
    private String description;

    /**
     * Indicates whether the employer has hired internationally or not.
     * Null if we don't know
     */
    @Nullable
    private Boolean hasHiredInternationally;

    @Nullable
    private String website;

    //TODO JC Enums matching SF Office size and Geography

}
