/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.service.db.impl;

import java.util.Set;
import org.springframework.stereotype.Service;
import org.tbbtalent.server.model.db.ExportColumn;
import org.tbbtalent.server.model.db.SavedList;
import org.tbbtalent.server.repository.db.ExportColumnRepository;
import org.tbbtalent.server.service.db.ExportColumnsService;

@Service
public class ExportColumnsServiceImpl implements ExportColumnsService {
  
  private final ExportColumnRepository exportColumnRepository;

  public ExportColumnsServiceImpl(
      ExportColumnRepository exportColumnRepository) {
    this.exportColumnRepository = exportColumnRepository;
  }

  @Override
  public void clearExportColumns(SavedList savedList) {
    Set<ExportColumn> cols = savedList.getExportColumns();
    exportColumnRepository.deleteAll(cols);
    savedList.setExportColumns(null);
  }
}
