package org.tbbtalent.server.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.tbbtalent.server.model.Translation;

public interface TranslationRepository extends JpaRepository<Translation, Long>, JpaSpecificationExecutor<Translation> {

    @Query("select t from Translation t "
            + " where t.objectType = ?1 "
            + " and t.language = ?2 ")
    List<Translation> findByTypeLanguage(String type,
                                         String selectedLanguage);
    
    @Query("select t from Translation t "
                    + " where t.objectId in (?1) "
                    + " and t.objectType = ?2 "
                    + " and t.language = ?3 ")
    List<Translation> findByIdsTypeLanguage(List<Long> countryIds,
                                            String type,
                                            String selectedLanguage);

}
