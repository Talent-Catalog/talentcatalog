package org.tbbtalent.server.service;

import org.springframework.data.domain.Page;
import org.tbbtalent.server.exception.EntityExistsException;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.model.Industry;
import org.tbbtalent.server.request.industry.CreateIndustryRequest;
import org.tbbtalent.server.request.industry.SearchIndustryRequest;
import org.tbbtalent.server.request.industry.UpdateIndustryRequest;

import java.util.List;

public interface IndustryService {

    List<Industry> listIndustries();

    Page<Industry> searchIndustries(SearchIndustryRequest request);

    Industry getIndustry(long id);

    Industry createIndustry(CreateIndustryRequest request) throws EntityExistsException;

    Industry updateIndustry(long id, UpdateIndustryRequest request) throws EntityExistsException ;

    boolean deleteIndustry(long id) throws EntityReferencedException;

}
