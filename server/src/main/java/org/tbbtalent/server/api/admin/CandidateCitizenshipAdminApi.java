package org.tbbtalent.server.api.admin;

import java.util.Map;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.CandidateCitizenship;
import org.tbbtalent.server.request.candidate.citizenship.CreateCandidateCitizenshipRequest;
import org.tbbtalent.server.service.db.CandidateCitizenshipService;
import org.tbbtalent.server.util.dto.DtoBuilder;

@RestController()
@RequestMapping("/api/admin/candidate-citizenship")
public class CandidateCitizenshipAdminApi 
        implements IJoinedTableApi<CreateCandidateCitizenshipRequest,
        CreateCandidateCitizenshipRequest,CreateCandidateCitizenshipRequest> {
    private final CandidateCitizenshipService candidateCitizenshipService;

    public CandidateCitizenshipAdminApi(
            CandidateCitizenshipService candidateCitizenshipService) {
        this.candidateCitizenshipService = candidateCitizenshipService;
    }

    /**
     * Creates a new candidate citizenship record from the data in the given 
     * request. 
     * @param candidateId ID of candidate
     * @param request Request containing citizenship details
     * @return Created record - including database id of citizenship record
     * @throws NoSuchObjectException if the there is no Candidate record with 
     * that candidateId or no Nationality with the id given in the request  
     */
    @Override
    public @NotNull Map<String, Object> create(
            long candidateId, @Valid CreateCandidateCitizenshipRequest request) 
            throws NoSuchObjectException {
        CandidateCitizenship candidateCitizenship = 
                this.candidateCitizenshipService
                        .createCitizenship(candidateId, request);
        return candidateCitizenshipDto().build(candidateCitizenship);
    }

    /**
     * Delete the candidate citizenship with the given id.  
     * @param id ID of record to be deleted
     * @return True if record was deleted, false if it was not found.
     * @throws EntityReferencedException if the object cannot be deleted because 
     * it is referenced by another object.
     * @throws InvalidRequestException if not authorized to delete this list.
     */
    @Override
    public boolean delete(long id) 
            throws EntityReferencedException, InvalidRequestException {
        return candidateCitizenshipService.deleteCitizenship(id);
    }
    
    private DtoBuilder candidateCitizenshipDto() {
        return new DtoBuilder()
                .add("id")
                .add("nationality", nationalityDto())
                .add("hasPassport")
                .add("notes")
                ;
    }

    private DtoBuilder nationalityDto() {
        return new DtoBuilder()
                .add("id")
                .add("name")
                .add("status")
                ;
    }
    
}
