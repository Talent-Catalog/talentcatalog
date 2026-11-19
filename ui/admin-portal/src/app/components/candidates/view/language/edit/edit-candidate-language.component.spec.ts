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
import {EditCandidateLanguageComponent} from "./edit-candidate-language.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {LanguageService} from "../../../../../services/language.service";
import {LanguageLevelService} from "../../../../../services/language-level.service";
import {CandidateLanguageService} from "../../../../../services/candidate-language.service";
import {CandidateLanguage} from "../../../../../model/candidate-language";
import {MockCandidate} from "../../../../../MockData/MockCandidate";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {of, throwError} from "rxjs";
import {LanguageLevel} from "../../../../../model/language-level";
import {Language} from "../../../../../model/language";
import {expect, vi} from 'vitest';

describe('EditCandidateLanguageComponent', () => {
    let component: EditCandidateLanguageComponent;
    let fixture: ComponentFixture<EditCandidateLanguageComponent>;
    let mockActiveModal: MockedObject<NgbActiveModal>;
    let mockLanguageService: MockedObject<LanguageService>;
    let mockLanguageLevelService: MockedObject<LanguageLevelService>;
    let mockCandidateLanguageService: MockedObject<CandidateLanguageService>;
    const mockCandidate = new MockCandidate();

    const mockLanguages: Language[] = [
        { id: 1, name: 'English', status: 'active' },
        { id: 2, name: 'French', status: 'active' },
        // Add more languages as needed
    ];
    const mockLanguageLevels: LanguageLevel[] = [
        { id: 1, name: 'Beginner', level: 1, cefrLevel: 'A1', status: 'Active' },
        { id: 2, name: 'Intermediate', level: 2, cefrLevel: 'A2', status: 'Active' },
        { id: 3, name: 'Advanced', level: 3, cefrLevel: 'B2', status: 'Active' },
        // Add more language levels as needed
    ];

    const mockCandidateLanguages = mockCandidate.candidateLanguages;
    beforeEach(async () => {
        mockActiveModal = ({
            close: vi.fn().mockName("NgbActiveModal.close"),
            dismiss: vi.fn().mockName("NgbActiveModal.dismiss")
        } as unknown as typeof mockActiveModal);
        mockLanguageService = ({
            listLanguages: vi.fn().mockName("LanguageService.listLanguages")
        } as unknown as typeof mockLanguageService);
        mockLanguageLevelService = ({
            listLanguageLevels: vi.fn().mockName("LanguageLevelService.listLanguageLevels")
        } as unknown as typeof mockLanguageLevelService);
        mockCandidateLanguageService = ({
            update: vi.fn().mockName("CandidateLanguageService.update")
        } as unknown as typeof mockCandidateLanguageService);

        await TestBed.configureTestingModule({
            declarations: [EditCandidateLanguageComponent],
            imports: [FormsModule, ReactiveFormsModule, NgSelectModule],
            providers: [
                { provide: NgbActiveModal, useValue: mockActiveModal },
                { provide: LanguageService, useValue: mockLanguageService },
                { provide: LanguageLevelService, useValue: mockLanguageLevelService },
                { provide: CandidateLanguageService, useValue: mockCandidateLanguageService },
                UntypedFormBuilder,
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(EditCandidateLanguageComponent);
        component = fixture.componentInstance;
        mockLanguageService.listLanguages.mockReturnValue(of(mockLanguages));
        mockLanguageLevelService.listLanguageLevels.mockReturnValue(of(mockLanguageLevels));
        component.candidateLanguage = mockCandidateLanguages[0];
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize form with correct values', () => {
        expect(component.candidateForm.get('languageId').value).toEqual(mockCandidateLanguages[0].language.id);
        expect(component.candidateForm.get('spokenLevelId').value).toEqual(mockCandidateLanguages[0].spokenLevel.id);
        expect(component.candidateForm.get('writtenLevelId').value).toEqual(mockCandidateLanguages[0].writtenLevel.id);
    });

    it('should load languages and language levels on initialization', fakeAsync(() => {
        mockLanguageService.listLanguages.mockReturnValue(of(mockLanguages));
        mockLanguageLevelService.listLanguageLevels.mockReturnValue(of(mockLanguageLevels));

        fixture.detectChanges();
        tick();

        expect(component.languages).toEqual(mockLanguages);
        expect(component.languageLevels).toEqual(mockLanguageLevels);
    }));

    it('should handle error when loading languages', fakeAsync(() => {
        mockLanguageService.listLanguages.mockReturnValue(throwError('Error loading languages'));

        component.ngOnInit();
        tick();

        expect(component.error).toEqual('Error loading languages');
        expect(component.loading).toBe(false);
    }));

    it('should handle error when loading language levels', fakeAsync(() => {
        mockLanguageLevelService.listLanguageLevels.mockReturnValue(throwError('Error loading language levels'));

        component.ngOnInit();
        tick();

        expect(component.error).toEqual('Error loading language levels');
        expect(component.loading).toBe(false);
    }));

    it('should call candidate language service update on save', fakeAsync(() => {
        const updatedCandidateLanguage: CandidateLanguage = mockCandidateLanguages[0];
        mockCandidateLanguageService.update.mockReturnValue(of(updatedCandidateLanguage));

        component.candidateForm.patchValue({
            languageId: updatedCandidateLanguage.language.id,
            spokenLevelId: updatedCandidateLanguage.spokenLevel.id,
            writtenLevelId: updatedCandidateLanguage.writtenLevel.id
        });

        component.onSave();
        tick();

        expect(mockCandidateLanguageService.update).toHaveBeenCalled();
        expect(mockActiveModal.close).toHaveBeenCalledWith(updatedCandidateLanguage);
    }));

    it('should handle error when updating candidate language', fakeAsync(() => {
        mockCandidateLanguageService.update.mockReturnValue(throwError('Error updating candidate language'));

        component.onSave();
        tick();

        expect(component.error).toEqual('Error updating candidate language');
        expect(mockActiveModal.close).not.toHaveBeenCalled();
    }));

    it('should close modal when dismissed', () => {
        component.dismiss();
        expect(mockActiveModal.dismiss).toHaveBeenCalledWith(false);
    });
});
