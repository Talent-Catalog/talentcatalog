package org.tbbtalent.server.api.admin;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.model.db.CandidateLanguage;
import org.tbbtalent.server.request.candidate.language.UpdateCandidateLanguageRequest;
import org.tbbtalent.server.service.CandidateLanguageService;
import org.tbbtalent.server.util.dto.DtoBuilder;

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

    @PutMapping("{id}")
    public Map<String, Object> update(@PathVariable("id") long id,
                                      @RequestBody UpdateCandidateLanguageRequest request) {
        CandidateLanguage candidateLanguage = this.candidateLanguageService.updateCandidateLanguage(id, request);
        return candidateLanguageDto().build(candidateLanguage);
    }




    private DtoBuilder candidateLanguageDto() {
        return new DtoBuilder()
                .add("id")
                .add("migrationLanguage")
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
                .add("name")
                .add("level")
                ;
    }

}
