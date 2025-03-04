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

package org.tctalent.server.service.db;

import java.util.List;
import java.util.Map;
import org.springframework.lang.Nullable;
import org.tctalent.server.model.db.AbstractTranslatableDomainObject;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.Translation;
import org.tctalent.server.model.db.User;
import org.tctalent.server.request.translation.CreateTranslationRequest;
import org.tctalent.server.request.translation.UpdateTranslationRequest;
import org.tctalent.server.util.dto.DtoBuilder;

/**
 * THere are two different ways of doing translations:
 * <ol>
 *     <li>Translations of english values stored in the database/entities. These translations are
 *     stored in the translation table/entity - see {@link Translation}</li>
 *     <li>Translations of keys in the Angular HTML code. These translations are driven by
 *     key/values selected by language which are stored as JSON files on Amazon S3 and uploaded
 *     as requested to the Angular front end where the ngx_translation module does the
 *     key substitution into the generated HTML.</li>
 * </ol>
 */
public interface TranslationService {

    //Database translations of standard English values

    /**
     * Create a translation.
     * @param user User (admin) who generated this translation
     * @param request Request containing details of the translation
     * @return Translation entity
     */
    Translation createTranslation(User user, CreateTranslationRequest request);

    /**
     * Same as {@link #createTranslation(User, CreateTranslationRequest)} where user is passed
     * in as null.
     */
    Translation createTranslation(CreateTranslationRequest request);

    /**
     * Deletes all translations for a given language and type of data (eg "country").
     * @param langCode Language
     * @param objectType Type of data - ie table - eg translations of country table values.
     */
    void deleteTranslations(String langCode, String objectType);

    /**
     * Translates the given entities - ie table values.
     * <p/>
     * Note that every standard database table entity extends AbstractTranslatableDomainObject,
     * which has an id and name (the String being translated) as well as a Transient
     * translatedName field which is used to hold the translation.
     * The transient fields are populated in services such as {@link CountryService}.
     * <p/>
     * Note also that {@link DtoBuilder} is coded to substitute
     * the "name" property with the "translatedName" property when requested. This is how
     * translated values are magically sent up to the Angular front end.
     * @param entities List of entities retrieved from the database
     * @param objectType Object type/table - eg "country"
     * @param language Required language
     * @param <T> Entity class - eg {@link Country}
     */
    <T extends AbstractTranslatableDomainObject<Long>> void translate(
        List<T> entities, String objectType, String language);

    /**
     * Same as {@link #translate(List, String, String)} where the language is set to the language
     * associated with the currently logged in user (candidate).
     */
    <T extends AbstractTranslatableDomainObject<Long>> void translate(
        List<T> entities, String objectType);

    /**
     * Updates a given translation on the database.
     * @param id ID of translation to be updated
     * @param request Containing the new translated value
     * @return Updated translation
     */
    Translation updateTranslation(long id, UpdateTranslationRequest request);


    //Translation key values retrieved from JSON language files on Amazon server

    /**
     * Returns a Map corresponding to the nested JSON translation file stored on Amazon for the
     * given language.
     * <p/>
     * Note that a "nested" JSON translation file stores keys in a nested fashion.
     * For example:
     * HEADER.NAV.LOGOUT = "Logout" (which is unnested)
     * would be stored as
     * {"HEADER":{"NAV":{"LOGOUT":"Logout"...}}}
     * which would be coded as Map HEADER -> Map NAV -> Map LOGOUT -> Logout
     * @param language Language
     * @return Map storing nested key/values
     */
    Map<String, Object> getTranslationFile(String language);

    /**
     * Look up the translation using the given nesting keys
     * @param translations Nested key translations
     * @param keys Keys
     * @return Translation if one found, otherwise null
     */
    @Nullable
    String translate(Map<String, Object> translations, String... keys);

    /**
     * Look up the English translation using the given nesting keys
     * @param keys Keys
     * @return Translation if one found, otherwise null
     */
    String translateToEnglish(String... keys);

    /**
     * Updates the nested JSON translation file stored on Amazon for the given language with the
     * given key/value translations.
     * @param language Language
     * @param translations Nested key/values
     */
    void updateTranslationFile(String language, Map<String, Object> translations);
}
