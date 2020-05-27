/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.request.list;

import org.tbbtalent.server.request.candidate.AbstractUpdateCandidateSourceRequest;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * Request for modifying the info associated with a SavedList 
 * - eg changing the name
 */
@Getter
@Setter
@ToString(callSuper = true)
public class UpdateSavedListInfoRequest extends AbstractUpdateCandidateSourceRequest {
}
