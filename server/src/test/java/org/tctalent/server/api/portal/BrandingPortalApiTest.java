package org.tctalent.server.api.portal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.tctalent.server.model.db.BrandingInfo;
import org.tctalent.server.service.db.BrandingService;

class BrandingPortalApiTest {

  @Mock
  private BrandingService brandingService;

  @InjectMocks
  private BrandingPortalApi brandingPortalApi;

  @BeforeEach
  void setUp() {
    MockitoAnnotations.openMocks(this);
  }

  @Test
  void testGetBrandingInfo_WithPartnerAbbreviation() {
    String partnerAbbreviation = "testPartner";
    BrandingInfo brandingInfo = new BrandingInfo();
    brandingInfo.setLogo("logo.png");
    brandingInfo.setPartnerName("Test Partner");
    brandingInfo.setWebsiteUrl("https://testpartner.com");

    when(brandingService.getBrandingInfo(eq(partnerAbbreviation))).thenReturn(brandingInfo);

    Map<String, Object> result = brandingPortalApi.getBrandingInfo(partnerAbbreviation);

    assertNotNull(result);
    assertEquals("logo.png", result.get("logo"));
    assertEquals("Test Partner", result.get("partnerName"));
    assertEquals("https://testpartner.com", result.get("websiteUrl"));
    verify(brandingService).getBrandingInfo(partnerAbbreviation);
  }

  @Test
  void testGetBrandingInfo_WithoutPartnerAbbreviation() {
    BrandingInfo brandingInfo = new BrandingInfo();
    brandingInfo.setLogo("default.png");
    brandingInfo.setPartnerName("Default Partner");
    brandingInfo.setWebsiteUrl("https://default.com");

    when(brandingService.getBrandingInfo(null)).thenReturn(brandingInfo);

    Map<String, Object> result = brandingPortalApi.getBrandingInfo(null);

    assertNotNull(result);
    assertEquals("default.png", result.get("logo"));
    assertEquals("Default Partner", result.get("partnerName"));
    assertEquals("https://default.com", result.get("websiteUrl"));
    verify(brandingService).getBrandingInfo(null);
  }

  @Test
  void testGetBrandingInfo_NullBrandingInfo() {
    String partnerAbbreviation = "testPartner";
    when(brandingService.getBrandingInfo(eq(partnerAbbreviation))).thenReturn(null);

    Map<String, Object> result = brandingPortalApi.getBrandingInfo(partnerAbbreviation);

    assertNotNull(result);
    assertTrue(result.isEmpty(), "Result should be an empty map when BrandingInfo is null");
    verify(brandingService).getBrandingInfo(partnerAbbreviation);
  }
}