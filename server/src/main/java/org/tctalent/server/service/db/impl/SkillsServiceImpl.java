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

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.tctalent.server.model.db.SkillsEscoEn;
import org.tctalent.server.repository.db.SkillsEscoEnRepository;
import org.tctalent.server.service.api.ExtractSkillsRequest;
import org.tctalent.server.service.api.ExtractSkillsResponse;
import org.tctalent.server.service.api.TcSkillsExtractionService;
import org.tctalent.server.service.db.SkillsService;

/**
 * This service imports skills from
 * <a href="https://esco.ec.europa.eu/en/use-esco/download">ESCO</a>
 * which requires the following acknowledgements:
 * <ul>
 *     <li>
 * For services, tools and applications integrating totally or partially ESCO:
 * "This service uses the ESCO classification of the European Commission."
 *     </li>
 *     <li>
 * For other documents such as studies, analysis or reports making use of ESCO:
 * "This publication uses the ESCO classification of the European Commission."
 *     </li>
 * </ul>
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class SkillsServiceImpl implements SkillsService {
    private final TcSkillsExtractionService skillsExtractionService;
    private final SkillsEscoEnRepository skillsEscoEnRepository;
    private List<String> cachedList;

    private final static int CHUNK_SIZE = 100;
    private final static String DELIMITER = "\n";
    private final static int INITIAL_CAPACITY = 25_000;

    @Override
    public List<String> extractSkills(String text) {
        ExtractSkillsRequest request = new ExtractSkillsRequest();
        request.setText(text);
        final ExtractSkillsResponse response = skillsExtractionService.extractSkills(request);
        return response.getSkills();
    }

    @Override
    public @NonNull List<String> getSkills() {

        if (cachedList == null) {
            Set<String> skills = new HashSet<>(INITIAL_CAPACITY);

            loadEscoSkills(skills);
            loadOnetSkills(skills);

            //Copy into an immutable list so that it can be shared around.
            cachedList = List.copyOf(skills);
        }

        return cachedList;
    }

    private void loadOnetSkills(Set<String> skills) {
        //TODO JC Implement loadOnetSkills
    }

    private void loadEscoSkills(Set<String> skills) {
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
    }

    private void addSkills(Collection<String> skills, SkillsEscoEn see) {

        final String preferredlabel = see.getPreferredlabel();
        if (!ObjectUtils.isEmpty(preferredlabel)) {
            skills.add(preferredlabel.toLowerCase(Locale.ENGLISH));
        }

        final String altLabels = see.getPreferredlabel();
        if (!ObjectUtils.isEmpty(altLabels)) {
            addDelimitedSkills(skills, altLabels.toLowerCase(Locale.ENGLISH));
        }

        final String hiddenLabels = see.getPreferredlabel();
        if (!ObjectUtils.isEmpty(hiddenLabels)) {
            addDelimitedSkills(skills, hiddenLabels.toLowerCase(Locale.ENGLISH));
        }
    }

    /**
     * Some fields in the ESCO database consist of multiple skills in a single text field
     * delimited by newline characters.
     * @param skills List of skills we are accumulating
     * @param textWithDelimitedSkills text containing multiple skills separated by delimiter
     */
    private void addDelimitedSkills(Collection<String> skills, String textWithDelimitedSkills) {
        if (!ObjectUtils.isEmpty(textWithDelimitedSkills)) {
            skills.addAll(List.of(textWithDelimitedSkills.split(DELIMITER)));
        }
    }
}
