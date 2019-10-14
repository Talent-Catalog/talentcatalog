package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.model.Occupation;
import org.tbbtalent.server.service.CandidateOccupationService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping("/api/admin/candidate-occupation")
public class CandidateOccupationAdminApi {

    private final CandidateOccupationService candidateOccupationService;

    @Autowired
    public CandidateOccupationAdminApi(CandidateOccupationService candidateOccupationService) {
        this.candidateOccupationService = candidateOccupationService;
    }

    @GetMapping("verified")
    public List<Map<String, Object>> get() {
        List<Occupation> candidateOccupations = this.candidateOccupationService.listVerifiedOccupations();
        return occupationDto().buildList(candidateOccupations);
    }

    private DtoBuilder occupationDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

    private DtoBuilder candidateOccupationDto() {
        return new DtoBuilder()
                .add("id")
                .add("occupationType")
                .add("country", countryDto())
                .add("lengthOfCourseYears")
                .add("institution")
                .add("courseName")
                .add("yearCompleted")
                ;
    }

    private DtoBuilder countryDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("status")
                ;
    }


}
