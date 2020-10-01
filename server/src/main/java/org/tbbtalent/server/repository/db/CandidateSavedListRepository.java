/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.repository.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tbbtalent.server.model.db.CandidateSavedList;
import org.tbbtalent.server.model.db.CandidateSavedListKey;

/**
 * See doc for {@link CandidateSavedList}
 */
public interface CandidateSavedListRepository extends 
        JpaRepository<CandidateSavedList, CandidateSavedListKey> {
}
