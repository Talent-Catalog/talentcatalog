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
import {Component, EventEmitter, forwardRef, Input, NO_ERRORS_SCHEMA, Output, ChangeDetectionStrategy} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {ControlValueAccessor, NG_VALUE_ACCESSOR, ReactiveFormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {TranslateModule, TranslateService} from '@ngx-translate/core';
import {of, throwError} from 'rxjs';

import {RegistrationLanguageComponent} from './registration-language.component';
import {CandidateLanguage} from '../../../model/candidate-language';
import {Language} from '../../../model/language';
import {LanguageLevel} from '../../../model/language-level';
import {CandidateLanguageService} from '../../../services/candidate-language.service';
import {CandidateService} from '../../../services/candidate.service';
import {LanguageService} from '../../../services/language.service';
import {LanguageLevelService} from '../../../services/language-level.service';
import {RegistrationService} from '../../../services/registration.service';

@Component({
    selector: 'ng-select',
    template: '',
    providers: [{
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => NgSelectStubComponent),
            multi: true
        }],
    changeDetection: ChangeDetectionStrategy.Eager,
    standalone: false
})
class NgSelectStubComponent implements ControlValueAccessor {
    @Input()
    id?: string;
    @Input()
    items?: unknown[];
    @Input()
    clearable?: boolean;
    @Input()
    placeholder?: string;
    @Input()
    formControlName?: string;
    @Input()
    bindValue?: string;
    @Input()
    bindLabel?: string;
    @Input()
    disabled?: boolean | string;

    writeValue(): void { }
    registerOnChange(): void { }
    registerOnTouched(): void { }
}

@Component({
    selector: 'tc-button',
    template: '<ng-content></ng-content>',
    changeDetection: ChangeDetectionStrategy.Eager,
    standalone: false
})
class TcButtonStubComponent {
    @Input()
    type?: string;
    @Input()
    disabled?: boolean;
    @Output()
    onClick = new EventEmitter<void>();
}

function makeLanguage(id: number, name: string): Language {
    return { id, name };
}

function makeLanguageLevel(id: number, name: string): LanguageLevel {
    return { id, name, level: id };
}

function makeCandidateLanguage(overrides: Partial<CandidateLanguage> = {}): CandidateLanguage {
    return {
        id: 1,
        language: makeLanguage(2, 'Arabic'),
        languageId: 2,
        spokenLevel: makeLanguageLevel(10, 'Advanced'),
        spokenLevelId: 10,
        writtenLevel: makeLanguageLevel(20, 'Intermediate'),
        writtenLevelId: 20,
        ...overrides
    };
}

