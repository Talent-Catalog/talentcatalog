package org.tbbtalent.server.model;

import org.apache.commons.lang3.StringUtils;

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
import java.time.OffsetDateTime;
import java.util.HashSet;
import java.util.Set;

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

    private Boolean readOnly;
    
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

    public Boolean getReadOnly() { return readOnly; }

    public void setReadOnly(Boolean readOnly) { this.readOnly = readOnly; }

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

    public Set<SavedSearch> getSharedSearches() {
        return sharedSearches;
    }

    public void setSharedSearches(Set<SavedSearch> sharedSearches) {
        this.sharedSearches = sharedSearches;
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

    public void addSharedSearch(SavedSearch savedSearch) {
        sharedSearches.add(savedSearch);
        savedSearch.getUsers().add(this);
    }

    public void removeSharedSearch(SavedSearch savedSearch) {
        sharedSearches.remove(savedSearch);
        savedSearch.getUsers().remove(this);
    }
}
