package org.tbbtalent.server.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.model.Candidate;
import org.tbbtalent.server.model.User;

@Service
public class UserContext {

    public User getLoggedInUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AuthenticatedUser) {
            return ((AuthenticatedUser) auth.getPrincipal()).getUser();
        }
        return null;
    }

    public Candidate getLoggedInCandidate(){
        User user = getLoggedInUser();
        return user.getCandidate();
    }
    
    public String getUserLanguage() {
        User user = getLoggedInUser();
        return user.getSelectedLanguage();
    }
}
