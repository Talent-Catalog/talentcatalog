package org.tbbtalent.server.model;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "candidate")
@SequenceGenerator(name = "seq_gen", sequenceName = "candidate_id_seq", allocationSize = 1)
public class Candidate extends AbstractAuditableDomainObject<Long> {

    private String candidateNumber;
    private String phone;
    private String whatsapp;
    @Enumerated(EnumType.STRING)
    private Gender gender;
    private LocalDate dob;
    private String address1;
    private String city;
    private Integer yearOfArrival;
    private Boolean unRegistered;
    private String unRegistrationNumber;
    private String additionalInfo;

    @Enumerated(EnumType.STRING)
    private CandidateStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "max_education_level_id")
    private EducationLevel maxEducationLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nationality_id")
    private Nationality nationality;

    // TODO: need to fetch to manyToOne to allow lazy-fetching
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    private Set<CandidateOccupation> candidateOccupations = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    private Set<CandidateEducation> candidateEducation = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    private Set<CandidateLanguage> candidateLanguages = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    private Set<CandidateJobExperience> candidateJobExperiences = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    private Set<CandidateCertification> candidateCertifications = new HashSet<>();

    public Candidate() {
    }

    public Candidate(User user, String phone, String whatsapp, User caller) {
        super(caller);
        this.user = user;
        this.phone = phone;
        this.whatsapp = whatsapp;
        this.status = CandidateStatus.active;
    }

    public String getCandidateNumber() {
        return candidateNumber;
    }

    public void setCandidateNumber(String candidateNumber) {
        this.candidateNumber = candidateNumber;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(String whatsapp) {
        this.whatsapp = whatsapp;
    }

    public Gender getGender() {
        return gender;
    }

    public void setGender(Gender gender) {
        this.gender = gender;
    }

    public LocalDate getDob() {
        return dob;
    }

    public void setDob(LocalDate dob) {
        this.dob = dob;
    }

    public String getAddress1() {
        return address1;
    }

    public void setAddress1(String address1) {
        this.address1 = address1;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public Integer getYearOfArrival() {
        return yearOfArrival;
    }

    public void setYearOfArrival(Integer yearOfArrival) {
        this.yearOfArrival = yearOfArrival;
    }

    public Boolean getUnRegistered() {
        return unRegistered;
    }

    public void setUnRegistered(Boolean unRegistered) {
        this.unRegistered = unRegistered;
    }

    public String getUnRegistrationNumber() {
        return unRegistrationNumber;
    }

    public void setUnRegistrationNumber(String unRegistrationNumber) {
        this.unRegistrationNumber = unRegistrationNumber;
    }

    public EducationLevel getMaxEducationLevel() {
        return maxEducationLevel;
    }

    public void setMaxEducationLevel(EducationLevel maxEducationLevel) {
        this.maxEducationLevel = maxEducationLevel;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public void setAdditionalInfo(String additionalInfo) {
        this.additionalInfo = additionalInfo;
    }

    public CandidateStatus getStatus() {
        return status;
    }

    public void setStatus(CandidateStatus status) {
        this.status = status;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
    }

    public Nationality getNationality() {
        return nationality;
    }

    public void setNationality(Nationality nationality) {
        this.nationality = nationality;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<CandidateOccupation> getCandidateOccupations() {
        return candidateOccupations;
    }

    public void setCandidateOccupations(Set<CandidateOccupation> candidateOccupations) {
        this.candidateOccupations = candidateOccupations;
    }

    public Set<CandidateEducation> getCandidateEducation() {
        return candidateEducation;
    }

    public void setCandidateEducation(Set<CandidateEducation> candidateEducation) {
        this.candidateEducation = candidateEducation;
    }

    public Set<CandidateLanguage> getCandidateLanguages() {
        return candidateLanguages;
    }

    public void setCandidateLanguages(Set<CandidateLanguage> candidateLanguages) {
        this.candidateLanguages = candidateLanguages;
    }

    public Set<CandidateJobExperience> getCandidateJobExperiences() {
        return candidateJobExperiences;
    }

    public void setCandidateJobExperiences(Set<CandidateJobExperience> candidateJobExperiences) {
        this.candidateJobExperiences = candidateJobExperiences;
    }

    public Set<CandidateCertification> getCandidateCertifications() {
        return candidateCertifications;
    }

    public void setCandidateCertifications(Set<CandidateCertification> candidateCertifications) {
        this.candidateCertifications = candidateCertifications;
    }
}
