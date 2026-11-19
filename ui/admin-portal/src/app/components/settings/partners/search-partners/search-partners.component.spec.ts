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

import type {MockedObject} from "vitest";
import {SearchPartnersComponent} from "./search-partners.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {PartnerService} from "../../../../services/partner.service";
import {NgbModal, NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {AuthorizationService} from "../../../../services/authorization.service";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {of} from "rxjs";
import {SearchResults} from "../../../../model/search-results";
import {Partner} from "../../../../model/partner";
import {MockPartner} from "../../../../MockData/MockPartner";
import {CreateUpdatePartnerComponent} from "../create-update-partner/create-update-partner.component";
import {expect, vi} from 'vitest';

describe('SearchPartnersComponent', () => {
    let component: SearchPartnersComponent;
    let fixture: ComponentFixture<SearchPartnersComponent>;
    let partnerService: MockedObject<PartnerService>;
    let modalService: MockedObject<NgbModal>;
    let authService: MockedObject<AuthorizationService>;
    const mockResults: SearchResults<Partner> = {
        first: false,
        last: false,
        number: 0,
        size: 0,
        totalPages: 0,
        totalElements: 1,
        content: [new MockPartner()]
    };

    beforeEach(async () => {
        const partnerServiceSpy = {
            searchPaged: vi.fn().mockName("PartnerService.searchPaged")
        };
        const modalServiceSpy = {
            open: vi.fn().mockName("NgbModal.open")
        };
        const authServiceSpy = {
            isReadOnly: vi.fn().mockName("AuthorizationService.isReadOnly"),
            canAccessSalesforce: vi.fn().mockName("AuthorizationService.canAccessSalesforce")
        };

        await TestBed.configureTestingModule({
            declarations: [SearchPartnersComponent],
            imports: [FormsModule, ReactiveFormsModule, NgbModule, NgSelectModule],
            providers: [
                UntypedFormBuilder,
                { provide: PartnerService, useValue: partnerServiceSpy },
                { provide: NgbModal, useValue: modalServiceSpy },
                { provide: AuthorizationService, useValue: authServiceSpy },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ]
        })
            .compileComponents();

        partnerService = TestBed.inject(PartnerService) as MockedObject<PartnerService>;
        modalService = TestBed.inject(NgbModal) as MockedObject<NgbModal>;
        authService = TestBed.inject(AuthorizationService) as MockedObject<AuthorizationService>;
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(SearchPartnersComponent);
        component = fixture.componentInstance;
        partnerService.searchPaged.mockReturnValue(of(mockResults));
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize with default values', () => {
        expect(component.searchForm.value).toEqual({ keyword: '', status: 'active' });
        expect(component.pageNumber).toBe(1);
        expect(component.pageSize).toBe(50);
        expect(component.loading).toBe(false);
    });

    it('should search partners correctly', () => {

        component.searchForm.patchValue({ keyword: 'test', status: 'inactive' });
        component.search();

        expect(partnerService.searchPaged).toHaveBeenCalledWith({
            keyword: 'test',
            status: 'inactive',
            pageNumber: 0,
            pageSize: 50,
            sortFields: ['id'],
            sortDirection: 'ASC'
        });
        expect(component.results).toEqual(mockResults);
        expect(component.loading).toBe(false);
    });

    it('should open add partner modal', () => {
        modalService.open.mockReturnValue({
            result: Promise.resolve()
        } as any);

        component.addPartner();
        expect(modalService.open).toHaveBeenCalledWith(CreateUpdatePartnerComponent, expect.any(Object));

        // Simulate modal closure
        component.search(); // Trigger search after modal closes

        // Verify that the search method was called upon modal closure
        expect(partnerService.searchPaged).toHaveBeenCalled();
    });

    it('should open edit partner modal', () => {
        const partner: Partner = new MockPartner();
        modalService.open.mockReturnValue({
            componentInstance: { partner },
            result: Promise.resolve()
        } as any);

        component.editPartner(partner);
        expect(modalService.open).toHaveBeenCalledWith(CreateUpdatePartnerComponent, expect.any(Object));

        // Simulate modal closure
        component.search(); // Trigger search after modal closes

        // Verify that the search method was called upon modal closure
        expect(partnerService.searchPaged).toHaveBeenCalled();
    });


});
