package org.tbbtalent.server.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

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
    private String candidateMessage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "survey_type_id")
    private SurveyType surveyType;

    private String surveyComment;

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

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private OffsetDateTime registeredDate;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "registered_by")
    private User registered_by;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    private List<CandidateOccupation> candidateOccupations;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    private List<CandidateEducation> candidateEducations;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    private List<CandidateLanguage> candidateLanguages;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    private List<CandidateJobExperience> candidateJobExperiences;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    private List<CandidateCertification> candidateCertifications;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    private List<CandidateShortlistItem> candidateShortlistItems;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    private List<CandidateSkill> candidateSkills;

    //old data only links to candidate needs to be searchable
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "migration_education_major_id")
    private EducationMajor migrationEducationMajor;

    private String migrationNationality;

    private String folderlink;

    private String sflink;

    private String videolink;



    public Candidate() {
    }

    public Candidate(User user, String phone, String whatsapp, User caller) {
        super(caller);
        this.user = user;
        this.phone = phone;
        this.whatsapp = whatsapp;
        this.status = CandidateStatus.draft;
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

    public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }

    public String getCandidateMessage() {
        return candidateMessage;
    }

    public void setCandidateMessage(String candidateMessage) {
        this.candidateMessage = candidateMessage;
    }

    public SurveyType getSurveyType() { return surveyType; }

    public void setSurveyType(SurveyType surveyType) { this.surveyType = surveyType; }

    public String getSurveyComment() { return surveyComment; }

    public void setSurveyComment(String surveyComment) { this.surveyComment = surveyComment; }

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


    public OffsetDateTime getRegisteredDate() {
        return registeredDate;
    }

    public void setRegisteredDate(OffsetDateTime registeredDate) {
        this.registeredDate = registeredDate;
    }

    public User getRegistered_by() {
        return registered_by;
    }

    public void setRegistered_by(User registered_by) {
        this.registered_by = registered_by;
    }

    public List<CandidateOccupation> getCandidateOccupations() {
        return candidateOccupations;
    }

    public void setCandidateOccupations(List<CandidateOccupation> candidateOccupations) {
        this.candidateOccupations = candidateOccupations;
    }

    public List<CandidateEducation> getCandidateEducations() {
        return candidateEducations;
    }

    public void setCandidateEducations(List<CandidateEducation> candidateEducations) {
        this.candidateEducations = candidateEducations;
    }

    public List<CandidateLanguage> getCandidateLanguages() {
        return candidateLanguages;
    }

    public void setCandidateLanguages(List<CandidateLanguage> candidateLanguages) {
        this.candidateLanguages = candidateLanguages;
    }

    public List<CandidateJobExperience> getCandidateJobExperiences() {
        return candidateJobExperiences;
    }

    public void setCandidateJobExperiences(List<CandidateJobExperience> candidateJobExperiences) {
        this.candidateJobExperiences = candidateJobExperiences;
    }

    public List<CandidateCertification> getCandidateCertifications() {
        return candidateCertifications;
    }

    public void setCandidateCertifications(List<CandidateCertification> candidateCertifications) {
        this.candidateCertifications = candidateCertifications;
    }

    public List<CandidateShortlistItem> getCandidateShortlistItems() {
        return candidateShortlistItems;
    }

    public void setCandidateShortlistItems(List<CandidateShortlistItem> candidateShortlistItems) {
        this.candidateShortlistItems = candidateShortlistItems;
    }


    public EducationMajor getMigrationEducationMajor() {
        return migrationEducationMajor;
    }

    public void setMigrationEducationMajor(EducationMajor migrationEducationMajor) {
        this.migrationEducationMajor = migrationEducationMajor;
    }

    public List<CandidateSkill> getCandidateSkills() {
        return candidateSkills;
    }

    public void setCandidateSkills(List<CandidateSkill> candidateSkills) {
        this.candidateSkills = candidateSkills;
    }

    public String getMigrationCountry() {
        return migrationNationality;
    }

    public void setMigrationCountry(String migrationCountry) {
        this.migrationNationality = migrationCountry;
    }

    public String getMigrationNationality() {
        return migrationNationality;
    }

    public void setMigrationNationality(String migrationNationality) {
        this.migrationNationality = migrationNationality;
    }

    public String getFolderlink() {
        return folderlink;
    }

    public void setFolderlink(String folderlink) {
        this.folderlink = folderlink;
    }

    public String getSflink() {
        return sflink;
    }

    public void setSflink(String sflink) {
        this.sflink = sflink;
    }

    public String getVideolink() {
        return videolink;
    }

    public void setVideolink(String videolink) {
        this.videolink = videolink;
    }
}
