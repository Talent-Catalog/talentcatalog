package org.tbbtalent.server.api.admin;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.exception.UsernameTakenException;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.Role;
import org.tbbtalent.server.model.User;
import org.tbbtalent.server.request.candidate.CandidateEmailSearchRequest;
import org.tbbtalent.server.request.candidate.CandidateNumberOrNameSearchRequest;
import org.tbbtalent.server.request.candidate.CandidatePhoneSearchRequest;
import org.tbbtalent.server.request.candidate.CreateCandidateRequest;
import org.tbbtalent.server.request.candidate.SavedSearchRunRequest;
import org.tbbtalent.server.request.candidate.SearchCandidateRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateAdditionalInfoRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateLinksRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateStatusRequest;
import org.tbbtalent.server.request.candidate.UpdateCandidateSurveyRequest;
import org.tbbtalent.server.security.UserContext;
import org.tbbtalent.server.service.CandidateService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.server.ExportException;
import java.util.Map;

@RestController()
@RequestMapping("/api/admin/candidate")
public class CandidateAdminApi {

    private final CandidateService candidateService;
    private final UserContext userContext;

    @Autowired
    public CandidateAdminApi(CandidateService candidateService,
                             UserContext userContext) {
        this.candidateService = candidateService;
        this.userContext = userContext;
    }

    @PostMapping("runsavedsearch")
    public Map<String, Object> runSavedSearch(@RequestBody SavedSearchRunRequest request) {
        Page<Candidate> candidates = this.candidateService.searchCandidates(request);
        Map<String, Object> map = candidateBaseDto().buildPage(candidates);
        return map;
    }

    @PostMapping("search")
    public Map<String, Object> search(@RequestBody SearchCandidateRequest request) {
        Page<Candidate> candidates = this.candidateService.searchCandidates(request);
        User user = userContext.getLoggedInUser();
        if (user.getRole() == Role.admin || user.getRole() == Role.sourcepartneradmin) {
            Map<String, Object> map = candidateBaseDto().buildPage(candidates);
            return map;
        } else if (user.getRole() == Role.semilimited){
            Map<String, Object> map = candidateSemiLimitedDto().buildPage(candidates);
            return map;
        } else {
            Map<String, Object> map = candidateLimitedDto().buildPage(candidates);
            return map;
        }
    }

    @PostMapping("findbyemail")
    public Map<String, Object> findByCandidateEmail(@RequestBody CandidateEmailSearchRequest request) {
        Page<Candidate> candidates = this.candidateService.searchCandidates(request);
        Map<String, Object> map = candidateBaseDto().buildPage(candidates);
        return map;
    }

    @PostMapping("findbynumberorname")
    public Map<String, Object> findByCandidateNumberOrName(@RequestBody CandidateNumberOrNameSearchRequest request) {
        Page<Candidate> candidates = this.candidateService.searchCandidates(request);
        Map<String, Object> map = candidateBaseDto().buildPage(candidates);
        return map;
    }

    @PostMapping("findbyphone")
    public Map<String, Object> findByCandidatePhone(@RequestBody CandidatePhoneSearchRequest request) {
        Page<Candidate> candidates = this.candidateService.searchCandidates(request);
        Map<String, Object> map = candidateBaseDto().buildPage(candidates);
        return map;
    }

    @GetMapping("number/{number}")
    public Map<String, Object> get(@PathVariable("number") String number) {
        Candidate candidate = this.candidateService.findByCandidateNumber(number);
        User user = userContext.getLoggedInUser();
        if (user.getRole() == Role.admin || user.getRole() == Role.sourcepartneradmin) {
            return candidateDto().build(candidate);
        } else if (user.getRole() == Role.semilimited){
            return candidateSemiLimitedDto().build(candidate);
        } else {
            return candidateLimitedDto().build(candidate);
        }
    }

    @GetMapping("{id}")
    public Map<String, Object> get(@PathVariable("id") long id) {
        Candidate candidate = this.candidateService.getCandidate(id);
        User user = userContext.getLoggedInUser();
        if (user.getRole() == Role.admin || user.getRole() == Role.sourcepartneradmin) {
            return candidateDto().build(candidate);
        } else if (user.getRole() == Role.semilimited){
            return candidateSemiLimitedDto().build(candidate);
        } else {
            return candidateLimitedDto().build(candidate);
        }
    }
    @PostMapping
    public Map<String, Object> create(@RequestBody CreateCandidateRequest request) throws UsernameTakenException {
        Candidate candidate = this.candidateService.createCandidate(request);
        return candidateDto().build(candidate);
    }

    @PutMapping("{id}/links")
    public Map<String, Object> updateLinks(@PathVariable("id") long id,
                            @RequestBody UpdateCandidateLinksRequest request) {
        Candidate candidate = this.candidateService.updateCandidateLinks(id, request);
        return candidateDto().build(candidate);
    }

    @PutMapping("{id}/status")
    public Map<String, Object> update(@PathVariable("id") long id,
                            @RequestBody UpdateCandidateStatusRequest request) {
        Candidate candidate = this.candidateService.updateCandidateStatus(id, request);
        return candidateDto().build(candidate);
    }

