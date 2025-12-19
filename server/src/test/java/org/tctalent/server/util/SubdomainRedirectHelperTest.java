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

package org.tctalent.server.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class SubdomainRedirectHelperTest {

    @Test
    void computeRedirectUrl() {
        String s;
        s = SubdomainRedirectHelper.computeRedirectUrl("rp.tctalent.org");
        assertNotNull(s);
        assertEquals("https://tctalent.org?p=rp", s);

        //Note that www is treated as a partner - that is OK for our purposes.
        s = SubdomainRedirectHelper.computeRedirectUrl("www.tctalent.org");
        assertNotNull(s);
        assertEquals("https://tctalent.org?p=www", s);

        s = SubdomainRedirectHelper.computeRedirectUrl("tctalent.org");
        assertNull(s);

        s = SubdomainRedirectHelper.computeRedirectUrl("172.31.31.31:8080");
        assertNull(s);
    }
}
