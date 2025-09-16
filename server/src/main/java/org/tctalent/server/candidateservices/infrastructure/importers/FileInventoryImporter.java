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

package org.tctalent.server.candidateservices.infrastructure.importers;

import org.springframework.web.multipart.MultipartFile;
import org.tctalent.server.exception.ImportFailedException;

public interface FileInventoryImporter {

  /**
   * Imports service resources e.g. Duolingo coupons from a file.
   * <p/>
   * This method processes a file containing service resource data (e.g. coupon codes)
   * and adds the resources to the database. It is useful for bulk importing coupons.
   *
   * @param file the MultipartFile representing the file to import.
   */
  void importFile(MultipartFile file, String serviceCode) throws ImportFailedException;
}
