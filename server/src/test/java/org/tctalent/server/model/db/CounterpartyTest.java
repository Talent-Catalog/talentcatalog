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

package org.tctalent.server.model.db;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.casi.domain.model.ServiceProvider;

@ExtendWith(MockitoExtension.class)
class CounterpartyTest {

  @Mock
  private PartnerImpl partner;

  @Mock
  private Employer employer;

  @Test
  void getDisplayNameReturnsExplicitNameFirst() {
    Counterparty counterparty = new Counterparty();
    counterparty.setName("Open Pathway Collective");
    counterparty.setPartner(partner);
    counterparty.setEmployer(employer);
    counterparty.setServiceProvider(ServiceProvider.UNHCR);

    assertEquals("Open Pathway Collective", counterparty.getDisplayName());

    verifyNoInteractions(partner, employer);
  }

  @Test
  void getDisplayNameReturnsPartnerNameWhenNameIsNull() {
    Counterparty counterparty = new Counterparty();
    counterparty.setPartner(partner);
    counterparty.setEmployer(employer);
    counterparty.setServiceProvider(ServiceProvider.UNHCR);

    when(partner.getName()).thenReturn("Talent Beyond Boundaries");

    assertEquals("Talent Beyond Boundaries", counterparty.getDisplayName());

    verifyNoInteractions(employer);
  }

  @Test
  void getDisplayNameReturnsEmployerNameWhenNameAndPartnerAreNull() {
    Counterparty counterparty = new Counterparty();
    counterparty.setEmployer(employer);
    counterparty.setServiceProvider(ServiceProvider.UNHCR);

    when(employer.getName()).thenReturn("Example Employer");

    assertEquals("Example Employer", counterparty.getDisplayName());
  }

  @Test
  void getDisplayNameReturnsServiceProviderNameWhenNoNamePartnerOrEmployer() {
    Counterparty counterparty = new Counterparty();
    counterparty.setServiceProvider(ServiceProvider.UNHCR);

    assertEquals("UNHCR", counterparty.getDisplayName());
  }

  @Test
  void getDisplayNameReturnsNullWhenNoDisplaySourceExists() {
    Counterparty counterparty = new Counterparty();

    assertNull(counterparty.getDisplayName());
  }

  @Test
  void lombokBackedFieldsCanBeSetAndRead() {
    Counterparty counterparty = new Counterparty();

    counterparty.setType(CounterpartyType.SERVICE_PROVIDER);
    counterparty.setPartner(partner);
    counterparty.setEmployer(employer);
    counterparty.setServiceProvider(ServiceProvider.UNHCR);
    counterparty.setName("Manual Counterparty");

    assertEquals(CounterpartyType.SERVICE_PROVIDER, counterparty.getType());
    assertSame(partner, counterparty.getPartner());
    assertSame(employer, counterparty.getEmployer());
    assertEquals(ServiceProvider.UNHCR, counterparty.getServiceProvider());
    assertEquals("Manual Counterparty", counterparty.getName());
  }
}