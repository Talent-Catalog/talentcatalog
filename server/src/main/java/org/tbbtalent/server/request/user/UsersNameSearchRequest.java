package org.tbbtalent.server.request.user;

import org.tbbtalent.server.request.PagedSearchRequest;

public class UsersNameSearchRequest extends PagedSearchRequest {

    private String usersName;

    public String getUsersName() { return usersName; }

    public void setUserName(String usersName) { this.usersName = usersName; }
}
