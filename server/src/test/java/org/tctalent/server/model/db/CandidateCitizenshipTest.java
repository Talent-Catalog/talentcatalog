package org.tctalent.server.model.db;

import static org.junit.jupiter.api.Assertions.*;

import java.time.LocalDate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tctalent.server.request.candidate.CandidateIntakeDataUpdate;

class CandidateCitizenshipTest {

  private CandidateCitizenship candidateCitizenship;

  @BeforeEach
  void setUp() {
    candidateCitizenship = new CandidateCitizenship();
  }

  @Test
  void testGettersAndSetters() {
    Candidate candidate = new Candidate();
    Country nationality = new Country();
    HasPassport hasPassport = HasPassport.ValidPassport;
    LocalDate passportExp = LocalDate.of(2030, 12, 31);
    String notes = "Some notes";

    candidateCitizenship.setCandidate(candidate);
    candidateCitizenship.setNationality(nationality);
    candidateCitizenship.setHasPassport(hasPassport);
    candidateCitizenship.setPassportExp(passportExp);
    candidateCitizenship.setNotes(notes);

    assertSame(candidate, candidateCitizenship.getCandidate());
    assertSame(nationality, candidateCitizenship.getNationality());
    assertEquals(hasPassport, candidateCitizenship.getHasPassport());
    assertEquals(passportExp, candidateCitizenship.getPassportExp());
    assertEquals(notes, candidateCitizenship.getNotes());
  }

  @Test
  void testPopulateIntakeData_allFieldsSet() {
    Candidate candidate = new Candidate();
    Country nationality = new Country();
    CandidateIntakeDataUpdate data = new CandidateIntakeDataUpdate();

    String notes = "Citizen notes";
    HasPassport hasPassport = HasPassport.NoPassport;
    LocalDate passportExp = LocalDate.of(2025, 5, 20);

    data.setCitizenNotes(notes);
    data.setCitizenHasPassport(hasPassport);
    data.setCitizenPassportExp(passportExp);

    candidateCitizenship.populateIntakeData(candidate, nationality, data);

    assertSame(candidate, candidateCitizenship.getCandidate());
    assertSame(nationality, candidateCitizenship.getNationality());
    assertEquals(notes, candidateCitizenship.getNotes());
    assertEquals(hasPassport, candidateCitizenship.getHasPassport());
    assertEquals(passportExp, candidateCitizenship.getPassportExp());
  }

  @Test
  void testPopulateIntakeData_partialFieldsSet() {
    Candidate candidate = new Candidate();
    Country nationality = new Country();
    CandidateIntakeDataUpdate data = new CandidateIntakeDataUpdate();

    data.setCitizenNotes(null);
    data.setCitizenHasPassport(null);
    data.setCitizenPassportExp(null);

    candidateCitizenship.populateIntakeData(candidate, nationality, data);

    assertSame(candidate, candidateCitizenship.getCandidate());
    assertSame(nationality, candidateCitizenship.getNationality());
    assertNull(candidateCitizenship.getNotes());
    assertNull(candidateCitizenship.getHasPassport());
    assertNull(candidateCitizenship.getPassportExp());
  }
}
