/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db.util;
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
