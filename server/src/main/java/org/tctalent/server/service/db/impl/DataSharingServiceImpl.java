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

package org.tctalent.server.service.db.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.service.db.DataSharingService;
import org.tctalent.server.service.db.email.EmailSender;
import org.tctalent.server.service.db.util.PartnerDatabaseDefinition;
import org.tctalent.server.service.db.util.PartnerTableDefinition;

/**
 * Data is shared with destination partners by copying to their own databases,
 * as configured in an XML configuration file stored in resources whose path is
 * specified in tc.partner-dbcopy-config in application.yml.
 * <p/>
 * The configuration file contains a different configuration for each partner
 * (typically by country - eg Australian partner, UK partner etc).
 * <p/>
 * Each destination configuration defines the structure of destination tables
 * which are populated from the TC database.
 * <p/>
 * In this implementation data is copied in two stages. First a local
 * "in memory" copy of the destination database is created on this server
 * (using an H2 in memory database). This involves reading a subset of data from
 * the normal TC database (as defined in the "populate" elements of the xml
 * configuration) and inserting it into the appropriate fields of the
 * destination tables (as defined in the "fields" elements for each destination
 * table in the xml configuration).
 * <p/>
 * Once the first stage is completed, there is an exact copy of the tables
 * we want the destination partner to have in our in memory H2 database.
 * All that remains is to copy all those tables up to the destination partner's
 * database - replacing any existing content.
 * This is done in the {@link #exportImport} method in an slightly unusual
 * way for performance reasons - using an Export/Import method which first
 * exports all table data in the form of a CSV file which is then imported
 * into the destination database using "LOAD DATA LOCAL INFILE" statements.
 * See https://dev.mysql.com/doc/refman/8.0/en/load-data.html
 */
@Service
@Slf4j
public class DataSharingServiceImpl implements DataSharingService {

    @Value("${spring.datasource.url}")
    private String masterJdbcUrl;

    @Value("${spring.datasource.username}")
    private String masterUser;

    @Value("${spring.datasource.password}")
    private String masterPwd;

    @Value("${tc.partner-dbcopy-config}")
    private String partnerDbcopyConfig = "data.sharing/tcCopies.xml";

    private static final String DB_LOCAL_COPY_URL = "jdbc:h2:mem:";

    private Connection tcMaster;
    private Connection tcLocalCopy;

    private final EmailSender emailSender;

