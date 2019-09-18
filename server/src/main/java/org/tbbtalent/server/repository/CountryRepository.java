package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tbbtalent.server.model.Country;

public interface CountryRepository extends JpaRepository<Country, Long> {

}
