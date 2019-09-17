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
    private String country;
    private String city;
    private String yearOfArrival;

    private String nationality;
    @Column(name = "registered_with_un")
    private String registeredWithUN;
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
    private Set<Language> languages = new HashSet<>();

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

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getYearOfArrival() {
        return yearOfArrival;
    }

    public void setYearOfArrival(String yearOfArrival) {
        this.yearOfArrival = yearOfArrival;
    }

    public String getNationality() { return nationality; }

    public void setNationality(String nationality) { this.nationality = nationality; }

    public String getRegisteredWithUN() { return registeredWithUN; }

    public void setRegisteredWithUN(String registeredWithUN) { this.registeredWithUN = registeredWithUN; }

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

    public Set<Language> getLanguages() {
        return languages;
    }

    public void setLanguages(Set<Language> languages) {
        this.languages = languages;
    }
}
