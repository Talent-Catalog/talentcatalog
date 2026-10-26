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

package org.tctalent.server.security;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Null encoded passwords are never valid.
 * <p>
 * This used to be implemented by overriding matches. Since Spring Security 7 the BCrypt encoder
 * already returns false for a null (or empty) encoded password, and matches can no longer be
 * overridden - TC-1189.
 */
public class TcPasswordEncoder extends BCryptPasswordEncoder {
}
