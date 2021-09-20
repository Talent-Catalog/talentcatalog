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

package org.tbbtalent.server.api.admin;

import javax.validation.constraints.NotNull;
import org.tbbtalent.server.util.dto.DtoBuilder;

/**
 *  Utility for selecting a ExportColumns DTO builder
 *
 * @author John Cameron
 */
public class ExportColumnsBuilderSelector {

  public @NotNull DtoBuilder selectBuilder() {
    return exportColumnDto();
  }

  private DtoBuilder exportColumnDto() {
    return new DtoBuilder()
        .add("index")
        .add("key")
        .add("properties", publishedDocColumnPropsDto())
        ;
  }

  private DtoBuilder publishedDocColumnPropsDto() {
    return new DtoBuilder()
        .add("header")
        .add("constant")
        ;
  }

}
