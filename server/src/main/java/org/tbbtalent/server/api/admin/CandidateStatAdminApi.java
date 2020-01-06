package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.model.DataRow;
import org.tbbtalent.server.service.CandidateService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping("/api/admin/candidate/stat")
public class CandidateStatAdminApi {

    private final CandidateService candidateService;

    @Autowired
    public CandidateStatAdminApi(CandidateService candidateService) {
        this.candidateService = candidateService;
    }


    @GetMapping("nationality")
    public List<Map<String, Object>> get() {
        List<DataRow> dataRowList = this.candidateService.getNationalityStats();
        return statDto().buildList(dataRowList);
    }


    private DtoBuilder statDto() {
        return new DtoBuilder()
                .add("label")
                .add("value");

    }



}
