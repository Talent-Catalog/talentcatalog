/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.impl;

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

import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.JDOMException;
import org.jdom2.input.SAXBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.service.DataSharingService;
import org.tbbtalent.server.service.email.EmailSender;
import org.tbbtalent.server.service.util.PartnerDatabaseDefinition;
import org.tbbtalent.server.service.util.PartnerTableDefinition;

@Service
public class DataSharingServiceImpl implements DataSharingService {
    private static final Logger log = LoggerFactory.getLogger(DataSharingServiceImpl.class);

    @Value("${spring.datasource.url}")
    private String masterJdbcUrl;

    @Value("${spring.datasource.username}")
    private String masterUser;

    @Value("${spring.datasource.password}")
    private String masterPwd;

    //Need to set zeroDateTimeBehavior because of all the null Dates in the
    //database. Otherwise cannot process those dates.
//    static final String ZERO_DATE_TIME_CONFIG =
//            "?zeroDateTimeBehavior=convertToNull";
//
//    private static final String DB_MASTER_URL =
//            "jdbc:postgresql://prod-tbb.cskpt7osayvj.us-east-1.rds.amazonaws.com:5432/tbbtalent" +
//                    ZERO_DATE_TIME_CONFIG;

    private static final String DB_LOCAL_COPY_URL = "jdbc:h2:mem:"; 
//    + ZERO_DATE_TIME_CONFIG;

    private Connection tbbMaster;
    private Connection tbbLocalCopy;
    
    private final EmailSender emailSender;

    @Autowired
    public DataSharingServiceImpl(EmailSender emailSender) {
        this.emailSender = emailSender;
    }

    public DataSharingServiceImpl() {
        this(null);
    }

    @Override
    @Scheduled(cron = "0 30 23 * * ?", zone = "GMT")
    public void dbCopy() throws Exception {
        reportError("dbCopy has been started. Pull this out once emails are working", null);
        performCopies();
    }

    private boolean performCopies() throws Exception {

        boolean ok = true;
        
        try {
            //Connect to TBB database and local copy
            connect();

            List<PartnerDatabaseDefinition> destinations = getDestinations();

            for (PartnerDatabaseDefinition destination : destinations) {

                log.info("Copying to " + destination.getCountry());

                try {
                    List<PartnerTableDefinition> defs = destination.getTables();

                    log.info("Create local copy");
                    for (PartnerTableDefinition def : defs) {
                        copyTable(def);
                    }

                    postProcess();

                    log.info("Export/Import remote copy");
                    try (Connection tbbRemoteCopy = destination.connect()) {
                        for (PartnerTableDefinition def : defs) {
                            exportImport(tbbRemoteCopy, def);
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

        tbbLocalCopy = DriverManager.getConnection(DB_LOCAL_COPY_URL, dbLocalCopyUser, dbLocalCopyPassword);
        tbbMaster = DriverManager.getConnection(masterJdbcUrl, masterUser, masterPwd);

    }

    private void disconnect() {
        try {
            if (tbbMaster != null) {
                tbbMaster.close();
                tbbMaster = null;
            }
        } catch (Exception ex) {
            reportError("Could not close master DB connection", ex);
        }

        try {
            if (tbbLocalCopy != null) {
                Statement st = tbbLocalCopy.createStatement();
                st.execute("DROP ALL OBJECTS");
                tbbLocalCopy.close();
                tbbLocalCopy = null;
            }
        } catch (Exception ex) {
            reportError("Could not close local temporary DB connection", ex);
        }
    }

    private void copyTable(PartnerTableDefinition def) {

        log.info(def.getTableName());
        final String populateTableSQL = def.getPopulateTableSQL();
        try (final Statement masterSelect = tbbMaster.createStatement();
             final ResultSet masterData =
                     masterSelect.executeQuery(populateTableSQL);
             final Statement reCreateCopy = tbbLocalCopy.createStatement()) {
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
                         tbbLocalCopy.prepareStatement(insertSQL)) {
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
            Connection tbbRemoteCopy, PartnerTableDefinition def) {
        final String tableName = def.getTableName();
        log.info(tableName);

        String tmpDir = System.getProperty("java.io.tmpdir");
        File exportFile = new File(tmpDir, tableName + ".csv");
        if (exportFile.exists()) {
            boolean ok = exportFile.delete();
            if (!ok) {
                log.warn("Failed to delete old temp file");
            }
        }
        final String exportFilePath = exportFile.getAbsolutePath();
        log.info("Exporting to " + exportFilePath);

        try (final Statement exportSt = tbbLocalCopy.createStatement()) {
            String s = "CALL CSVWRITE(" +
                    "'" + exportFilePath + "'" +
                    ", 'SELECT * FROM " + tableName + "'" +
                    ")";
            exportSt.execute(s);
            try (final Statement importSt = tbbRemoteCopy.createStatement()) {
                //Drop and recreate existing table
                importSt.executeUpdate(def.getDropTableSQL());
                importSt.executeUpdate(def.getCreateTableSQL());
                //Create index if we have one
                String createIndexSQL = def.getCreateTableIndexSQL();
                if (createIndexSQL != null) {
                    importSt.executeUpdate(createIndexSQL);
                }
                //Now do import
                importSt.execute("LOAD DATA LOCAL INFILE '" +
                        exportFilePath +
                        "' INTO TABLE " + tableName +
                        " FIELDS TERMINATED BY ',' ENCLOSED BY '\"' " +
                        " LINES TERMINATED BY '\n' IGNORE 1 LINES");
            }
        } catch (Exception ex) {
            reportError("Exception exporting " + tableName, ex);
        }
    }

    private List<PartnerDatabaseDefinition> getDestinations() throws JDOMException, IOException {

        List<PartnerDatabaseDefinition> defs = new ArrayList<>();

        ClassLoader cl = this.getClass().getClassLoader();
        InputStream in = cl.getResourceAsStream("data.sharing/tbbCopies.xml");

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
            String indexSQL = getValue(indexEl);

            tableDefs.add(new PartnerTableDefinition(
                    filter, name, fields, populateSQL, indexSQL));
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
        log.error(s, ex);
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
