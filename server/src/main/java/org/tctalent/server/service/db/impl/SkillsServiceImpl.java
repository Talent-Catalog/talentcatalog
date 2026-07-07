/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
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
import org.tctalent.server.model.db.SkillsTcEn;
import org.tctalent.server.model.db.SkillsTechOnetEn;
import org.tctalent.server.repository.db.SkillsEscoEnRepository;
import org.tctalent.server.repository.db.SkillsTcEnRepository;
import org.tctalent.server.repository.db.SkillsTechOnetEnRepository;
import org.tctalent.server.service.api.ExtractSkillsRequest;
import org.tctalent.server.service.api.SkillName;
import org.tctalent.server.service.api.TcSkillsExtractionService;
import org.tctalent.server.service.db.SkillsService;

/**
 * This service manages all known skills. Skills can be single words or short phrases.
 * There could be around 20,000 of them.
 * The skills are loaded from the database and cached in memory.
 * <p>
 *     The skills can come from multiple sources (for example Esco and ONet) which have been copied
 *     into our local database.
 *     In addition, there is a table for extra skills that TC users have identified that are not in
 *     the other sources.
 * </p>
 *
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
    private final SkillsTcEnRepository skillsTcEnRepository;
    private final SkillsTechOnetEnRepository skillsTechOnetEnRepository;

    /**
     * Cached list of skills. This is loaded from the database on the first request and then
     * cached in memory.
     * The list is immutable and can be shared across threads.
     */
    private List<SkillName> cachedList;

    private final static int CHUNK_SIZE = 100;
    private final static String DELIMITER = "\n";
    private final static int INITIAL_CAPACITY = 30_000;

    @Override
    public void addTcSkillsIfNew(@NonNull List<String> skills, @NonNull String languageCode) {
        if (skills.isEmpty()) {
            return;
        }
        final String skillsAsString = String.join(",", skills);
        List<SkillName> matchedSkills = extractSkillNames(skillsAsString, languageCode);

        //Are there any skills that are not already in the database?
        if (matchedSkills.size() < skills.size()) {
            // Find which skills didn't match and are therefore new (case-insensitive, trimmed).
            final List<String> newSkills = getNewSkills(skills, matchedSkills);

            if (!newSkills.isEmpty()) {
                log.info("Unmatched (new) skills: {}", newSkills);
                // TODO: persist new skills into the TC skills table.
                //TODO JC Note that a restart is required to pick up new skills because the cached
                // list is only loaded once on first request.
            }
        }

    }

    /**
     * Identify which input skill strings are not present in the provided list of
     * matched {@link SkillName} results.
     * <p>
     * Matching is performed case-insensitively and with surrounding whitespace removed
     * (using {@link Locale#ENGLISH} for lower-casing). Null entries in either list are
     * ignored. The returned list contains the original input strings from {@code skills}
     * that had no match in {@code matchedSkills} after normalization.
     *
     * @param skills input candidate skill strings to check (must not be null)
     * @param matchedSkills skill names returned by the extraction process (must not be null)
     * @return list of skills from {@code skills} that are not present in {@code matchedSkills}
     */
    @NonNull
    private static List<String> getNewSkills(@NonNull List<String> skills,
        List<SkillName> matchedSkills) {
        final Set<String> matchedSet = new HashSet<>();
        for (SkillName sn : matchedSkills) {
            if (sn != null && sn.getName() != null) {
                matchedSet.add(sn.getName().toLowerCase(Locale.ENGLISH).trim());
            }
        }

        final List<String> newSkills = new java.util.ArrayList<>();
        for (String s : skills) {
            if (s == null) {
                continue;
            }
            final String normalized = s.toLowerCase(Locale.ENGLISH).trim();
            if (!matchedSet.contains(normalized)) {
                newSkills.add(s);
            }
        }
        return newSkills;
    }

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
            loadTcSkills(skills);

            //Copy into an immutable list so that it can be shared around.
            cachedList = List.copyOf(skills);
        }

        return cachedList;
    }

    private void loadTcSkills(Set<SkillName> skillNames) {
        PageRequest request = PageRequest.ofSize(CHUNK_SIZE);
        request = request.first();
        Page<SkillsTcEn> page;
        do {
            //Get page of skills
            page = skillsTcEnRepository.findAll(request);
            //Process skills in page
            final List<SkillsTcEn> pageContent = page.getContent();
            for (SkillsTcEn skillsTcEn : pageContent) {
                addSkills(skillNames, skillsTcEn);
            }
            request = request.next();
        } while (page.hasNext());
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
     * Adds TC skill names
     * @param skillNames Collection of skill names to add to
     * @param rec TC skill record
     */
    private void addSkills(Collection<SkillName> skillNames, SkillsTcEn rec) {
        final String name = rec.getName();
        if (!ObjectUtils.isEmpty(name)) {
            String lower = name.toLowerCase(Locale.ENGLISH);
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
