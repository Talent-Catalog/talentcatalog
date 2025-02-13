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

package org.tctalent.server.request.candidate.stat;

import java.time.LocalDate;
import java.util.List;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.tctalent.server.model.db.Stat;

@Getter
@Setter
@ToString
public class CandidateStatsRequest {

    /**
     * If not null and true, stats are computed using the old method.
     */
    private Boolean runOldStats;

    /**
     * If not null, stats are computed on candidates in the corresponding list.
     */
    private Long listId;

    /**
     * If not null, stats are computed on candidates in the corresponding saved search.
     */
    private Long searchId;

    /**
     * Start date for stats.
     * <p/>
     * If null, then start from earliest data.
     */
    private LocalDate dateFrom;

    /**
     * End date for stats.
     * <p/>
     * If null, then we report up to most recent data
     */
    private LocalDate dateTo;

    /**
     * The selected stats to be run (limited to avoid large processing)
     */
    private List<Stat> selectedStats;

}
