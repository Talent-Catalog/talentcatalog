package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tbbtalent.server.model.EduType;

public interface EducationTypeRepository extends JpaRepository<EduType, Long> {

}
