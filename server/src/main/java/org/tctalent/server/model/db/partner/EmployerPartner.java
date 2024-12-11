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

package org.tctalent.server.model.db.partner;

import org.tctalent.server.model.db.Employer;

/**
 * An employer in a destination country who connects directly to us, searching for suitable
 * candidates themselves.
 * <p/>
 * By contrast, some employers prefer to work through a {@link RecruiterPartner}
 *
 * @author John Cameron
 */
public interface EmployerPartner extends JobCreator {

    /**
     * Employer associated with this partner
     * @return Employer
     */
    Employer getEmployer();
    void setEmployer(Employer employer);

}
