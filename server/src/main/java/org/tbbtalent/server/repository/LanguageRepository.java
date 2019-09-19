package org.tbbtalent.server.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.tbbtalent.server.model.Language;

public interface LanguageRepository extends JpaRepository<Language, Long> {

}
