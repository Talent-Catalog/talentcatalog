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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.tctalent.server.model.db.SkillsEscoEn;
import org.tctalent.server.repository.db.SkillsEscoEnRepository;
import org.tctalent.server.service.db.SkillsService;

@Service
@RequiredArgsConstructor
@Slf4j
public class SkillsServiceImpl implements SkillsService {
    private final SkillsEscoEnRepository skillsEscoEnRepository;
    private List<String> cachedList;

    private final static int CHUNK_SIZE = 100;
    private final static String DELIMITER = "\n";
    private final static int INITIAL_CAPACITY = 25_000;  //todo How big should this be?

    @Override
    public @NonNull List<String> getEscoSkills() {

        if (cachedList == null) {
            List<String> skills = new ArrayList<>(INITIAL_CAPACITY);
            //TODO JC Could this be a hashSet to eliminate duplicates?
            PageRequest request = PageRequest.ofSize(CHUNK_SIZE);
            request = request.first();
            Page<SkillsEscoEn> page;
            do {
                //Get page of skills
                page = skillsEscoEnRepository.findBySkilltype("knowledge", request);
                //Process skills in page
                final List<SkillsEscoEn> pageContent = page.getContent();
                for (SkillsEscoEn skillsEscoEn : pageContent) {
                    addSkills(skills, skillsEscoEn);
                }
                request = request.next();
            } while (page.hasNext());

            cachedList = Collections.unmodifiableList(skills);
        }

        return cachedList;
    }

    private void addSkills(List<String> skills, SkillsEscoEn see) {
        skills.add(see.getPreferredlabel());

        addDelimitedSkills(skills, see.getAltlabels());

        addDelimitedSkills(skills, see.getHiddenlabels());
    }

    /**
     * Some fields in the ESCO database consist of multiple skills in a single text field
     * delimited by newline characters.
     * @param skills List of skills we are accumulating
     * @param textWithDelimitedSkills text containing multiple skills separated by delimiter
     */
    private void addDelimitedSkills(List<String> skills, String textWithDelimitedSkills) {
        if (!ObjectUtils.isEmpty(textWithDelimitedSkills)) {
            skills.addAll(List.of(textWithDelimitedSkills.split(DELIMITER)));
        }
    }
}
