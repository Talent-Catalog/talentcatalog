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

import org.tctalent.server.model.db.SavedList;

/**
 * Unfortunately we have to manually remove export columns. We can't rely on
 * setExportColumns(savedList) automatically deleting the previous columns of the list.
 * <p/>
 * See <a href="https://stackoverflow.com/questions/2011519/jpa-onetomany-not-deleting-child">...</a>
 * <p/>
 * Note from above reference that JPA 2 supports a solution around "optional=false" and
 * "orphanRemoval"
 * but not sure how well that will work given that ExportColumns table is used for both saved lists
 * and saved searches - so those ids are optional (if saved search export columns are being
 * specified, then savedSearchId will be specified but savedListId won't).
 * Simpler and more straightforward to delete the export columns explicitly by SavedList id.
 * <p/>
 * So before each setExportColumns call, you should use this service to clearExportColumns for
 * that SavedList.
 * @author John Cameron
 */
public interface ExportColumnsService {

    /**
     * Removes all export columns associated with the given list
     * @param savedList List whose export columns are being cleared.
     */
    void clearExportColumns(SavedList savedList);

}
