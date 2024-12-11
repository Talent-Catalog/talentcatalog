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

package org.tctalent.server.model.db;

import java.math.BigDecimal;
import java.math.BigInteger;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Represents a row of a {@link StatReport} made up of a label and a value
 * which is a count.
 */
@Getter
@Setter
@ToString
public class DataRow {
    /**
     * Name of counter
     */
    String label;

    /**
     * Value representing a count
     */
    BigDecimal value;

    public DataRow(String label, BigInteger value) {
        this.label = label;
        this.value = new BigDecimal(value);
    }

    public DataRow(String label, Long value) {
        this.label = label;
        this.value = new BigDecimal(value);
    }

    public DataRow(String label, BigDecimal value) {
        this.label = label;
        this.value = value;
    }
}
