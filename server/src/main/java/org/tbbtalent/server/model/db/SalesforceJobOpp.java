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

import java.time.OffsetDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;

/**
 * This is a copy of an Employer Job Opportunity on Salesforce
 *
 * @author John Cameron
 */
@Getter
@Setter
@ToString
@Entity
@Table(name = "salesforce_job_opp")
public class SalesforceJobOpp {

    @Id
    @javax.persistence.Id
    @Column(name = "id")
    private String id;

    /**
     * True if opportunity is closed
     */
    private boolean closed;

    /**
     * Name of country where job is located
     */
    private String country;

    /**
     * Name of employer - maps to Account name on Salesforce
     */
    private String employer;

    /**
     * Name of opportunity - maps to Opportunity name on Salesforce
     */
    private String name;

    /**
     * Stage of job opportunity
     */
    @Enumerated(EnumType.STRING)
    private JobOpportunityStage stage;

    /**
     * Stage of job opportunity expressed as number - 0 being first stage.
     * <p/>
     * Used for sorting by stage.
     * <p/>
     * This is effectively a computed field, computed by calling the ordinal() method of the
     * {@link #stage} enum.
     */
    private int stageOrder;

    /**
     * Last time that this was updated from Salesforce (which holds the master copy)
     */
    private OffsetDateTime lastUpdate;

    /**
     * Override standard setStage to automatically also update stageOrder
     * @param stage New job opportunity stage
     */
    public void setStage(JobOpportunityStage stage) {
        this.stage = stage;
        setStageOrder(stage.ordinal());
    }
}
