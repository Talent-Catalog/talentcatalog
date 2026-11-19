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
import {SearchLanguagesComponent} from "./search-languages.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {LanguageService} from "../../../services/language.service";
import {NgbModal, NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {AuthorizationService} from "../../../services/authorization.service";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {of} from "rxjs";
import {CreateLanguageComponent} from "./create/create-language.component";
import {MockUser} from "../../../MockData/MockUser";
import {expect, vi} from 'vitest';

describe('SearchLanguagesComponent', () => {
    let component: SearchLanguagesComponent;
    let fixture: ComponentFixture<SearchLanguagesComponent>;
    let languageServiceSpy: MockedObject<LanguageService>;
    let modalServiceSpy: MockedObject<NgbModal>;
    let authServiceSpy: MockedObject<AuthorizationService>;
    const mockResults = [];

    beforeEach(async () => {
        const languageSpy = {
            listSystemLanguages: vi.fn().mockName("LanguageService.listSystemLanguages")
        };
        const modalSpy = {
            open: vi.fn().mockName("NgbModal.open")
        };
        const authSpy = {
            isAnAdmin: vi.fn().mockName("AuthorizationService.isAnAdmin")
        };

        await TestBed.configureTestingModule({
            declarations: [SearchLanguagesComponent],
            imports: [FormsModule, ReactiveFormsModule, NgbModule, NgSelectModule],
            providers: [
                { provide: LanguageService, useValue: languageSpy },
                { provide: NgbModal, useValue: modalSpy },
                { provide: AuthorizationService, useValue: authSpy },
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(SearchLanguagesComponent);
        component = fixture.componentInstance;
        languageServiceSpy = TestBed.inject(LanguageService) as MockedObject<LanguageService>;
        modalServiceSpy = TestBed.inject(NgbModal) as MockedObject<NgbModal>;
        authServiceSpy = TestBed.inject(AuthorizationService) as MockedObject<AuthorizationService>;
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(SearchLanguagesComponent);
        component = fixture.componentInstance;
        languageServiceSpy.listSystemLanguages.mockReturnValue(of(mockResults));
        component.loggedInUser = new MockUser();
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });


    it('should search languages', () => {

        component.search();

        expect(languageServiceSpy.listSystemLanguages).toHaveBeenCalled();
        expect(component.results).toEqual(mockResults);
        expect(component.loading).toBe(false);
    });

    it('should add language', () => {
        const modalRefMock = { result: Promise.resolve(true) } as any;
        modalServiceSpy.open.mockReturnValue(modalRefMock);

        component.addLanguage();

        expect(modalServiceSpy.open).toHaveBeenCalledWith(CreateLanguageComponent, { centered: true, backdrop: 'static' });
    });

    it('should check if user is admin', () => {
        authServiceSpy.isAnAdmin.mockReturnValue(true);
        expect(component.isAnAdmin()).toBe(true);
    });
});
