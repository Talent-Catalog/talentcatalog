package org.tbbtalent.server.service;

import org.springframework.data.domain.Page;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.model.Nationality;
import org.tbbtalent.server.request.nationality.CreateNationalityRequest;
import org.tbbtalent.server.request.nationality.SearchNationalityRequest;
import org.tbbtalent.server.request.nationality.UpdateNationalityRequest;

import java.util.List;

public interface NationalityService {

    List<Nationality> listNationalities();

    Page<Nationality> searchNationalities(SearchNationalityRequest request);

    Nationality getNationality(long id);

    Nationality createNationality(CreateNationalityRequest request) throws EntityExistsException;

    Nationality updateNationality(long id, UpdateNationalityRequest request) throws EntityExistsException ;

    boolean deleteNationality(long id);

}
