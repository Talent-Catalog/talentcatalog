/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 *  the terms of the GNU General Public License as published by the Free
 *  Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db.impl;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.Country;
import org.tctalent.server.model.db.Employer;
import org.tctalent.server.model.db.User;
import org.tctalent.server.model.sf.Account;
import org.tctalent.server.repository.db.EmployerRepository;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.service.db.SalesforceService;
import org.tctalent.server.service.db.UserService;
import org.tctalent.server.service.db.email.EmailHelper;

@ExtendWith(MockitoExtension.class)
class EmployerServiceImplTest {

  private static final String SF_ID = "001123456789ABC";

  @Mock
  private CountryService countryService;

  @Mock
  private EmailHelper emailHelper;

  @Mock
  private EmployerRepository employerRepository;

  @Mock
  private SalesforceService salesforceService;

  @Mock
  private UserService userService;

  @Mock
  private Account account;

  @Mock
  private User loggedInUser;

  @Mock
  private Country country;

  private EmployerServiceImpl service;

  @BeforeEach
  void setUp() {
    service = new EmployerServiceImpl(
        countryService,
        emailHelper,
        employerRepository,
        salesforceService,
        userService
    );
  }

  @Test
  void findOrCreateFromAccountReturnsExistingEmployer() {
    Employer existingEmployer = new Employer();

    when(account.getId()).thenReturn(SF_ID);
    when(employerRepository.findFirstBySfId(SF_ID))
        .thenReturn(Optional.of(existingEmployer));

    Employer result =
        service.findOrCreateEmployerFromSalesforceAccount(account);

    assertSame(existingEmployer, result);

    verify(employerRepository, never()).save(any());
    verifyNoInteractions(
        countryService,
        emailHelper,
        salesforceService,
        userService
    );
  }

