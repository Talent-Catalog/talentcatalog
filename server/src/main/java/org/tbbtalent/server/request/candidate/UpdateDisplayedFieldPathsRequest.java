/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.request.candidate;

import java.util.List;

import org.springframework.lang.Nullable;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UpdateDisplayedFieldPathsRequest {
    /**
     * Field paths of candidate fields to be displayed in long format candidate
     * results.
     * <p/>
     * Empty list indicates that default fields should be displayed.
     */
    @Nullable
    private List<String> displayedFieldsLong;

    /**
     * Field paths of candidate fields to be displayed in short format candidate
     * results.
     * <p/>
     * Empty list indicates that default fields should be displayed.
     */
    @Nullable
    private List<String> displayedFieldsShort;
}
