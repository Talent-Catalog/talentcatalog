/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db;

import java.util.List;

import org.tbbtalent.server.model.db.SurveyType;

public interface SurveyTypeService {
    List<SurveyType> listSurveyTypes();
}

