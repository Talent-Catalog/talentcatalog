package org.tbbtalent.server.api.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.model.db.Gender;
import org.tbbtalent.server.model.db.StatReport;
import org.tbbtalent.server.request.candidate.stat.CandidateStatDateRequest;
import org.tbbtalent.server.service.CandidateService;
import org.tbbtalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/candidate/stat")
public class CandidateStatAdminApi {

    private final CandidateService candidateService;

    @Autowired
    public CandidateStatAdminApi(CandidateService candidateService) {
        this.candidateService = candidateService;
    }

    @PostMapping("all")
    public List<Map<String, Object>> getAllStats(@RequestBody CandidateStatDateRequest request) {
        String language;
        String title;
        String chartType;

        List<StatReport> statReports = new ArrayList<>();

        chartType = "bar";
        statReports.add(new StatReport("Gender",
                this.candidateService.getGenderStats(request), chartType));

        title = "Registrations";
        chartType = "bar";
        statReports.add(new StatReport(title,
                this.candidateService.getRegistrationStats(request), chartType));
        statReports.add(new StatReport(title + " (by occupations)",
                this.candidateService.getRegistrationOccupationStats(request)));

        title = "Birth years";
        chartType = "bar";
        statReports.add(new StatReport(title,
                this.candidateService.getBirthYearStats(null, request), chartType));
        statReports.add(new StatReport(title + " (male)",
                this.candidateService.getBirthYearStats(Gender.male, request), chartType));
        statReports.add(new StatReport(title + " (female)",
                this.candidateService.getBirthYearStats(Gender.female, request), chartType));

        title = "Nationalities";
        statReports.add(new StatReport(title,
                this.candidateService.getNationalityStats(null, null, request)));
        statReports.add(new StatReport(title + " (male)",
                this.candidateService.getNationalityStats(Gender.male, null, request)));
        statReports.add(new StatReport(title + " (female)",
                this.candidateService.getNationalityStats(Gender.female, null, request)));
        statReports.add(new StatReport(title + " (Jordan)",
                this.candidateService.getNationalityStats(null, "jordan", request)));
        statReports.add(new StatReport(title + " (Lebanon)",
                this.candidateService.getNationalityStats(null, "lebanon", request)));

        title = "Occupations";
        statReports.add(new StatReport(title + "",
                this.candidateService.getOccupationStats(null, request)));
        statReports.add(new StatReport(title + " (male)",
                this.candidateService.getOccupationStats(Gender.male, request)));
        statReports.add(new StatReport(title + " (female)",
                this.candidateService.getOccupationStats(Gender.female, request)));

        title = "Most Common Occupations";
        statReports.add(new StatReport(title + "",
                this.candidateService.getMostCommonOccupationStats(null, request)));
        statReports.add(new StatReport(title + " (male)",
                this.candidateService.getMostCommonOccupationStats(Gender.male, request)));
        statReports.add(new StatReport(title + " (female)",
                this.candidateService.getMostCommonOccupationStats(Gender.female, request)));

        title = "Max Education Level";
        statReports.add(new StatReport(title, 
                this.candidateService.getMaxEducationStats(null, request)));
        statReports.add(new StatReport(title + " (male)", 
                this.candidateService.getMaxEducationStats(Gender.male, request)));
        statReports.add(new StatReport(title + " (female)", 
                this.candidateService.getMaxEducationStats(Gender.female, request)));

        title = "Languages";
        statReports.add(new StatReport(title, 
                this.candidateService.getLanguageStats(null, request)));
        statReports.add(new StatReport(title + " (male)", 
                this.candidateService.getLanguageStats(Gender.male, request)));
        statReports.add(new StatReport(title + " (female)", 
                this.candidateService.getLanguageStats(Gender.female, request)));

        title = "Survey";
        statReports.add(new StatReport(title, 
                this.candidateService.getSurveyStats(null, null, request)));
        statReports.add(new StatReport(title + " (Jordan)",
                this.candidateService.getSurveyStats(null, "jordan", request)));
        statReports.add(new StatReport(title + " (Lebanon)",
                this.candidateService.getSurveyStats(null, "lebanon", request)));
        statReports.add(new StatReport(title + " (male)", 
                this.candidateService.getSurveyStats(Gender.male, null, request)));
        statReports.add(new StatReport(title + " (female)", 
                this.candidateService.getSurveyStats(Gender.female, null, request)));

        
        language = "English";
        title = "Spoken " + language + " Language Level"; 
        statReports.add(new StatReport(title, 
                this.candidateService.getSpokenLanguageLevelStats(null, language, request)));
        statReports.add(new StatReport(title + " (male)", 
                this.candidateService.getSpokenLanguageLevelStats(Gender.male, language, request)));
        statReports.add(new StatReport(title + " (female)", 
                this.candidateService.getSpokenLanguageLevelStats(Gender.female, language, request)));

        language = "French";
        title = "Spoken " + language + " Language Level"; 
        statReports.add(new StatReport(title, 
                this.candidateService.getSpokenLanguageLevelStats(null, language, request)));
        statReports.add(new StatReport(title + " (male)", 
                this.candidateService.getSpokenLanguageLevelStats(Gender.male, language, request)));
        statReports.add(new StatReport(title + " (female)", 
                this.candidateService.getSpokenLanguageLevelStats(Gender.female, language, request)));

        //Construct the dto - just a list of all individual report dtos
        List<Map<String, Object>> dto = new ArrayList<>();
        for (StatReport statReport: statReports) {
            dto.add(statDto().buildReport(statReport));
        }
        
        return dto;
    }

    private DtoBuilder statDto() {
        return new DtoBuilder()
                .add("label")
                .add("value");
    }

}