    @PutMapping("{id}")
    public Map<String, Object> updateContactDetails(@PathVariable("id") long id,
                                      @RequestBody UpdateCandidateRequest request) {
        Candidate candidate = this.candidateService.updateCandidate(id, request);
        return candidateDto().build(candidate);
    }

    @PutMapping("{id}/info")
    public Map<String, Object> updateAdditionalInfo(@PathVariable("id") long id,
                                                    @RequestBody UpdateCandidateAdditionalInfoRequest request) {
        Candidate candidate = this.candidateService.updateCandidateAdditionalInfo(id, request);
        return candidateDto().build(candidate);
    }

    @PutMapping("{id}/survey")
    public Map<String, Object> updateSurvey(@PathVariable("id") long id,
                                                    @RequestBody UpdateCandidateSurveyRequest request) {
        Candidate candidate = this.candidateService.updateCandidateSurvey(id, request);
        return candidateDto().build(candidate);
    }

    @DeleteMapping("{id}")
    public boolean delete(@PathVariable("id") long id) {
        return this.candidateService.deleteCandidate(id);
    }

    @PostMapping(value = "export/csv", produces = MediaType.TEXT_PLAIN_VALUE)
    public void export(@RequestBody SearchCandidateRequest request,
                       HttpServletResponse response) throws IOException, ExportException {
        response.setHeader("Content-Disposition", "attachment; filename=\"" + "candidates.csv\"");
        response.setContentType("text/csv; charset=utf-8");
        candidateService.exportToCsv(request, response.getWriter());
    }

    @PostMapping(value = "exportsearch/csv", produces = MediaType.TEXT_PLAIN_VALUE)
    public void export(@RequestBody SavedSearchRunRequest request,
                       HttpServletResponse response) throws IOException, ExportException {
        response.setHeader("Content-Disposition", "attachment; filename=\"" + "candidates.csv\"");
        response.setContentType("text/csv; charset=utf-8");
        candidateService.exportToCsv(request, response.getWriter());
    }

    @GetMapping(value = "{id}/cv.pdf")
    public void downloadStudentListAsPdf(@PathVariable("id") long id, HttpServletResponse response)
            throws IOException {

        Candidate candidate = candidateService.getCandidate(id);
        String name = candidate.getUser().getDisplayName()+"-"+ "CV";
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=" + name + ".pdf");

        Resource report = candidateService.generateCv(candidate);
        try (InputStream reportStream = report.getInputStream()) {
            IOUtils.copy(reportStream, response.getOutputStream());
            response.flushBuffer();
        }
    }

    private DtoBuilder candidateBaseDto() {
        return new DtoBuilder()
                .add("id")
                .add("status")
                .add("candidateNumber")
                .add("gender")
                .add("dob")
                .add("phone")
                .add("whatsapp")
                .add("city")
                .add("address1")
                .add("yearOfArrival")
                .add("additionalInfo")
                .add("candidateMessage")
                .add("folderlink")
                .add("sflink")
                .add("videolink")
                .add("unRegistered")
                .add("unRegistrationNumber")
                .add("surveyComment")
                .add("surveyType", surveyTypeDto())
                .add("country", countryDto())
                .add("nationality", nationalityDto())
                .add("user", userDto())
                .add("candidateShortlistItems", shortlistDto())
                ;
    }

    private DtoBuilder candidateDto() {
        return candidateBaseDto()
                .add("maxEducationLevel", educationLevelDto())
                .add("user", userDto())
                .add("candidateShortlistItems", shortlistDto())
                ;
    }

    private DtoBuilder candidateSemiLimitedDto() {
        return new DtoBuilder()
                .add("id")
                .add("status")
                .add("candidateNumber")
                .add("gender")
                .add("dob")
                .add("city")
                .add("address1")
                .add("yearOfArrival")
                .add("additionalInfo")
                .add("candidateMessage")
                .add("folderlink")
                .add("sflink")
                .add("country", countryDto())
                .add("user",userSemiLimitedDto())
                .add("nationality", nationalityDto())
                .add("candidateShortlistItems", shortlistDto())
                ;
    }

    private DtoBuilder candidateLimitedDto() {
        return new DtoBuilder()
                .add("id")
                .add("status")
                .add("candidateNumber")
                .add("gender")
                .add("dob")
                .add("yearOfArrival")
                .add("additionalInfo")
                .add("candidateMessage")
                .add("folderlink")
                .add("sflink")
                .add("user",userSemiLimitedDto())
                .add("candidateShortlistItems", shortlistDto())
                ;
    }

    private DtoBuilder userDto() {
        return new DtoBuilder()
                .add("id")
                .add("firstName")
                .add("lastName")
                .add("email")
                ;
    }

    private DtoBuilder userSemiLimitedDto() {
        return new DtoBuilder()
                .add("id")
                ;
    }

    private DtoBuilder countryDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

    private DtoBuilder nationalityDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

    private DtoBuilder shortlistDto() {
        return new DtoBuilder()
                .add("id")
                .add("shortlistStatus")
                .add("savedSearch", savedSearchDto())
                ;
    }

    private DtoBuilder surveyTypeDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

    private DtoBuilder savedSearchDto() {
        return new DtoBuilder()
                .add("id")
                ;
    }

    private DtoBuilder educationMajor() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

    private DtoBuilder educationLevelDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }

}
