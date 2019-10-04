package org.tbbtalent.server.service;

import org.springframework.data.domain.Page;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.model.EducationMajor;
import org.tbbtalent.server.request.education.major.CreateEducationMajorRequest;
import org.tbbtalent.server.request.education.major.SearchEducationMajorRequest;
import org.tbbtalent.server.request.education.major.UpdateEducationMajorRequest;

import java.util.List;

public interface EducationMajorService {

    List<EducationMajor> listActiveEducationMajors();

    Page<EducationMajor> searchEducationMajors(SearchEducationMajorRequest request);

    EducationMajor getEducationMajor(long id);

    EducationMajor createEducationMajor(CreateEducationMajorRequest request) throws EntityExistsException;

    EducationMajor updateEducationMajor(long id, UpdateEducationMajorRequest request) throws EntityExistsException ;

    boolean deleteEducationMajor(long id) throws EntityReferencedException;

}
