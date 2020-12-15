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
    private final String filter;
    private final String populateTableSQL;
    private final String sqlTableFields;
    private final String sqlTableIndexCreate;
    private final String tableName;
    
    private static final String NEW_PREFIX = "_new_";
    private static final String OLD_PREFIX = "_old_";

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

    public String getCreateTableSQLAsNew() {
        return "CREATE TABLE " + NEW_PREFIX + tableName + "(" + sqlTableFields + ")";
    }

    public String getDropTableSQLAsOld() {
        return "DROP TABLE IF EXISTS " + OLD_PREFIX + tableName;
    }

    public String getInsertSQLAsNew(int nColumns) {
        StringBuilder insertSQL = new StringBuilder(
                "INSERT INTO " + NEW_PREFIX + tableName + " VALUES(?");
        for (int i = 1; i < nColumns; i++) {
            insertSQL.append(",?");
        }
        insertSQL.append(")");
        return insertSQL.toString();
    }

    public String getRenameSQL() {
        return "RENAME TABLE " + 
                tableName + " TO " + OLD_PREFIX + tableName + ", " + 
                NEW_PREFIX + tableName + " TO " + tableName;
    }

    public String getPopulateTableSQL() {
        return filter == null ? populateTableSQL :
                populateTableSQL + " AND " + filter;
    }

    public String getTableName() {
        return tableName;
    }

}
