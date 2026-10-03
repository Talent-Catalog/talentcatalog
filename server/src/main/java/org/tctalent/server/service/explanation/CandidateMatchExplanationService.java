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
     * <p>If {@code jobId} is supplied, the corresponding Talent Catalog job is loaded (throwing
     * {@link NoSuchObjectException} if it doesn't exist) and the generated explanation is
     * persisted against the (candidate, job) pair, replacing any explanation already persisted
     * for that pair. If {@code jobId} is null, the explanation is generated and returned but not
     * persisted. Either way, {@code opportunityDescription} - not anything derived from the job -
     * is always the text sent to the explanation service.</p>
     *
     * @param candidateId ID of the candidate to explain
     * @param jobId ID of the Talent Catalog job this request relates to, or null if the request
     * is not associated with a specific Talent Catalog job. This is carried through as TC context
     * for persistence purposes - it is not sent to the Python explanation service and is not used
     * to look up or derive {@code opportunityDescription}.
     * @param opportunityDescription description of the opportunity/job to compare against
     * @return generated match explanation
     * @throws NoSuchObjectException if no candidate is found with the given id, or if a non-null
     * {@code jobId} does not correspond to an existing Talent Catalog job
     * @throws MatchExplanationException if the explanation could not be generated, for example
     * because the Python match explanation service returned no result, an item-level error, a
     * mismatched candidate ID, or could not be reached
     */
    @NonNull
    CandidateMatchExplanation generateExplanation(long candidateId, @Nullable Long jobId,
        @NonNull String opportunityDescription) throws NoSuchObjectException, MatchExplanationException;

    /**
     * Retrieves a previously persisted candidate/job match explanation.
     * <p>This does NOT regenerate the explanation - it is a pure lookup of whatever was last
     * persisted by {@link #generateExplanation}, and does not call the Python explanation
     * service.</p>
     *
     * @param candidateId ID of the candidate
     * @param jobId ID of the Talent Catalog job
     * @return the persisted match explanation for that candidate/job pair
     * @throws NoSuchObjectException if no explanation has been persisted for that candidate/job
     * pair
     */
    @NonNull
    CandidateMatchExplanation getPersistedExplanation(long candidateId, long jobId)
        throws NoSuchObjectException;
}
