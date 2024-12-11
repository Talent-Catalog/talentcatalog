/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.security;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.InvalidPasswordFormatException;

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


