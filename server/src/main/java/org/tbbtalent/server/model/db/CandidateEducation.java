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

package org.tbbtalent.server.model.db;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "candidate_education")
@SequenceGenerator(name = "seq_gen", sequenceName = "candidate_education_id_seq", allocationSize = 1)
public class CandidateEducation extends AbstractDomainObject<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @Enumerated(EnumType.STRING)
    private EducationType educationType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "major_id")
    private EducationMajor educationMajor;

    private Integer lengthOfCourseYears;
    private String institution;
    private String courseName;

    private Integer yearCompleted;

    private Boolean incomplete;

    public CandidateEducation() {
    }

    public CandidateEducation(Candidate candidate, EducationType educationType, Country country, EducationMajor educationMajor, Integer lengthOfCourseYears, String institution, String courseName, Integer yearCompleted, Boolean incomplete) {
        this.candidate = candidate;
        this.educationType = educationType;
        this.country = country;
        this.educationMajor = educationMajor;
        this.lengthOfCourseYears = lengthOfCourseYears;
        this.institution = institution;
        this.courseName = courseName;
        this.yearCompleted = yearCompleted;
        this.incomplete = incomplete;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public EducationType getEducationType() { return educationType; }

    public void setEducationType(EducationType educationType) { this.educationType = educationType; }

    public Country getCountry() { return country; }

    public void setCountry(Country country) {
        this.country = country;
    }

    public Integer getLengthOfCourseYears() {
        return lengthOfCourseYears;
    }

    public void setLengthOfCourseYears(Integer lengthOfCourseYears) {
        this.lengthOfCourseYears = lengthOfCourseYears;
    }

    public String getInstitution() {
        return institution;
    }

    public void setInstitution(String institution) {
        this.institution = institution;
    }

    public String getCourseName() {
        return courseName;
    }

    public void setCourseName(String courseName) {
        this.courseName = courseName;
    }

    public Integer getYearCompleted() {
        return yearCompleted;
    }

    public void setYearCompleted(Integer yearCompleted) {
        this.yearCompleted = yearCompleted;
    }

    public EducationMajor getEducationMajor() {
        return educationMajor;
    }

    public void setEducationMajor(EducationMajor educationMajor) {
        this.educationMajor = educationMajor;
    }

    public Boolean getIncomplete() {
        return incomplete;
    }

    public void setIncomplete(Boolean incomplete) {
        this.incomplete = incomplete;
    }
}
