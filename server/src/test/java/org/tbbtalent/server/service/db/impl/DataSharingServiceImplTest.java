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