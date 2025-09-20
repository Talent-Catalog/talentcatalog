/*
 * [License]
 */

package org.tctalent.server.repository.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.tctalent.server.model.db.CandidateFormInstanceKey;
import org.tctalent.server.model.db.CandidateTravelDocForm;

@RepositoryRestResource(path = "travel-doc-form")
public interface TravelDocFormRepository
    extends JpaRepository<CandidateTravelDocForm, CandidateFormInstanceKey> {
}
