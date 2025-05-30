/*
 * Copyright (c) 2025 Talent Catalog.
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

package org.tctalent.server.data;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.Status;

public class CountryTestData {

    // Source countries
    public static final Country LEBANON = new Country("Lebanon", Status.active);
    public static final Country JORDAN = new Country("Jordan", Status.active);
    public static final Country PAKISTAN = new Country("Pakistan", Status.active);
    public static final Country COSTA_RICA = new Country("Costa Rica", Status.active);

    public static List<Country> getSourceCountryList() {
        return new ArrayList<>(List.of(LEBANON, JORDAN));
    }

    public static Set<Country> getSourceCountrySet() {
        return new HashSet<>(Set.of(LEBANON, JORDAN));
    }

    // Destination countries
    public static final Country AUSTRALIA = new Country("Australia", Status.active);
    public static final Country UNITED_STATES = new Country("United States", Status.active);
    public static final Country CANADA = new Country("Canada", Status.active);
    public static final Country UNITED_KINGDOM = new Country("United Kingdom", Status.active);

    // Nationalities
    public static final Country VENEZUELA = new Country("Venezuela", Status.active);
    public static final Country HONDURAS = new Country("Honduras", Status.active);

}
