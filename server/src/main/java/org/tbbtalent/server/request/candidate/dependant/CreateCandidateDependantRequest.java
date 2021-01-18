package org.tbbtalent.server.request.candidate.dependant;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.springframework.lang.Nullable;
import org.tbbtalent.server.model.db.DependantRelations;
import org.tbbtalent.server.model.db.YesNo;

import java.time.LocalDate;

@Getter
@Setter
@ToString
public class CreateCandidateDependantRequest {

    @Nullable
    private DependantRelations relation;

    @Nullable
    private LocalDate dob;

    @Nullable
    private YesNo healthConcern;

    @Nullable
    private String notes;
}
