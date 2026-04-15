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

package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.*;
import static org.tctalent.server.service.db.impl.GoogleSheetPublisherServiceImpl.validateDataRangeCapacity;

import com.google.api.services.sheets.v4.model.GridRange;
import java.io.IOException;
import org.junit.jupiter.api.Test;

class GoogleSheetPublisherServiceImplTest {

  @Test
  public void throwsIfTooManyRows() {
    GridRange grid = new GridRange()
        .setStartRowIndex(5)
        .setEndRowIndex(10); // 5 rows max

    assertThrows(IOException.class, () -> {
      validateDataRangeCapacity("Main!B6:W10", grid, 6); // trying to write 6 rows
    });
  }

  @Test
  public void doesNotThrowIfFitsExactly() {
    GridRange grid = new GridRange()
        .setStartRowIndex(5)
        .setEndRowIndex(10); // 5 rows

    assertDoesNotThrow(() -> {
      validateDataRangeCapacity("Main!B6:W10", grid, 5);
    });
  }

}
