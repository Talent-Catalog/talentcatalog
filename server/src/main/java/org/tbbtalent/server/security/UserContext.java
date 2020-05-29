package org.tbbtalent.server.security;

import org.springframework.lang.Nullable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.User;

@Service
public class UserContext {

    public @Nullable User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser) {
            return ((AuthenticatedUser) auth.getPrincipal()).getUser();
        }
        return null;
    }

    public @Nullable Candidate getLoggedInCandidate(){
        User user = getLoggedInUser();
        return user == null ? null : user.getCandidate();
    }
    
    public @Nullable String getUserLanguage() {
        User user = getLoggedInUser();
        return user == null ? null : user.getSelectedLanguage();
    }
}
