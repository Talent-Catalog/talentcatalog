package org.tctalent.server.model.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.tctalent.server.model.db.mapper.UserMapperImpl;
import org.tctalent.server.repository.db.read.dto.UserReadDto;

class UserMapperImplTest {

  private final UserMapperImpl mapper = new UserMapperImpl();

  @Test
  void userIdentityToUser_shouldReturnNull_whenSourceIsNull() {
    assertNull(mapper.userIdentityToUser(null));
  }

  @Test
  void userIdentityToUser_shouldMapIdentityFields() {
    org.tctalent.anonymization.model.User source =
        mock(org.tctalent.anonymization.model.User.class);

    when(source.getFirstName()).thenReturn("Ada");
    when(source.getLastName()).thenReturn("Lovelace");
    when(source.getEmail()).thenReturn("ada@example.org");

    User result = mapper.userIdentityToUser(source);

    assertNotNull(result);
    assertEquals("Ada", result.getFirstName());
    assertEquals("Lovelace", result.getLastName());
    assertEquals("ada@example.org", result.getEmail());
  }

  @Test
  void toDto_shouldReturnNull_whenUserIsNull() {
    assertNull(mapper.toDto(null));
  }

  @Test
  void toDto_shouldMapUserAndPartner_whenValuesPresent() {
    User user = mock(User.class);
    PartnerImpl partner = mock(PartnerImpl.class);

    when(user.getEmail()).thenReturn("ada@example.org");
    when(user.getEmailVerified()).thenReturn(Boolean.TRUE);
    when(user.getFirstName()).thenReturn("Ada");
    when(user.getLastName()).thenReturn("Lovelace");
    when(user.getUsername()).thenReturn("ada");
    when(user.getPartner()).thenReturn(partner);

    when(partner.getAbbreviation()).thenReturn("TC");
    when(partner.getName()).thenReturn("Talent Catalog");
    when(partner.getWebsiteUrl()).thenReturn("https://example.org");

    UserReadDto result = mapper.toDto(user);

    assertNotNull(result);
    assertEquals("ada@example.org", result.getEmail());
    assertEquals("true", result.getEmailVerified());
    assertEquals("Ada", result.getFirstName());
    assertEquals("Lovelace", result.getLastName());
    assertEquals("ada", result.getUsername());

    assertNotNull(result.getPartner());
    assertEquals("TC", result.getPartner().getAbbreviation());
    assertEquals("Talent Catalog", result.getPartner().getName());
    assertEquals("https://example.org", result.getPartner().getWebsiteUrl());
  }

  @Test
  void toDto_shouldLeaveEmailVerifiedAndPartnerNull_whenSourceValuesAreNull() {
    User user = mock(User.class);

    when(user.getEmailVerified()).thenReturn(null);
    when(user.getPartner()).thenReturn(null);

    UserReadDto result = mapper.toDto(user);

    assertNotNull(result);
    assertNull(result.getEmailVerified());
    assertNull(result.getPartner());
  }
}