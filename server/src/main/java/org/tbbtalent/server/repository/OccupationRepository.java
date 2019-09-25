package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tbbtalent.server.model.Industry;
import org.tbbtalent.server.model.Occupation;

public interface OccupationRepository extends JpaRepository<Occupation, Long> {

}
