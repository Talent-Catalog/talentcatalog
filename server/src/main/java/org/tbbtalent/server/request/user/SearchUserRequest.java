package org.tbbtalent.server.request.user;

import org.tbbtalent.server.model.Role;
import org.tbbtalent.server.model.Status;
import org.tbbtalent.server.request.SearchRequest;

public class SearchUserRequest extends SearchRequest {

    private String keyword;
    private Role role;
    private Status status;


    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public Role getRole() {
        return role;
    }

    public void setRole(Role role) {
        this.role = role;
    }

    public Status getStatus() { return status; }

    public void setStatus(Status status) { this.status = status; }
}

