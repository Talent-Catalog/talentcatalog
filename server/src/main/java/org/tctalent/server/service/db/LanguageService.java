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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.tctalent.server.exception.EntityExistsException;
import org.tctalent.server.exception.EntityReferencedException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Language;
import org.tctalent.server.model.db.SystemLanguage;
import org.tctalent.server.request.language.CreateLanguageRequest;
import org.tctalent.server.request.language.SearchLanguageRequest;
import org.tctalent.server.request.language.UpdateLanguageRequest;
import org.tctalent.server.response.DatePickerNames;

public interface LanguageService {

    /**
     * Adds a new system language - ie a candidate portal language a user can select
     * @param langCode Code of language to add
     * @return Added SystemLanguage
     * @throws EntityExistsException If the language already exists as a system language
     * @throws NoSuchObjectException If the language code is not known
     */
    SystemLanguage addSystemLanguage(String langCode)
        throws EntityExistsException, NoSuchObjectException;

    /**
     * Adds translations of values from the given table.
     * <p/>
     * Replaces any existing translations for the given langCode and tableName.
     * <p/>
     * The translations are in an input stream of comma separated data with the following format:
     * <p/>
     * [id],[translated value]
     * <p/>
     * where [id] is the id in the given table of the value being translated and [translated value]
     * is the translation.
     * @param langCode Language being translated to
     * @param tableName Name of table whose values are being translated - eg 'occupation'
     * @param translations Input stream containing translations
     * @return SystemLanguage of translated data
     * @throws NoSuchObjectException If no system language for the given langCode has been set up.
     * @throws IOException if there is a problem reading the translations, or they are badly
     * formatted.
     */
    SystemLanguage addSystemLanguageTranslations(
        String langCode, String tableName, InputStream translations)
        throws IOException, NoSuchObjectException;

    /**
     * Find language matching given
     * <a href="https://en.wikipedia.org/wiki/List_of_ISO_639_language_codes">ISO code</a>.
     * @param isoCode Language ISO code
     * @return language or null if none found
     * @throws NoSuchObjectException if not found
     */
    @NonNull
    Language findByIsoCode(String isoCode);

    List<Language> listLanguages();

    Page<Language> searchLanguages(SearchLanguageRequest request);

    /**
     * Retrieve the abbreviated day and month names as used in the Angular date picker component
     * appropriate for the given language.
     * @param lang Language code - eg 'en' for English.
     * @return Language sensitive abbreviated names for date picker component.
     */
    DatePickerNames getDatePickerNames(String lang);

    /**
     * Find language matching given id.
     * @param id Language id
     * @return language
     * @throws NoSuchObjectException if not found
     */
    Language getLanguage(long id);

    Language createLanguage(CreateLanguageRequest request) throws EntityExistsException;

    Language updateLanguage(long id, UpdateLanguageRequest request) throws EntityExistsException ;

    boolean deleteLanguage(long id) throws EntityReferencedException;

    List<SystemLanguage> listSystemLanguages();

    Language getLanguage(String languageName);

    /**
     * Sets the language ISO codes of all countries with names matching the English language names
     * returned by Java's Locale class.
     * @return String containing names of languages in the data base which did not find a match
     * among the names returned by Locale - and which, therefore, did not have their ISO code set.
     */
    String updateIsoCodes();
}
