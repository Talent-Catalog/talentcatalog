package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tbbtalent.server.model.EducationType;

public interface EducationTypeRepository extends JpaRepository<EducationType, Long> {

}
