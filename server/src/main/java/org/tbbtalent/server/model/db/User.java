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

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;

import com.sun.xml.bind.v2.TODO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.lang.Nullable;

/** TODO: TRANSFER THESE TO THEIR RESPECTIVE FIELDS
 * @param username created by the user (must be unique)
 * @param firstName actual first name
 * @param lastName actual surname
 * @param email currently not used by TC functionality (must be unique)
 * @param role type of access to the TC (system admin / full admin / source partner admin / semi limited / limited)
 * @param approver tc user who approved a new admin user's registration, where required — this will be another admin user
 * @param purpose is related to approver — in instances where approval was required for a new-user registration, it's the reason given for their TC use, e.g., 'Facilitate job searches in Uganda.'
 * @param partner is the organisation that the user belongs to — has a material effect on functionality and access, so is a class of its own
 * @param userSourceCountry designations the nation(s) whose candidates the user has access to — e.g. a user in India who only has access to candidates based in India
 * @param status new users are 'active' by default — rather than delete departing users, we make them 'inactive'
 * @param readOnly limits functionality, in May '23 this option is somewhat buggy and not advised to be used
 * @param usingMfa basic security requirement, checked by default
 */

@Entity
@Table(name = "users")
@SequenceGenerator(name = "seq_gen", sequenceName = "users_id_seq", allocationSize = 1)
public class User extends AbstractAuditableDomainObject<Long> {

    private String username;
    private String firstName;
    private String lastName;
    private String email;

    @Enumerated(EnumType.STRING)
    private Role role;

    /**
     * Use boolean rather than Boolean so that default value is false, not null.
     * Null is not allowed in Db definition
     */
    private boolean readOnly;

    private String passwordEnc;

    @Enumerated(EnumType.STRING)
    private Status status;

    private OffsetDateTime lastLogin;

    @Column
    private String resetToken;

    @Column(name = "reset_token_issued_date")
    private OffsetDateTime resetTokenIssuedDate;

    @Column(name = "password_updated_date")
    private OffsetDateTime passwordUpdatedDate;

    /**
     * Use boolean rather than Boolean so that default value is false, not null.
     * Null is not allowed in Db definition
     */
    private boolean usingMfa;

    private String mfaSecret;

    @OneToOne(mappedBy = "user")
    private Candidate candidate;

