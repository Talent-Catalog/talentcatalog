package org.tctalent.server.service.explanation;

import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.tctalent.server.exception.MatchExplanationException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.response.CandidateMatchExplanation;

public interface CandidateMatchExplanationService {

    /**
     * Generates an on-demand explanation of how a candidate's existing job experience relates to
     * the given opportunity description.
     * <p>This compares the supplied opportunity description with the candidate's existing job
     * experience text. It does not perform candidate matching or rerun any Best-N matching
     * algorithm.</p>
     *
     * @param candidateId ID of the candidate to explain
     * @param jobId ID of the Talent Catalog job this request relates to, or null if the request
     * is not associated with a specific Talent Catalog job. This is carried through purely as TC
     * context (e.g. for possible future persistence, caching or auditing) - it is not sent to the
     * Python explanation service, is not used to look up or derive {@code opportunityDescription},
     * and the referenced job is not required to exist.
     * @param opportunityDescription description of the opportunity/job to compare against
     * @return generated match explanation
     * @throws NoSuchObjectException if no candidate is found with the given id
     * @throws MatchExplanationException if the explanation could not be generated, for example
     * because the Python match explanation service returned no result, an item-level error, a
     * mismatched candidate ID, or could not be reached
     */
    @NonNull
    CandidateMatchExplanation generateExplanation(long candidateId, @Nullable Long jobId,
        @NonNull String opportunityDescription) throws NoSuchObjectException, MatchExplanationException;
}
