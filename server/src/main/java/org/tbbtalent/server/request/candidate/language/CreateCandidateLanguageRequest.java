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

package org.tbbtalent.server.request.candidate.language;

public class CreateCandidateLanguageRequest {

    private Long languageId;
    private Long writtenLevelId;
    private Long spokenLevelId;

    public Long getLanguageId() { return languageId; }

    public void setLanguageId(Long languageId) { this.languageId = languageId; }

    public Long getWrittenLevelId() { return writtenLevelId; }

    public void setWrittenLevelId(Long writtenLevelId) { this.writtenLevelId = writtenLevelId; }

    public Long getSpokenLevelId() { return spokenLevelId; }

    public void setSpokenLevelId(Long spokenLevelId) { this.spokenLevelId = spokenLevelId;
    }
}
