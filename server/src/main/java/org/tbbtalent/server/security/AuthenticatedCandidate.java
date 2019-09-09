package org.tbbtalent.server.security;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.tbbtalent.server.model.Candidate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AuthenticatedCandidate implements UserDetails {

    private Candidate candidate;
    private List<GrantedAuthority> authorities;

    public AuthenticatedCandidate(Candidate candidate) {
        this.candidate = candidate;
        this.authorities = new ArrayList<>();
        this.authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
    }

    public Candidate getCandidate() {
        return candidate;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return candidate.getPasswordEnc();
    }

    @Override
    public String getUsername() {
        if (StringUtils.isNotBlank(candidate.getEmail())) {
            return candidate.getEmail();
        } else if (StringUtils.isNotBlank(candidate.getPhone())) {
            return candidate.getPhone();
        } else if (StringUtils.isNotBlank(candidate.getWhatsapp())) {
            return candidate.getWhatsapp();
        }
        return null;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

}
