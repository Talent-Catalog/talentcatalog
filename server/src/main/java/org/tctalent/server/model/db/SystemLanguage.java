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

package org.tctalent.server.model.db;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.SequenceGenerator;
import jakarta.persistence.Table;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;
import org.tctalent.server.util.locale.LocaleHelper;

/**
 * These are the various languages that are supported by the candidate portal (front end).
 * <p/>
 * Adding a new language:
 * <ul>
 *     <li>
 *         Use tool like Phrase to generate translations. Export file as Nested JSON
 *     </li>
 *     <li>
 *         Upload JSON file to Amazon S3 named as [two character language code].json
 *     </li>
 *     <li>
 *         Add new entity (table entry) for the new language to this table.
 *     </li>
 *     <li>
 *         On the front end Angular code for candidate-portal update the isSelectedLanguageRtl
 *         method in the LanguageService - depending on whether or not the language is right to left.
 *     </li>
 *     <li>
 *         Add new translations for the various drop downs (eg Countries) in the back end Settings.
 *     </li>
 * </ul>
 */
@Getter
@Setter
@Entity
@Table(name = "system_language")
@SequenceGenerator(name = "seq_gen", sequenceName = "system_language_id_seq", allocationSize = 1)
public class SystemLanguage extends AbstractAuditableDomainObject<Long> {

    private String language;
    private String label;

    @Transient
    private boolean rtl;

    @Enumerated(EnumType.STRING)
    private Status status;

    public SystemLanguage() {
    }

    public SystemLanguage(String language) {
        this.language = language;
        this.status = Status.active;
        this.label = LocaleHelper.getOwnLanguageDisplayName(language);
    }

    public boolean isRtl() {
        return LocaleHelper.isRtlLanguage(language);
    }
}
