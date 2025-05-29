/*
 * Copyright (c) 2025 Talent Catalog.
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

package org.tctalent.server.data;

import org.tctalent.server.model.db.Gender;
import org.tctalent.server.model.db.SavedSearch;
import org.tctalent.server.model.db.SavedSearchType;

public class SavedSearchTestData {

    public static SavedSearch getSavedSearch() {
        SavedSearch savedSearch = new SavedSearch();
        savedSearch.setId(123L);
        savedSearch.setDescription("This is a search about nothing.");
        savedSearch.setName("My Search");
        savedSearch.setSavedSearchType(SavedSearchType.other);
        savedSearch.setSimpleQueryString("search + term");
        savedSearch.setStatuses("active,pending");
        savedSearch.setGender(Gender.male);
        savedSearch.setOccupationIds("8577,8484");
        return savedSearch;
    }

}
