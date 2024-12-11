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

package org.tctalent.server.service.db;

import org.springframework.lang.NonNull;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Employer;
import org.tctalent.server.model.sf.Account;

public interface EmployerService {
    /**
     * Looks for an employer matching an account on Salesforce, creating one if necessary.
     * @param account Salesforce account object
     * @return Matching employer
     */
    Employer findOrCreateEmployerFromSalesforceAccount(Account account);

    //todo How do I update Account from TC?

    /**
     * Find employer (ie Salesforce account) matching given Salesforce id
     * @param sfId Salesforce id of an account on Salesforce
     * @return Matching employer
     * @throws NoSuchObjectException if there is no Salesforce account matching that sfId.
     */
    @NonNull
    Employer findOrCreateEmployerFromSalesforceId(String sfId) throws NoSuchObjectException;

    /**
     * Find employer (ie Salesforce account) matching given Salesforce url
     * @param sflink Salesforce url of an account on Salesforce
     * @return Matching employer.
     * @throws NoSuchObjectException if there is no Salesforce account matching that url.
     */
    @NonNull
    Employer findOrCreateEmployerFromSalesforceLink(String sflink) throws NoSuchObjectException;

    /**
     * Get the Employer with the given id.
     * @param id ID of Employer to get
     * @return Employer
     * @throws NoSuchObjectException if there is no employer with this id.
     */
    @NonNull
    Employer get(long id) throws NoSuchObjectException;

}
