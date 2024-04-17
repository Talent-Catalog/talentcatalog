/*
 * Copyright (c) 2023 Talent Beyond Boundaries.
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

import javax.transaction.Transactional;
import org.springframework.stereotype.Service;
import org.tctalent.server.model.db.SavedList;
import org.tctalent.server.repository.db.ExportColumnRepository;
import org.tctalent.server.service.db.ExportColumnsService;

@Service
public class ExportColumnsServiceImpl implements ExportColumnsService {

  private final ExportColumnRepository exportColumnRepository;

  public ExportColumnsServiceImpl(
      ExportColumnRepository exportColumnRepository) {
    this.exportColumnRepository = exportColumnRepository;
  }

  @Override
  @Transactional
  public void clearExportColumns(SavedList savedList) {
    exportColumnRepository.deleteBySavedList(savedList);
    savedList.setExportColumns(null);
  }
}
