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

import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tctalent.server.model.db.CandidateSkill;
import org.tctalent.server.request.skill.SearchCandidateSkillRequest;
import org.tctalent.server.service.db.CandidateSkillService;
import org.tctalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/candidate-skill")
public class CandidateSkillAdminApi {

    private final CandidateSkillService candidateSkillService;

    @Autowired
    public CandidateSkillAdminApi(CandidateSkillService candidateSkillService) {
        this.candidateSkillService = candidateSkillService;
    }

    @PostMapping("search")
    public Map<String, Object> search(@RequestBody SearchCandidateSkillRequest request) {
        Page<CandidateSkill> candidateSkills = this.candidateSkillService.searchCandidateSkills(request);
        return candidateSkillDto().buildPage(candidateSkills);
    }

    private DtoBuilder candidateSkillDto() {
        return new DtoBuilder()
                .add("id")
                .add("skill")
                .add("timePeriod")
                ;
    }

    private DtoBuilder userDto() {
        return new DtoBuilder()
                .add("id")
                .add("firstName")
                .add("lastName")
                ;
    }


}
