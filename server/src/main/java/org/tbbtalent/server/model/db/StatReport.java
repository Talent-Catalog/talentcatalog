/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.model.db;

import java.util.List;

public class StatReport {
    String name;
    List<DataRow> rows;
    String chartType;

    public StatReport(String name, List<DataRow> rows, String chartType) {
        this.name = name;
        this.rows = rows;
        this.chartType = chartType;
    }

    public StatReport(String name, List<DataRow> rows) {
        this(name, rows, "doughnut");
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<DataRow> getRows() {
        return rows;
    }

    public void setRows(List<DataRow> rows) {
        this.rows = rows;
    }

    public String getChartType() {
        return chartType;
    }

    public void setChartType(String chartType) {
        this.chartType = chartType;
    }
}
