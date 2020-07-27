/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.model.es;

import java.util.List;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import org.springframework.data.annotation.Id;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.CandidateAttachment;
import org.tbbtalent.server.model.db.CandidateEducation;
import org.tbbtalent.server.model.db.CandidateJobExperience;
import org.tbbtalent.server.model.db.CandidateOccupation;
import org.tbbtalent.server.model.db.CandidateStatus;
import org.tbbtalent.server.model.db.Gender;
import org.tbbtalent.server.request.PagedSearchRequest;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Document(indexName = "candidates")
public class CandidateEs {

    private static final String[] sortingFields = {
            "masterId",
            "country",
            "gender",
            "firstName",
            "lastName",
            "nationality",
            "status"
    }; 
   
    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String country;

    @Field(type = FieldType.Text)
    private String cvs;

    @Field(type = FieldType.Text)
    private String educations;

    @Field(type = FieldType.Keyword)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Field(type = FieldType.Keyword)
    private String firstName;

    @Field(type = FieldType.Text)
    private String jobExperiences;

    @Field(type = FieldType.Keyword)
    private String lastName;

    /**
     * Id of matching Candidate record in database
     */
    private Long masterId;

    @Field(type = FieldType.Keyword)
    private String nationality;

    @Field(type = FieldType.Text)
    private String occupations;

    @Field(type = FieldType.Keyword)
    @Enumerated(EnumType.STRING)
    private CandidateStatus status;


    //todo add extra text fields and fields needed for sorting
//    private String additionalInfo;
//    private String candidateMessage;
//    private List<CandidateCertification> candidateCertifications;
//    private List<CandidateSkill> candidateSkills;

    public CandidateEs() {
    }

    public CandidateEs(Candidate candidate) {
        this();
        copy(candidate);
    }
    
    public void copy(Candidate candidate) {
//        this.additionalInfo = candidate.getAdditionalInfo();
//        this.candidateMessage = candidate.getCandidateMessage();
//        this.candidateCertifications = candidate.getCandidateCertifications();
//        this.candidateSkills = candidate.getCandidateSkills();

        this.firstName = candidate.getUser() == null ? null 
                : candidate.getUser().getFirstName();
        this.gender = candidate.getGender();
        this.country = candidate.getCountry() == null ? null 
                : candidate.getCountry().getName();
        this.lastName = candidate.getUser() == null ? null 
                : candidate.getUser().getLastName();
        this.masterId = candidate.getId();
        this.nationality = candidate.getNationality() == null ? null
                : candidate.getNationality().getName();
        this.status = candidate.getStatus();

        this.cvs = "";
        List<CandidateAttachment> candidateAttachments = candidate.getCandidateAttachments();
        if (candidateAttachments != null) {
            for (CandidateAttachment attachment : candidateAttachments) {
                final String textExtract = attachment.getTextExtract();
                if (textExtract != null) {
                    this.cvs += textExtract + " ";
                }
            }
        }

        this.educations = "";
        List<CandidateEducation> educations = candidate.getCandidateEducations();
        if (educations != null) {
            for (CandidateEducation education : educations) {
                final String text = education.getCourseName();
                if (text != null) {
                    this.educations += text + " ";
                }
            }
        }
        
        this.jobExperiences = "";
        List<CandidateJobExperience> jobs = candidate.getCandidateJobExperiences();
        if (jobs != null) {
            for (CandidateJobExperience job : jobs) {
                String text = job.getRole();
                if (text != null) {
                    this.jobExperiences += text + " ";
                }
                text = job.getDescription();
                if (text != null) {
                    this.jobExperiences += text + " ";
                }
            }
        }
        
        this.occupations = "";
        List<CandidateOccupation> occupations = candidate.getCandidateOccupations();
        if (occupations != null) {
            for (CandidateOccupation occupation : occupations) {
                if (occupation != null && occupation.getOccupation() != null) {
                    String text = occupation.getOccupation().getName();
                    if (text != null) {
                        this.occupations += text + " ";
                    }
                }
            }
        }
    }
    
    public static PageRequest getAdjustedPagedSearchRequest(
            PagedSearchRequest request) {
        PageRequest requestAdj;
        
        String[] sortFields = request.getSortFields();
        if (sortFields != null && sortFields.length > 0) {
            String sortField = sortFields[0];
            
            //Special hack for id field - which is masterId in CandidateEs
            if (sortField.equals("id")) {
                sortField = "masterId";
            }
            
            boolean matched = false;

            //Type to match field with a sortable field
            for (String sortingField : sortingFields) {
                if (sortField.contains(sortingField)) {
                    matched = true;
                    sortField = sortingField;
                    break;
                }
            }
            
            if (!matched) {
                requestAdj = PageRequest.of(
                        request.getPageNumber(), request.getPageSize());
            } else {
                requestAdj = PageRequest.of(
                        request.getPageNumber(), request.getPageSize(),
                        request.getSortDirection(), sortField
                );
            }
        } else {
            requestAdj = PageRequest.of(
                    request.getPageNumber(), request.getPageSize());
        }
        return requestAdj;
    }
}
