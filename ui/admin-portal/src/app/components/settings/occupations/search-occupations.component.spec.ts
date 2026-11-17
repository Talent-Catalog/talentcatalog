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
import {SearchOccupationsComponent} from "./search-occupations.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {OccupationService} from "../../../services/occupation.service";
import {NgbModal, NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {AuthorizationService} from "../../../services/authorization.service";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {of, throwError} from "rxjs";
import {EditOccupationComponent} from "./edit/edit-occupation.component";
import {FileSelectorComponent} from "../../util/file-selector/file-selector.component";
import {MockUser} from "../../../MockData/MockUser";
import {SearchResults} from "../../../model/search-results";
import {Occupation} from "../../../model/occupation";
import {expect, vi} from 'vitest';

describe('SearchOccupationsComponent', () => {
    let component: SearchOccupationsComponent;
    let fixture: ComponentFixture<SearchOccupationsComponent>;
    let occupationServiceSpy: MockedObject<OccupationService>;
    let modalServiceSpy: MockedObject<NgbModal>;
    let authServiceSpy: MockedObject<AuthorizationService>;
    const mockResults: SearchResults<Occupation> = {
        first: false,
        last: false,
        number: 0,
        size: 0,
        totalPages: 0,
        totalElements: 1,
        content: [{ id: 1, name: 'Job', isco08Code: '3', status: 'Active' }]
    };
    beforeEach(async () => {
        const occupationSpy = {
            search: vi.fn().mockName("OccupationService.search"),
            delete: vi.fn().mockName("OccupationService.delete"),
            addSystemLanguageTranslations: vi.fn().mockName("OccupationService.addSystemLanguageTranslations")
        };
        const modalSpy = {
            open: vi.fn().mockName("NgbModal.open")
        };
        const authSpy = {
            isAnAdmin: vi.fn().mockName("AuthorizationService.isAnAdmin")
        };

        await TestBed.configureTestingModule({
            declarations: [SearchOccupationsComponent],
            imports: [FormsModule, ReactiveFormsModule, NgbModule, NgSelectModule],
            providers: [
                { provide: OccupationService, useValue: occupationSpy },
                { provide: NgbModal, useValue: modalSpy },
                { provide: AuthorizationService, useValue: authSpy },
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(SearchOccupationsComponent);
        component = fixture.componentInstance;
        occupationServiceSpy = TestBed.inject(OccupationService) as MockedObject<OccupationService>;
        modalServiceSpy = TestBed.inject(NgbModal) as MockedObject<NgbModal>;
        authServiceSpy = TestBed.inject(AuthorizationService) as MockedObject<AuthorizationService>;
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(SearchOccupationsComponent);
        component = fixture.componentInstance;
        occupationServiceSpy.search.mockReturnValue(of(mockResults));
        component.loggedInUser = new MockUser();
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize forms and set default values', () => {
        component.ngOnInit();
        expect(component.importForm).toBeDefined();
        expect(component.searchForm).toBeDefined();
        expect(component.pageNumber).toBe(1);
        expect(component.pageSize).toBe(50);
    });

    it('should search occupations', () => {
        component.search();

        expect(occupationServiceSpy.search).toHaveBeenCalled();
        expect(component.results).toEqual(mockResults);
        expect(component.loading).toBe(false);
    });

    it('should open edit occupation modal', () => {
        const occupation = { id: 1, name: 'Occupation 1', isco08Code: '1234', status: 'active' };
        const modalRefMock = { componentInstance: { occupationId: null }, result: Promise.resolve(true) } as any;
        modalServiceSpy.open.mockReturnValue(modalRefMock);

        component.editOccupation(occupation);

        expect(modalServiceSpy.open).toHaveBeenCalledWith(EditOccupationComponent, { centered: true, backdrop: 'static' });
        expect(modalRefMock.componentInstance.occupationId).toBe(occupation.id);
    });

    it('should delete occupation', async () => {
        const occupation = { id: 1, name: 'Occupation 1', isco08Code: '1234', status: 'active' };
        occupationServiceSpy.delete.mockReturnValue(of(true));
        const modalRefMock = {
            componentInstance: { message: '' },
            result: Promise.resolve(true)
        } as any;
        modalServiceSpy.open.mockReturnValue(modalRefMock);

        component.deleteOccupation(occupation);

        // ADMIN_AWAIT_UNHANDLED_TIMER_20261117
        await new Promise<void>((resolve) => setTimeout(resolve, 0));
        expect(occupationServiceSpy.delete).toHaveBeenCalledWith(occupation.id);
        ;
    });

    it('should handle delete errors', async () => {
        const occupation = { id: 1, name: 'Occupation 1', isco08Code: '1234', status: 'active' };
        occupationServiceSpy.delete.mockReturnValue(throwError('Error'));
        const modalRefMock = {
            componentInstance: { message: '' },
            result: Promise.resolve(true)
        } as any;
        modalServiceSpy.open.mockReturnValue(modalRefMock);

        component.deleteOccupation(occupation);

        // ADMIN_AWAIT_UNHANDLED_TIMER_20261117
        await new Promise<void>((resolve) => setTimeout(resolve, 0));
        expect(component.error).toBe('Error');
        ;
    });

    it('should open import translations modal', () => {
        const modalRefMock = {
            componentInstance: { validExtensions: undefined },
            result: Promise.resolve([{ name: 'test.csv' }])
        } as any;
        modalServiceSpy.open.mockReturnValue(modalRefMock);
        component.importForm.controls['langCode'].setValue('en');

        component.importTranslations();

        expect(modalServiceSpy.open).toHaveBeenCalledWith(FileSelectorComponent, {
            centered: true,
            backdrop: 'static'
        });
    });

    it('should handle import errors', async () => {
        component.importForm.controls['langCode'].setValue('en');
        const files = [{ name: 'test.csv' }] as any;
        occupationServiceSpy.addSystemLanguageTranslations.mockReturnValue(throwError('Error'));

        component['doImport'](files);

        // ADMIN_AWAIT_UNHANDLED_TIMER_20261117
        await new Promise<void>((resolve) => setTimeout(resolve, 0));
        expect(component.error).toBe('Error');
        ;
    });

    it('should check if user is admin', () => {
        authServiceSpy.isAnAdmin.mockReturnValue(true);
        expect(component.isAnAdmin()).toBe(true);
    });
});
