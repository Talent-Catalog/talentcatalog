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

package org.tctalent.server.casi.application.support;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.repository.db.CandidateOpportunityRepository;
import org.tctalent.server.service.db.CandidateService;

/**
 * Resolves relevant country ISO codes for signposting services.
 * Order: relocated country, offer+ opportunity countries, current country.
 *
 * @author sadatmalik
 */
@Component
@RequiredArgsConstructor
public class RelevantCountryResolver {

  private final CandidateService candidateService;
  private final CandidateOpportunityRepository candidateOpportunityRepository;

  public List<String> resolveCountryIsoCodes(Long candidateId) {
    Candidate candidate = candidateService.getCandidate(candidateId);
    return resolveCountryIsoCodes(candidate);
  }

  public List<String> resolveCountryIsoCodes(Candidate candidate) {
    LinkedHashSet<String> isoCodes = new LinkedHashSet<>();
    isoCodes.add(normalize(candidate.getRelocatedCountry() == null ? null
        : candidate.getRelocatedCountry().getIsoCode()));

    candidateOpportunityRepository.findByCandidate_Id(candidate.getId()).stream()
        .filter(this::isAtOrBeyondOffer)
        .map(opp -> opp.getJobOpp() == null ? null : opp.getJobOpp().getCountry())
        .filter(Objects::nonNull)
        .map(country -> normalize(country.getIsoCode()))
        .filter(Objects::nonNull)
        .sorted()
        .forEach(isoCodes::add);

    isoCodes.add(normalize(candidate.getCountry() == null ? null
        : candidate.getCountry().getIsoCode()));

    return isoCodes.stream()
        .filter(Objects::nonNull)
        .toList();
  }

  private boolean isAtOrBeyondOffer(CandidateOpportunity opportunity) {
    return opportunity != null
        && opportunity.getStage() != null
        && opportunity.getStage().isAtOrBeyondOffer();
  }

  private String normalize(String isoCode) {
    if (isoCode == null || isoCode.isBlank()) {
      return null;
    }
    return isoCode.trim().toUpperCase(Locale.ROOT);
  }
}
