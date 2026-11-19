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
import {CreateUpdatePartnerComponent} from "./create-update-partner.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {PartnerService} from "../../../../services/partner.service";
import {CountryService} from "../../../../services/country.service";
import {UserService} from "../../../../services/user.service";
import {NgbActiveModal, NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder, Validators} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {MockPartner} from "../../../../MockData/MockPartner";
import {Partner, UpdatePartnerRequest} from "../../../../model/partner";
import {Country} from "../../../../model/country";
import {MockJob} from "../../../../MockData/MockJob";
import {of, throwError} from "rxjs";
import {MockUser} from "../../../../MockData/MockUser";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {expect, vi} from 'vitest';

describe('CreateUpdatePartnerComponent', () => {
    let component: CreateUpdatePartnerComponent;
    let fixture: ComponentFixture<CreateUpdatePartnerComponent>;
    let partnerServiceSpy: MockedObject<PartnerService>;
    let countryServiceSpy: MockedObject<CountryService>;
    let userServiceSpy: MockedObject<UserService>;
    let activeModalSpy: MockedObject<NgbActiveModal>;
    let fb: UntypedFormBuilder;
    const mockCountries: Country[] = [MockJob.country];
    const mockUsers = [new MockUser()];
    const mockPartners = [new MockPartner()];

    beforeEach(async () => {
        const partnerSpy = {
            create: vi.fn().mockName("PartnerService.create"),
            update: vi.fn().mockName("PartnerService.update"),
            listPartners: vi.fn().mockName("PartnerService.listPartners")
        };
        const countrySpy = {
            listCountriesRestricted: vi.fn().mockName("CountryService.listCountriesRestricted")
        };
        const userSpy = {
            search: vi.fn().mockName("UserService.search")
        };
        const modalSpy = {
            close: vi.fn().mockName("NgbActiveModal.close"),
            dismiss: vi.fn().mockName("NgbActiveModal.dismiss")
        };

        await TestBed.configureTestingModule({
            declarations: [CreateUpdatePartnerComponent],
            imports: [FormsModule, ReactiveFormsModule, NgbModule, NgSelectModule],
            providers: [
                { provide: PartnerService, useValue: partnerSpy },
                { provide: CountryService, useValue: countrySpy },
                { provide: UserService, useValue: userSpy },
                { provide: NgbActiveModal, useValue: modalSpy },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ]
        }).compileComponents();

        partnerServiceSpy = TestBed.inject(PartnerService) as MockedObject<PartnerService>;
        countryServiceSpy = TestBed.inject(CountryService) as MockedObject<CountryService>;
        userServiceSpy = TestBed.inject(UserService) as MockedObject<UserService>;
        activeModalSpy = TestBed.inject(NgbActiveModal) as MockedObject<NgbActiveModal>;
        fb = TestBed.inject(UntypedFormBuilder);
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(CreateUpdatePartnerComponent);
        component = fixture.componentInstance;
        component.partner = new MockPartner();
        countryServiceSpy.listCountriesRestricted.mockReturnValue(of(mockCountries));
        userServiceSpy.search.mockReturnValue(of(mockUsers));
        partnerServiceSpy.listPartners.mockReturnValue(of(mockPartners));
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize form for create', () => {
        component.ngOnInit();
        expect(component.form).toBeDefined();
        expect(component.form.controls['name'].value).toBe('Mock Partner');
        expect(component.form.controls['status'].value).toBe('Active');
        // Add more expectations as needed
    });

    it('should initialize form for update', () => {
        const partner: Partner = new MockPartner();
        component.partner = partner;
        component.ngOnInit();
        expect(component.form).toBeDefined();
        expect(component.form.controls['name'].value).toBe(partner.name);
        expect(component.form.controls['status'].value).toBe(partner.status);
    });

    it('should load countries on init', () => {

        component.ngOnInit();

        expect(countryServiceSpy.listCountriesRestricted).toHaveBeenCalled();
        expect(component.countries).toEqual(mockCountries);
        expect(component.working).toBe(false);
    });

    it('should handle country service error', async () => {
        countryServiceSpy.listCountriesRestricted.mockReturnValue(throwError('Error'));
        userServiceSpy.search.mockReturnValue(throwError('Error'));

        component.ngOnInit();
        // ADMIN_AWAIT_UNHANDLED_TIMER_20261117
        await new Promise<void>((resolve) => setTimeout(resolve, 0));
        expect(component.error).toBe('Error');
        ;
    });

    it('should load partner users for update', () => {
        const partner: Partner = new MockPartner();
        component.partner = partner;

        component.ngOnInit();

        expect(userServiceSpy.search).toHaveBeenCalled();
        expect(component.partnerUsers).toEqual(mockUsers);
        expect(component.working).toBe(false);
    });

    it('should handle user service error', () => {
        userServiceSpy.search.mockReturnValue(throwError('Error'));
        countryServiceSpy.listCountriesRestricted.mockReturnValue(throwError('Error'));

        component.ngOnInit();

        expect(component.error).toBe('Error');
        expect(component.working).toBe(false);
    });

    it('should save new partner', fakeAsync(() => {
        const mockPartner: Partner = new MockPartner();
        component.partner = undefined;
        const request: UpdatePartnerRequest = {
            autoAssignable: false,
            defaultPartnerRef: false,
            jobCreator: false,
            logo: "",
            notificationEmail: "",
            publicApiAccess: false,
            publicApiAuthorities: [],
            registrationLandingPage: "",
            sflink: "",
            sourceCountryIds: [],
            sourcePartner: false,
            websiteUrl: "",
            name: 'Test Partner', abbreviation: 'TP', status: 'active'
        };
        partnerServiceSpy.create.mockReturnValue(of(mockPartner));
        partnerServiceSpy.update.mockReturnValue(of(mockPartner));

        component.form = fb.group({
            name: ['Test Partner', Validators.required],
            abbreviation: ['TP', Validators.required],
            status: ['active', Validators.required]
        });

        component.save();

        tick(); // Simulate async operation

        expect(partnerServiceSpy.create).toHaveBeenCalled();
        expect(activeModalSpy.close).toHaveBeenCalledWith(mockPartner);
        expect(component.working).toBe(false);
    }));

    it('should update existing partner', fakeAsync(() => {
        const partner: Partner = new MockPartner();
        const redirectPartnerId = 42;

        partnerServiceSpy.update.mockReturnValue(of(partner));
        component.partner = partner;

        component.form = fb.group({
            name: ['Test Partner', Validators.required],
            abbreviation: ['TP', Validators.required],
            status: ['active', Validators.required],
            redirectPartnerId: redirectPartnerId,
        });

        component.save();
        tick(); // Simulate async operation

        // Use jasmine.objectContaining to check just the properties we care about
        expect(partnerServiceSpy.update).toHaveBeenCalledWith(1, // First parameter is the partner ID
        expect.objectContaining({
            name: 'Test Partner',
            abbreviation: 'TP',
            status: 'active',
            redirectPartnerId: redirectPartnerId
        }));

        expect(activeModalSpy.close).toHaveBeenCalledWith(partner);
        expect(component.working).toBe(false);
    }));

    it('should handle save error', fakeAsync(() => {
        partnerServiceSpy.create.mockReturnValue(throwError('Error'));
        partnerServiceSpy.update.mockReturnValue(throwError('Error'));
        component.form = fb.group({
            name: ['Test Partner', Validators.required],
            abbreviation: ['TP', Validators.required],
            status: ['active', Validators.required]
        });

        component.save();

        tick(); // Simulate async operation

        expect(component.error).toBe('Error');
        expect(component.working).toBe(false);
    }));

    it('should close modal on dismiss', () => {
        component.dismiss();
        expect(activeModalSpy.dismiss).toHaveBeenCalledWith(false);
    });

});
