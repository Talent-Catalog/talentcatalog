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

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * This is used to store the many-to-many relation between partners and jobs.
 * <p/>
 * It also can store "context" information relating to both a partner and a job - for example
 * the contact user that the partner has assigned to this particular job.
 * <p/>
 * Note on the naming convention:
 * <p/>
 * If this was named PartnerJob, it would be storing job information linked to a partner
 * - like CandidateAttachment stores information about an attachment linked to a candidate.
 * The "Relation" part of the name PartnerJobRelation indicates that the entity represents
 * the actual many-to-many relationship - plus any contextual data uniquely associated with that
 * relationship.
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Entity
@Table(name = "partner_job")
public class PartnerJobRelation {

    /**
     * Composite key consisting of job id and source partner id
     */
    @EqualsAndHashCode.Include
    @EmbeddedId
    PartnerJobRelationKey id;

    /**
     * Job associated with relation
     */
    @ManyToOne
    @MapsId("tcJobId")
    @JoinColumn(name = "tc_job_id")
    private SalesforceJobOpp job;

    /**
     * Partner associated with relation
     */
    @ManyToOne
    @MapsId("partnerId")
    @JoinColumn(name = "partner_id")
    private PartnerImpl partner;

    /**
     * The contact related to the above job and partner.
     */
    @ManyToOne
    @JoinColumn(name = "contact_id")
    private User contact;

    public PartnerJobRelation() {
    }

    public PartnerJobRelation(PartnerImpl partner, SalesforceJobOpp job) {
        this.partner = partner;
        this.job = job;
        this.id = new PartnerJobRelationKey(partner.getId(), job.getId());
    }

}
