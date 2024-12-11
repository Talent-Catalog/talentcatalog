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
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;

/**
 * Defines a destination partner's database.
 * @author John Cameron
 */
public class PartnerDatabaseDefinition {
    private String country;
    private String dbUrl;
    private String dbUser;
    private String dbPassword;
    private List<PartnerTableDefinition> tables;

    public PartnerDatabaseDefinition(
            String country,
            String dbUrl,
            String dbUser,
            String dbPassword,
            List<PartnerTableDefinition> tables) {
        this.country = country;
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.tables = tables;
    }

    public Connection connect() throws SQLException {
        return DriverManager.getConnection(
                dbUrl /* + DBCopy.ZERO_DATE_TIME_CONFIG*/, dbUser, dbPassword);
    }

    public String getCountry() {
        return country;
    }

    String getDbUrl() {
        return dbUrl;
    }

    public List<PartnerTableDefinition> getTables() {
        return tables;
    }
}
