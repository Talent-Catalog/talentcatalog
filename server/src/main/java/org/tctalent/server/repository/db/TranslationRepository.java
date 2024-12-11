/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.repository.db;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.tctalent.server.model.db.Translation;

import java.util.List;
import java.util.Optional;

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

    @Query("select t from Translation t "
            + " where t.objectId = (:objectId)"
            + " and t.objectType = (:objectType)"
            + " and t.language = (:lang)")
    Optional<Translation> findByObjectIdTypeLang(@Param("objectId") Long objectId,
                                                 @Param("objectType") String objectType,
                                                 @Param("lang") String lang);

    @Modifying
    @Query("delete from Translation t where"
        + " t.objectType = (:objectType)"
        + " and t.language = (:langCode)")
    void deleteTranslations(@Param("langCode") String langCode, @Param("objectType") String objectType);
}
