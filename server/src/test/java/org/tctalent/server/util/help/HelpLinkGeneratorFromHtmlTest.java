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

package org.tctalent.server.util.help;

import java.io.IOException;
import org.junit.jupiter.api.BeforeEach;

class HelpLinkGeneratorFromHtmlTest {
    private HelpLinkGeneratorFromHtml generator;

    @BeforeEach
    void setUp() {
        generator = new HelpLinkGeneratorFromHtml();
    }

    /**
     * Not really a test - but just uncomment @Test annotation to run this to generate the
     * HelpLink DB Insert statements.
     */
//    @Test
    void generateCanadaHelpFromHtml() throws IOException {
        generator.generateCanadaHelpFromHtml(
            "https://tchelp.tettra.site/canada-1/operations-manual-1");

    }
}
