/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.service.db;

import java.util.List;

import org.springframework.data.domain.Page;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.model.db.Occupation;
import org.tbbtalent.server.request.occupation.CreateOccupationRequest;
import org.tbbtalent.server.request.occupation.SearchOccupationRequest;
import org.tbbtalent.server.request.occupation.UpdateOccupationRequest;

public interface OccupationService {

    List<Occupation> listOccupations();

    Page<Occupation> searchOccupations(SearchOccupationRequest request);

    Occupation getOccupation(long id);

    Occupation createOccupation(CreateOccupationRequest request) throws EntityExistsException;

    Occupation updateOccupation(long id, UpdateOccupationRequest request) throws EntityExistsException ;

    boolean deleteOccupation(long id) throws EntityReferencedException;

}
