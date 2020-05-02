/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service;

import org.tbbtalent.server.model.SavedList;
import org.tbbtalent.server.request.list.UpdateSavedListRequest;

/**
 * TODO JC Doc
 *
 * @author John Cameron
 */
public interface SavedListService {
    SavedList createSavedList(UpdateSavedListRequest request);
    
}
