package org.tbbtalent.server.request.candidate.destination;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;
import org.tbbtalent.server.model.db.Country;
import org.tbbtalent.server.model.db.FamilyRelations;
import org.tbbtalent.server.model.db.YesNoUnsure;

@Getter
@Setter
@ToString
public class CreateCandidateDestinationRequest {
    @Nullable
    private Country country;

    @Nullable
    private YesNoUnsure interest;

    @Nullable
    private FamilyRelations family;

    @Nullable
    private String location;

    @Nullable
    private String notes;
}
