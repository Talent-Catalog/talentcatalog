package org.tbbtalent.server.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.tbbtalent.server.model.Role;
import org.tbbtalent.server.model.User;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AuthenticatedUser implements UserDetails {

    private User user;
    private List<GrantedAuthority> authorities;

    public AuthenticatedUser(User user) {
        this.user = user;
        this.authorities = new ArrayList<>();

        // If read only is checked assign the read only role
        if(user.getReadOnly()){
            this.authorities.add(new SimpleGrantedAuthority("ROLE_READONLY"));
        } else if (user.getRole().equals(Role.admin)) {
            this.authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else if (user.getRole().equals(Role.sourcepartneradmin)) {
            this.authorities.add(new SimpleGrantedAuthority("ROLE_SOURCEPARTNERADMIN"));
        } else if (user.getRole().equals(Role.semilimited)) {
            this.authorities.add(new SimpleGrantedAuthority("ROLE_SEMILIMITED"));
        } else if (user.getRole().equals(Role.limited)) {
            this.authorities.add(new SimpleGrantedAuthority("ROLE_LIMITED"));
        } else if (user.getRole().equals(Role.readonly)) {
            this.authorities.add(new SimpleGrantedAuthority("ROLE_READONLY"));
        } else {
            this.authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }
//
//        if (user.getRole().equals(Role.admin)) {
//            this.authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
//        } else if (user.getRole().equals(Role.sourcepartneradmin)) {
//            this.authorities.add(new SimpleGrantedAuthority("ROLE_SOURCEPARTNERADMIN"));
//        } else if (user.getRole().equals(Role.semilimited)) {
//            this.authorities.add(new SimpleGrantedAuthority("ROLE_SEMILIMITED"));
//        } else if (user.getRole().equals(Role.limited)) {
//            this.authorities.add(new SimpleGrantedAuthority("ROLE_LIMITED"));
//        } else if (user.getRole().equals(Role.readonly)) {
//            this.authorities.add(new SimpleGrantedAuthority("ROLE_READONLY"));
//        } else {
//            this.authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
//        }

        /*
        if(user.getReadOnly === true){
          this.authorities.add(new SimpleGrantedAuthority("ROLE_READONLY")
        } else if (user.getRole().equals(Role.admin)) {
            this.authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        } else if (user.getRole().equals(Role.sourcepartneradmin)) {
            this.authorities.add(new SimpleGrantedAuthority("ROLE_SOURCEPARTNERADMIN"));
        } else if (user.getRole().equals(Role.semilimited)) {
            this.authorities.add(new SimpleGrantedAuthority("ROLE_SEMILIMITED"));
        } else if (user.getRole().equals(Role.limited)) {
            this.authorities.add(new SimpleGrantedAuthority("ROLE_LIMITED"));
        } else if (user.getRole().equals(Role.readonly)) {
            this.authorities.add(new SimpleGrantedAuthority("ROLE_READONLY"));
        } else {
            this.authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
        }



        */
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return user.getPasswordEnc();
    }

    @Override
    public String getUsername() {
        return user.getUsername();
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
