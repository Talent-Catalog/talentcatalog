/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.api.admin;

import javax.validation.constraints.NotNull;
import org.tbbtalent.server.util.dto.DtoBuilder;

/**
 * Utility for selecting the right DTO job intake builder.
 *
 * @author John Cameron
 */
public class JobIntakeDataBuilderSelector {

    public JobIntakeDataBuilderSelector() {
    }

    public @NotNull DtoBuilder selectBuilder() {
        return joiDto();
    }

    private DtoBuilder joiDto() {
        return new DtoBuilder()
            .add("id")
            .add("jobOpp", jobOppDto())
            .add("salaryRange")
            .add("recruitmentProcess")
            .add("employerCostCommitment")
            .add("location")
            .add("locationDetails")
            .add("benefits")
            .add("languageRequirements")
            .add("educationRequirements")
            .add("skillRequirements")
            .add("employmentExperience")
            .add("occupationCode")
            ;
    }

    private DtoBuilder jobOppDto() {
        return new DtoBuilder()
            .add("id")
            .add("sfId")
            .add("accepting")
            .add("contactEmail")
            .add("countryObject", countryDto())
            .add("createdDate")
            .add("employer")
            .add("name")
            ;
    }

    private DtoBuilder countryDto() {
        return new DtoBuilder()
            .add("id")
            .add("name")
            ;
    }
}

