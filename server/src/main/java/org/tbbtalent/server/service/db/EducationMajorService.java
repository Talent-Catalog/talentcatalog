/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db;

import java.util.List;

import org.springframework.data.domain.Page;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.model.db.EducationMajor;
import org.tbbtalent.server.request.education.major.CreateEducationMajorRequest;
import org.tbbtalent.server.request.education.major.SearchEducationMajorRequest;
import org.tbbtalent.server.request.education.major.UpdateEducationMajorRequest;

public interface EducationMajorService {

    List<EducationMajor> listActiveEducationMajors();

    Page<EducationMajor> searchEducationMajors(SearchEducationMajorRequest request);

    EducationMajor getEducationMajor(long id);

    EducationMajor createEducationMajor(CreateEducationMajorRequest request) throws EntityExistsException;

    EducationMajor updateEducationMajor(long id, UpdateEducationMajorRequest request) throws EntityExistsException ;

    boolean deleteEducationMajor(long id) throws EntityReferencedException;

}
