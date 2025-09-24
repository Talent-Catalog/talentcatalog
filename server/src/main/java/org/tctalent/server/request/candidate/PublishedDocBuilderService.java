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

package org.tctalent.server.request.candidate;

import java.util.List;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tctalent.server.model.db.Candidate;

/**
 * Service for building published documents
 *
 * @author John Cameron
 */
public interface PublishedDocBuilderService {


    /**
     * Builds a row from the given candidate or a potential multivalued value of the candidate
     * @param candidate Candidate
     * @param expandingColumnDef If not null, refers to a multi valued field. In which case
     *                           one extra row will be created
     * @param expandingCount Only relevant if expandingColumnDef. In that case it indicates which
     *                       value will be used to create a row. If the count is 0, the row is
     *                       created from the candidate. If > 0, then it indicates which of the
     *                       multiple values will be used to create a for
     * @param columnInfos Definition of columns within the row.
     * @return List of column values which make up the row.
     */
    List<Object> buildRow(
        Candidate candidate, @Nullable PublishedDocColumnDef expandingColumnDef,
        int expandingCount, List<PublishedDocColumnDef> columnInfos);

    List<Object> buildTitle(List<PublishedDocColumnDef> columnInfos);

    /**
     * Returns the number of rows that will be generated for the given candidate and its
     * multivalued field.
     * @param candidate Candidate
     * @param expandingColumnDef If null, method will always return 1. Otherwise one row will be
     *                           created for the candidate plus one row for each of the values
     *                           of this column.
     * @return Number of rows which will be generated.
     */
    int computeNumberOfRowsByCandidate(
        @NonNull Candidate candidate, @Nullable PublishedDocColumnDef expandingColumnDef);

}
