/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.request.candidate;

import java.util.regex.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseCandidateContactRequest {

    /**
     * Matches one invisible/blank Unicode character: whitespace, plus the Hangul/Khmer filler
     * characters that render blank but aren't classified as whitespace.
     *
     * <p>Mirrors INVISIBLE_OR_WHITESPACE_CHAR in
     * ui/candidate-portal/src/app/model/base.ts - keep both in sync.
     */
    private static final String INVISIBLE_OR_WHITESPACE_CHAR =
        "[\\s\\p{Zs}\\p{Cf}\\u115F\\u1160\\u17B4\\u17B5\\u3164\\uFFA0]";

    /**
     * Strips invisible/blank characters from the start and end of a pasted email address - see
     * TC-723. Leaves anything in the middle untouched, since that would mean the email is
     * genuinely malformed rather than just messily pasted.
     */
    private static final Pattern EDGE_INVISIBLE_OR_WHITESPACE = Pattern.compile(
        "^" + INVISIBLE_OR_WHITESPACE_CHAR + "+|" + INVISIBLE_OR_WHITESPACE_CHAR + "+$"
    );

    private Long id;
    private String email;
    private String phone;
    private String whatsapp;

    public void setEmail(String email) {
      // Apply strip() as an additional safety net for Java-recognized whitespace.
        this.email = email == null ? null : EDGE_INVISIBLE_OR_WHITESPACE.matcher(email).replaceAll("").strip();
    }
}
