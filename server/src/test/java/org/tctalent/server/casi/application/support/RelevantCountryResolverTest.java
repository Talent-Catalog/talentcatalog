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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.model.db.Candidate;
import org.tctalent.server.model.db.CandidateOpportunity;
import org.tctalent.server.model.db.CandidateOpportunityStage;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.SalesforceJobOpp;
import org.tctalent.server.repository.db.CandidateOpportunityRepository;
import org.tctalent.server.service.db.CandidateService;

@ExtendWith(MockitoExtension.class)
class RelevantCountryResolverTest {

  @Mock
  private CandidateService candidateService;
  @Mock
  private CandidateOpportunityRepository candidateOpportunityRepository;

  private RelevantCountryResolver resolver;

  @BeforeEach
  void setUp() {
    resolver = new RelevantCountryResolver(candidateService, candidateOpportunityRepository);
  }

  @Test
  @DisplayName("resolves countries in configured order")
  void resolvesCountriesInConfiguredOrder() {
    Candidate candidate = candidate(10L, "pk", "au");
    when(candidateService.getCandidate(10L)).thenReturn(candidate);
    when(candidateOpportunityRepository.findByCandidate_Id(10L)).thenReturn(List.of(
        opportunity(CandidateOpportunityStage.noJobOffer, "ca"),
        opportunity(CandidateOpportunityStage.offer, "de"),
        opportunity(CandidateOpportunityStage.acceptance, "us")
    ));

    List<String> result = resolver.resolveCountryIsoCodes(10L);

    assertThat(result).containsExactly("AU", "DE", "US", "PK");
  }

  @Test
  @DisplayName("deduplicates and normalizes ISO codes")
  void deduplicatesAndNormalizesIsoCodes() {
    Candidate candidate = candidate(11L, "au", "AU");
    when(candidateService.getCandidate(11L)).thenReturn(candidate);
    when(candidateOpportunityRepository.findByCandidate_Id(11L)).thenReturn(List.of(
        opportunity(CandidateOpportunityStage.offer, "au"),
        opportunity(CandidateOpportunityStage.acceptance, "AU")
    ));

    List<String> result = resolver.resolveCountryIsoCodes(11L);

    assertThat(result).containsExactly("AU");
  }

  private Candidate candidate(Long id, String countryIso, String relocatedIso) {
    Candidate candidate = new Candidate();
    candidate.setId(id);

    Country country = new Country();
    country.setIsoCode(countryIso);
    candidate.setCountry(country);

    Country relocatedCountry = new Country();
    relocatedCountry.setIsoCode(relocatedIso);
    candidate.setRelocatedCountry(relocatedCountry);
    return candidate;
  }

  private CandidateOpportunity opportunity(CandidateOpportunityStage stage, String iso) {
    CandidateOpportunity opportunity = new CandidateOpportunity();
    opportunity.setStage(stage);

    SalesforceJobOpp jobOpp = new SalesforceJobOpp();
    Country country = new Country();
    country.setIsoCode(iso);
    jobOpp.setCountry(country);

    opportunity.setJobOpp(jobOpp);
    return opportunity;
  }
}
