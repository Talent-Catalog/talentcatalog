package org.tbbtalent.server.api.admin;

import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.CandidateVisa;
import org.tbbtalent.server.request.candidate.visa.CreateCandidateVisaRequest;
import org.tbbtalent.server.service.db.CandidateVisaService;
import org.tbbtalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/candidate-visa")
public class CandidateVisaAdminApi 
        implements IJoinedTableApi<CreateCandidateVisaRequest,
        CreateCandidateVisaRequest,CreateCandidateVisaRequest> {
    private final CandidateVisaService candidateVisaService;

    public CandidateVisaAdminApi(
            CandidateVisaService candidateVisaService) {
        this.candidateVisaService = candidateVisaService;
    }

    /**
     * Creates a new candidate visa check record from the data in the given 
     * request. 
     * @param candidateId ID of candidate
     * @param request Request containing visa check details
     * @return Created record - including database id of visa check record
     * @throws NoSuchObjectException if the there is no Candidate record with 
     * that candidateId or no country with the id given in the request  
     */
    @Override
    public @NotNull Map<String, Object> create(
            long candidateId, @Valid CreateCandidateVisaRequest request) 
            throws NoSuchObjectException {
        CandidateVisa candidateVisa = 
                this.candidateVisaService
                        .createVisa(candidateId, request);
        return candidateVisaDto().build(candidateVisa);
    }

    /**
     * Delete the candidate visa check with the given id.  
     * @param id ID of record to be deleted
     * @return True if record was deleted, false if it was not found.
     * @throws EntityReferencedException if the object cannot be deleted because 
     * it is referenced by another object.
     * @throws InvalidRequestException if not authorized to delete this list.
     */
    @Override
    public boolean delete(long id) 
            throws EntityReferencedException, InvalidRequestException {
        return candidateVisaService.deleteVisa(id);
    }
    
    private DtoBuilder candidateVisaDto() {
        return new DtoBuilder()
                .add("id")
                .add("country", countryDto())
                .add("eligibility")
                .add("assessmentNotes")
                ;
    }

    private DtoBuilder countryDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                ;
    }
    
}
