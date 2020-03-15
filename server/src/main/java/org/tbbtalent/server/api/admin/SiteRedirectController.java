package org.tbbtalent.server.api.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.service.CandidateService;

@Controller()
@RequestMapping("/backend/jobseeker")
public class SiteRedirectController {

    private CandidateService candidateService;

    @Value("${web.admin}")
    private String adminUrl;


    @Autowired
    public SiteRedirectController(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @RequestMapping(value = "/view-resume")
    public String redirectOldResumeUrl(@RequestParam("id") String candidateNumber) {
        Candidate candidate = candidateService.findByCandidateNumber(candidateNumber);
        if (candidate != null){
            return "redirect:" + adminUrl + "/candidates/" + candidateNumber;
        } else {
            return "redirect:" + adminUrl + "/candidates";
        }

    }
}