    //Note that this has to be an EAGER fetch (ie not LAZY) because the partner has to be fully
    //loaded in order to know its type because it uses a discriminator column to determine class
    //type.
    //See https://stackoverflow.com/questions/70394739/behavior-of-hibernate-get-vs-load-with-discriminator
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "partner_id")
    private PartnerImpl partner;

    /**
     * This is to fetch a user's approver if their registration required one — the approver is another admin user
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private User approver;

    private String purpose;

    //Note use of Set rather than List as strongly recommended for Many to Many
    //relationships here:
    // https://thoughts-on-java.org/best-practices-for-many-to-many-associations-with-hibernate-and-jpa/
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(
            name = "user_saved_search",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "saved_search_id")
    )
    private Set<SavedSearch> sharedSearches = new HashSet<>();

    //Note use of Set rather than List as strongly recommended for Many to Many
    //relationships here:
    // https://thoughts-on-java.org/best-practices-for-many-to-many-associations-with-hibernate-and-jpa/
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(
            name = "user_saved_list",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "saved_list_id")
    )
    private Set<SavedList> sharedLists = new HashSet<>();

    @Transient
    private String selectedLanguage = "en";

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_source_country",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "country_id"))
    Set<Country> sourceCountries = new HashSet<>();

    public User() {
    }

    public User(String username, String firstName, String lastName, String email, Role role) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.status = Status.active;
        this.setCreatedDate(OffsetDateTime.now());
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName == null ? null : firstName.trim();
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName == null ? null : lastName.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public boolean getReadOnly() { return readOnly; }

    public String getPurpose() { return purpose; }

    public void setPurpose(String purpose) { this.purpose = purpose; }

    public void setReadOnly(boolean readOnly) { this.readOnly = readOnly; }

    public String getPasswordEnc() {
        return passwordEnc;
    }

    public void setPasswordEnc(String passwordEnc) {
        this.passwordEnc = passwordEnc;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }

    public OffsetDateTime getLastLogin() {
        return lastLogin;
    }

    public void setLastLogin(OffsetDateTime lastLogin) {
        this.lastLogin = lastLogin;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public String getResetToken() {
        return resetToken;
    }

    public void setResetToken(String resetToken) {
        this.resetToken = resetToken;
    }

    public OffsetDateTime getResetTokenIssuedDate() {
        return resetTokenIssuedDate;
    }

    public void setResetTokenIssuedDate(OffsetDateTime resetTokenIssuedDate) {
        this.resetTokenIssuedDate = resetTokenIssuedDate;
    }

    public OffsetDateTime getPasswordUpdatedDate() {
        return passwordUpdatedDate;
    }

    public void setPasswordUpdatedDate(OffsetDateTime passwordUpdatedDate) {
        this.passwordUpdatedDate = passwordUpdatedDate;
    }

    public String getSelectedLanguage() {
        return selectedLanguage;
    }

    public void setSelectedLanguage(String selectedLanguage) {
        this.selectedLanguage = selectedLanguage;
    }

    public boolean getUsingMfa() {
        return usingMfa;
    }

    public void setUsingMfa(boolean usingMFA) {
        this.usingMfa = usingMFA;
    }

    public boolean getMfaConfigured() {
        return mfaSecret != null;
    }

    public String getMfaSecret() {
        return mfaSecret;
    }

    public void setMfaSecret(String totpSecret) {
        this.mfaSecret = totpSecret;
    }

    public Set<SavedList> getSharedLists() {
        return sharedLists;
    }

    public void setSharedLists(Set<SavedList> sharedLists) {
        this.sharedLists.clear();
        for (SavedList sharedList : sharedLists) {
            addSharedList(sharedList);
        }
    }

    public void addSharedList(SavedList savedList) {
        this.sharedLists.add(savedList);
        savedList.getUsers().add(this);
    }

    public void removeSharedList(SavedList savedList) {
        this.sharedLists.remove(savedList);
        savedList.getUsers().remove(this);
    }

    public Set<SavedSearch> getSharedSearches() {
        return sharedSearches;
    }

    public void setSharedSearches(Set<SavedSearch> sharedSearches) {
        this.sharedSearches.clear();
        for (SavedSearch sharedSearch : sharedSearches) {
            addSharedSearch(sharedSearch);
        }
    }

    public void addSharedSearch(SavedSearch savedSearch) {
        sharedSearches.add(savedSearch);
        savedSearch.getUsers().add(this);
    }

    public void removeSharedSearch(SavedSearch savedSearch) {
        sharedSearches.remove(savedSearch);
        savedSearch.getUsers().remove(this);
    }

    public Set<Country> getSourceCountries() { return sourceCountries; }

    public void setSourceCountries(Set<Country> sourceCountries) { this.sourceCountries = sourceCountries; }

    public PartnerImpl getPartner() {
        return partner;
    }

    public void setPartner(PartnerImpl partner) {
        this.partner = partner;
    }

    public User getApprover() {
        return approver;
    }

    public void setApprover(User approver) {
        this.approver = approver;
    }

    @Transient
    public String getDisplayName() {
        String displayName = "";
        if (StringUtils.isNotBlank(firstName) && StringUtils.isNotBlank(lastName)) {
            displayName = firstName + " " + lastName;
        } else if (StringUtils.isNotBlank(firstName)) {
            displayName = firstName;
        } else if (StringUtils.isNotBlank(lastName)) {
            displayName = lastName;
        }
        return displayName;
    }
}
