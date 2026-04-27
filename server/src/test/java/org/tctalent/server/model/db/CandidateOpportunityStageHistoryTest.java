package org.tctalent.server.model.db;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;

import org.junit.jupiter.api.Test;
import org.tctalent.server.model.sf.OpportunityHistory;

class CandidateOpportunityStageHistoryTest {

  @Test
  void decodeFromSfHistory_validData_setsStageAndTime() {
    CandidateOpportunityStageHistory history = new CandidateOpportunityStageHistory();

    OpportunityHistory sfHistory = new OpportunityHistory();
    sfHistory.setOpportunityId("OPP123");
    sfHistory.setSystemModstamp("2024-06-30T14:00:00+0000");
    sfHistory.setStageName("Offer");

    history.decodeFromSfHistory(sfHistory);

    assertNotNull(history.getTimeStamp());
    assertTrue(history.getTimeStamp().isEqual(OffsetDateTime.parse("2024-06-30T14:00:00+00:00")));
    assertEquals(CandidateOpportunityStage.offer, history.getStage());
  }


  @Test
  void decodeFromSfHistory_invalidDate_logsErrorAndSetsNullTime() {
    CandidateOpportunityStageHistory history = new CandidateOpportunityStageHistory();

    OpportunityHistory sfHistory = new OpportunityHistory();
    sfHistory.setOpportunityId("OPP456");
    sfHistory.setSystemModstamp("invalid-date");
    sfHistory.setStageName("Offer");

    history.decodeFromSfHistory(sfHistory);

    assertNull(history.getTimeStamp());
    assertEquals(CandidateOpportunityStage.offer, history.getStage());
  }

  @Test
  void decodeFromSfHistory_unknownStage_setsDefaultStageAndLogsError() {
    CandidateOpportunityStageHistory history = new CandidateOpportunityStageHistory();

    OpportunityHistory sfHistory = new OpportunityHistory();
    sfHistory.setOpportunityId("OPP789");
    sfHistory.setSystemModstamp("2024-06-30T14:00:00+0000");
    sfHistory.setStageName("Alien Invasion");

    history.decodeFromSfHistory(sfHistory);

    assertNotNull(history.getTimeStamp());
    assertTrue(history.getTimeStamp().isEqual(OffsetDateTime.parse("2024-06-30T14:00:00Z")));
    assertEquals(CandidateOpportunityStage.prospect, history.getStage());
  }
}
