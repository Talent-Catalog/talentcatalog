package org.tbbtalent.server.api.admin;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.model.Gender;
import org.tbbtalent.server.model.StatReport;
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

    @GetMapping("all")
    public List<Map<String, Object>> getAllStats() {
        String language;
        String title;
        String chartType;

        List<StatReport> statReports = new ArrayList<>();

        chartType = "bar";
        statReports.add(new StatReport("Gender",
                this.candidateService.getGenderStats(), chartType)); 

        title = "Registrations";
        chartType = "bar";
        statReports.add(new StatReport(title + " (since start)",
                this.candidateService.getRegistrationStats(999999), chartType)); 
        statReports.add(new StatReport(title + " (last year)",
                this.candidateService.getRegistrationStats(366), chartType)); 
        statReports.add(new StatReport(title + " (last 3 weeks)",
                this.candidateService.getRegistrationStats(21), chartType)); 

        title = "Birth years";
        chartType = "bar";
        statReports.add(new StatReport(title,
                this.candidateService.getBirthYearStats(null), chartType)); 
        statReports.add(new StatReport(title + " (male)",
                this.candidateService.getBirthYearStats(Gender.male), chartType)); 
        statReports.add(new StatReport(title + " (female)",
                this.candidateService.getBirthYearStats(Gender.female), chartType)); 

        title = "Nationalities";
        statReports.add(new StatReport(title,
                this.candidateService.getNationalityStats(null, null))); 
        statReports.add(new StatReport(title + " (male)",
                this.candidateService.getNationalityStats(Gender.male, null))); 
        statReports.add(new StatReport(title + " (female)",
                this.candidateService.getNationalityStats(Gender.female, null))); 
        statReports.add(new StatReport(title + " (Jordan)",
                this.candidateService.getNationalityStats(null, "jordan")));
        statReports.add(new StatReport(title + " (Lebanon)",
                this.candidateService.getNationalityStats(null, "lebanon")));

        title = "Occupations";
        statReports.add(new StatReport(title + "",
                this.candidateService.getOccupationStats(null))); 
        statReports.add(new StatReport(title + " (male)",
                this.candidateService.getOccupationStats(Gender.male))); 
        statReports.add(new StatReport(title + " (female)",
                this.candidateService.getOccupationStats(Gender.female))); 

        title = "Main Occupations";
        statReports.add(new StatReport(title + "",
                this.candidateService.getMainOccupationStats(null))); 
        statReports.add(new StatReport(title + " (male)",
                this.candidateService.getMainOccupationStats(Gender.male))); 
        statReports.add(new StatReport(title + " (female)",
                this.candidateService.getMainOccupationStats(Gender.female))); 

        title = "Max Education Level";
        statReports.add(new StatReport(title, 
                this.candidateService.getMaxEducationStats(null)));
        statReports.add(new StatReport(title + " (male)", 
                this.candidateService.getMaxEducationStats(Gender.male)));
        statReports.add(new StatReport(title + " (female)", 
                this.candidateService.getMaxEducationStats(Gender.female)));

        title = "Languages";
        statReports.add(new StatReport(title, 
                this.candidateService.getLanguageStats(null)));
        statReports.add(new StatReport(title + " (male)", 
                this.candidateService.getLanguageStats(Gender.male)));
        statReports.add(new StatReport(title + " (female)", 
                this.candidateService.getLanguageStats(Gender.female)));

        title = "Survey";
        statReports.add(new StatReport(title, 
                this.candidateService.getSurveyStats(null, null)));
        statReports.add(new StatReport(title + " (Jordan)",
                this.candidateService.getSurveyStats(null, "jordan")));
        statReports.add(new StatReport(title + " (Lebanon)",
                this.candidateService.getSurveyStats(null, "lebanon")));
        statReports.add(new StatReport(title + " (male)", 
                this.candidateService.getSurveyStats(Gender.male, null)));
        statReports.add(new StatReport(title + " (female)", 
                this.candidateService.getSurveyStats(Gender.female, null)));

        
        language = "English";
        title = "Spoken " + language + " Language Level"; 
        statReports.add(new StatReport(title, 
                this.candidateService.getSpokenLanguageLevelStats(null, language)));
        statReports.add(new StatReport(title + " (male)", 
                this.candidateService.getSpokenLanguageLevelStats(Gender.male, language)));
        statReports.add(new StatReport(title + " (female)", 
                this.candidateService.getSpokenLanguageLevelStats(Gender.female, language)));

        language = "French";
        title = "Spoken " + language + " Language Level"; 
        statReports.add(new StatReport(title, 
                this.candidateService.getSpokenLanguageLevelStats(null, language)));
        statReports.add(new StatReport(title + " (male)", 
                this.candidateService.getSpokenLanguageLevelStats(Gender.male, language)));
        statReports.add(new StatReport(title + " (female)", 
                this.candidateService.getSpokenLanguageLevelStats(Gender.female, language)));

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
