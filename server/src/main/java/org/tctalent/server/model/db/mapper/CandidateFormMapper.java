// Copyright 2008 Orc Software AB. All rights reserved.
// Reproduction in whole or in part in any form or medium without express
// written permission of Orc Software AB is strictly prohibited.

package org.tctalent.server.model.db.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;
import org.tctalent.server.model.db.CandidateForm;
import org.tctalent.server.request.form.UpdateCandidateFormRequest;

@Mapper
public interface CandidateFormMapper {
    void updateFromRequest(
        UpdateCandidateFormRequest request, @MappingTarget CandidateForm candidateForm);

}
