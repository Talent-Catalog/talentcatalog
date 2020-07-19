/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.fail;

class DataSharingServiceImplTest {
    private DataSharingServiceImpl service;

//    @BeforeEach
    void setUp() {
        service = new DataSharingServiceImpl();
        service.setMasterJdbcUrl("jdbc:postgresql://prod-tbb.cskpt7osayvj.us-east-1.rds.amazonaws.com:5432/tbbtalent");
        service.setMasterUser("tbbtalent");
        service.setMasterPwd("T884T@lent!N0W");
    }

//    @Test
    void dbCopy() {
        try {
            service.dbCopy();
        } catch (Exception ex) {
            fail(ex);
        }
    }
}