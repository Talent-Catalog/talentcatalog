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
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.springframework.lang.Nullable;

/**
 * Base class for both Job Opportunity and Candidate Opportunity objects.
 * <p/>
 * These objects have corresponding objects on Salesforce.
 *
 * @author John Cameron
 */
@Getter
@Setter
@MappedSuperclass
public class AbstractOpportunity extends AbstractSalesforceObject {

    /**
     * True if opportunity is closed
     */
    private boolean closed;

    /**
     * True if opportunity is won
     */
    private boolean won;

    /**
     * Closing comments on opportunity.
     * Normally null until opportunity has been closed.
     */
    @Nullable
    private String closingComments;

    /**
     * Next step for this opportunity
     */
    @Nullable
    private String nextStep;

    /**
     * Due date of next step
     */
    @Nullable
    private LocalDate nextStepDueDate;

    /**
     * Stage of opportunity expressed as number - 0 being first stage.
     * <p/>
     * Used for sorting by stage.
     * <p/>
     * This is effectively a computed field, computed by calling the ordinal() method of the
     * stage enum.
     */
    private int stageOrder;

}
