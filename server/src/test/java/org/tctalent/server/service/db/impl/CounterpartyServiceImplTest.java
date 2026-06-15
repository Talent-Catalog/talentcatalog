/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.tctalent.server.casi.domain.model.ServiceProvider;
import org.tctalent.server.model.db.Counterparty;
import org.tctalent.server.model.db.CounterpartyType;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.repository.db.CounterpartyRepository;

@ExtendWith(MockitoExtension.class)
class CounterpartyServiceImplTest {

    @Mock
    private CounterpartyRepository counterpartyRepository;

    @InjectMocks
    private CounterpartyServiceImpl counterpartyService;

    @Test
    @DisplayName("findOrCreateByTypeAndName returns existing counterparty when present")
    void findOrCreateByTypeAndName_returnsExistingCounterparty() {
        Counterparty existing = new Counterparty();
        existing.setId(11L);
        existing.setType(CounterpartyType.DATABASE_PROVIDER);
        existing.setName("OPC");

        given(counterpartyRepository.findByTypeAndNameIgnoreCase(
            CounterpartyType.DATABASE_PROVIDER, "OPC")).willReturn(Optional.of(existing));

        Counterparty result = counterpartyService.findOrCreateByTypeAndName(
            CounterpartyType.DATABASE_PROVIDER, "OPC");

        assertThat(result).isSameAs(existing);
        verify(counterpartyRepository, never()).save(any(Counterparty.class));
    }

    @Test
    @DisplayName("findOrCreateByTypeAndName creates and saves counterparty when missing")
    void findOrCreateByTypeAndName_createsCounterpartyWhenMissing() {
        Counterparty saved = new Counterparty();
        saved.setId(12L);
        saved.setType(CounterpartyType.DATABASE_PROVIDER);
        saved.setName("OPC");

        given(counterpartyRepository.findByTypeAndNameIgnoreCase(
            CounterpartyType.DATABASE_PROVIDER, "OPC")).willReturn(Optional.empty());
        given(counterpartyRepository.save(any(Counterparty.class))).willReturn(saved);

        Counterparty result = counterpartyService.findOrCreateByTypeAndName(
            CounterpartyType.DATABASE_PROVIDER, "OPC");

        assertThat(result).isSameAs(saved);

        ArgumentCaptor<Counterparty> counterpartyCaptor = ArgumentCaptor.forClass(Counterparty.class);
        verify(counterpartyRepository).save(counterpartyCaptor.capture());
        Counterparty created = counterpartyCaptor.getValue();
        assertThat(created.getType()).isEqualTo(CounterpartyType.DATABASE_PROVIDER);
        assertThat(created.getName()).isEqualTo("OPC");
    }

    @Test
    @DisplayName("findOrCreateByTypeAndPartner returns existing counterparty when present")
    void findOrCreateByTypeAndPartner_returnsExistingCounterparty() {
        PartnerImpl opcPartner = new PartnerImpl();
        opcPartner.setId(5L);

        Counterparty existing = new Counterparty();
        existing.setId(20L);
        existing.setType(CounterpartyType.DATABASE_PROVIDER);
        existing.setPartner(opcPartner);

        given(counterpartyRepository.findByTypeAndPartnerId(CounterpartyType.DATABASE_PROVIDER, 5L))
            .willReturn(Optional.of(existing));

        Counterparty result = counterpartyService.findOrCreateByTypeAndPartner(
            CounterpartyType.DATABASE_PROVIDER, opcPartner);

        assertThat(result).isSameAs(existing);
        verify(counterpartyRepository, never()).save(any(Counterparty.class));
    }

    @Test
    @DisplayName("findOrCreateByTypeAndPartner creates and saves counterparty when missing")
    void findOrCreateByTypeAndPartner_createsCounterpartyWhenMissing() {
        PartnerImpl opcPartner = new PartnerImpl();
        opcPartner.setId(5L);

        Counterparty saved = new Counterparty();
        saved.setId(21L);
        saved.setType(CounterpartyType.DATABASE_PROVIDER);
        saved.setPartner(opcPartner);

        given(counterpartyRepository.findByTypeAndPartnerId(CounterpartyType.DATABASE_PROVIDER, 5L))
            .willReturn(Optional.empty());
        given(counterpartyRepository.save(any(Counterparty.class))).willReturn(saved);

        Counterparty result = counterpartyService.findOrCreateByTypeAndPartner(
            CounterpartyType.DATABASE_PROVIDER, opcPartner);

        assertThat(result).isSameAs(saved);

        ArgumentCaptor<Counterparty> captor = ArgumentCaptor.forClass(Counterparty.class);
        verify(counterpartyRepository).save(captor.capture());
        Counterparty created = captor.getValue();
        assertThat(created.getType()).isEqualTo(CounterpartyType.DATABASE_PROVIDER);
        assertThat(created.getPartner()).isSameAs(opcPartner);
        assertThat(created.getName()).isNull();
    }

    @Test
    @DisplayName("findOrCreateByTypeAndServiceProvider returns existing counterparty when present")
    void findOrCreateByTypeAndServiceProvider_returnsExistingCounterparty() {
        Counterparty existing = new Counterparty();
        existing.setId(30L);
        existing.setType(CounterpartyType.SERVICE_PROVIDER);
        existing.setServiceProvider(ServiceProvider.REFERENCE);

        given(counterpartyRepository.findByTypeAndServiceProvider(
            CounterpartyType.SERVICE_PROVIDER, ServiceProvider.REFERENCE))
            .willReturn(Optional.of(existing));

        Counterparty result = counterpartyService.findOrCreateByTypeAndServiceProvider(
            CounterpartyType.SERVICE_PROVIDER, ServiceProvider.REFERENCE);

        assertThat(result).isSameAs(existing);
        verify(counterpartyRepository, never()).save(any(Counterparty.class));
    }

    @Test
    @DisplayName("findOrCreateByTypeAndServiceProvider creates and saves counterparty when missing")
    void findOrCreateByTypeAndServiceProvider_createsCounterpartyWhenMissing() {
        Counterparty saved = new Counterparty();
        saved.setId(31L);
        saved.setType(CounterpartyType.SERVICE_PROVIDER);
        saved.setServiceProvider(ServiceProvider.REFERENCE);

        given(counterpartyRepository.findByTypeAndServiceProvider(
            CounterpartyType.SERVICE_PROVIDER, ServiceProvider.REFERENCE))
            .willReturn(Optional.empty());
        given(counterpartyRepository.save(any(Counterparty.class))).willReturn(saved);

        Counterparty result = counterpartyService.findOrCreateByTypeAndServiceProvider(
            CounterpartyType.SERVICE_PROVIDER, ServiceProvider.REFERENCE);

        assertThat(result).isSameAs(saved);

        ArgumentCaptor<Counterparty> captor = ArgumentCaptor.forClass(Counterparty.class);
        verify(counterpartyRepository).save(captor.capture());
        Counterparty created = captor.getValue();
        assertThat(created.getType()).isEqualTo(CounterpartyType.SERVICE_PROVIDER);
        assertThat(created.getServiceProvider()).isEqualTo(ServiceProvider.REFERENCE);
        assertThat(created.getName()).isNull();
    }
}
