package org.tctalent.server.api.admin;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;
import org.tctalent.server.model.db.TermsInfo;
import org.tctalent.server.model.db.TermsInfoDto;
import org.tctalent.server.model.db.TermsType;
import org.tctalent.server.model.db.mapper.TermsInfoMapper;
import org.tctalent.server.service.db.TermsInfoService;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.openMocks;

class TermsInfoAdminApiTest {

  @Mock
  private TermsInfoService termsInfoService;

  @Mock
  private TermsInfoMapper termsInfoMapper;

  @InjectMocks
  private TermsInfoAdminApi termsInfoAdminApi;

  public TermsInfoAdminApiTest() {
    openMocks(this);
  }

  @Test
  void getCurrentByType_returnsMappedDto() {
    // Arrange
    TermsType type = TermsType.CANDIDATE_PRIVACY_POLICY;

    TermsInfo termsInfo = new TermsInfo(
        "terms-123",
        "/terms/privacy.html",
        type,
        LocalDate.of(2025, 7, 1)
    );
    termsInfo.setContent("<p>Terms and conditions</p>");

    TermsInfoDto dto = new TermsInfoDto();
    dto.setId("terms-123");
    dto.setContent("<p>Terms and conditions</p>");

    when(termsInfoService.getCurrentByType(type)).thenReturn(termsInfo);
    when(termsInfoMapper.toDto(termsInfo)).thenReturn(dto);

    // Act
    ResponseEntity<TermsInfoDto> response = termsInfoAdminApi.getCurrentByType(type);

    // Assert
    assertEquals(200, response.getStatusCodeValue());
    assertEquals("terms-123", response.getBody().getId());
    assertEquals("<p>Terms and conditions</p>", response.getBody().getContent());

    verify(termsInfoService).getCurrentByType(type);
    verify(termsInfoMapper).toDto(termsInfo);
  }
}
