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

package org.tctalent.server.model.db;

/**
 * These correspond to the different types of candidate subfolder.
 * <p/>
 * See https://docs.google.com/document/d/1ytIH3LQ40ICrKa2YM-eIVp09THpHHbEI8kBeWQXjAEQ/
 * and
 * https://docs.google.com/document/d/1VEFQAVapmbond_3GIjdWfV95E3QLd3wBlIBDNZsFNcI/
 *
 * @author John Cameron
 */
public enum CandidateSubfolderType {
    address,
    character,
    employer,
    engagement,
    experience,
    family,
    identity,
    immigration,
    language,
    medical,
    qualification,
    registration
}
