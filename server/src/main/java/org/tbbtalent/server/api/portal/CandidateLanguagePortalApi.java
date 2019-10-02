package org.tbbtalent.server.api.portal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.tbbtalent.server.model.CandidateLanguage;
import org.tbbtalent.server.request.candidate.language.CreateCandidateLanguageRequest;
import org.tbbtalent.server.service.CandidateLanguageService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import javax.validation.Valid;
import java.util.Map;

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
