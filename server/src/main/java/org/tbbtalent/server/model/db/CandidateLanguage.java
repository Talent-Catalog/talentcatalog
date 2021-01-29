/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

package org.tbbtalent.server.model.db;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;

@Entity
@Table(name = "candidate_language")
@SequenceGenerator(name = "seq_gen", sequenceName = "candidate_language_id_seq", allocationSize = 1)
public class CandidateLanguage  extends AbstractDomainObject<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "candidate_id")
    private Candidate candidate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "language_id")
    private Language language;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "written_level_id")
    private LanguageLevel writtenLevel;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "spoken_level_id")
    private LanguageLevel spokenLevel;

    private String migrationLanguage;

    public CandidateLanguage() {
    }

    public CandidateLanguage(Candidate candidate, Language language, LanguageLevel writtenLevel, LanguageLevel spokenLevel) {
        this.candidate = candidate;
        this.language = language;
        this.writtenLevel = writtenLevel;
        this.spokenLevel = spokenLevel;
    }

    public Candidate getCandidate() {
        return candidate;
    }

    public void setCandidate(Candidate candidate) {
        this.candidate = candidate;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public LanguageLevel getWrittenLevel() {
        return writtenLevel;
    }

    public void setWrittenLevel(LanguageLevel writtenLevel) {
        this.writtenLevel = writtenLevel;
    }

    public LanguageLevel getSpokenLevel() {
        return spokenLevel;
    }

    public void setSpokenLevel(LanguageLevel spokenLevel) {
        this.spokenLevel = spokenLevel;
    }

    public String getMigrationLanguage() {
        return migrationLanguage;
    }

    public void setMigrationLanguage(String migrationLanguage) {
        this.migrationLanguage = migrationLanguage;
    }
}

