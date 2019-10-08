package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.exception.UsernameTakenException;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.CandidateLanguage;
import org.tbbtalent.server.request.candidate.CreateCandidateRequest;
import org.tbbtalent.server.request.candidate.SearchCandidateRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateRequest;
import org.tbbtalent.server.service.CandidateLanguageService;
import org.tbbtalent.server.service.CandidateService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import java.util.List;
import java.util.Map;

@RestController()
@RequestMapping("/api/admin/candidate-language")
public class CandidateLanguageAdminApi {

    private final CandidateLanguageService candidateLanguageService;

    @Autowired
    public CandidateLanguageAdminApi(CandidateLanguageService candidateLanguageService) {
        this.candidateLanguageService = candidateLanguageService;
    }


    @GetMapping("{id}/list")
    public List<Map<String, Object>> get(@PathVariable("id") long id) {
        List<CandidateLanguage> candidateLanguages = this.candidateLanguageService.list(id);
        return candidateLanguageDto().buildList(candidateLanguages);
    }




    private DtoBuilder candidateLanguageDto() {
        return new DtoBuilder()
                .add("id")
                .add("language", languageDto())
                .add("writtenLevel", languageLevelDto())
                .add("spokenLevel",languageLevelDto())
                ;
    }

    private DtoBuilder languageDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

    private DtoBuilder languageLevelDto() {
        return new DtoBuilder()
                .add("id")
                .add("level")
                ;
    }

}
