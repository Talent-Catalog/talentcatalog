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

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

/**
 * Information about a Next step.
 * <p/>
 * This information is usually sourced from process documentation which is
 * captured in the Talent Catalog's HelpLink Settings.
 *
 * @author John Cameron
 */
@Getter
@Setter
@Embeddable
public class NextStepInfo {

    /**
     * Number of days typically required to complete this next step.
     * <p/>
     * This can be used to default an opportunity's NextStepDueDate
     * (See {@link AbstractOpportunity#getNextStepDueDate()})
     */
    private int nextStepDays;

    /**
     * Name used to identify a next step.
     * <p/>
     * This is typically displayed in TC help to allow the user to select
     * which next step they are interested in.
     */
    private String nextStepName;

    /**
     * Description of the next step.
     * <p/>
     * This text can be used to default an opportunity's NextStep
     * (See {@link AbstractOpportunity#getNextStep()})
     */
    private String nextStepText;
}
