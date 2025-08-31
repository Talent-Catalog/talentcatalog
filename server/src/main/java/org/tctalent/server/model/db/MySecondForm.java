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

package org.tctalent.server.model.db;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Very simple form
 *
 * @author John Cameron
 */
@Entity
@Table(name = "candidate_form_instance")
public class MySecondForm extends CandidateFormInstance {
    private static final String HAIR_COLOUR_PROPERTY_NAME = "hairColour";

    public String getCity() {
        return getWorkingCandidate().getCity();
    }

    public void setCity(String city) {
        getWorkingCandidate().setCity(city);
    }

    public String getHairColour() {
        return getProperty(HAIR_COLOUR_PROPERTY_NAME);
    }

    public void setHairColour(String hairColour) {
        setProperty(HAIR_COLOUR_PROPERTY_NAME, hairColour);
    }
}
