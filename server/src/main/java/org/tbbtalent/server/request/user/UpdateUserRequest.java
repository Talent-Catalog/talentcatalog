package org.tbbtalent.server.request.user;

import org.tbbtalent.server.model.Country;
import org.tbbtalent.server.model.Role;
import org.tbbtalent.server.model.Status;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

public class UpdateUserRequest {

    @NotBlank
    private String firstName;
    @NotBlank
    private String lastName;
    @NotBlank
    private String email;
    @NotBlank
    private String username;
    @NotNull
    private Status status;
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

    public String getEmail() { return email; }

    public void setEmail(String email) { this.email = email; }

    public String getUsername() { return username; }

    public void setUsername(String username) { this.username = username; }

    public Status getStatus() { return status; }

    public void setStatus(Status status) { this.status = status; }

    public Role getRole() { return role; }

    public void setRole(Role role) { this.role = role; }

    public List<Country> getSourceCountries() { return sourceCountries; }

    public void setSourceCountries(List<Country> sourceCountries) { this.sourceCountries = sourceCountries; }

    public Boolean getReadOnly() { return readOnly; }

    public void setReadOnly(Boolean readOnly) { this.readOnly = readOnly; }
}
