/*
 * [License]
 */

package org.tctalent.server.repository.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tctalent.server.model.db.CandidateFormInstanceKey;
import org.tctalent.server.model.db.TravelDocForm;

public interface TravelDocFormRepository extends JpaRepository<TravelDocForm, CandidateFormInstanceKey> {
}