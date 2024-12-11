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

package org.tctalent.server.model.db;

import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import org.apache.commons.lang3.StringUtils;

@Entity
@Table(name = "users")
@SequenceGenerator(name = "seq_gen", sequenceName = "users_id_seq", allocationSize = 1)
public class User extends AbstractAuditableDomainObject<Long> {
    /**
     * username must be unique - at May '23 this is usually set up by admin as first name initial followed by surname
     */
    private String username;

    /**
     * firstName is the user's actual first name
     */
    private String firstName;

    /**
     * lastName is the user's actual surname
     */
    private String lastName;

    /**
     * email - user's email, has to be unique
     */
    private String email;

    /**
     * role is the user's level of access to the TC (system admin / full admin / source partner admin / semi limited / limited)
     */
    @Enumerated(EnumType.STRING)
    private Role role;

    /**
     * Use boolean rather than Boolean so that default value is false, not null.
     * Null is not allowed in Db definition
     * readOnly limits functionality, in May '23 this option is somewhat buggy and not advised to be used
     */
    private boolean readOnly;

    /**
     * passwordEnc - for security passwords are visible only in encrypted form to all users
     */
    private String passwordEnc;

    /**
     * status is 'active' by default — rather than delete departing users, we set them to 'inactive'
     */
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
     * usingMfa is a basic security requirement, checked by default
     * Use boolean rather than Boolean so that default value is false, not null.
     * Null is not allowed in Db definition
     */
    private boolean usingMfa;

    private String mfaSecret;

    /**
     * candidate profiles (if a user has one) are linked to a user via 'user_id'
     */
    @OneToOne(mappedBy = "user")
    private Candidate candidate;

    //Note that this has to be an EAGER fetch (ie not LAZY) because the partner has to be fully
    //loaded in order to know its type because it uses a discriminator column to determine class
    //type.
    //See https://stackoverflow.com/questions/70394739/behavior-of-hibernate-get-vs-load-with-discriminator
    /**
     * partner is the org that the user belongs to — affects functionality and access, is a class of its own
     * <p/>
     * For candidate users, partner is the source partner that the candidate is assigned to.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "partner_id")
    private PartnerImpl partner;

    /**
     * approver denotes the TC user that authorised a user's registration if required
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approver_id")
    private User approver;

    /**
     * purpose is related to approver — in instances where approval was required for a new-user registration, it's the reason given for their TC use, e.g., 'Facilitate job searches in Uganda.'
     */
    private String purpose;

    /**
     * userSavedSearch allows users to save the parameters of a candidate search
     */
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
    /**
     * userSavedList allows admin users to save lists of chosen candidates
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = CascadeType.MERGE)
    @JoinTable(
            name = "user_saved_list",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "saved_list_id")
    )
    private Set<SavedList> sharedLists = new HashSet<>();

    /**
     * selectedLanguage sets the display language of the TC admin portal — at May '23 only English is available
     */
    @Transient
    private String selectedLanguage = "en";

    /**
     * sourceCountries can be used to restrict certain users to viewing candidates in only one or more countries
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "user_source_country",
            joinColumns = @JoinColumn(name = "user_id"),
            inverseJoinColumns = @JoinColumn(name = "country_id"))
    Set<Country> sourceCountries = new HashSet<>();

    /**
     * If true, this user can create new jobs on the TC.
     */
    private boolean jobCreator;

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

    public boolean isJobCreator() {
        return jobCreator;
    }

    public void setJobCreator(boolean jobCreator) {
        this.jobCreator = jobCreator;
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
