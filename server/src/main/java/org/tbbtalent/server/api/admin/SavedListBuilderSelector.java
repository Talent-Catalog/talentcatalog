/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.api.admin;

import javax.validation.constraints.NotNull;

import org.tbbtalent.server.util.dto.DtoBuilder;

/**
 * Utility for selecting a SavedList DTO builder
 *
 * @author John Cameron
 */
public class SavedListBuilderSelector {

    public @NotNull DtoBuilder selectBuilder() {
        return savedListDto();
    }

    private DtoBuilder savedListDto() {
        return new DtoBuilder()
                .add("id")
                .add("status")
                .add("name")
                .add("fixed")
                .add("createdBy", userDto())
                .add("createdDate")
                .add("updatedBy", userDto())
                .add("updatedDate")
                .add("users", userDto())
        ;
    }

    private DtoBuilder userDto() {
        return new DtoBuilder()
                .add("id")
                .add("firstName")
                .add("lastName")
                ;
    }
}
