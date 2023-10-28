/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

package org.tctalent.server.model.db.partner;

/**
 * This is a partner, typically located in a destination, who helps employers in that destination
 * recruit candidates. In other words, and employment agency working with us.
 * <p/>
 * Sometimes employers can connect directly to us. See {@link EmployerPartner}.
 * However some employers prefer to outsource that process to RecruiterPartners.
 *
 * @author John Cameron
 */
public interface RecruiterPartner extends JobCreator {

}
