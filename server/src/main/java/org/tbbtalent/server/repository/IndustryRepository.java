package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tbbtalent.server.model.Industry;

public interface IndustryRepository extends JpaRepository<Industry, Long> {

}
