package org.tbbtalent.server.api.admin;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.tbbtalent.server.exception.EntityReferencedException;
import org.tbbtalent.server.exception.InvalidRequestException;
import org.tbbtalent.server.exception.NoSuchObjectException;
import org.tbbtalent.server.model.db.CandidateDependant;
import org.tbbtalent.server.request.candidate.dependant.CreateCandidateDependantRequest;
import org.tbbtalent.server.service.db.CandidateDependantService;
import org.tbbtalent.server.util.dto.DtoBuilder;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.Map;

@RestController()
@RequestMapping("/api/admin/candidate-dependant")
public class CandidateDependantAdminApi
        implements IJoinedTableApi<CreateCandidateDependantRequest,
        CreateCandidateDependantRequest,CreateCandidateDependantRequest> {
    private final CandidateDependantService candidateDependantService;

    public CandidateDependantAdminApi(
            CandidateDependantService candidateDependantService) {
        this.candidateDependantService = candidateDependantService;
    }

    /**
     * Creates a new candidate dependant record from the data in the given
     * request.
     * @param candidateId ID of candidate
     * @param request Request containing dependant details
     * @return Created record - including database id of dependant record
     * @throws NoSuchObjectException if the there is no Candidate record with
     * that candidateId or no Nationality with the id given in the request
     */
    @Override
    public @NotNull Map<String, Object> create(
            long candidateId, @Valid CreateCandidateDependantRequest request)
            throws NoSuchObjectException {
        CandidateDependant candidateDependant =
                this.candidateDependantService
                        .createDependant(candidateId, request);
        return candidateDependantDto().build(candidateDependant);
    }

    /**
     * Delete the candidate dependant with the given id.
     * @param id ID of record to be deleted
     * @return True if record was deleted, false if it was not found.
     * @throws EntityReferencedException if the object cannot be deleted because
     * it is referenced by another object.
     * @throws InvalidRequestException if not authorized to delete this list.
     */
    @Override
    public boolean delete(long id)
            throws EntityReferencedException, InvalidRequestException {
        return candidateDependantService.deleteDependant(id);
    }

    private DtoBuilder candidateDependantDto() {
        return new DtoBuilder()
                .add("id")
                .add("relation")
                .add("dob")
                .add("healthConcern")
                .add("notes")
                ;
    }


}
