package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.model.CandidateSkill;
import org.tbbtalent.server.request.skill.SearchCandidateSkillRequest;
import org.tbbtalent.server.service.CandidateSkillService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import java.util.Map;

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
