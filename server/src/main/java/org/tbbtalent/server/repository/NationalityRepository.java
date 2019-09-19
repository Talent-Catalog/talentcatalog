package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tbbtalent.server.model.Nationality;

public interface NationalityRepository extends JpaRepository<Nationality, Long> {

}
