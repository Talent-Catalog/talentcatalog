/*
 * Copyright (c) 2025 Talent Catalog.
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

package org.tctalent.server.util;

import org.tctalent.server.model.db.CandidateOpportunityStage;

/**
 * Utilities related to translations
 *
 * @author John Cameron
 */
public class TranslationHelper {

    /**
     * Returns the translation keys corresponding to the description of the given Candidate
     * Opportunity (Case) stage.
     * <p/>
     * These keys can be passed to TranslationService translate methods to fetch the stage
     * description.
     * @param stage Case stage
     * @return Translation keys
     */
    public static String[] getCaseStageTranslationKeys(CandidateOpportunityStage stage) {
        return new String[] {"CASE-STAGE", stage.name().toUpperCase()};
    }

}
