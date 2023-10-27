/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

package org.tctalent.server.request.work.experience;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;
import org.tctalent.server.util.html.HtmlSanitizer;

@Getter
@Setter
public class CreateJobExperienceRequest {

    private Long candidateId;

    @NotBlank
    private String companyName;

    @NotNull
    private Long countryId;

    @NotNull
    private Long candidateOccupationId;

    @NotBlank
    private String role;

    private LocalDate startDate;
    private LocalDate endDate;
    private Boolean fullTime;
    private Boolean paid;

    @NotBlank
    private String description;

    public void setCountry(Long countryId) {
        this.countryId = countryId;
    }

    public void setDescription(String description) {
        this.description = HtmlSanitizer.sanitize(description);
    }
}
