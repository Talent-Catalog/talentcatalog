/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

/**
 * Utility for generating HelpLinks (as database insert commands) from HTML
 *
 * @author John Cameron
 */
public class HelpLinkGeneratorFromHtml {
    public void generateCanadaHelpFromHtml(String linkToHtml) throws IOException {
        Document doc = Jsoup.connect(linkToHtml).get();
        doc.select("h3").forEach(System.out::println);
    }
}
