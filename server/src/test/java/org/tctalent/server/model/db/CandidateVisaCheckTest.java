package org.tctalent.server.model.db;

import org.junit.jupiter.api.Test;
import org.tctalent.server.request.candidate.visa.CandidateVisaCheckData;

import static org.assertj.core.api.Assertions.assertThat;

class CandidateVisaCheckTest {

  @Test
  void testPopulateIntakeData_setsFieldsCorrectly() {
    // Given
    CandidateVisaCheck visaCheck = new CandidateVisaCheck();
    CandidateVisaCheckData data = new CandidateVisaCheckData();

    data.setVisaProtection(YesNo.Yes);
    data.setVisaProtectionGrounds("Political reasons");
    data.setVisaEnglishThreshold(YesNo.Yes);
    data.setVisaEnglishThresholdNotes("IELTS score above threshold");
    data.setVisaHealthAssessment(YesNo.Yes);
    data.setVisaHealthAssessmentNotes("Cleared by panel physician");
    data.setVisaCharacterAssessment(YesNo.No);
    data.setVisaCharacterAssessmentNotes("Previous minor criminal record");
    data.setVisaSecurityRisk(YesNo.No);
    data.setVisaSecurityRiskNotes("No known risks");
    data.setVisaOverallRisk(RiskLevel.Low);
    data.setVisaOverallRiskNotes("Candidate is low risk");
    data.setVisaValidTravelDocs(DocumentStatus.Valid);
    data.setVisaValidTravelDocsNotes("Has valid passport");
    data.setVisaPathwayAssessment(YesNoUnsure.Yes);
    data.setVisaPathwayAssessmentNotes("Meets skill and language requirements");
    data.setVisaDestinationFamily(FamilyRelations.Sibling);
    data.setVisaDestinationFamilyLocation("Sydney");

    // When
    visaCheck.populateIntakeData(data);

    // Then
    assertThat(visaCheck.getProtection()).isEqualTo(YesNo.Yes);
    assertThat(visaCheck.getProtectionGrounds()).isEqualTo("Political reasons");
    assertThat(visaCheck.getEnglishThreshold()).isEqualTo(YesNo.Yes);
    assertThat(visaCheck.getEnglishThresholdNotes()).isEqualTo("IELTS score above threshold");
    assertThat(visaCheck.getHealthAssessment()).isEqualTo(YesNo.Yes);
    assertThat(visaCheck.getHealthAssessmentNotes()).isEqualTo("Cleared by panel physician");
    assertThat(visaCheck.getCharacterAssessment()).isEqualTo(YesNo.No);
    assertThat(visaCheck.getCharacterAssessmentNotes()).isEqualTo("Previous minor criminal record");
    assertThat(visaCheck.getSecurityRisk()).isEqualTo(YesNo.No);
    assertThat(visaCheck.getSecurityRiskNotes()).isEqualTo("No known risks");
    assertThat(visaCheck.getOverallRisk()).isEqualTo(RiskLevel.Low);
    assertThat(visaCheck.getOverallRiskNotes()).isEqualTo("Candidate is low risk");
    assertThat(visaCheck.getValidTravelDocs()).isEqualTo(DocumentStatus.Valid);
    assertThat(visaCheck.getValidTravelDocsNotes()).isEqualTo("Has valid passport");
    assertThat(visaCheck.getPathwayAssessment()).isEqualTo(YesNoUnsure.Yes);
    assertThat(visaCheck.getPathwayAssessmentNotes()).isEqualTo("Meets skill and language requirements");
    assertThat(visaCheck.getDestinationFamily()).isEqualTo(FamilyRelations.Sibling);
    assertThat(visaCheck.getDestinationFamilyLocation()).isEqualTo("Sydney");
  }
}
