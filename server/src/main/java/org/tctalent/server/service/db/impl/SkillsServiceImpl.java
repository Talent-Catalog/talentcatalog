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

package org.tctalent.server.service.db.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.tctalent.server.model.db.SkillsEscoEn;
import org.tctalent.server.service.db.SkillsService;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkillsServiceImpl implements SkillsService {
    private final SkillsService skillsService;
    private List<String> cachedList;

    private final static String DELIMITER = "\n";

    @Override
    public @NonNull List<String> getEscoSkills() {

        if (cachedList == null) {
            List<String> skills = new ArrayList<>(20_000); //todo How big should this be?

            //TODO JC Get page of skills
                //TODO JC Process skills in page
                    SkillsEscoEn see = new SkillsEscoEn(); //todo read from page
                    addSkills(skills, see);

            cachedList = Collections.unmodifiableList(skills);
        }

        return cachedList;
    }

    private void addSkills(List<String> skills, SkillsEscoEn see) {
        skills.add(see.getPreferredlabel());

        addDelimitedSkills(skills, see.getAltlabels());

        addDelimitedSkills(skills, see.getHiddenlabels());
    }

    private void addDelimitedSkills(List<String> skills, String delimitedLabels) {
        if (ObjectUtils.isEmpty(delimitedLabels)) {
            skills.addAll(List.of(delimitedLabels.split(DELIMITER)));
        }
    }
}
