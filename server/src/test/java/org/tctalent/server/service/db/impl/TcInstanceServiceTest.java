/*
 * Copyright (c) 2026 Talent Catalog.
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

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.tctalent.server.model.db.TcInstanceType;

class TcInstanceServiceTest {

    private final TcInstanceService service = new TcInstanceService();

    @Test
    void shouldReturnGrnPdfLogoFileForGrnInstance() {
        ReflectionTestUtils.setField(service, "instanceType", TcInstanceType.GRN);

        assertThat(service.getLogoFile()).isEqualTo("grnlogo.png");
    }

    @Test
    void shouldReturnTbbPdfLogoFileForTbbInstance() {
        ReflectionTestUtils.setField(service, "instanceType", TcInstanceType.TBB);

        assertThat(service.getLogoFile()).isEqualTo("tbblogo.png");
    }
}
