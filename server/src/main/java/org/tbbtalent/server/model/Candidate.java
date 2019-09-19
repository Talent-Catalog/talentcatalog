package org.tbbtalent.server.model;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "candidate")
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "candidate_gen")
    @SequenceGenerator(name = "candidate_gen", sequenceName = "candidate_id_seq", allocationSize = 1)
    private Long id;

    private String candidateNumber;
    private String firstName;
    private String lastName;
    private String email;
    private String phone;
    private String whatsapp;
    private String passwordEnc;
    private String gender;
    private String dob;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "country_id")
    private Country country;
    private String city;
    private Integer yearOfArrival;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nationality_id")
    private Nationality nationality;
    @Column(name = "registered_with_un")
    private Boolean registeredWithUN;
    private String registrationId;

    private String educationLevel;
    private String additionalInfo;

    @Enumerated(EnumType.STRING)
    private Status status;

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    private Set<Profession> professions = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    private Set<Education> educations = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    private Set<CandidateLanguage> candidateLanguages = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    private Set<WorkExperience> workExperiences = new HashSet<>();

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "candidate", cascade = CascadeType.MERGE)
    private Set<Certification> certifications = new HashSet<>();

    public Candidate() {
    }

    public Candidate(String firstName, String lastName, String email, String phone, String whatsapp) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.phone = phone;
        this.whatsapp = whatsapp;
        this.status = Status.active;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCandidateNumber() {
        return candidateNumber;
    }

    public void setCandidateNumber(String candidateNumber) {
        this.candidateNumber = candidateNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getDisplayName() {
        return StringUtils.join(this.firstName, " ", this.lastName);
    }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

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

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getDob() {
        return dob;
    }

    public void setDob(String dob) {
        this.dob = dob;
    }

    public Country getCountry() {
        return country;
    }

    public void setCountry(Country country) {
        this.country = country;
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

    public Nationality getNationality() { return nationality; }

    public void setNationality(Nationality nationality) { this.nationality = nationality; }

    public Boolean getRegisteredWithUN() { return registeredWithUN; }

    public void setRegisteredWithUN(Boolean registeredWithUN) { this.registeredWithUN = registeredWithUN; }

    public String getRegistrationId() { return registrationId; }

    public void setRegistrationId(String registrationId) {
        this.registrationId = registrationId;
    }

    public String getEducationLevel() { return educationLevel; }

    public void setEducationLevel(String educationLevel) { this.educationLevel = educationLevel; }

    public String getAdditionalInfo() { return additionalInfo; }

    public void setAdditionalInfo(String additionalInfo) { this.additionalInfo = additionalInfo; }

    public String getPasswordEnc() {
        return passwordEnc;
    }

    public void setPasswordEnc(String passwordEnc) {
        this.passwordEnc = passwordEnc;
    }

    public String getUsername() {
        if (StringUtils.isNotBlank(email)) {
            return email;
        } else if (StringUtils.isNotBlank(phone)) {
            return phone;
        } else if (StringUtils.isNotBlank(whatsapp)) {
            return whatsapp;
        }
        return null;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public Set<Profession> getProfessions() {
        return professions;
    }

    public void setProfessions(Set<Profession> professions) {
        this.professions = professions;
    }

    public Set<Education> getEducations() {
        return educations;
    }

    public void setEducations(Set<Education> educations) {
        this.educations = educations;
    }

    public Set<CandidateLanguage> getCandidateLanguages() {
        return candidateLanguages;
    }

    public void setCandidateLanguages(Set<CandidateLanguage> candidateLanguages) {
        this.candidateLanguages = candidateLanguages;
    }

    public Set<WorkExperience> getWorkExperiences() { return workExperiences; }

    public void setWorkExperiences(Set<WorkExperience> workExperiences) { this.workExperiences = workExperiences; }

    public Set<Certification> getCertifications() {
        return certifications;
    }

    public void setCertifications(Set<Certification> certifications) { this.certifications = certifications; }


}
