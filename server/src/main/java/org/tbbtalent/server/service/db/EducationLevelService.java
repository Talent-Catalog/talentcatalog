/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db;

import java.util.List;

import org.springframework.data.domain.Page;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.model.db.EducationLevel;
import org.tbbtalent.server.request.education.level.CreateEducationLevelRequest;
import org.tbbtalent.server.request.education.level.SearchEducationLevelRequest;
import org.tbbtalent.server.request.education.level.UpdateEducationLevelRequest;

public interface EducationLevelService {

    List<EducationLevel> listEducationLevels();

    Page<EducationLevel> searchEducationLevels(SearchEducationLevelRequest request);

    EducationLevel getEducationLevel(long id);

    EducationLevel createEducationLevel(CreateEducationLevelRequest request) throws EntityExistsException;

    EducationLevel updateEducationLevel(long id, UpdateEducationLevelRequest request) throws EntityExistsException ;

    boolean deleteEducationLevel(long id) throws EntityReferencedException;

}
