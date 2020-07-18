package org.tbbtalent.server.request.user;

import java.util.List;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import org.tbbtalent.server.model.db.Country;
import org.tbbtalent.server.model.db.Role;

public class CreateUserRequest {

    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @NotBlank
    private String username;
    @NotBlank
    private String password;
    @NotBlank
    private String email;
    @NotNull
    private Role role;

    private List<Country> sourceCountries;

    private Boolean readOnly;


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

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() { return password; }

    public void setPassword(String password) { this.password = password; }

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public Role getRole() { return role; }

    public void setRole(Role role) { this.role = role; }

    public List<Country> getSourceCountries() { return sourceCountries; }

    public void setSourceCountries(List<Country> sourceCountries) { this.sourceCountries = sourceCountries; }

    public Boolean getReadOnly() { return readOnly; }

    public void setReadOnly(Boolean readOnly) { this.readOnly = readOnly; }
}
