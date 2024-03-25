/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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

import static org.tctalent.server.util.HelpLinkHelper.generateRequestSequence;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.HelpLink;
import org.tctalent.server.repository.db.HelpLinkRepository;
import org.tctalent.server.repository.db.HelpLinkSpecification;
import org.tctalent.server.request.helplink.SearchHelpLinkRequest;
import org.tctalent.server.request.helplink.UpdateHelpLinkRequest;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.service.db.HelpLinkService;

@Service
@RequiredArgsConstructor
public class HelpLinkServiceImpl implements HelpLinkService {
    private final HelpLinkRepository helpLinkRepository;
    private final CountryService countryService;

    @Override
    public @NotNull HelpLink createHelpLink(UpdateHelpLinkRequest request)  throws NoSuchObjectException {
        HelpLink helpLink = new HelpLink();
        populateAttributes(request, helpLink);
        return helpLinkRepository.save(helpLink);
    }

    @Override
    public boolean deleteHelpLink(long id) {
        boolean found = helpLinkRepository.existsById(id);
        if (found) {
            helpLinkRepository.deleteById(id);
        }
        return found;
    }

    @Override
    public @NonNull List<HelpLink> fetchHelp(SearchHelpLinkRequest request) {
        //TODO JC Enrich request with context based on user.

        List<SearchHelpLinkRequest> requests = generateRequestSequence(request);

        //Cycle through the generated requests returning the results of the first one that finds
        //help.
        List<HelpLink> helpLinks = new ArrayList<>();
        for (SearchHelpLinkRequest childRequest : requests) {
            helpLinks = helpLinkRepository.findAll(
                HelpLinkSpecification.buildSearchQuery(childRequest), request.getSort());
            if (!helpLinks.isEmpty()) {
                break;
            }
        }

        if (helpLinks.isEmpty()) {
            //TODO JC Add in default help link
        }

        return helpLinks;
    }

    @Override
    public @NonNull List<HelpLink> search(SearchHelpLinkRequest request) {
        List<HelpLink> helpLinks = helpLinkRepository.findAll(
            HelpLinkSpecification.buildSearchQuery(request), request.getSort());

        return helpLinks;
    }

    @Override
    public @NonNull Page<HelpLink> searchPaged(SearchHelpLinkRequest request) {

        Page<HelpLink> helpLinks = helpLinkRepository.findAll(
            HelpLinkSpecification.buildSearchQuery(request), request.getPageRequest());

        return helpLinks;
    }

    @Override
    public @NotNull HelpLink updateHelpLink(long id, UpdateHelpLinkRequest request)
        throws NoSuchObjectException {
        HelpLink helpLink = this.helpLinkRepository.findById(id)
            .orElseThrow(() -> new NoSuchObjectException(HelpLink.class, id));

        populateAttributes(request, helpLink);
        return helpLinkRepository.save(helpLink);
    }

    private void populateAttributes(UpdateHelpLinkRequest request, HelpLink helpLink)
        throws NoSuchObjectException {

        Long countryId = request.getCountryId();
        helpLink.setCountry(countryId == null ? null : countryService.getCountry(countryId));

        helpLink.setCaseStage(request.getCaseStage());
        helpLink.setJobStage(request.getJobStage());
        helpLink.setLabel(request.getLabel());
        helpLink.setLink(request.getLink());
        helpLink.setFocus(request.getFocus());
        helpLink.setNextStepInfo(request.getNextStepInfo());
    }
}
