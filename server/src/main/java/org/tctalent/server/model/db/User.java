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
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

@Getter
@Setter
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
     * The issuer of the user's identity, as provided by the identity provider (IDP).
     */
    private String idpIssuer;

    /**
     * The subject (unique identifier in the IDP) of the user, as provided by the identity provider
     * (IDP).
     */
    private String idpSubject;

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
     * The email verification token sent to the user for verifying their email address.
     */
    @Column(name = "email_verification_token")
    private String emailVerificationToken;

    /**
     * The timestamp when the email verification token was issued.
     */
    @Column(name = "email_verification_token_issued_time")
    private OffsetDateTime emailVerificationTokenIssuedDate;

    /**
     * Indicates whether the user's email address has been verified.
     */
    @Column(name = "email_verified" , nullable = false)
    private boolean emailVerified;

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

    public void setFirstName(String firstName) {
        this.firstName = firstName == null ? null : firstName.trim();
    }

    public void setLastName(String lastName) {
        this.lastName = lastName == null ? null : lastName.trim();
    }

    public boolean getReadOnly() { return readOnly; }

    public Boolean getEmailVerified() {
        return emailVerified;
    }

    public void setEmailVerified(Boolean emailVerified) {
        this.emailVerified = emailVerified;
    }

    public boolean getUsingMfa() {
        return usingMfa;
    }

    public boolean getMfaConfigured() {
        return mfaSecret != null;
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
