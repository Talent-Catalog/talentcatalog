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

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

@Getter
@Setter
@Entity
@Table(name = "help_link")
@SequenceGenerator(name = "seq_gen", sequenceName = "help_link_id_seq", allocationSize = 1)
public class HelpLink extends AbstractAuditableDomainObject<Long> {

    /**
     * Country associated with help
     */
    @Nullable
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    /**
     * Relevant stage, if any
     * <p/>
     * Only one kind of stage can be present: case or job - not both
     */
    @Enumerated(EnumType.STRING)
    @Nullable
    private CandidateOpportunityStage caseStage;

    /**
     * Current user focus - ie what is the user wanting to do
     */
    @Enumerated(EnumType.STRING)
    @Nullable
    private HelpFocus focus;

    /**
     * Relevant stage, if any.
     * <p/>
     * Only one kind of stage can be present: case or job - not both
     */
    @Enumerated(EnumType.STRING)
    @Nullable
    private JobOpportunityStage jobStage;

    /**
     * Describes this help.
     */
    @NonNull
    private String label;

    /**
     * Url link to help.
     */
    @NonNull
    private String link;

    /**
     * Information about Next step associated with help
     */
    @Nullable
    @Embedded
    private NextStepInfo nextStepInfo;
}
