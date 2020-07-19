/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

package org.tbbtalent.server.repository.db;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tbbtalent.server.model.db.Translation;

public interface TranslationRepository extends JpaRepository<Translation, Long>, JpaSpecificationExecutor<Translation> {

    @Query("select t from Translation t "
            + " where t.objectType = :type "
            + " and t.language = :selectedLanguage ")
    List<Translation> findByTypeLanguage(@Param("type") String type,
                                         @Param("selectedLanguage") String selectedLanguage);
    
    @Query("select t from Translation t "
                    + " where t.objectId in (:countryIds) "
                    + " and t.objectType = :type "
                    + " and t.language = :selectedLanguage ")
    List<Translation> findByIdsTypeLanguage(@Param("countryIds") List<Long> countryIds,
                                            @Param("type") String type,
                                            @Param("selectedLanguage") String selectedLanguage);

}