  @Test
  void findOrCreateFromAccountCreatesAndMapsEmployer() {
    when(account.getId()).thenReturn(SF_ID);
    when(account.getName()).thenReturn("Example Employer");
    when(account.getHasHiredInternationally()).thenReturn("Yes");
    when(account.getDescription()).thenReturn("Employer description");
    when(account.getWebsite()).thenReturn("https://employer.example.com");
    when(account.getCountry()).thenReturn("Canada");

    when(employerRepository.findFirstBySfId(SF_ID))
        .thenReturn(Optional.empty());
    when(userService.getLoggedInUser()).thenReturn(loggedInUser);
    when(countryService.findByName("Canada")).thenReturn(country);
    when(employerRepository.save(any(Employer.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    OffsetDateTime before = OffsetDateTime.now().minusSeconds(1);

    Employer result =
        service.findOrCreateEmployerFromSalesforceAccount(account);

    OffsetDateTime after = OffsetDateTime.now().plusSeconds(1);

    assertAll(
        () -> assertEquals("Example Employer", result.getName()),
        () -> assertEquals(SF_ID, result.getSfId()),
        () -> assertSame(loggedInUser, result.getCreatedBy()),
        () -> assertTrue(result.getCreatedDate().isAfter(before)),
        () -> assertTrue(result.getCreatedDate().isBefore(after)),
        () -> assertEquals(Boolean.TRUE, result.getHasHiredInternationally()),
        () -> assertEquals(
            "Employer description",
            result.getDescription()
        ),
        () -> assertEquals(
            "https://employer.example.com",
            result.getWebsite()
        ),
        () -> assertSame(country, result.getCountry())
    );

    verify(emailHelper, never()).sendAlert(anyString());
    verify(employerRepository).save(result);
  }

  @Test
  void findOrCreateFromAccountMapsNoToFalse() {
    when(account.getId()).thenReturn(SF_ID);
    when(account.getHasHiredInternationally()).thenReturn("No");
    when(account.getCountry()).thenReturn("Canada");

    when(employerRepository.findFirstBySfId(SF_ID))
        .thenReturn(Optional.empty());
    when(countryService.findByName("Canada")).thenReturn(country);
    when(employerRepository.save(any(Employer.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Employer result =
        service.findOrCreateEmployerFromSalesforceAccount(account);

    assertNotEquals(Boolean.TRUE, result.getHasHiredInternationally());
    verify(emailHelper, never()).sendAlert(anyString());
  }

  @Test
  void findOrCreateFromAccountPreservesUnknownHiringStatusAsNull() {
    when(account.getId()).thenReturn(SF_ID);
    when(account.getName()).thenReturn("Unknown Employer");
    when(account.getHasHiredInternationally()).thenReturn(null);
    when(account.getCountry()).thenReturn("Atlantis");

    when(employerRepository.findFirstBySfId(SF_ID))
        .thenReturn(Optional.empty());
    when(countryService.findByName("Atlantis")).thenReturn(null);
    when(employerRepository.save(any(Employer.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Employer result =
        service.findOrCreateEmployerFromSalesforceAccount(account);

    assertAll(
        () -> assertNull(result.getHasHiredInternationally()),
        () -> assertNull(result.getCountry())
    );

    verify(emailHelper).sendAlert(
        "Salesforce country Atlantis in SF account "
            + "Unknown Employer not found in database."
    );
  }

  @Test
  void findOrCreateFromAccountHandlesLowercaseHiringStatus() {
    when(account.getId()).thenReturn(SF_ID);
    when(account.getHasHiredInternationally()).thenReturn("yes");
    when(account.getCountry()).thenReturn("Canada");

    when(employerRepository.findFirstBySfId(SF_ID))
        .thenReturn(Optional.empty());
    when(countryService.findByName("Canada")).thenReturn(country);
    when(employerRepository.save(any(Employer.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Employer result =
        service.findOrCreateEmployerFromSalesforceAccount(account);

    assertEquals(Boolean.TRUE, result.getHasHiredInternationally());
  }

  @Test
  void findOrCreateFromSalesforceIdReturnsExistingEmployer() {
    Employer existingEmployer = new Employer();

    when(employerRepository.findFirstBySfId(SF_ID))
        .thenReturn(Optional.of(existingEmployer));

    Employer result =
        service.findOrCreateEmployerFromSalesforceId(SF_ID);

    assertSame(existingEmployer, result);

    verifyNoInteractions(
        salesforceService,
        countryService,
        emailHelper,
        userService
    );
    verify(employerRepository, never()).save(any());
  }

  @Test
  void findOrCreateFromSalesforceIdCreatesEmployerFromAccount() {
    when(employerRepository.findFirstBySfId(SF_ID))
        .thenReturn(Optional.empty());
    when(salesforceService.findAccount(SF_ID)).thenReturn(account);

    when(account.getId()).thenReturn(SF_ID);
    when(account.getName()).thenReturn("Salesforce Employer");
    when(account.getCountry()).thenReturn("Canada");

    when(countryService.findByName("Canada")).thenReturn(country);
    when(userService.getLoggedInUser()).thenReturn(loggedInUser);
    when(employerRepository.save(any(Employer.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    Employer result =
        service.findOrCreateEmployerFromSalesforceId(SF_ID);

    assertAll(
        () -> assertEquals(SF_ID, result.getSfId()),
        () -> assertEquals("Salesforce Employer", result.getName()),
        () -> assertSame(country, result.getCountry()),
        () -> assertSame(loggedInUser, result.getCreatedBy())
    );

    verify(salesforceService).findAccount(SF_ID);
    verify(employerRepository).save(result);
  }

  @Test
  void findOrCreateFromSalesforceIdThrowsWhenAccountDoesNotExist() {
    when(employerRepository.findFirstBySfId(SF_ID))
        .thenReturn(Optional.empty());
    when(salesforceService.findAccount(SF_ID)).thenReturn(null);

    NoSuchObjectException exception = assertThrows(
        NoSuchObjectException.class,
        () -> service.findOrCreateEmployerFromSalesforceId(SF_ID)
    );

    assertEquals(
        "No such Salesforce account: " + SF_ID,
        exception.getMessage()
    );

    verify(employerRepository, never()).save(any());
    verifyNoInteractions(
        countryService,
        emailHelper,
        userService
    );
  }

  @Test
  void findOrCreateFromSalesforceLinkExtractsId() {
    Employer existingEmployer = new Employer();

    String salesforceLink =
        "https://talentbeyondboundaries.lightning.force.com/"
            + "lightning/r/Account/"
            + SF_ID
            + "/view/";

    when(employerRepository.findFirstBySfId(SF_ID))
        .thenReturn(Optional.of(existingEmployer));

    Employer result =
        service.findOrCreateEmployerFromSalesforceLink(
            salesforceLink
        );

    assertSame(existingEmployer, result);

    verify(employerRepository).findFirstBySfId(SF_ID);
  }

  @Test
  void findOrCreateFromSalesforceLinkThrowsForInvalidLink() {
    String invalidLink = "not-a-salesforce-link";

    NoSuchObjectException exception = assertThrows(
        NoSuchObjectException.class,
        () -> service.findOrCreateEmployerFromSalesforceLink(
            invalidLink
        )
    );

    assertEquals(
        "No such Salesforce url: " + invalidLink,
        exception.getMessage()
    );

    verifyNoInteractions(
        employerRepository,
        salesforceService,
        countryService,
        emailHelper,
        userService
    );
  }

  @Test
  void getReturnsEmployerWhenItExists() {
    Employer employer = new Employer();

    when(employerRepository.findById(50L))
        .thenReturn(Optional.of(employer));

    Employer result = service.get(50L);

    assertSame(employer, result);
  }

  @Test
  void getThrowsWhenEmployerDoesNotExist() {
    when(employerRepository.findById(50L))
        .thenReturn(Optional.empty());

    assertThrows(
        NoSuchObjectException.class,
        () -> service.get(50L)
    );
  }
}