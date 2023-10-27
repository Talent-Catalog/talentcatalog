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

package org.tctalent.server.service.db;

import org.tctalent.server.model.db.SavedList;

/**
 * Unfortunately we have to manually remove export columns. We can't rely on
 * setExportColumns(savedList) automatically deleting the previous columns of the list.
 * <p/>
 * See https://stackoverflow.com/questions/2011519/jpa-onetomany-not-deleting-child
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
