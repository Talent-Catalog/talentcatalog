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

import java.util.List;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.tctalent.server.util.dto.DtoBuilder;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
public class VisaPathwayApi {
    @GetMapping("/country/{countryId}")
    public List<Map<String, Object>> listByCountryId(@PathVariable("countryId") long countryId) {
        // todo create service and service method
        //List<VisaPathway> visaPathways;
        //visaPathways = this.visaPathwaysService.list(countryId);
        //visaPathwaysDto().buildList(visaPathways);
        return null;
    }

    private DtoBuilder visaPathwaysDto() {
        return new DtoBuilder()
            .add("id")
            .add("name")
            .add("description")
            .add("country", countryDto())
            .add("age")
            .add("language")
            .add("empCommitment")
            .add("inclusions")
            .add("other")
            .add("workExperience")
            .add("education")
            .add("educationCredential")
            ;
    }

    private DtoBuilder countryDto() {
        return new DtoBuilder()
            .add("id")
            .add("name")
            .add("isoCode")
            .add("status")
            ;
    }

}
