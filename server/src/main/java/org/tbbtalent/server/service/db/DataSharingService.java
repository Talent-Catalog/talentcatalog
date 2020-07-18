/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db;

/**
 * Sharing TBB data with third parties.
 *
 * @author John Cameron
 */
public interface DataSharingService {
    void dbCopy() throws Exception;
}
