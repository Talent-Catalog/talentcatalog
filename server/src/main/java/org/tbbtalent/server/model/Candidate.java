package org.tbbtalent.server.model;

import org.apache.commons.lang3.StringUtils;

import javax.persistence.*;

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
    @Enumerated(EnumType.STRING)
    private Status status;

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
}
