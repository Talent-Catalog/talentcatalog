/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db.util;
/**
 * Defines a TBB table in a destination partner's database.
 *
 * @author John Cameron
 */
public class PartnerTableDefinition {
    private String filter;
    private String populateTableSQL;
    private String sqlTableFields;
    private String sqlTableIndexCreate;
    private String tableName;

    public PartnerTableDefinition(
            String filter,
            String tableName, String sqlTableFields, String populateTableSQL,
            String sqlTableIndexCreate) {
        this.filter = filter;
        this.tableName = tableName;
        this.sqlTableFields = sqlTableFields;
        this.populateTableSQL = populateTableSQL;
        this.sqlTableIndexCreate = sqlTableIndexCreate;
    }

    public String getCreateTableIndexSQL() {
        return  sqlTableIndexCreate;
    }

    public String getCreateTableSQL() {
        return "CREATE TABLE " + tableName + "(" + sqlTableFields + ")";
    }

    public String getDropTableSQL() {
        return "DROP TABLE IF EXISTS " + tableName;
    }

    public String getInsertSQL(int nColumns) {
        StringBuilder insertSQL = new StringBuilder(
                "INSERT INTO " + tableName + " VALUES(?");
        for (int i = 1; i < nColumns; i++) {
            insertSQL.append(",?");
        }
        insertSQL.append(")");
        return insertSQL.toString();
    }

    public String getPopulateTableSQL() {
        return filter == null ? populateTableSQL :
                populateTableSQL + " AND " + filter;
    }

    public String getTableName() {
        return tableName;
    }

}
