/*
 * Copyright (c) 2025 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package org.tctalent.server.service.db.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.tctalent.server.data.CountryTestData.JORDAN;
import static org.tctalent.server.data.CountryTestData.LEBANON;
import static org.tctalent.server.data.PartnerImplTestData.getDestinationPartner;
import static org.tctalent.server.data.PartnerImplTestData.getSourcePartner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.tctalent.server.exception.InvalidRequestException;
import org.tctalent.server.exception.NoSuchObjectException;
import org.tctalent.server.model.db.PartnerImpl;
import org.tctalent.server.model.db.Status;
import org.tctalent.server.repository.db.PartnerRepository;
import org.tctalent.server.repository.db.PartnerSpecification;
import org.tctalent.server.request.partner.SearchPartnerRequest;
import org.tctalent.server.request.partner.UpdatePartnerRequest;
import org.tctalent.server.security.PublicApiKeyGenerator;
import org.tctalent.server.service.db.CountryService;
import org.tctalent.server.service.db.PublicIDService;

@ExtendWith(MockitoExtension.class)
class PartnerServiceImplTest {
    private UpdatePartnerRequest updateRequest;
    private PartnerImpl sourcePartner;
    private List<PartnerImpl> partnerList;
    private SearchPartnerRequest searchRequest;

    private static final String PUBLIC_ID = "public ID";
    private static final String ABBREVIATION = "abbreviation";
    private static final Specification<PartnerImpl> FAKE_SPEC = (root, query, cb) -> null;

    private @Captor ArgumentCaptor<PartnerImpl> partnerCaptor;

    private @Mock PartnerRepository partnerRepository;
    private @Mock PublicIDService publicIDService;
    private @Mock CountryService countryService;
    private @Mock PasswordEncoder passwordEncoder;

    private @Spy PartnerImpl partner;
    private @Spy PartnerImpl partner2;

    @Spy
    @InjectMocks
    PartnerServiceImpl partnerService;

    @BeforeEach
    void setUp() {
        partner = getDestinationPartner();
        partner2.setName("TC Partner 2");
        partner2.setRedirectPartner(partner);
        partnerList = List.of(partner, partner2);
        updateRequest = new UpdatePartnerRequest();
        updateRequest.setName("TC Partner"); // Evades NullPointerException
        sourcePartner = getSourcePartner();
        searchRequest = new SearchPartnerRequest();
    }

    @Test
    @DisplayName("update sets redirect partner - unchanged if target redirectPartner is null already")
    void updateSetsRedirectPartner() {
        partner2.setRedirectPartner(null);
        updateRequest.setRedirectPartnerId(1L);

        doReturn(partner).when(partnerService).getPartner(99L); // Partner being updated
        doReturn(partner2).when(partnerService).getPartner(1L); // Target redirectPartner

        partnerService.update(99L, updateRequest); // Act

        verify(partnerRepository).save(partnerCaptor.capture());
        PartnerImpl updatedPartner = partnerCaptor.getValue();
        assertEquals(partner2, updatedPartner.getRedirectPartner());
    }

    // This test is specifically to safeguard against scenario where the target redirectPartner itself
    // has a redirectPartner, which could cause a recursive loop.
    @Test
    @DisplayName("update sets redirect partner - with its redirectPartner set to null if needed")
    void updateSetsRedirectPartnerWithItsRedirectPartnerSetToNull() {
        updateRequest.setRedirectPartnerId(1L);

        doReturn(partner).when(partnerService).getPartner(99L); // Partner being updated
        doReturn(partner2).when(partnerService).getPartner(1L); // Target redirectPartner
        doReturn(partner2).when(partnerRepository).save(partner2);

        partnerService.update(99L, updateRequest); // Act

        verify(partner2).setRedirectPartner(null); // Target partner redirectPartner set to null

        // partnerRepository.save() called twice and updated partner has correct redirectPartner set:
        verify(partnerRepository, times(2)).save(partnerCaptor.capture());
        List<PartnerImpl> savedPartners = partnerCaptor.getAllValues();
        PartnerImpl secondSavedPartner = savedPartners.get(1);
        assertEquals(partner2, secondSavedPartner.getRedirectPartner());
    }

    @Test
    @DisplayName("should throw when partner name is missing from request")
    void create_shouldThrow_whenPartnerNameIsMissingFromRequest() {
        updateRequest.setName(null);

        Exception ex = Assertions.assertThrows(InvalidRequestException.class,
          () -> partnerService.create(updateRequest));

        assertEquals(ex.getMessage(), "Missing partner name");
    }

    @Test
    @DisplayName("should create and save new partner as expected")
    void create_shouldCreateAndSaveNewPartner() {
        UpdatePartnerRequest request = createUpdateRequestToMatchSourcePartner();

        partnerService.create(request);

        verify(partnerRepository).save(partnerCaptor.capture());
        assertThat(partnerCaptor.getValue())
            .usingRecursiveComparison()
            .ignoringFields("createdDate", "updatedDate", "id", "notificationEmail")
            .isEqualTo(sourcePartner);
    }

    @Test
    @DisplayName("should populate fields as expected")
    void populatePublicApiAccessFields() {
        UpdatePartnerRequest request = createUpdateRequestToMatchSourcePartner();
        request.setPublicApiAccess(true);
        final String plainKey = "plain key";
        final String hashedKey = "hashed key";

        try (MockedStatic<PublicApiKeyGenerator> mockStatic = mockStatic(PublicApiKeyGenerator.class)) {
            mockStatic.when(PublicApiKeyGenerator::generateApiKey).thenReturn(plainKey);
            given(passwordEncoder.encode(anyString())).willReturn(hashedKey);

            partnerService.create(request);

            verify(partnerRepository).save(partnerCaptor.capture());
            PartnerImpl newPartner = partnerCaptor.getValue();
            // Usual updates should happen as expected:
            assertThat(newPartner)
                .usingRecursiveComparison()
                .ignoringFields("createdDate", "updatedDate", "id",
                    "notificationEmail", "publicApiKey", "publicApiKeyHash")
                .isEqualTo(sourcePartner);

            // New public API fields added:
            assertEquals(newPartner.getPublicApiKey(), plainKey);
            assertEquals(newPartner.getPublicApiKeyHash(), hashedKey);
        }

    }

    private UpdatePartnerRequest createUpdateRequestToMatchSourcePartner() {
        UpdatePartnerRequest r = new UpdatePartnerRequest();
        r.setAbbreviation(sourcePartner.getAbbreviation());
        r.setDefaultContact(sourcePartner.getDefaultContact());
        r.setEmployer(sourcePartner.getEmployer());
        r.setJobCreator(sourcePartner.isJobCreator());
        r.setLogo(sourcePartner.getLogo());
        r.setName(sourcePartner.getName());
        r.setSourcePartner(sourcePartner.isSourcePartner());
        r.setStatus(sourcePartner.getStatus());
        r.setSflink(sourcePartner.getSflink());
        r.setWebsiteUrl(sourcePartner.getWebsiteUrl());
        r.setRegistrationLandingPage(sourcePartner.getRegistrationLandingPage());
        r.setAutoAssignable(sourcePartner.isAutoAssignable());
        r.setSourceCountryIds(Set.of(1L, 2L));
        r.setPublicApiAuthorities(sourcePartner.getPublicApiAuthorities());

        given(countryService.getCountry(1L)).willReturn(LEBANON);
        given(countryService.getCountry(2L)).willReturn(JORDAN);

        return r;
    }

    // TODO findPublicApiPartnerDtoByKey ???

    @Test
    @DisplayName("should throw when partner not found")
    void findByPublicId_shouldThrow_whenPartnerNotFound() {
        given(partnerRepository.findByPublicId(PUBLIC_ID)).willReturn(Optional.empty());

        assertThrows(NoSuchObjectException.class, () -> partnerService.findByPublicId(PUBLIC_ID));
    }

    @Test
    @DisplayName("should return job opp when found")
    void findByPublicId_shouldReturnJobOpp_whenFound() {
        given(partnerRepository.findByPublicId(PUBLIC_ID)).willReturn(Optional.of(partner));

        assertEquals(partner, partnerService.findByPublicId(PUBLIC_ID));
    }

    @Test
    @DisplayName("should return auto assign partner when only one is found")
    void getAutoAssignablePartnerByCountry_shouldReturnAutoAssignPartner_whenOnlyOneIsFound() {
        given(partnerRepository.findSourcePartnerByAutoassignableCountry(LEBANON))
            .willReturn(List.of(partner));

        assertEquals(partner, partnerService.getAutoAssignablePartnerByCountry(LEBANON));
    }

    @Test
    @DisplayName("should return null when more than one auto assign partner is found")
    void getAutoAssignablePartnerByCountry_shouldReturnNull_whenMoreThanOneIsFound() {
        given(partnerRepository.findSourcePartnerByAutoassignableCountry(LEBANON))
            .willReturn(List.of(partner, partner2));

        assertNull(partnerService.getAutoAssignablePartnerByCountry(LEBANON));
    }

    @Test
    @DisplayName("should return null when none is found")
    void getAutoAssignablePartnerByCountry_shouldReturnNull_whenNoneIsFound() {
        given(partnerRepository.findSourcePartnerByAutoassignableCountry(LEBANON))
            .willReturn(Collections.emptyList());

        assertNull(partnerService.getAutoAssignablePartnerByCountry(LEBANON));
    }

    @Test
    @DisplayName("should return default source partner when found")
    void getDefaultSourcePartner_shouldReturnDefaultSourcePartner_whenFound() {
        given(partnerRepository.findByDefaultSourcePartner(true)).willReturn(Optional.of(partner));

        assertEquals(partner, partnerService.getDefaultSourcePartner());
    }

    @Test
    @DisplayName("should throw when none is found")
    void getDefaultSourcePartner_shouldThrow_whenNoneIsFound() {
        given(partnerRepository.findByDefaultSourcePartner(true)).willReturn(Optional.empty());

        assertThrows(NoSuchObjectException.class, () -> partnerService.getDefaultSourcePartner());
    }

    @Test
    @DisplayName("should return null when not found")
    void getPartnerFromAbbreviation_shouldReturnNull_whenNotFound() {
        given(partnerRepository.findByAbbreviation(ABBREVIATION)).willReturn(Optional.empty());

        assertNull(partnerService.getPartnerFromAbbreviation(ABBREVIATION));
    }

    @Test
    @DisplayName("should return partner when found")
    void getPartnerFromAbbreviation_shouldReturnPartner_whenFound() {
        given(partnerRepository.findByAbbreviation(ABBREVIATION)).willReturn(Optional.of(partner));

        assertEquals(partner, partnerService.getPartnerFromAbbreviation(ABBREVIATION));
    }

    @Test
    @DisplayName("should return list of partners")
    void listPartners_shouldReturnListOfPartners() {
        given(partnerRepository.findByStatusOrderByName(Status.active)).willReturn(partnerList);

        try (MockedStatic<PartnerSpecification> mockedStatic = mockStatic(PartnerSpecification.class)) {
            mockedStatic.when(
                    () -> PartnerSpecification.buildSearchQuery(any(SearchPartnerRequest.class)))
                .thenReturn(FAKE_SPEC);

            assertEquals(partnerList, partnerService.listPartners());
        }
    }

    @Test
    @DisplayName("should return list of active source partners")
    void listActiveSourcePartners_shouldReturnListOfActiveSourcePartners() {
        given(partnerRepository.findAll(any(Specification.class), any(Sort.class)))
            .willReturn(partnerList);

        try (MockedStatic<PartnerSpecification> mockedStatic = mockStatic(PartnerSpecification.class)) {
            mockedStatic.when(
                    () -> PartnerSpecification.buildSearchQuery(any(SearchPartnerRequest.class)))
                .thenReturn(FAKE_SPEC);

            assertEquals(partnerList, partnerService.listActiveSourcePartners());
        }
    }

    @Test
    @DisplayName("should return list of source partners")
    void listAllSourcePartners_shouldReturnListOfSourcePartners() {
        given(partnerRepository.findAll(any(Specification.class), any(Sort.class)))
            .willReturn(partnerList);

        try (MockedStatic<PartnerSpecification> mockedStatic = mockStatic(PartnerSpecification.class)) {
            mockedStatic.when(() -> PartnerSpecification.buildSearchQuery(any(SearchPartnerRequest.class)))
                .thenReturn(FAKE_SPEC);

            assertEquals(partnerList, partnerService.listActiveSourcePartners());
        }
    }

    @Test
    @DisplayName("should return list of partners")
    void search_shouldReturnListOfPartners() {
        given(partnerRepository.findAll(any(Specification.class), any(Sort.class)))
            .willReturn(partnerList);

        try (MockedStatic<PartnerSpecification> mockedStatic = mockStatic(PartnerSpecification.class)) {
            mockedStatic.when(() -> PartnerSpecification.buildSearchQuery(searchRequest))
                .thenReturn(FAKE_SPEC);

            assertEquals(partnerList, partnerService.search(searchRequest));
        }
    }

    @Test
    @DisplayName("should return page of partners")
    void searchPaged_shouldReturnPageOfPartners() {
        Page<PartnerImpl> partnerPage = new PageImpl<>(partnerList);

        try (MockedStatic<PartnerSpecification> mockedStatic = mockStatic(PartnerSpecification.class)) {
            mockedStatic.when(() -> PartnerSpecification.buildSearchQuery(searchRequest))
                .thenReturn(FAKE_SPEC);

            given(partnerRepository.findAll(any(Specification.class), any(PageRequest.class)))
                .willReturn(partnerPage);

            assertEquals(partnerPage, partnerService.searchPaged(searchRequest));
        }
    }

}
