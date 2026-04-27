package org.tctalent.server.model.db;

import org.junit.jupiter.api.Test;
import org.tctalent.server.request.candidate.visa.CandidateVisaCheckData;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CandidateVisaJobCheckTest {

  @Test
  void testPopulateIntakeData_fillsFieldsCorrectly() {
    // Given
    CandidateVisaJobCheck jobCheck = new CandidateVisaJobCheck();
    Occupation occupation = new Occupation("Software Developer", Status.active);
    occupation.setIsco08Code("2512");

    CandidateVisaCheckData data = new CandidateVisaCheckData();
    data.setVisaJobOccupationId(1L);
    data.setVisaJobOccupationNotes("Has prior experience in related role");
    data.setVisaJobQualification(YesNo.Yes);
    data.setVisaJobQualificationNotes("Bachelor's degree required");
    data.setVisaJobSalaryTsmit(YesNo.Yes);
    data.setVisaJobRegional(YesNo.No);
    data.setVisaJobInterest(YesNo.Yes);
    data.setVisaJobInterestNotes("Keen to work in IT");
    data.setVisaJobEligible494(YesNo.Yes);
    data.setVisaJobEligible494Notes("Meets all criteria");
    data.setVisaJobEligible186(YesNo.No);
    data.setVisaJobEligible186Notes("Not eligible due to age");
    data.setVisaJobEligibleOther(OtherVisas.DirectEnt);
    data.setVisaJobEligibleOtherNotes("Short-term stream");
    data.setVisaJobPutForward(VisaEligibility.Yes);
    data.setVisaJobTbbEligibility(TBBEligibilityAssessment.Proceed);
    data.setVisaJobNotes("Strong candidate overall");
    data.setVisaJobRelevantWorkExp("5+ years in software engineering");
    data.setVisaJobAgeRequirement("Under 45");
    data.setVisaJobPreferredPathways("Subclass 494");
    data.setVisaJobIneligiblePathways("Subclass 186");
    data.setVisaJobEligiblePathways("Subclass 491");
    data.setVisaJobOccupationCategory("ICT");
    data.setVisaJobOccupationSubCategory("Software Development");
    data.setVisaJobEnglishThreshold(YesNo.Yes);
    data.setVisaJobLanguagesRequired(List.of(1L, 2L)); // Example language IDs
    data.setVisaJobLanguagesThresholdMet(YesNo.Yes);
    data.setVisaJobLanguagesThresholdNotes("Meets English language requirement");

    // When
    jobCheck.populateIntakeData(occupation, data);

    // Then
    assertThat(jobCheck.getOccupation()).isEqualTo(occupation);
    assertThat(jobCheck.getOccupationNotes()).isEqualTo("Has prior experience in related role");
    assertThat(jobCheck.getQualification()).isEqualTo(YesNo.Yes);
    assertThat(jobCheck.getQualificationNotes()).isEqualTo("Bachelor's degree required");
    assertThat(jobCheck.getSalaryTsmit()).isEqualTo(YesNo.Yes);
    assertThat(jobCheck.getRegional()).isEqualTo(YesNo.No);
    assertThat(jobCheck.getInterest()).isEqualTo(YesNo.Yes);
    assertThat(jobCheck.getInterestNotes()).isEqualTo("Keen to work in IT");
    assertThat(jobCheck.getEligible_494()).isEqualTo(YesNo.Yes);
    assertThat(jobCheck.getEligible_494_Notes()).isEqualTo("Meets all criteria");
    assertThat(jobCheck.getEligible_186()).isEqualTo(YesNo.No);
    assertThat(jobCheck.getEligible_186_Notes()).isEqualTo("Not eligible due to age");
    assertThat(jobCheck.getEligibleOther()).isEqualTo(OtherVisas.DirectEnt);
    assertThat(jobCheck.getEligibleOtherNotes()).isEqualTo("Short-term stream");
    assertThat(jobCheck.getPutForward()).isEqualTo(VisaEligibility.Yes);
    assertThat(jobCheck.getTbbEligibility()).isEqualTo(TBBEligibilityAssessment.Proceed);
    assertThat(jobCheck.getNotes()).isEqualTo("Strong candidate overall");
    assertThat(jobCheck.getRelevantWorkExp()).isEqualTo("5+ years in software engineering");
    assertThat(jobCheck.getAgeRequirement()).isEqualTo("Under 45");
    assertThat(jobCheck.getPreferredPathways()).isEqualTo("Subclass 494");
    assertThat(jobCheck.getIneligiblePathways()).isEqualTo("Subclass 186");
    assertThat(jobCheck.getEligiblePathways()).isEqualTo("Subclass 491");
    assertThat(jobCheck.getOccupationCategory()).isEqualTo("ICT");
    assertThat(jobCheck.getOccupationSubCategory()).isEqualTo("Software Development");
    assertThat(jobCheck.getEnglishThreshold()).isEqualTo(YesNo.Yes);
    assertThat(jobCheck.getLanguagesRequired()).containsExactly(1L, 2L);
    assertThat(jobCheck.getLanguagesThresholdMet()).isEqualTo(YesNo.Yes);
    assertThat(jobCheck.getLanguagesThresholdNotes()).isEqualTo("Meets English language requirement");
  }
}
