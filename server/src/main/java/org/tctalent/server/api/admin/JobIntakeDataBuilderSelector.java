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

package org.tctalent.server.api.admin;

import jakarta.validation.constraints.NotNull;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.util.dto.DtoBuilder;

/**
 * Utility for selecting the right DTO job intake builder.
 *
 * @author John Cameron
 */
public class JobIntakeDataBuilderSelector {
    private final CountryService countryService;

    public JobIntakeDataBuilderSelector(CountryService countryService) {
        this.countryService = countryService;
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
            .add("contactUser", shortUserDto())
            .add("countryObject", countryService.selectBuilder())
            .add("createdDate")
            .add("employer")
            .add("name")
            ;
    }

    private DtoBuilder shortUserDto() {
        return new DtoBuilder()
            .add("id")
            .add("firstName")
            .add("lastName")
            .add("email")
            ;
    }
}

