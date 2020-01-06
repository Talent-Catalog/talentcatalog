package org.tbbtalent.server.security;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.exception.InvalidPasswordFormatException;

@Service
public class PasswordHelper {

    private final PasswordEncoder passwordEncoder;

    @Autowired
    public PasswordHelper(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    public String validateAndEncodePassword(String password) throws InvalidPasswordFormatException {
        validatePasswordRules(password);
        return encodePassword(password);
    }

    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }

    public void validatePasswordRules(String password) throws InvalidPasswordFormatException {

        if (StringUtils.isBlank(password)) {
            throw new InvalidPasswordFormatException("Password must not be blank");
        }

        if (StringUtils.length(password) < 8) {
           throw new InvalidPasswordFormatException("Password must be at least 8 characters long");
        }
/*
        if (!password.matches(".*\\d+.*")) {
           throw new InvalidPasswordFormatException("Password must have at least one number character");
        }
        if (!password.matches(".*[a-z]+.*")) {
            throw new InvalidPasswordFormatException("Password must have at least one lower case character");
        }
        if (!password.matches(".*[A-Z]+.*")) {
           throw new InvalidPasswordFormatException("Password must have at least one upper case character");
        }
 */
    }

    public boolean isValidPassword(String password, String passwordEnc) {
        return passwordEncoder.matches(password, passwordEnc);
    }


}


