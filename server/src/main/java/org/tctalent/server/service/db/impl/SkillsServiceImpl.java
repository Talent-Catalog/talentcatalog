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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.SkillsEscoEn;
import org.tctalent.server.model.db.SkillsTechOnetEn;
import org.tctalent.server.repository.db.SkillsEscoEnRepository;
import org.tctalent.server.repository.db.SkillsTechOnetEnRepository;
import org.tctalent.server.service.api.ExtractSkillsRequest;
import org.tctalent.server.service.api.SkillName;
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
    private final SkillsTechOnetEnRepository skillsTechOnetEnRepository;
    private List<SkillName> cachedList;

    private final static int CHUNK_SIZE = 100;
    private final static String DELIMITER = "\n";
    private final static int INITIAL_CAPACITY = 30_000;

    @Override
    public List<SkillName> extractSkillNames(@NonNull String text, @NonNull String languageCode) {
        checkLanguageAvailability(languageCode);
        ExtractSkillsRequest request = new ExtractSkillsRequest();
        request.setText(text);
        request.setLang(languageCode);
        return skillsExtractionService.extractSkills(request);
    }

    @Override
    public Page<SkillName> getSkillNames(PageRequest request, @NonNull String languageCode) {
        List<SkillName> skillNames = getSkillNames(languageCode);

        final int fromIndex = (int) request.getOffset();
        final int toIndex = (int) Math.min(skillNames.size(),
            request.getOffset() + request.getPageSize());
        if (toIndex <= fromIndex) {
            //Requesting non existent page. Return empty content.
            return new PageImpl<>(List.of(), request, skillNames.size());
        }

        //Extract sub list as content.
        List<SkillName> content = skillNames.subList(fromIndex, toIndex);

        return new PageImpl<>(content, request, skillNames.size());
    }

    @Override
    public @NonNull List<SkillName> getSkillNames(@NonNull String languageCode) {
        checkLanguageAvailability(languageCode);

        if (cachedList == null) {
            //Use set to remove duplicates
            Set<SkillName> skills = new HashSet<>(INITIAL_CAPACITY);

            loadEscoSkills(skills);
            loadOnetSkills(skills);

            //Copy into an immutable list so that it can be shared around.
            cachedList = List.copyOf(skills);
        }

        return cachedList;
    }

    private void loadOnetSkills(Set<SkillName> skillNames) {
        PageRequest request = PageRequest.ofSize(CHUNK_SIZE);
        request = request.first();
        Page<SkillsTechOnetEn> page;
        do {
            //Get page of skills
            page = skillsTechOnetEnRepository.findAll(request);
            //Process skills in page
            final List<SkillsTechOnetEn> pageContent = page.getContent();
            for (SkillsTechOnetEn skillsTechOnetEn : pageContent) {
                addSkills(skillNames, skillsTechOnetEn);
            }
            request = request.next();
        } while (page.hasNext());
    }

    private void loadEscoSkills(Set<SkillName> skillNames) {
        PageRequest request = PageRequest.ofSize(CHUNK_SIZE);
        request = request.first();
        Page<SkillsEscoEn> page;
        do {
            //Get page of skills
            page = skillsEscoEnRepository.findBySkilltype("knowledge", request);
            //Process skills in page
            final List<SkillsEscoEn> pageContent = page.getContent();
            for (SkillsEscoEn skillsEscoEn : pageContent) {
                addSkills(skillNames, skillsEscoEn);
            }
            request = request.next();
        } while (page.hasNext());
    }

    /**
     * Adds ESCO skill names
     * @param skillNames Collection of skill names to add to
     * @param rec ESCO skill record
     */
    private void addSkills(Collection<SkillName> skillNames, SkillsEscoEn rec) {

        final String preferredlabel = rec.getPreferredlabel();
        if (!ObjectUtils.isEmpty(preferredlabel)) {
            skillNames.add(new SkillName(Locale.ENGLISH.getLanguage(),
                preferredlabel.toLowerCase(Locale.ENGLISH)));
        }

        final String altLabels = rec.getAltlabels();
        if (!ObjectUtils.isEmpty(altLabels)) {
            addDelimitedSkills(skillNames, altLabels.toLowerCase(Locale.ENGLISH));
        }

        final String hiddenLabels = rec.getHiddenlabels();
        if (!ObjectUtils.isEmpty(hiddenLabels)) {
            addDelimitedSkills(skillNames, hiddenLabels.toLowerCase(Locale.ENGLISH));
        }
    }

    /**
     * Adds ONET Tech skill names
     * @param skillNames Collection of skill names to add to
     * @param rec ONET tech skill record
     */
    private void addSkills(Collection<SkillName> skillNames, SkillsTechOnetEn rec) {
        final String example = rec.getExample();
        if (!ObjectUtils.isEmpty(example)) {
            String lower = example.toLowerCase(Locale.ENGLISH);
            final SkillName sn = new SkillName(Locale.ENGLISH.getLanguage(), lower);
            skillNames.add(sn);
        }
    }

    /**
     * Some fields in the ESCO database consist of multiple skills in a single text field
     * delimited by newline characters.
     * @param skills List of skills we are accumulating
     * @param textWithDelimitedSkills text containing multiple skills separated by delimiter
     */
    private void addDelimitedSkills(Collection<SkillName> skills, String textWithDelimitedSkills) {
        if (!ObjectUtils.isEmpty(textWithDelimitedSkills)) {
            final String[] split = textWithDelimitedSkills.split(DELIMITER);
            for (String s : split) {
                if (!ObjectUtils.isEmpty(s)) {
                    skills.add(
                        new SkillName(Locale.ENGLISH.getLanguage(), s.toLowerCase(Locale.ENGLISH)));
                }
            }
        }
    }

    private void checkLanguageAvailability(String languageCode) {
        if (!Locale.ENGLISH.getLanguage().equals(languageCode)) {
            throw new NoSuchObjectException(SkillsService.class, languageCode);
        }
    }
}
