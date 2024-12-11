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

package org.tctalent.server.service.db.util;
/**
 * Defines a TBB table in a destination partner's database.
 *
 * @author John Cameron
 */
public class PartnerTableDefinition {
    private final String filter;
    private final String populateTableSQL;
    private final String sqlTableFields;
    private final String indexField;
    private final String tableName;

    private static final String OLD_PREFIX = "_old_";

    public PartnerTableDefinition(
            String filter,
            String tableName, String sqlTableFields, String populateTableSQL,
            String indexField) {
        this.filter = filter;
        this.tableName = tableName;
        this.sqlTableFields = sqlTableFields;
        this.populateTableSQL = populateTableSQL;
        this.indexField = indexField;
    }

    public String getCreateTableIndexSQL() {
        return  indexField == null ? null :
                "CREATE UNIQUE INDEX " +
                tableName + "_" + indexField + "_uindex ON " +
                tableName + " (" + indexField + ")";
    }

    public String getCreateTableSQL() {
        return "CREATE TABLE IF NOT EXISTS " + tableName + "(" + sqlTableFields + ")";
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

    public String getDropTableSQLAsNew() {
        return "DROP TABLE IF EXISTS " + getNewTableName();
    }

    public String getCreateTableSQLAsNew() {
        return "CREATE TABLE " + getNewTableName() + "(" + sqlTableFields + ")";
    }

    public String getCreateTableIndexSQLAsNew() {
        return  indexField == null ? null :
                "CREATE UNIQUE INDEX " +
                        tableName + "_" + indexField + "_uindex ON " +
                        getNewTableName() + " (" + indexField + ")";
    }

    public String getDropTableSQLAsOld() {
        return "DROP TABLE IF EXISTS " + OLD_PREFIX + tableName;
    }

    public String getInsertSQLAsNew(int nColumns) {
        StringBuilder insertSQL = new StringBuilder(
                "INSERT INTO " + getNewTableName() + " VALUES(?");
        for (int i = 1; i < nColumns; i++) {
            insertSQL.append(",?");
        }
        insertSQL.append(")");
        return insertSQL.toString();
    }

    public String getRenameSQL() {
        return "RENAME TABLE " +
                tableName + " TO " + OLD_PREFIX + tableName + ", " +
                getNewTableName() + " TO " + tableName;
    }

    public String getPopulateTableSQL() {
        return filter == null ? populateTableSQL :
                populateTableSQL + " AND " + filter;
    }

    public String getNewTableName() {
        String NEW_PREFIX = "_new_";
        return NEW_PREFIX + tableName;
    }

    public String getTableName() {
        return tableName;
    }
}
