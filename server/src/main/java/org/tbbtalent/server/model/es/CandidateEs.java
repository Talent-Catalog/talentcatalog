/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.model.es;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.tbbtalent.server.model.db.*;
import org.tbbtalent.server.request.PagedSearchRequest;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.util.ArrayList;
import java.util.List;

/**
 * This defines the fields which are stored in Elasticsearch "documents"
 * corresponding to these Candidate "entities".
 * All these candidate records comprise the "candidates" index - roughly
 * comparable to a candidate table in a traditional database.
 */
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
            "status",
            "updated",
            "phone",
            "unhcrStatus",
            "maritalStatus",
            "drivingLicense"
    }; 
   
    @Id
    private String id;

    @Field(type = FieldType.Text)
    private String additionalInfo;

    @Field(type = FieldType.Text)
    private List<String> certifications;

    @Field(type = FieldType.Keyword)
    private String country;

    @Field(type = FieldType.Text)
    private List<String> cvs;

    @Field(type = FieldType.Text)
    private List<String> educations;

    @Field(type = FieldType.Keyword)
    @Enumerated(EnumType.STRING)
    private Gender gender;

    @Field(type = FieldType.Keyword)
    private String firstName;

    @Field(type = FieldType.Text)
    private List<String> jobExperiences;

    @Field(type = FieldType.Keyword)
    private String lastName;

    @Field(type = FieldType.Keyword)
    private Integer minEnglishSpokenLevel;

    @Field(type = FieldType.Keyword)
    private Integer minEnglishWrittenLevel;

    @Field(type = FieldType.Keyword)
    private Long updated;

    @Field(type = FieldType.Keyword)
    private String phone;

    @Field(type = FieldType.Keyword)
    @Enumerated(EnumType.STRING)
    private UnhcrStatus unhcrStatus;

    @Field(type = FieldType.Keyword)
    @Enumerated(EnumType.STRING)
    private MaritalStatus maritalStatus;

    @Field(type = FieldType.Keyword)
    @Enumerated(EnumType.STRING)
    private DrivingLicenseStatus drivingLicense;

    /**
     * Id of matching Candidate record in database
     */
    private Long masterId;

    @Field(type = FieldType.Keyword)
    private String nationality;

    @Field(type = FieldType.Text)
    private List<String> occupations;

    @Field(type = FieldType.Text)
    private List<String> skills;

    @Field(type = FieldType.Keyword)
    @Enumerated(EnumType.STRING)
    private CandidateStatus status;

    public CandidateEs() {
    }

    public CandidateEs(Candidate candidate) {
        this();
        copy(candidate);
    }
    
    public void copy(Candidate candidate) {

        this.additionalInfo = candidate.getAdditionalInfo();
        this.firstName = candidate.getUser() == null ? null 
                : candidate.getUser().getFirstName();
        this.gender = candidate.getGender();
        this.country = candidate.getCountry() == null ? null 
                : candidate.getCountry().getName();
        this.lastName = candidate.getUser() == null ? null 
                : candidate.getUser().getLastName();
        this.masterId = candidate.getId();
        this.updated = candidate.getUpdatedDate().toInstant().toEpochMilli();
        this.nationality = candidate.getNationality() == null ? null
                : candidate.getNationality().getName();
        this.status = candidate.getStatus();

        this.phone = candidate.getPhone();
        this.unhcrStatus = candidate.getUnhcrStatus();
        this.maritalStatus = candidate.getMaritalStatus();
        this.drivingLicense = candidate.getDrivingLicense();

        this.minEnglishSpokenLevel = null;
        this.minEnglishWrittenLevel = null;
        List<CandidateLanguage> proficiencies = candidate.getCandidateLanguages();
        if (proficiencies != null) {
            for (CandidateLanguage proficiency : proficiencies) {
                //Protect against bad data
                if (proficiency.getLanguage() != null) {
                    if ("english".equals(proficiency.getLanguage().getName().toLowerCase())) {
                        if (proficiency.getSpokenLevel() != null) {
                            this.minEnglishSpokenLevel = proficiency.getSpokenLevel().getLevel();
                        }
                        if (proficiency.getWrittenLevel() != null) {
                            this.minEnglishWrittenLevel = proficiency.getWrittenLevel().getLevel();
                        }
                        break;
                    }
                }
            }
        }
        
        this.certifications = new ArrayList<>();
        List<CandidateCertification> certifications = candidate.getCandidateCertifications();
        if (certifications != null) {
            for (CandidateCertification certification : certifications) {
                final String text = certification.getName();
                if (text != null) {
                    this.certifications.add(text);
                }
            }
        }

        this.cvs = new ArrayList<>();
        List<CandidateAttachment> candidateAttachments = candidate.getCandidateAttachments();
        if (candidateAttachments != null) {
            for (CandidateAttachment attachment : candidateAttachments) {
                final String textExtract = attachment.getTextExtract();
                if (textExtract != null) {
                    this.cvs.add(textExtract);
                }
            }
        }

        this.educations = new ArrayList<>();
        List<CandidateEducation> educations = candidate.getCandidateEducations();
        if (educations != null) {
            for (CandidateEducation education : educations) {
                final String text = education.getCourseName();
                if (text != null) {
                    this.educations.add(text);
                }
            }
        }
        
        this.jobExperiences = new ArrayList<>();
        List<CandidateJobExperience> jobs = candidate.getCandidateJobExperiences();
        if (jobs != null) {
            for (CandidateJobExperience job : jobs) {
                String role = job.getRole();
                String description = job.getDescription();

                String text = null;
                if (role == null) {
                    if (description != null) {
                        text = description;
                    }
                } else {
                    text = role;
                    if (description != null) {
                        text += " " + description;
                    }
                }
                
                if (text != null) {
                    this.jobExperiences.add(text);
                }
            }
        }
        
        this.occupations = new ArrayList<>();
        List<CandidateOccupation> occupations = candidate.getCandidateOccupations();
        if (occupations != null) {
            for (CandidateOccupation occupation : occupations) {
                if (occupation != null && occupation.getOccupation() != null) {
                    String text = occupation.getOccupation().getName();
                    if (text != null) {
                        this.occupations.add(text);
                    }
                }
            }
        }

        this.skills = new ArrayList<>();
        List<CandidateSkill> skills = candidate.getCandidateSkills();
        if (skills != null) {
            for (CandidateSkill skill : skills) {
                if (skill != null && skill.getSkill() != null) {
                    this.skills.add(skill.getSkill());
                }
            }
        }
    }

    /**
     * Elasticsearch only supports a subset of sort fields. This method
     * takes a standard PagedSearchRequest and returns a PageRequest which will
     * only containing a sort field if is supported by Elasticsearch
     * (as defined in {@link this#sortingFields})
     * @param request Incoming request which may contain a sort field.
     * @return PageRequest with modified sort fields suitable for Elasticsearch
     */
    public static PageRequest convertToElasticSortField(
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
                //This logic assumes that sorting field, apart from masterId 
                //and updated, is assumed to be a keyword field.
                //This will need to change if we add other sorting fields 
                //that are not keyword fields (eg numeric fields).
                boolean keywordField = 
                        !sortField.equals("masterId") &&
                        !sortField.equals("updated");
                
                String esFieldSpec = sortField;
                if (keywordField) {
                    //Keyword fields can be stored in ES as both text and 
                    //keyword types. For sorting purposes, we need to explicitly
                    //specify "keyword" otherwise it will try and sort by
                    //the text version of the field which will result in an
                    //error.
                    esFieldSpec += ".keyword";
                }
                requestAdj = PageRequest.of(
                        request.getPageNumber(), request.getPageSize(),
                        request.getSortDirection(), esFieldSpec
                );
            }
        } else {
            requestAdj = PageRequest.of(
                    request.getPageNumber(), request.getPageSize());
        }
        return requestAdj;
    }
}
