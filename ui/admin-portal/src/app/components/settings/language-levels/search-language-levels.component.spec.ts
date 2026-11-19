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
import {SearchResults} from "../../../model/search-results";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgbModal, NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {NgSelectModule} from "@ng-select/ng-select";
import {SearchLanguageLevelsComponent} from "./search-language-levels.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {LanguageLevelService} from "../../../services/language-level.service";
import {AuthorizationService} from "../../../services/authorization.service";
import {of, throwError} from "rxjs";
import {MockUser} from "../../../MockData/MockUser";
import {LanguageLevel} from "../../../model/language-level";
import {CreateLanguageLevelComponent} from "./create/create-language-level.component";
import {EditLanguageLevelComponent} from "./edit/edit-language-level.component";
import {FileSelectorComponent} from "../../util/file-selector/file-selector.component";
import {expect, vi} from 'vitest';

describe('SearchLanguageLevelsComponent', () => {
    let component: SearchLanguageLevelsComponent;
    let fixture: ComponentFixture<SearchLanguageLevelsComponent>;
    let languageLevelServiceSpy: MockedObject<LanguageLevelService>;
    let modalServiceSpy: MockedObject<NgbModal>;
    let authServiceSpy: MockedObject<AuthorizationService>;
    const searchResults: SearchResults<LanguageLevel> = {
        first: false,
        last: false,
        number: 0,
        size: 0,
        totalPages: 0,
        totalElements: 1,
        content: [{ id: 1, name: 'IELTS', level: 3, cefrLevel: 'B1', status: 'Active' }]
    };
    beforeEach(async () => {
        const languageLevelSpy = {
            search: vi.fn().mockName("LanguageLevelService.search"),
            delete: vi.fn().mockName("LanguageLevelService.delete"),
            addSystemLanguageTranslations: vi.fn().mockName("LanguageLevelService.addSystemLanguageTranslations")
        };
        const modalSpy = {
            open: vi.fn().mockName("NgbModal.open")
        };
        const authSpy = {
            isAnAdmin: vi.fn().mockName("AuthorizationService.isAnAdmin")
        };

        await TestBed.configureTestingModule({
            declarations: [SearchLanguageLevelsComponent],
            imports: [FormsModule, ReactiveFormsModule, NgbModule, NgSelectModule],
            providers: [
                { provide: LanguageLevelService, useValue: languageLevelSpy },
                { provide: NgbModal, useValue: modalSpy },
                { provide: AuthorizationService, useValue: authSpy },
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(SearchLanguageLevelsComponent);
        component = fixture.componentInstance;
        languageLevelServiceSpy = TestBed.inject(LanguageLevelService) as MockedObject<LanguageLevelService>;
        modalServiceSpy = TestBed.inject(NgbModal) as MockedObject<NgbModal>;
        authServiceSpy = TestBed.inject(AuthorizationService) as MockedObject<AuthorizationService>;
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(SearchLanguageLevelsComponent);
        component = fixture.componentInstance;
        languageLevelServiceSpy.search.mockReturnValue(of(searchResults));
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

    it('should search language levels', () => {

        component.search();
        expect(languageLevelServiceSpy.search).toHaveBeenCalled();
        expect(component.results).toEqual(searchResults);
        expect(component.loading).toBe(false);
    });

    it('should add language level', () => {
        const modalRefMock = { result: Promise.resolve(true) } as any;
        modalServiceSpy.open.mockReturnValue(modalRefMock);

        component.addLanguageLevel();

        expect(modalServiceSpy.open).toHaveBeenCalledWith(CreateLanguageLevelComponent, { centered: true, backdrop: 'static' });
    });

    it('should edit language level', () => {
        const languageLevel = { id: 1, level: 'A1', name: 'Beginner', status: 'active' };
        const modalRefMock = { componentInstance: { languageLevelId: null, validExtensions: undefined }, result: Promise.resolve(true) } as any;
        modalServiceSpy.open.mockReturnValue(modalRefMock);

        component.editLanguageLevel(languageLevel);

        expect(modalServiceSpy.open).toHaveBeenCalledWith(EditLanguageLevelComponent, { centered: true, backdrop: 'static' });
        expect(modalRefMock.componentInstance.languageLevelId).toBe(languageLevel.id);
    });

    it('should delete language level', async () => {
        const languageLevel = { id: 1, level: 'A1', name: 'Beginner', status: 'active' };
        languageLevelServiceSpy.delete.mockReturnValue(of(true));
        const modalRefMock = {
            componentInstance: { message: '', validExtensions: undefined },
            result: Promise.resolve(true)
        } as any;
        modalServiceSpy.open.mockReturnValue(modalRefMock);

        component.deleteLanguageLevel(languageLevel);

        // ADMIN_AWAIT_UNHANDLED_TIMER_20261117
        await new Promise<void>((resolve) => setTimeout(resolve, 0));
        expect(languageLevelServiceSpy.delete).toHaveBeenCalledWith(languageLevel.id);
        ;
    });

    it('should handle delete errors', async () => {
        const languageLevel = { id: 1, level: 'A1', name: 'Beginner', status: 'active' };
        languageLevelServiceSpy.delete.mockReturnValue(throwError('Error'));
        const modalRefMock = {
            componentInstance: { message: '', validExtensions: undefined },
            result: Promise.resolve(true)
        } as any;
        modalServiceSpy.open.mockReturnValue(modalRefMock);

        component.deleteLanguageLevel(languageLevel);

        // ADMIN_AWAIT_UNHANDLED_TIMER_20261117
        await new Promise<void>((resolve) => setTimeout(resolve, 0));
        expect(component.error).toBe('Error');
        ;
    });

    it('should import translations', () => {
        const modalRefMock = {
            componentInstance: { message: '', validExtensions: undefined },
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
        languageLevelServiceSpy.addSystemLanguageTranslations.mockReturnValue(throwError('Error'));

        component['doImport'](files);

        // ADMIN_AWAIT_UNHANDLED_TIMER_20261117
        await new Promise<void>((resolve) => setTimeout(resolve, 0));
        expect(component.error).toBe('Error');
        ;
    });
});
