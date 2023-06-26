/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.model.es;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.CandidateAttachment;
import org.tbbtalent.server.model.db.CandidateCertification;
import org.tbbtalent.server.model.db.CandidateEducation;
import org.tbbtalent.server.model.db.CandidateJobExperience;
import org.tbbtalent.server.model.db.CandidateLanguage;
import org.tbbtalent.server.model.db.CandidateOccupation;
import org.tbbtalent.server.model.db.CandidateSkill;
import org.tbbtalent.server.model.db.CandidateStatus;
import org.tbbtalent.server.model.db.DocumentStatus;
import org.tbbtalent.server.model.db.Gender;
import org.tbbtalent.server.model.db.MaritalStatus;
import org.tbbtalent.server.model.db.ResidenceStatus;
import org.tbbtalent.server.model.db.UnhcrStatus;
import org.tbbtalent.server.request.PagedSearchRequest;
import org.tbbtalent.server.util.html.TextExtracter;

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
            "residenceStatus",
            "numberDependants",
    };

    @Id
    private String id;

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

    @Field(type = FieldType.Text)
    private List<String> educations;

    @Enumerated(EnumType.STRING)
    private Gender gender;

    private String firstName;

    @Field(type = FieldType.Text)
    private List<String> jobExperiences;

    private String lastName;

    private Integer minEnglishSpokenLevel;

    private Integer minEnglishWrittenLevel;

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

    @Field(type = FieldType.Text)
    private List<String> occupations;

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
    private Long numberDependants;

    public CandidateEs() {
    }

    public void copy(Candidate candidate, TextExtracter textExtracter) {

        this.additionalInfo = candidate.getAdditionalInfo();
        this.firstName = candidate.getUser() == null ? null
                : candidate.getUser().getFirstName();
        this.gender = candidate.getGender();
        this.country = candidate.getCountry() == null ? null
                : candidate.getCountry().getName();
        this.state = candidate.getState();
        this.city = candidate.getCity();
        this.lastName = candidate.getUser() == null ? null
                : candidate.getUser().getLastName();
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
        this.numberDependants = candidate.getNumberDependants();

        this.maxEducationLevel = null;
        if (candidate.getMaxEducationLevel() != null) {
            this.maxEducationLevel = candidate.getMaxEducationLevel().getLevel();
        }

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
        List<CandidateOccupation> occupations = candidate.getCandidateOccupations();
        if (occupations != null) {
            for (CandidateOccupation occupation : occupations) {
                if (occupation != null && occupation.getOccupation() != null) {
                    String text = occupation.getOccupation().getName();
                    if (text != null) {
                        this.occupations.add(text);
                    }
                    if (occupation.getMigrationOccupation() != null) {
                        this.migrationOccupation = occupation.getMigrationOccupation();
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
                String[] nonKeywordFields = {"masterId", "updated", "maxEducationLevel", "ieltsScore", "numberDependants"};

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
