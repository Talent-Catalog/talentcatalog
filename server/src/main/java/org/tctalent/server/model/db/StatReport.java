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

import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * A statistical report - in the form of a list of data points
 * - typically counts - in the form of {@link DataRow}s.
 * <p/>
 * Also includes a recommended chartType suited to displaying the data.
 */
@Getter
@Setter
@ToString
public class StatReport {
    /**
     * Name of report
     */
    String name;

    /**
     * Data points
     */
    List<DataRow> rows;

    /**
     * Recommended chart type for display.
     * <p/>
     * Currently supported types are:
     * 'line' | 'bar' | 'horizontalBar' | 'radar' | 'doughnut' | 'polarArea' | 'bubble' | 'pie' | 'scatter'
     * These come from https://www.chartjs.org/ which is what is used to
     * actually display the data on the Angular front end.
     */
    String chartType;

    public StatReport(String name, List<DataRow> rows, String chartType) {
        this.name = name;
        this.rows = rows;
        this.chartType = chartType;
    }

    public StatReport(String name, List<DataRow> rows) {
        this(name, rows, "doughnut");
    }
}
