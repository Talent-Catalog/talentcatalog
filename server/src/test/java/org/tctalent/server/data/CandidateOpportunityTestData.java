/*
 * Copyright (c) 2025 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.data;

import static org.tctalent.server.data.SalesforceJobOppTestData.getSalesforceJobOppMinimal;
import static org.tctalent.server.data.UserTestData.getAdminUser;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.CandidateOpportunityStage;
import org.tctalent.server.request.candidate.opportunity.CandidateOpportunityParams;
import org.tctalent.server.request.candidate.UpdateCandidateOppsRequest;

public class CandidateOpportunityTestData {

    public static CandidateOpportunity getCandidateOpp() {
        CandidateOpportunity op = new CandidateOpportunity();
        op.setCandidate(CandidateTestData.getCandidate());
        op.setJobOpp(getSalesforceJobOppMinimal());
        op.setClosingCommentsForCandidate("Some closing comments for candidate");
        op.setEmployerFeedback("Some employer feedback");
        op.setStage(CandidateOpportunityStage.offer);
        op.setNextStep("Next step");
        op.setRelocatingDependantIds(List.of(1L, 2L));
        return op;
    }

    /**
     * Holds an {@link UpdateCandidateOppsRequest} along with the expected {@link CandidateOpportunity}
     * that should result from applying the request.
     */
    public record CreateUpdateCandidateOppTestData(
        UpdateCandidateOppsRequest request,
        CandidateOpportunity expectedOpp
    ) {}

    /**
     * Constructs a {@link CreateUpdateCandidateOppTestData record containing an
     * {@link UpdateCandidateOppsRequest} and the expected {@link CandidateOpportunity} that should
     * result from using it.
     */
    public static CreateUpdateCandidateOppTestData createUpdateCandidateOppRequestAndExpectedOpp() {
        final String sfJobOppId = "UUUU8888800000YYYYIIII";
        final Set<Long> candidateIds = Set.of(1L, 2L);
        final String nextStep = "Next step";
        final String closingComments = "Closing comments";
        final String employerFeedback = "Employer feedback";
        final LocalDate nextStepDueDate = LocalDate.of(2026, 1, 1);

        final CandidateOpportunityParams params = new CandidateOpportunityParams();
        params.setNextStep(nextStep);
        params.setNextStepDueDate(nextStepDueDate);
        params.setStage(CandidateOpportunityStage.notFitForRole);
        params.setClosingComments(closingComments);
        params.setEmployerFeedback(employerFeedback);

        UpdateCandidateOppsRequest request = new UpdateCandidateOppsRequest();
        request.setSfJobOppId(sfJobOppId);
        request.setCandidateIds(candidateIds);
        request.setCandidateOppParams(params);

        CandidateOpportunity opp = getCandidateOpp();
        opp.setNextStep(nextStep);
        opp.setNextStepDueDate(nextStepDueDate);
        opp.setStage(CandidateOpportunityStage.notFitForRole);
        opp.setClosingComments(closingComments);
        opp.setEmployerFeedback(employerFeedback);
        opp.setUpdatedBy(getAdminUser());

        return new CreateUpdateCandidateOppTestData(request, opp);
    }

}
