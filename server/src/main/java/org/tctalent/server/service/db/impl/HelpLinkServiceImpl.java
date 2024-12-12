/*
 * Copyright (c) 2024 Talent Catalog.
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

import static org.tctalent.server.util.help.HelpLinkHelper.generateRequestSequence;

import java.util.ArrayList;
import java.util.List;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.logging.LogBuilder;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.CandidateOpportunityStage;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.HelpLink;
import org.tctalent.server.model.db.JobOpportunityStage;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.repository.db.HelpLinkFetchSpecification;
import org.tctalent.server.repository.db.HelpLinkRepository;
import org.tctalent.server.repository.db.HelpLinkSettingsSpecification;
import org.tctalent.server.request.helplink.SearchHelpLinkRequest;
import org.tctalent.server.request.helplink.UpdateHelpLinkRequest;
import org.tctalent.server.service.db.CandidateOpportunityService;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.service.db.HelpLinkService;
import org.tctalent.server.service.db.JobService;

@Service
@RequiredArgsConstructor
@Slf4j
public class HelpLinkServiceImpl implements HelpLinkService {
    private final HelpLinkRepository helpLinkRepository;
    private final CandidateOpportunityService candidateOpportunityService;
    private final CountryService countryService;
    private final JobService jobService;

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
        //Enrich request using context.

        //Country
        if (request.getCountryId() == null) {
            Country computedCountry = computeCountry(request);
            if (computedCountry != null) {
                request.setCountryId(computedCountry.getId());
            }
        }

        List<HelpLink> helpLinks = new ArrayList<>();

        final CandidateOpportunityStage caseStage = request.getCaseStage();
        final JobOpportunityStage jobStage = request.getJobStage();
        HelpLink standardDocLink = null;
        if (jobStage != null || caseStage != null) {
            //If it is a stage related request, always add the link associated with our standard
            //stage doc.
            if (caseStage != null) {
                standardDocLink = helpLinkRepository.findFirstByCaseStageAndCountry(
                    caseStage, null);
            } else {
                standardDocLink = helpLinkRepository.findFirstByJobStageAndCountry(
                    jobStage, null);
            }
            if (standardDocLink != null) {
                helpLinks.add(standardDocLink);
            } else {
               LogBuilder.builder(log)
                   .action("FetchHelp")
                   .message("Could not find standard stage doc " +
                       (caseStage != null ? caseStage : jobStage))
                   .logWarn();
            }
        }

        List<SearchHelpLinkRequest> requests = generateRequestSequence(request);

        //Cycle through the generated requests returning the results of the first one that finds
        //help.
        for (SearchHelpLinkRequest childRequest : requests) {

            //This needs to add links to any preloaded ones.
            final List<HelpLink> searchResults = helpLinkRepository.findAll(
                HelpLinkFetchSpecification.buildSearchQuery(childRequest), request.getSort());
            if (!searchResults.isEmpty()) {
                if (standardDocLink != null) {
                    //Remove standardDocLink if it was found - because that is already there.
                    //Don't want it twice.
                    searchResults.remove(standardDocLink);
                }
                helpLinks.addAll(searchResults);
                break;
            }
        }

        if (helpLinks.isEmpty()) {
            //TODO JC Add in default help link
        }

        return helpLinks;
    }

    @Nullable
    private Country computeCountry(SearchHelpLinkRequest request) {
        Country country = null;

        //See if we have a jobOpp specified
        SalesforceJobOpp jobOpp = null;
        if (request.getJobOppId() != null) {
            jobOpp = jobService.getJob(request.getJobOppId());
        } else if (request.getCaseOppId() != null) {
            CandidateOpportunity caseOpp = candidateOpportunityService
                .getCandidateOpportunity(request.getCaseOppId());
            jobOpp = caseOpp.getJobOpp();
        }

        //Get country from job opp
        if (jobOpp != null) {
            country = jobOpp.getCountry();
            if (country == null && jobOpp.getEmployerEntity() != null) {
                country = jobOpp.getEmployerEntity().getCountry();
            }
        }
        return country;
    }

    @Override
    public @NonNull List<HelpLink> search(SearchHelpLinkRequest request) {
        List<HelpLink> helpLinks = helpLinkRepository.findAll(
            HelpLinkSettingsSpecification.buildSearchQuery(request), request.getSort());

        return helpLinks;
    }

    @Override
    public @NonNull Page<HelpLink> searchPaged(SearchHelpLinkRequest request) {

        Page<HelpLink> helpLinks = helpLinkRepository.findAll(
            HelpLinkSettingsSpecification.buildSearchQuery(request), request.getPageRequest());

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
