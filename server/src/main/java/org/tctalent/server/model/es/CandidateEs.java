/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.model.es;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.lang.Nullable;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateAttachment;
import org.tctalent.server.model.db.CandidateCertification;
import org.tctalent.server.model.db.CandidateEducation;
import org.tctalent.server.model.db.CandidateJobExperience;
import org.tctalent.server.model.db.CandidateLanguage;
import org.tctalent.server.model.db.CandidateOccupation;
import org.tctalent.server.model.db.CandidateSkill;
import org.tctalent.server.model.db.CandidateStatus;
import org.tctalent.server.model.db.DocumentStatus;
import org.tctalent.server.model.db.EducationMajor;
import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.MaritalStatus;
import org.tctalent.server.model.db.ResidenceStatus;
import org.tctalent.server.model.db.UnhcrStatus;
import org.tctalent.server.request.PagedSearchRequest;
import org.tctalent.server.util.html.TextExtracter;

/**
 * This defines the fields which are stored in Elasticsearch "documents"
 * corresponding to these Candidate "entities".
 * All these candidate records comprise the "candidates" index - roughly
 * comparable to a candidate table in a traditional database.
 */
@Getter
@Setter
@Document(indexName = "candidates", createIndex = false)
public class CandidateEs {

    public static final String INDEX_NAME = "candidates";

    private static final String[] sortingFields = {
            "masterId",
            "country",
            "regoReferrerParam",
            "state",
            "city",
            "gender",
            "firstName",
            "lastName",
            "nationality",
            "partner",
            "status",
            "updated",
            "phone",
            "unhcrStatus",
            "maritalStatus",
            "drivingLicense",
            "dob",
            "maxEducationLevel",
            "ieltsScore",
            "englishAssessmentScoreDet",
            "residenceStatus",
            "numberDependants",
    };

    @Id
    private String id;
    private String externalId;
    private String candidateNumber;

    @Field(type = FieldType.Text)
    private String additionalInfo;

    @Field(type = FieldType.Text)
    private List<String> certifications;

    /*
       NOTE: Do NOT add @Field(type = FieldType.Keyword) - that messes up the new automatic
       defaulting of Strings to be both text AND keyword which can cause problems with our
       queries.
       See https://www.elastic.co/blog/strings-are-dead-long-live-strings
       The mess up happens in the Spring Data Elasticsearch support which on connection
       (in ElasticsearchConfiguration.java) will force the mapping strictly according to any
       @Field annotation - which means that Elasticsearch's own clever defaulting never happens.
       - JC
     */
    private String country;

    private String state;

    private String city;

    @Field(type = FieldType.Text)
    private List<String> cvs;

    /**
     * This is populated (in {@link #copy(Candidate, TextExtracter)}) with the course names of the
     * candidate's various {@link CandidateEducation}s which is just free text because course names
     * can be anything.
     */
    @Field(type = FieldType.Text)
    private List<String> educations;

    private List<String> educationMajors;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String firstName;
    private String lastName;

    /**
     * Populated automatically from the {@code firstName} and {@code lastName} fields, in the
     * format: "firstName lastName". Allows for simpler querying where full name searches are
     * required.
     */
    private String fullName;

    private String email;

    @Field(type = FieldType.Text)
    private List<String> jobExperiences;

    private Integer minEnglishSpokenLevel;

    private Integer minEnglishWrittenLevel;

    @Field(type = FieldType.Nested)
    private List<Language> otherLanguages;

    @Getter
    @Setter
    static class Language {
        private String name;

        @Field(type = FieldType.Integer)
        private Integer minSpokenLevel;

        @Field(type = FieldType.Integer)
        private Integer minWrittenLevel;
    }

    private String regoReferrerParam;

    private Long updated;

    private String phone;

    @Enumerated(EnumType.STRING)
    private UnhcrStatus unhcrStatus;

    @Enumerated(EnumType.STRING)
    private MaritalStatus maritalStatus;

    @Enumerated(EnumType.STRING)
    private DocumentStatus drivingLicense;

    @Field(type = FieldType.Date, format = DateFormat.basic_date)
    private LocalDate dob;

    /**
     * Id of matching Candidate record in database
     */
    private Long masterId;

    private String nationality;

    private String partner;

    @Field(type = FieldType.Nested)
    private List<Occupation> occupations;

    @Getter
    @Setter
    public static class Occupation {
        private String name;

        @Field(type = FieldType.Long)
        private Long yearsExperience;
    }

    @Field(type = FieldType.Text)
    private String migrationOccupation;

