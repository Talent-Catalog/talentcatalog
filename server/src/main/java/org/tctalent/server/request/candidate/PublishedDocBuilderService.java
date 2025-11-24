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
import org.tctalent.server.model.db.HasMultipleRows;

/**
 * Service for building published documents
 *
 * @author John Cameron
 */
public interface PublishedDocBuilderService {


    /**
     * Builds a row from the given candidate or a potential multivalued value of the candidate
     * @param candidate Candidate
     * @param expandingData If not null, refers to a multivalued field. In which case
     *                      extra rows will be created
     * @param expandingCount Only relevant if expandingColumnDef is not null.
     *                       In that case it indicates which value will be used to create this row.
     *                       If the count is 0, the row is created from the candidate.
     *                       If > 0, then it indicates which of the multiple sets of values will be
     *                       used to populate the columns of this row.
     * @param columnInfos Definition of columns within the row.
     * @return List of column values which make up the row.
     */
    List<Object> buildRow(
        Candidate candidate, @Nullable HasMultipleRows expandingData,
        int expandingCount, List<PublishedDocColumnDef> columnInfos);

    List<Object> buildTitle(List<PublishedDocColumnDef> columnInfos);

    /**
     * Searches given columnConfigs for a {@link PublishedDocColumnDef} which can supply a second
     * dimension to the candidate data to be published.
     * This could mean that a single candidate could generate more than one row in the published doc.
     * <p>
     * An example is a column related to a candidate's dependants. If a candidate has dependants, then
     * we can publish a row for each dependant in addition to the row for the candidate themselves.
     * @param columnConfigs the configs of a number of columns
     */
    @Nullable
    PublishedDocColumnDef findExpandingColumnDef(List<PublishedDocColumnConfig> columnConfigs);

    /**
     * Loads the expanding data from the given expanding field of the given candidate.
     * @param candidate Candidate
     * @param expandingColumnDef Defines the candidate field to load the expanding data from.
     * @return Null if expandingColumnDef is null, otherwise loads the expanding data.
     */
    @Nullable
    HasMultipleRows loadExpandingData(
        @NonNull Candidate candidate, @Nullable PublishedDocColumnDef expandingColumnDef);
}