    @Autowired
    public DataSharingServiceImpl(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    public DataSharingServiceImpl() {
        this(null);
    }

    @Override
//    @Scheduled(cron = "0 30 23 * * ?", zone = "GMT")
//    @SchedulerLock(name = "DataSharingService_dbCopy", lockAtLeastFor = "PT23H", lockAtMostFor = "PT23H")
    public void dbCopy() throws Exception {
//        reportError("dbCopy has been started. Pull this out once emails are working", null);
        performCopies();
    }

    private boolean performCopies() {

        boolean ok = true;

        try {
            //Connect to TC database and local copy
            connect();

            List<PartnerDatabaseDefinition> destinations = getDestinations();

            for (PartnerDatabaseDefinition destination : destinations) {

                LogBuilder.builder(log)
                    .action("DataSharingServiceImpl")
                    .message("Copying to " + destination.getCountry())
                    .logInfo();

                try {
                    List<PartnerTableDefinition> defs = destination.getTables();

                    LogBuilder.builder(log)
                        .action("DataSharingServiceImpl")
                        .message("Create local copy")
                        .logInfo();

                    for (PartnerTableDefinition def : defs) {
                        copyTable(def);
                    }

                    postProcess();

                    LogBuilder.builder(log)
                        .action("DataSharingServiceImpl")
                        .message("Export/Import remote copy")
                        .logInfo();

                    try (Connection tcRemoteCopy = destination.connect()) {
                        for (PartnerTableDefinition def : defs) {
                            exportImport(tcRemoteCopy, def);
                        }
                    }
                } catch (Exception ex) {
                    ok = false;
                    reportError("Failed to copy to destination: " + destination.getCountry(), ex);
                }
            }
        } catch (Exception ex) {
            reportError("Exception performing data sharing", ex);
        }

        disconnect();

        return ok;
    }

    private void connect() throws SQLException {
        //This is the default h2 user and password
        String dbLocalCopyUser = "sa";
        String dbLocalCopyPassword = "";

        tcLocalCopy = DriverManager.getConnection(DB_LOCAL_COPY_URL, dbLocalCopyUser, dbLocalCopyPassword);
        tcMaster = DriverManager.getConnection(masterJdbcUrl, masterUser, masterPwd);

    }

    private void disconnect() {
        try {
            if (tcMaster != null) {
                tcMaster.close();
                tcMaster = null;
            }
        } catch (Exception ex) {
            reportError("Could not close master DB connection", ex);
        }

        try {
            if (tcLocalCopy != null) {
                Statement st = tcLocalCopy.createStatement();
                st.execute("DROP ALL OBJECTS");
                tcLocalCopy.close();
                tcLocalCopy = null;
            }
        } catch (Exception ex) {
            reportError("Could not close local temporary DB connection", ex);
        }
    }

    private void copyTable(PartnerTableDefinition def) {

        LogBuilder.builder(log)
            .action("DataSharingServiceImpl")
            .message("Copying " + def.getTableName())
            .logInfo();

        final String populateTableSQL = def.getPopulateTableSQL();
        try (final Statement masterSelect = tcMaster.createStatement();
             final ResultSet masterData =
                     masterSelect.executeQuery(populateTableSQL);
             final Statement reCreateCopy = tcLocalCopy.createStatement()) {
            ResultSetMetaData rsmd = masterData.getMetaData();

            //Drop and recreate any existing table
            reCreateCopy.executeUpdate(def.getDropTableSQL());
            reCreateCopy.executeUpdate(def.getCreateTableSQL());
            String createIndexSQL = def.getCreateTableIndexSQL();
            if (createIndexSQL != null) {
                reCreateCopy.executeUpdate(createIndexSQL);
            }

            int nColumns = rsmd.getColumnCount();
            String insertSQL = def.getInsertSQL(nColumns);

            try (final PreparedStatement insertStatement =
                         tcLocalCopy.prepareStatement(insertSQL)) {
                int count = 0;
                while (masterData.next()) {
                    // Insert a row with these values into copy
                    insertStatement.clearParameters();

                    for (int i = 1; i <= nColumns; i++) {
                        try {
                            final Object masterDataObject = masterData.getObject(i);
                            insertStatement.setObject(i, masterDataObject);
                        } catch (Exception ex) {
                            reportError(masterData.getString(i), ex);
                        }
                    }

                    insertStatement.executeUpdate();

                    count++;
                    if (count % 100 == 0) {
                        System.out.print('.');
                    }
                }
                System.out.println();
            }
        } catch (Exception ex) {
            reportError("Exception copying " + def.getTableName(), ex);
        }
    }

    private void exportImport(
            Connection tcRemoteCopy, PartnerTableDefinition def) {
        final String tableName = def.getTableName();

        LogBuilder.builder(log)
            .action("DataSharingServiceImpl")
            .message("Exporting " + tableName)
            .logInfo();

        String tmpDir = System.getProperty("java.io.tmpdir");
        File exportFile = new File(tmpDir, tableName + ".csv");
        if (exportFile.exists()) {
            boolean ok = exportFile.delete();
            if (!ok) {
                LogBuilder.builder(log)
                    .action("DataSharingServiceImpl")
                    .message("Failed to delete old temp file")
                    .logWarn();
            }
        }
        final String exportFilePath = exportFile.getAbsolutePath();

        LogBuilder.builder(log)
            .action("DataSharingServiceImpl")
            .message("Exporting to " + exportFilePath)
            .logInfo();

        try (final Statement exportSt = tcLocalCopy.createStatement()) {
            String s = "CALL CSVWRITE(" +
                    "'" + exportFilePath + "'" +
                    ", 'SELECT * FROM " + tableName + "'" +
                    ")";
            exportSt.execute(s);
            try (final Statement importSt = tcRemoteCopy.createStatement()) {
                //Create a new table
                //Delete if already exists
                importSt.executeUpdate(def.getDropTableSQLAsNew());
                importSt.executeUpdate(def.getCreateTableSQLAsNew());
                //Create index if we have one
                String createIndexSQL = def.getCreateTableIndexSQLAsNew();
                if (createIndexSQL != null) {
                    importSt.executeUpdate(createIndexSQL);
                }
                //Now do import into new table
                importSt.execute("LOAD DATA LOCAL INFILE '" +
                        exportFilePath +
                        "' INTO TABLE " + def.getNewTableName() +
                        " FIELDS TERMINATED BY ',' ENCLOSED BY '\"' " +
                        " LINES TERMINATED BY '\n' IGNORE 1 LINES");

                //Create dummy table if one does not exist so that following rename doesn't fail
                importSt.executeUpdate(def.getCreateTableSQL());
                //Rename current to old, and new to current
                importSt.executeUpdate(def.getRenameSQL());

                //Finally drop the old table
                importSt.executeUpdate(def.getDropTableSQLAsOld());
            }
        } catch (Exception ex) {
            reportError("Exception exporting " + tableName, ex);
        }
    }

    private List<PartnerDatabaseDefinition> getDestinations() throws JDOMException, IOException {

        List<PartnerDatabaseDefinition> defs = new ArrayList<>();

        ClassLoader cl = this.getClass().getClassLoader();
        InputStream in = cl.getResourceAsStream(partnerDbcopyConfig);

        //Parse the XML input stream.
        SAXBuilder builder = new SAXBuilder();
        Document doc = builder.build(in);

        Element root = doc.getRootElement();

        List<Element> destinationEls = root.getChildren("destination");
        for (Element destinationEl : destinationEls) {

            Element databaseEl = destinationEl.getChild("database");

            List<PartnerTableDefinition> tableDefs = new ArrayList<>();


            //Process the candidate tables
            Element filterEl = databaseEl.getChild("filter");
            String filter = getValue(filterEl);
            Element tablesEl = databaseEl.getChild("tables");
            tableDefs.addAll(processTableElements(filter, tablesEl));


            //Process the String tables
            Element stringTablesEl = databaseEl.getChild("stringtables");
            tableDefs.addAll(processTableElements(null, stringTablesEl));


            Element countryEl = destinationEl.getChild("country");
            String country = getValue(countryEl);
            Element dbUrlEl = databaseEl.getChild("url");
            String dbUrl = getValue(dbUrlEl);
            Element dbUserEl = databaseEl.getChild("user");
            String dbUser = getValue(dbUserEl);
            Element dbPasswordEl = databaseEl.getChild("password");
            String dbPassword = getValue(dbPasswordEl);

            defs.add(new PartnerDatabaseDefinition(
                    country, dbUrl, dbUser, dbPassword, tableDefs));
        }

        return defs;
    }

    private List<PartnerTableDefinition> processTableElements(String filter, Element tablesEl) {
        List<PartnerTableDefinition> tableDefs = new ArrayList<>();

        List<Element> tableEls = tablesEl.getChildren("table");
        for (Element tableEl : tableEls) {
            Element nameEl = tableEl.getChild("name");
            String name = getValue(nameEl);

            Element populateEl = tableEl.getChild("populate");
            String populateSQL = getValue(populateEl);

            Element fieldsEl = tableEl.getChild("fields");
            String fields = getValue(fieldsEl);

            Element indexEl = tableEl.getChild("index");
            String indexField = getValue(indexEl);

            tableDefs.add(new PartnerTableDefinition(
                    filter, name, fields, populateSQL, indexField));
        }
        return tableDefs;
    }

    private static String getValue(Element el) {
        return el == null ? null :
                el.getValue().trim().replaceAll("\\s{2,}", " ");
    }

    private void postProcess() {

    }

    private void reportError(String s, @Nullable Exception ex) {
        LogBuilder.builder(log)
            .action("DataSharingServiceImpl")
            .message(s)
            .logError(ex);

        if (emailSender != null) {
            emailSender.sendAlert( s, ex);
        }
    }

    void setMasterJdbcUrl(String masterJdbcUrl) {
        this.masterJdbcUrl = masterJdbcUrl;
    }

    void setMasterUser(String masterUser) {
        this.masterUser = masterUser;
    }

    void setMasterPwd(String masterPwd) {
        this.masterPwd = masterPwd;
    }
}