    @Field(type = FieldType.Text)
    private List<String> skills;

    @Enumerated(EnumType.STRING)
    private CandidateStatus status;

    private Integer maxEducationLevel;

    @Enumerated(EnumType.STRING)
    private ResidenceStatus residenceStatus;

    @Field(type = FieldType.Double)
    private BigDecimal ieltsScore;

    @Field(type = FieldType.Long)
    private Long englishAssessmentScoreDet;

    @Field(type = FieldType.Long)
    private Long numberDependants;

    private Long fullIntakeCompletedDate;

    private Long miniIntakeCompletedDate;

    private Long surveyType;

    public CandidateEs() {
    }

    public void setFullName() {
        this.fullName = this.firstName + " " + this.lastName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
        setFullName();  // Update fullName whenever firstName changes
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
        setFullName();  // Update fullName whenever lastName changes
    }

    public void copy(Candidate candidate, TextExtracter textExtracter) {

        this.additionalInfo = candidate.getAdditionalInfo();
        this.firstName = candidate.getUser() == null ? null
            : candidate.getUser().getFirstName();
        this.lastName = candidate.getUser() == null ? null
            : candidate.getUser().getLastName();
        this.setFullName();
        this.email = candidate.getUser() == null ? null
            : candidate.getUser().getEmail();
        this.externalId = candidate.getExternalId();
        this.candidateNumber = candidate.getCandidateNumber();

        this.miniIntakeCompletedDate = candidate.getMiniIntakeCompletedDate() == null ?
            null : candidate.getMiniIntakeCompletedDate().toInstant().toEpochMilli();
        this.fullIntakeCompletedDate = candidate.getFullIntakeCompletedDate() == null ?
            null : candidate.getFullIntakeCompletedDate().toInstant().toEpochMilli();
        this.surveyType = candidate.getSurveyType() == null ?
            null : candidate.getSurveyType().getId();

        this.gender = candidate.getGender();
        this.country = candidate.getCountry() == null ? null
                : candidate.getCountry().getName();
        this.state = candidate.getState();
        this.city = candidate.getCity();
        this.masterId = candidate.getId();
        this.updated = candidate.getUpdatedDate().toInstant().toEpochMilli();
        this.nationality = candidate.getNationality() == null ? null
                : candidate.getNationality().getName();
        this.partner = candidate.getUser() == null ? null
                : candidate.getUser().getPartner().getAbbreviation();
        this.regoReferrerParam = candidate.getRegoReferrerParam();
        this.status = candidate.getStatus();

        this.phone = candidate.getPhone();
        this.unhcrStatus = candidate.getUnhcrStatus();
        this.maritalStatus = candidate.getMaritalStatus();
        this.drivingLicense = candidate.getDrivingLicense();
        this.dob = candidate.getDob();
        this.residenceStatus = candidate.getResidenceStatus();
        this.ieltsScore = candidate.getIeltsScore();
        this.englishAssessmentScoreDet = candidate.getEnglishAssessmentScoreDet();
        this.numberDependants = candidate.getNumberDependants();


        this.maxEducationLevel = null;
        if (candidate.getMaxEducationLevel() != null) {
            this.maxEducationLevel = candidate.getMaxEducationLevel().getLevel();
        }

        this.minEnglishSpokenLevel = null;
        this.minEnglishWrittenLevel = null;
        this.otherLanguages = new ArrayList<>();
        List<CandidateLanguage> proficiencies = candidate.getCandidateLanguages();
        if (proficiencies != null) {
            for (CandidateLanguage proficiency : proficiencies) {
                //Protect against bad data
                if (proficiency.getLanguage() != null) {
                    final String languageName = proficiency.getLanguage().getName();
                    if ("english".equalsIgnoreCase(languageName)) {
                        if (proficiency.getSpokenLevel() != null) {
                            this.minEnglishSpokenLevel = proficiency.getSpokenLevel().getLevel();
                        }
                        if (proficiency.getWrittenLevel() != null) {
                            this.minEnglishWrittenLevel = proficiency.getWrittenLevel().getLevel();
                        }
                    } else if (StringUtils.isNotBlank(languageName)) {
                        Language language = new Language();
                        language.setName(languageName);

                        if (proficiency.getSpokenLevel() != null) {
                            language.setMinSpokenLevel(proficiency.getSpokenLevel().getLevel());
                        }
                        if (proficiency.getWrittenLevel() != null) {
                            language.setMinWrittenLevel(proficiency.getWrittenLevel().getLevel());
                        }
                        this.otherLanguages.add(language);
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

        //Copy education related data
        List<CandidateEducation> candidateEducations = candidate.getCandidateEducations();

        //Course names are extracted as searchable text
        this.educations = new ArrayList<>();

        //Education majors are extracted as keywords that can be used as search filters
        this.educationMajors = new ArrayList<>();

        if (candidateEducations != null) {
            for (CandidateEducation education : candidateEducations) {
                final String courseName = education.getCourseName();
                if (courseName != null) {
                    this.educations.add(courseName);
                }

                addEducationMajor(education.getEducationMajor());
            }
        }

        //Education major can also come from the candidate's special migrationEducationMajor field
        addEducationMajor(candidate.getMigrationEducationMajor());

        this.jobExperiences = new ArrayList<>();
        List<CandidateJobExperience> jobs = candidate.getCandidateJobExperiences();
        if (jobs != null) {
            for (CandidateJobExperience job : jobs) {
                String role = job.getRole();
                String description = job.getDescription();

                String text = null;
                if (role == null) {
                    if (description != null) {
                        text = textExtracter.ExtractText(description);
                    }
                } else {
                    text = role;
                    if (description != null) {
                        text += " " + textExtracter.ExtractText(description);
                    }
                }

                if (text != null) {
                    this.jobExperiences.add(text);
                }
            }
        }

        this.occupations = new ArrayList<>();
        this.migrationOccupation = null;
        List<CandidateOccupation> candidateOccupations = candidate.getCandidateOccupations();
        if (candidateOccupations != null) {
            for (CandidateOccupation candidateOccupation : candidateOccupations) {
                if (candidateOccupation != null && candidateOccupation.getOccupation() != null) {
                    Occupation occupation = new Occupation();
                    String name = candidateOccupation.getOccupation().getName();
                    Long yearsExperience = candidateOccupation.getYearsExperience();
                    if (name != null) {
                        occupation.setName(name);
                        if (yearsExperience != null) {
                            occupation.setYearsExperience(yearsExperience);
                        }
                        occupations.add(occupation);
                    }

                    if (candidateOccupation.getMigrationOccupation() != null) {
                        this.migrationOccupation = candidateOccupation.getMigrationOccupation();
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

    private void addEducationMajor(@Nullable EducationMajor educationMajor) {
        if (educationMajor != null) {
            String majorName = educationMajor.getName();
            if (majorName != null) {
                this.educationMajors.add(majorName);
            }
        }
    }

    /**
     * Elasticsearch only supports a subset of sort fields. This method
     * takes a standard PagedSearchRequest and returns a PageRequest which will
     * only contain a sort field if is supported by Elasticsearch
     * (as defined in {@link this#sortingFields})
     * @param request Incoming request which may contain a sort field.
     * @return PageRequest with modified sort fields suitable for Elasticsearch
     */
    public static PageRequest convertToElasticSortField(
            PagedSearchRequest request) {
        PageRequest requestAdj;

        int pageNumber = request.getPageNumber() == null ? 0 : request.getPageNumber();
        int pageSize = request.getPageSize() == null ? 25 : request.getPageSize();

        String[] sortFields = request.getSortFields();

        if (sortFields != null && sortFields.length > 0) {
            String sortField = sortFields[0];

            //Special hack for id field - which is masterId in CandidateEs.
            //Sort by candidate's id even though displayed as candidate number on front end.
            //Candidate Number is a text field so can't be sorted in a sensible numeric way.
            //Thankfully the id and CN increment the same, so still displays in order.
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
                requestAdj = PageRequest.of(pageNumber, pageSize);
            } else {
                //todo extract this logic into a method that Candidate Service Impl can also call.
                //This logic assumes that sorting field, apart from masterId
                //and updated, is assumed to be a keyword field.
                //This will need to change if we add other sorting fields
                //that are not keyword fields (eg numeric fields).
                String[] nonKeywordFields = {"masterId", "updated", "maxEducationLevel","englishAssessmentScoreDet", "ieltsScore",
                    "numberDependants", "dob"};

                boolean keywordField = Arrays.stream(nonKeywordFields).noneMatch(sortField::equals);

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
                        pageNumber, pageSize,
                        (request.getSortDirection() == Sort.Direction.ASC ?
                                Sort.by(esFieldSpec).ascending() :
                                Sort.by(esFieldSpec).descending())
                        .and(Sort.by("masterId").descending())

                );
            }
        } else {
            requestAdj = PageRequest.of(pageNumber, pageSize);
        }
        return requestAdj;
    }
}
