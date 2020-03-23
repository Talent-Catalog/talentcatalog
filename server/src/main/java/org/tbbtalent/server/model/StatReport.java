package org.tbbtalent.server.model;

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
