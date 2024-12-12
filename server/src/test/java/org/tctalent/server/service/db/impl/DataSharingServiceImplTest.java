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

import static org.junit.jupiter.api.Assertions.fail;

class DataSharingServiceImplTest {
    private DataSharingServiceImpl service;
// See here: https://console.aws.amazon.com/rds/home?region=us-east-1#database:id=prod-tbb;is-cluster=false
//    @BeforeEach
    void setUp() {
        service = new DataSharingServiceImpl();
        service.setMasterJdbcUrl("See AWS");
        service.setMasterUser("See AWS");
        service.setMasterPwd("See AWS");
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
