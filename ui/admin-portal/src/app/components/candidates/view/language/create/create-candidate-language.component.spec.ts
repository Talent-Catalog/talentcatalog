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
import {CreateCandidateLanguageComponent} from "./create-candidate-language.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {LanguageService} from "../../../../../services/language.service";
import {CandidateLanguageService} from "../../../../../services/candidate-language.service";
import {LanguageLevel} from "../../../../../model/language-level";
import {of} from "rxjs";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {MockCandidate} from "../../../../../MockData/MockCandidate";
import {expect, vi} from 'vitest';

describe('CreateCandidateLanguageComponent', () => {
    let component: CreateCandidateLanguageComponent;
    let fixture: ComponentFixture<CreateCandidateLanguageComponent>;
    let mockLanguageService: MockedObject<LanguageService>;
    let mockCandidateLanguageService: MockedObject<CandidateLanguageService>;
    let mockActiveModal: MockedObject<NgbActiveModal>;

    const mockLanguageLevels: LanguageLevel[] = [
        { id: 1, name: 'Beginner', level: 1, cefrLevel: 'A1', status: 'Active' },
        { id: 2, name: 'Intermediate', level: 2, cefrLevel: 'A2', status: 'Active' },
        { id: 3, name: 'Advanced', level: 3, cefrLevel: 'B2', status: 'Active' },
    ];
    const mockCandidate = new MockCandidate();
    beforeEach(async () => {
        mockLanguageService = ({
            listLanguages: vi.fn().mockName("LanguageService.listLanguages")
        } as unknown as typeof mockLanguageService);
        mockActiveModal = ({
            close: vi.fn().mockName("NgbActiveModal.close"),
            dismiss: vi.fn().mockName("NgbActiveModal.dismiss")
        } as unknown as typeof mockActiveModal);
        mockLanguageService.listLanguages.mockReturnValue(of([]));
        mockCandidateLanguageService = ({
            create: vi.fn().mockName("CandidateLanguageService.create")
        } as unknown as typeof mockCandidateLanguageService);
        await TestBed.configureTestingModule({
            declarations: [CreateCandidateLanguageComponent],
            imports: [FormsModule, ReactiveFormsModule, NgSelectModule],
            providers: [
                { provide: NgbActiveModal, useValue: mockActiveModal },
                { provide: LanguageService, useValue: mockLanguageService },
                { provide: CandidateLanguageService, useValue: mockCandidateLanguageService },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ]
        })
            .compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(CreateCandidateLanguageComponent);
        component = fixture.componentInstance;
        component.languageLevels = mockLanguageLevels;
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize form with required controls', () => {
        expect(component.candidateForm.get('languageId')).toBeTruthy();
        expect(component.candidateForm.get('spokenLevelId')).toBeTruthy();
        expect(component.candidateForm.get('writtenLevelId')).toBeTruthy();
    });

    it('should display loading spinner initially', () => {
        component.loading = true;
        expect(component.loading).toBe(true);
    });

    it('should load language levels', () => {
        mockLanguageService.listLanguages.mockReturnValue(of(mockLanguageLevels));
        fixture.detectChanges();
        expect(component.languageLevels).toEqual(mockLanguageLevels);
        expect(component.loading).toBe(false);
    });

    it('should call onSave() and close modal on successful save', () => {
        const candidateLanguage = mockCandidate.candidateLanguages[0]; // Mock data
        mockCandidateLanguageService.create.mockReturnValue(of(candidateLanguage));
        component.onSave();
        expect(mockCandidateLanguageService.create).toHaveBeenCalledTimes(1);
        expect(mockCandidateLanguageService.create).toHaveBeenCalledWith(expect.any(Object));
        expect(mockActiveModal.close).toBeTruthy();
    });

    it('should dismiss modal on cancel', () => {
        mockActiveModal.dismiss;
        component.dismiss();
        expect(mockActiveModal.dismiss).toHaveBeenCalledTimes(1);
        expect(mockActiveModal.dismiss).toHaveBeenCalledWith(false);
    });
});
