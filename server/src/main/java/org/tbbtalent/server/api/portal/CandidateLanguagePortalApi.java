package org.tbbtalent.server.api.portal;

import java.util.List;
import java.util.Map;

import javax.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.model.db.CandidateLanguage;
import org.tbbtalent.server.request.candidate.language.CreateCandidateLanguageRequest;
import org.tbbtalent.server.request.candidate.language.UpdateCandidateLanguagesRequest;
import org.tbbtalent.server.service.db.CandidateLanguageService;
import org.tbbtalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/portal/candidate-language")
public class CandidateLanguagePortalApi {

    private final CandidateLanguageService candidateLanguageService;

    @Autowired
    public CandidateLanguagePortalApi(CandidateLanguageService candidateLanguageService) {
        this.candidateLanguageService = candidateLanguageService;
    }

    @PostMapping()
    public Map<String, Object> createCandidateLanguage(@Valid @RequestBody CreateCandidateLanguageRequest request) {
        CandidateLanguage candidateLanguage = candidateLanguageService.createCandidateLanguage(request);
        return candidateLanguageDto().build(candidateLanguage);
    }

    @PostMapping("update")
    public List<Map<String, Object>> updateCandidateLanguage(@Valid @RequestBody UpdateCandidateLanguagesRequest request) {
        List<CandidateLanguage> candidateLanguage = candidateLanguageService.updateCandidateLanguages(request);
        return candidateLanguageDto().buildList(candidateLanguage);
    }

    @DeleteMapping("{id}")
    public ResponseEntity deleteCandidateLanguage(@PathVariable("id") Long id) {
        candidateLanguageService.deleteCandidateLanguage(id);
        return ResponseEntity.ok().build();
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
