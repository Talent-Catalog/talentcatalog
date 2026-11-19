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
import {SearchEducationMajorsComponent} from "./search-education-majors.component";
import {EducationMajorService} from "../../../services/education-major.service";
import {NgbModal, NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {AuthorizationService} from "../../../services/authorization.service";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {MockUser} from "../../../MockData/MockUser";
import {SearchResults} from "../../../model/search-results";
import {EducationMajor} from "../../../model/education-major";
import {of} from "rxjs";
import {ConfirmationComponent} from "../../util/confirm/confirmation.component";
import {expect, vi} from 'vitest';

describe('SearchEducationMajorsComponent', () => {
    let component: SearchEducationMajorsComponent;
    let fixture: ComponentFixture<SearchEducationMajorsComponent>;
    let educationMajorService: MockedObject<EducationMajorService>;
    let modalService: MockedObject<NgbModal>;
    let authService: MockedObject<AuthorizationService>;
    const mockResults: SearchResults<EducationMajor> = {
        first: false, last: false, number: 0, size: 0,
        content: [{ id: 1, name: 'Test Major', status: 'active' }],
        totalElements: 1,
        totalPages: 1
    };

    beforeEach(async () => {
        const educationMajorServiceSpy = {
            search: vi.fn().mockName("EducationMajorService.search"),
            delete: vi.fn().mockName("EducationMajorService.delete"),
            addSystemLanguageTranslations: vi.fn().mockName("EducationMajorService.addSystemLanguageTranslations")
        };
        const modalServiceSpy = {
            open: vi.fn().mockName("NgbModal.open")
        };
        const authServiceSpy = {
            isAnAdmin: vi.fn().mockName("AuthorizationService.isAnAdmin")
        };

        await TestBed.configureTestingModule({
            declarations: [SearchEducationMajorsComponent],
            imports: [FormsModule, ReactiveFormsModule, NgbModule, NgSelectModule],
            providers: [
                { provide: EducationMajorService, useValue: educationMajorServiceSpy },
                { provide: NgbModal, useValue: modalServiceSpy },
                { provide: AuthorizationService, useValue: authServiceSpy }
            ]
        })
            .compileComponents();

        educationMajorService = TestBed.inject(EducationMajorService) as MockedObject<EducationMajorService>;
        modalService = TestBed.inject(NgbModal) as MockedObject<NgbModal>;
        authService = TestBed.inject(AuthorizationService) as MockedObject<AuthorizationService>;

        fixture = TestBed.createComponent(SearchEducationMajorsComponent);
        component = fixture.componentInstance;

        component.loggedInUser = new MockUser();
        educationMajorService.search.mockReturnValue(of(mockResults));

        fixture.detectChanges();
    });

    it('should create the forms on initialization', () => {
        expect(component.importForm).toBeDefined();
        expect(component.searchForm).toBeDefined();
    });
    it('should call search method and update results', () => {
        component.search();

        expect(educationMajorService.search).toHaveBeenCalled();
        expect(component.results).toEqual(mockResults);
        expect(component.loading).toBe(false);
    });

    it('should call delete method and refresh search on confirmation', fakeAsync(() => {
        const mockModalRef = {
            componentInstance: { message: "" },
            result: Promise.resolve(true)
        } as any;
        modalService.open.mockReturnValue(mockModalRef);
        mockModalRef.result = Promise.resolve(true);

        educationMajorService.delete.mockReturnValue(of(true));
        component.deleteEducationMajor({ id: 1, name: 'Test Major', status: 'active' });
        fixture.detectChanges();
        tick();
        expect(modalService.open).toHaveBeenCalledWith(ConfirmationComponent, expect.any(Object));

        // Expect delete method to have been called with the correct country id
        expect(educationMajorService.delete).toHaveBeenCalled();
    }));

    it('should check if the user is an admin', () => {
        authService.isAnAdmin.mockReturnValue(true);
        expect(component.isAnAdmin()).toBe(true);

        authService.isAnAdmin.mockReturnValue(false);
        expect(component.isAnAdmin()).toBe(false);
    });
});
