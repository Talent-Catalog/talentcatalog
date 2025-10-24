// Copyright 2008 Orc Software AB. All rights reserved.
// Reproduction in whole or in part in any form or medium without express
// written permission of Orc Software AB is strictly prohibited.

package org.tctalent.server.service.db;

import java.util.List;

/**
 * Service which extracts skills from text.
 *
 * @author John Cameron
 */
public interface SkillsExtractionService {

    /**
     * Extracts skills as Strings from the given text.
     * @param text Text to extract skills from - for example, from a CV or Job Description
     * @return List of skill names extracted from the text
     */
    List<String> extractSkillsFromText(String text);
}
