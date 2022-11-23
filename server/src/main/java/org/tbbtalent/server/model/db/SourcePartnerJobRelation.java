/*
 * Copyright (c) 2022 Talent Beyond Boundaries.
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

package org.tbbtalent.server.model.db;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * This is used to store the many-to-many relation between source partners and jobs.
 * <p/>
 * It also can store "context" information relating to both a source partner and a job - for example
 * the contact user that the source partner has assigned to this particular job.
 * <p/>
 * Note on the naming convention:
 * <p/>
 * If this was named SourcePartnerJob, it would be storing job information linked to a source partner
 * - like CandidateAttachment stores information about an attachment linked to a candidate.
 * The "Relation" part of the name JobSourcePartnerRelation indicates that the entity represents
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
@Table(name = "source_partner_job")
public class SourcePartnerJobRelation {

    /**
     * Composite key consisting of job id and source partner id
     */
    @EqualsAndHashCode.Include
    @EmbeddedId
    SourcePartnerJobRelationKey id;

    /**
     * Job associated with relation
     */
    @ManyToOne
    @MapsId("tcJobId")
    @JoinColumn(name = "tc_job_id")
    private SalesforceJobOpp job;

    /**
     * Source partner associated with relation
     */
    @ManyToOne
    @MapsId("sourcePartnerId")
    @JoinColumn(name = "source_partner_id")
    private SourcePartnerImpl sourcePartner;

    /**
     * The contact related to the above job and source partner.
     */
    @ManyToOne
    @JoinColumn(name = "contact_id")
    private User contact;

}