describe('RegistrationLanguageComponent', () => {
    let component: RegistrationLanguageComponent;
    let fixture: ComponentFixture<RegistrationLanguageComponent>;

    let candidateServiceSpy: MockedObject<CandidateService>;
    let candidateLanguageServiceSpy: MockedObject<CandidateLanguageService>;
    let languageServiceSpy: MockedObject<LanguageService>;
    let languageLevelServiceSpy: MockedObject<LanguageLevelService>;
    let registrationServiceSpy: MockedObject<RegistrationService>;

    async function configureAndCreate(options?: {
        candidateLanguages?: CandidateLanguage[];
        englishError?: unknown;
        languagesError?: unknown;
        languageLevelsError?: unknown;
        candidateError?: unknown;
        saveError?: unknown;
    }) {
        candidateServiceSpy = {
            getCandidateLanguages: vi.fn().mockName("CandidateService.getCandidateLanguages")
        } as unknown as MockedObject<CandidateService>;
        candidateLanguageServiceSpy = {
            updateCandidateLanguages: vi.fn().mockName("CandidateLanguageService.updateCandidateLanguages")
        } as unknown as MockedObject<CandidateLanguageService>;
        languageServiceSpy = {
            getLanguage: vi.fn().mockName("LanguageService.getLanguage"),
            listLanguages: vi.fn().mockName("LanguageService.listLanguages")
        } as unknown as MockedObject<LanguageService>;
        languageLevelServiceSpy = {
            listLanguageLevels: vi.fn().mockName("LanguageLevelService.listLanguageLevels")
        } as unknown as MockedObject<LanguageLevelService>;
        registrationServiceSpy = {
            next: vi.fn().mockName("RegistrationService.next"),
            back: vi.fn().mockName("RegistrationService.back")
        } as unknown as MockedObject<RegistrationService>;

        const english = makeLanguage(1, 'English');
        const languages = [english, makeLanguage(2, 'Arabic'), makeLanguage(3, 'French')];
        const languageLevels = [
            makeLanguageLevel(10, 'Advanced'),
            makeLanguageLevel(20, 'Intermediate')
        ];
        const candidateLanguages = options?.candidateLanguages ?? [makeCandidateLanguage()];

        if (options?.englishError) {
            languageServiceSpy.getLanguage.mockReturnValue(throwError(options.englishError));
        }
        else {
            languageServiceSpy.getLanguage.mockReturnValue(of(english));
        }

        if (options?.languagesError) {
            languageServiceSpy.listLanguages.mockReturnValue(throwError(options.languagesError));
        }
        else {
            languageServiceSpy.listLanguages.mockReturnValue(of(languages));
        }

        if (options?.languageLevelsError) {
            languageLevelServiceSpy.listLanguageLevels.mockReturnValue(throwError(options.languageLevelsError));
        }
        else {
            languageLevelServiceSpy.listLanguageLevels.mockReturnValue(of(languageLevels));
        }

        if (options?.candidateError) {
            candidateServiceSpy.getCandidateLanguages.mockReturnValue(throwError(options.candidateError));
        }
        else {
            candidateServiceSpy.getCandidateLanguages.mockReturnValue(of({
                candidateLanguages
            } as any));
        }

        if (options?.saveError) {
            candidateLanguageServiceSpy.updateCandidateLanguages.mockReturnValue(throwError(options.saveError));
        }
        else {
            candidateLanguageServiceSpy.updateCandidateLanguages.mockReturnValue(of({} as any));
        }

        await TestBed.configureTestingModule({
            declarations: [
                RegistrationLanguageComponent,
                NgSelectStubComponent,
                TcButtonStubComponent
            ],
            imports: [ReactiveFormsModule, TranslateModule.forRoot()],
            providers: [
                { provide: Router, useValue: {
                        navigate: vi.fn().mockName("Router.navigate")
                    } },
                { provide: CandidateService, useValue: candidateServiceSpy },
                { provide: CandidateLanguageService, useValue: candidateLanguageServiceSpy },
                { provide: LanguageService, useValue: languageServiceSpy },
                { provide: LanguageLevelService, useValue: languageLevelServiceSpy },
                { provide: RegistrationService, useValue: registrationServiceSpy }
            ],
            schemas: [NO_ERRORS_SCHEMA]
        }).compileComponents();

        fixture = TestBed.createComponent(RegistrationLanguageComponent);
        component = fixture.componentInstance;

        const translateService = TestBed.inject(TranslateService);
        translateService.use('en');

        fixture.detectChanges();
    }

    afterEach(() => TestBed.resetTestingModule());

    it('should create', async () => {
        await configureAndCreate();
        expect(component).toBeTruthy();
    });

    describe('ngOnInit', () => {
        beforeEach(async () => configureAndCreate());

        it('should build the expected form controls', () => {
            expect(component.form.contains('languageId')).toBe(true);
            expect(component.form.contains('spokenLevelId')).toBe(true);
            expect(component.form.contains('writtenLevelId')).toBe(true);
        });

        it('should load languages, language levels, english, and candidate languages', () => {
            expect(languageServiceSpy.getLanguage).toHaveBeenCalledWith('english');
            expect(languageServiceSpy.listLanguages).toHaveBeenCalled();
            expect(languageLevelServiceSpy.listLanguageLevels).toHaveBeenCalled();
            expect(candidateServiceSpy.getCandidateLanguages).toHaveBeenCalled();
            expect(component.candidateLanguages.length).toBe(1);
        });

        it('should default to adding English when the candidate has no languages', async () => {
            TestBed.resetTestingModule();
            await configureAndCreate({ candidateLanguages: [] });

            expect(component.addingLanguage).toBe(true);
            expect(component.form.value.languageId).toBe(1);
        });
    });

    describe('template tc components', () => {
        beforeEach(async () => configureAndCreate({ candidateLanguages: [] }));

        it('should render ng-select controls with the tc-select class when adding a language', () => {
            const selectEls = fixture.debugElement.queryAll(By.directive(NgSelectStubComponent));
            const selectIds = selectEls.map(debugEl => debugEl.componentInstance.id);

            expect(selectIds).toContain('spokenLevelId');
            expect(selectIds).toContain('writtenLevelId');
            selectEls.forEach(debugEl => {
                expect(debugEl.nativeElement.classList).toContain('tc-select');
            });
        });

        it('should render tc-label elements for migrated fields', () => {
            const nativeElement = fixture.nativeElement as HTMLElement;

            expect(nativeElement.querySelectorAll('tc-label').length).toBeGreaterThan(0);
        });

        it('should render the add tc-button', () => {
            const buttons = (fixture.nativeElement as HTMLElement).querySelectorAll('tc-button');

            expect(buttons.length).toBe(1);
        });
    });

    describe('behaviour', () => {
        beforeEach(async () => configureAndCreate());

        it('should set addingLanguage when addLanguage is called while not adding', () => {
            component.addingLanguage = false;

            component.addLanguage();

            expect(component.addingLanguage).toBe(true);
        });

        it('should push the current form value and clear the form when adding a language', async () => {
            TestBed.resetTestingModule();
            await configureAndCreate({ candidateLanguages: [] });

            component.form.patchValue({
                languageId: 2,
                spokenLevelId: 10,
                writtenLevelId: 20
            });

            component.addLanguage();

            expect(component.candidateLanguages.length).toBe(1);
            expect(component.candidateLanguages[0].languageId).toBe(2);
            expect(component.form.value.languageId).toBeNull();
        });

        it('should push the current form value when addLanguage is called while already adding', async () => {
            TestBed.resetTestingModule();
            await configureAndCreate({ candidateLanguages: [] });

            component.addingLanguage = true;
            component.form.patchValue({
                languageId: 3,
                spokenLevelId: 10,
                writtenLevelId: 20
            });

            component.addLanguage();

            expect(component.candidateLanguages.length).toBe(1);
            expect(component.candidateLanguages[0]).toEqual(expect.objectContaining({
                languageId: 3,
                spokenLevelId: 10,
                writtenLevelId: 20
            }));
            expect(component.form.value.languageId).toBeNull();
        });

        it('should delete a candidate language by index', () => {
            component.deleteCandidateLanguage(0);

            expect(component.candidateLanguages.length).toBe(0);
        });
    });

    describe('save flows', () => {
        beforeEach(async () => configureAndCreate());

        it('should save and navigate next when next() is called', () => {
            const onSaveSpy = vi.spyOn(component.onSave, 'emit').mockReturnValue(undefined);

            component.next();

            expect(candidateLanguageServiceSpy.updateCandidateLanguages).toHaveBeenCalledWith({
                updates: component.candidateLanguages
            });
            expect(onSaveSpy).toHaveBeenCalled();
            expect(registrationServiceSpy.next).toHaveBeenCalled();
            expect(component.saving).toBe(false);
        });

        it('should save and navigate back when back() is called', () => {
            component.back();

            expect(candidateLanguageServiceSpy.updateCandidateLanguages).toHaveBeenCalledWith({
                updates: component.candidateLanguages
            });
            expect(registrationServiceSpy.back).toHaveBeenCalled();
        });

        it('should add the current form language before saving when addingLanguage and form is valid', async () => {
            TestBed.resetTestingModule();
            await configureAndCreate({ candidateLanguages: [] });

            component.form.patchValue({
                languageId: 2,
                spokenLevelId: 10,
                writtenLevelId: 20
            });

            component.next();

            expect(candidateLanguageServiceSpy.updateCandidateLanguages).toHaveBeenCalledWith({
                updates: [expect.objectContaining({
                        languageId: 2,
                        spokenLevelId: 10,
                        writtenLevelId: 20
                    })]
            });
        });

        it('should set error and clear saving when save fails', async () => {
            TestBed.resetTestingModule();
            const serverError = { status: 500 };
            await configureAndCreate({ saveError: serverError });

            component.next();

            expect(component.error).toEqual(serverError);
            expect(component.saving).toBe(false);
            expect(registrationServiceSpy.next).not.toHaveBeenCalled();
        });

        it('should set error and clear saving when back() save fails', async () => {
            TestBed.resetTestingModule();
            const serverError = { status: 503 };
            await configureAndCreate({ saveError: serverError });

            component.back();

            expect(component.error).toEqual(serverError);
            expect(component.saving).toBe(false);
            expect(registrationServiceSpy.back).not.toHaveBeenCalled();
        });
    });

    describe('cancel', () => {
        beforeEach(async () => configureAndCreate());

        it('should emit onSave when cancel is called', () => {
            const onSaveSpy = vi.spyOn(component.onSave, 'emit').mockReturnValue(undefined);

            component.cancel();

            expect(onSaveSpy).toHaveBeenCalled();
        });
    });

    describe('helpers', () => {
        beforeEach(async () => configureAndCreate());

        it('should return the selected language name', () => {
            component.form.patchValue({ languageId: 2 });

            expect(component.getLanguageName()).toBe('Arabic');
        });

        it('should return an empty string when no language is selected', () => {
            component.form.patchValue({ languageId: null });

            expect(component.getLanguageName()).toBe('');
        });

        it('should identify English correctly', () => {
            expect(component.isEnglish(1)).toBe(true);
            expect(component.isEnglish(2)).toBe(false);
        });

        it('should filter out already selected languages', () => {
            const filteredIds = component.filteredLanguages.map(language => language.id);

            expect(filteredIds).toContain(1);
            expect(filteredIds).not.toContain(2);
        });
    });

    describe('error paths', () => {
        it('should set error when english fails to load', async () => {
            const serverError = { status: 500 };
            await configureAndCreate({ englishError: serverError });

            expect(component.error).toEqual(serverError);
            expect(component.loading).toBe(false);
        });

        it('should set error when languages fail to load', async () => {
            const serverError = { status: 503 };
            await configureAndCreate({ languagesError: serverError });

            expect(component.error).toEqual(serverError);
            expect(component.loading).toBe(true);
            expect(component['_loading'].languages).toBe(false);
            expect(component['_loading'].candidate).toBe(true);
        });

        it('should set error when language levels fail to load', async () => {
            const serverError = { status: 502 };
            await configureAndCreate({ languageLevelsError: serverError });

            expect(component.error).toEqual(serverError);
            expect(component.loading).toBe(false);
        });

        it('should set error when candidate languages fail to load', async () => {
            const serverError = { status: 504 };
            await configureAndCreate({ candidateError: serverError });

            expect(component.error).toEqual(serverError);
            expect(component.loading).toBe(false);
        });
    });
});
