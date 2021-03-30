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

import dev.samstevens.totp.secret.DefaultSecretGenerator;
import dev.samstevens.totp.secret.SecretGenerator;
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
import javax.persistence.OneToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Transient;
import org.apache.commons.lang3.StringUtils;

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
    Set<Country> sourceCountries = new HashSet<Country>();

    public User() {
    }

    public User(String username, String firstName, String lastName, String email, Role role) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.role = role;
        this.status = Status.active;
        SecretGenerator secretGenerator = new DefaultSecretGenerator();
        this.mfaSecret = secretGenerator.generate();
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
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
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
