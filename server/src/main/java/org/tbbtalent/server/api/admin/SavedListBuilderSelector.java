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

import org.tbbtalent.server.util.dto.DtoBuilder;

import javax.validation.constraints.NotNull;

/**
 * Utility for selecting a SavedList DTO builder
 *
 * @author John Cameron
 */
public class SavedListBuilderSelector {
    private final ExportColumnsBuilderSelector exportColumnsBuilderSelector 
        = new ExportColumnsBuilderSelector();

    public @NotNull DtoBuilder selectBuilder() {
        return savedListDto();
    }

    private DtoBuilder savedListDto() {
        return new DtoBuilder()
                .add("id")
                .add("description")
                .add("displayedFieldsLong")
                .add("displayedFieldsShort")
                .add("exportColumns", exportColumnsBuilderSelector.selectBuilder())
                .add("status")
                .add("name")
                .add("fixed")
                .add("global")
                .add("savedSearchSource", savedSearchSourceDto())
                .add("sfJoblink")
                .add("folderlink")
                .add("foldercvlink")
                .add("folderjdlink")
                .add("publishedDocLink")
                .add("registeredJob")
                .add("tbbShortName")
                .add("createdBy", userDto())
                .add("createdDate")
                .add("updatedBy", userDto())
                .add("updatedDate")
                .add("users", userDto())
                .add("tasks", taskDto())
        ;
    }

    private DtoBuilder savedSearchSourceDto() {
        return new DtoBuilder()
                .add("id")
                ;
    }

    private DtoBuilder userDto() {
        return new DtoBuilder()
                .add("id")
                .add("firstName")
                .add("lastName")
                ;
    }

    private DtoBuilder taskDto() {
        return new DtoBuilder()
                // TODO: other attributes
                .add("name")
                .add("optional")
                .add("type")
                ;
    }
}
