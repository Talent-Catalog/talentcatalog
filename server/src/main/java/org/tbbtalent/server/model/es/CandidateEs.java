/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.model.es;

import java.util.List;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.CandidateAttachment;
import org.tbbtalent.server.model.db.Gender;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(indexName = "jobs2")
public class CandidateEs {
    @Id
    private String id;

    /**
     * Id of matching Candidate record in database
     */
    private Long masterId;
    
    @Enumerated(EnumType.STRING)
    private Gender gender;
//    private String additionalInfo;
//    private String candidateMessage;
//
//    private EducationLevel maxEducationLevel;
//
//    private Country country;
//
//    private Nationality nationality;
//
//    private List<CandidateOccupation> candidateOccupations;
//
//    private List<CandidateEducation> candidateEducations;
//
//    private List<CandidateLanguage> candidateLanguages;
//
//    private List<CandidateJobExperience> candidateJobExperiences;
//
//    private List<CandidateCertification> candidateCertifications;
//
//    @Field(type = FieldType.Nested, includeInParent = true)
//    private List<CandidateSkill> candidateSkills;

    @Field(type = FieldType.Text)
    private String candidateAttachments;

    public CandidateEs() {
    }

    public CandidateEs(Candidate candidate) {
        this();
        copy(candidate);
    }
    
    void copy(Candidate candidate) {
        this.masterId = candidate.getId();
        this.gender = candidate.getGender();
//        this.additionalInfo = candidate.getAdditionalInfo();
//        this.candidateMessage = candidate.getCandidateMessage();
//        this.maxEducationLevel = candidate.getMaxEducationLevel();
//        this.country = candidate.getCountry();
//        this.nationality = candidate.getNationality();
//        this.candidateOccupations = candidate.getCandidateOccupations();
//        this.candidateEducations = candidate.getCandidateEducations();
//        this.candidateLanguages = candidate.getCandidateLanguages();
//        this.candidateJobExperiences = candidate.getCandidateJobExperiences();
//        this.candidateCertifications = candidate.getCandidateCertifications();
//        this.candidateSkills = candidate.getCandidateSkills();
        
        this.candidateAttachments = "";
        List<CandidateAttachment> candidateAttachments = candidate.getCandidateAttachments();
        for (CandidateAttachment attachment : candidateAttachments) {
            final String textExtract = attachment.getTextExtract();
            if (textExtract != null) {
                this.candidateAttachments += textExtract + " ";
            }
        }
    }
}
