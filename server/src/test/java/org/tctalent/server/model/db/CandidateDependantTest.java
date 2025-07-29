package org.tctalent.server.model.db;

import org.junit.jupiter.api.Test;
import org.tctalent.server.request.candidate.CandidateIntakeDataUpdate;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class CandidateDependantTest {

  @Test
  void testPopulateIntakeData_setsAllFieldsCorrectly() {
    // Given
    CandidateDependant dependant = new CandidateDependant();
    Candidate candidate = new Candidate();
    candidate.setId(123L);

    CandidateIntakeDataUpdate data = new CandidateIntakeDataUpdate();
    data.setDependantRelation(DependantRelations.Child);
    data.setDependantRelationOther("Step-child");
    data.setDependantDob(LocalDate.of(2012, 3, 14));
    data.setDependantGender(Gender.female);
    data.setDependantName("Alice Doe");
    data.setDependantRegistered(Registration.UNHCR);
    data.setDependantRegisteredNumber("REG-7890");
    data.setDependantRegisteredNotes("Registered in NSW");
    data.setDependantHealthConcerns(YesNo.Yes);
    data.setDependantHealthNotes("Mild asthma");

    // When
    dependant.populateIntakeData(candidate, data);

    // Then
    assertThat(dependant.getCandidate()).isEqualTo(candidate);
    assertThat(dependant.getRelation()).isEqualTo(DependantRelations.Child);
    assertThat(dependant.getRelationOther()).isEqualTo("Step-child");
    assertThat(dependant.getDob()).isEqualTo(LocalDate.of(2012, 3, 14));
    assertThat(dependant.getGender()).isEqualTo(Gender.female);
    assertThat(dependant.getName()).isEqualTo("Alice Doe");
    assertThat(dependant.getRegistered()).isEqualTo(Registration.UNHCR);
    assertThat(dependant.getRegisteredNumber()).isEqualTo("REG-7890");
    assertThat(dependant.getRegisteredNotes()).isEqualTo("Registered in NSW");
    assertThat(dependant.getHealthConcern()).isEqualTo(YesNo.Yes);
    assertThat(dependant.getHealthNotes()).isEqualTo("Mild asthma");
  }
}
