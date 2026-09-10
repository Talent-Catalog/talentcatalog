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

package org.tctalent.server.model.db;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class CandidateOpportunityStageTest {

  @Test
  @DisplayName("isAtOrBeyondOffer includes offer and acceptance")
  void isAtOrBeyondOfferIncludesOfferAndAcceptance() {
    assertThat(CandidateOpportunityStage.offer.isAtOrBeyondOffer()).isTrue();
    assertThat(CandidateOpportunityStage.acceptance.isAtOrBeyondOffer()).isTrue();
  }

  @Test
  @DisplayName("isAtOrBeyondOffer excludes closed-lost stages after offer")
  void isAtOrBeyondOfferExcludesClosedLostStages() {
    assertThat(CandidateOpportunityStage.noJobOffer.isAtOrBeyondOffer()).isFalse();
    assertThat(CandidateOpportunityStage.candidateRejectsOffer.isAtOrBeyondOffer()).isFalse();
  }

  @Test
  @DisplayName("isAtOrBeyondOffer includes closed won or employed stages")
  void isAtOrBeyondOfferIncludesClosedWonOrEmployedStages() {
    assertThat(CandidateOpportunityStage.durableSolution.isAtOrBeyondOffer()).isTrue();
    assertThat(CandidateOpportunityStage.candidateLeavesDestination.isAtOrBeyondOffer()).isTrue();
  }
}
