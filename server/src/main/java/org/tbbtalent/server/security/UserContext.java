package org.tbbtalent.server.security;

import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.model.db.Candidate;
import org.tbbtalent.server.model.db.User;

@Service
public class UserContext {

    //todo Make this optional
    public @Nullable User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser) {
            return ((AuthenticatedUser) auth.getPrincipal()).getUser();
        }
        return null;
    }

    /**
     * Returns id of logged in candidate or null if not logged in
     * @return id of logged in candidate
     */
    public @Nullable Long getLoggedInCandidateId() {
        Long ret = null;
        User user = getLoggedInUser();
        if (user != null) {
            Candidate candidate = user.getCandidate();
            if (candidate != null) {
                ret = candidate.getId();
            }
        }
        return ret;
    }

    /**
     * Returns candidate retrieved from the user context.
     * Note that this Candidate object is not retrieved from the database and
     * therefore will not be part of the JPA persistence context
     * so lazily loaded attributes will not be fetched from the database and
     * will be absent.
     * <p/>
     * It should therefore should only be used as a base for adding new
     * attributes and saving to the database.
     * @return Candidate with no associated attributes
     */
    public @Nullable Candidate getLoggedInCandidate(){
        User user = getLoggedInUser();
        return user == null ? null : user.getCandidate();
    }
    
    public @Nullable String getUserLanguage() {
        User user = getLoggedInUser();
        return user == null ? null : user.getSelectedLanguage();
    }
}
