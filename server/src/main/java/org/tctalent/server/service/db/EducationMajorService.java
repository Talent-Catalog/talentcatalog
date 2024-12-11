/*
 * Copyright (c) 2024 Talent Catalog.
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

package org.tctalent.server.service.db;

import java.util.List;

import org.springframework.data.domain.Page;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.model.db.EducationMajor;
import org.tctalent.server.request.education.major.CreateEducationMajorRequest;
import org.tctalent.server.request.education.major.SearchEducationMajorRequest;
import org.tctalent.server.request.education.major.UpdateEducationMajorRequest;

public interface EducationMajorService {

    List<EducationMajor> listActiveEducationMajors();

    Page<EducationMajor> searchEducationMajors(SearchEducationMajorRequest request);

    EducationMajor getEducationMajor(long id);

    EducationMajor createEducationMajor(CreateEducationMajorRequest request) throws EntityExistsException;

    EducationMajor updateEducationMajor(long id, UpdateEducationMajorRequest request) throws EntityExistsException ;

    boolean deleteEducationMajor(long id) throws EntityReferencedException;

}
