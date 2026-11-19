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
import {TranslateModule, TranslateService} from '@ngx-translate/core';
import {of, throwError} from 'rxjs';

import {CandidateExamFormComponent} from './candidate-exam-form.component';
import {Candidate, CandidateExam} from '../../../model/candidate';
import {CandidateExamService} from '../../../services/candidate-exam.service';
import {CandidateService} from '../../../services/candidate.service';
import {RegistrationService} from '../../../services/registration.service';

@Component({
    selector: 'tc-input',
    template: '',
    providers: [{
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => TcInputStubComponent),
            multi: true
        }],
    changeDetection: ChangeDetectionStrategy.Eager,
    standalone: false
})
class TcInputStubComponent implements ControlValueAccessor {
    @Input()
    id?: string;
    @Input()
    type?: string;
    @Input()
    formControlName?: string;
    @Input()
    placeholder?: string;
    writeValue(): void { }
    registerOnChange(): void { }
    registerOnTouched(): void { }
}

@Component({
    selector: 'tc-textarea',
    template: '',
    providers: [{
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => TcTextareaStubComponent),
            multi: true
        }],
    changeDetection: ChangeDetectionStrategy.Eager,
    standalone: false
})
class TcTextareaStubComponent implements ControlValueAccessor {
    @Input()
    id?: string;
    @Input()
    formControlName?: string;
    writeValue(): void { }
    registerOnChange(): void { }
    registerOnTouched(): void { }
}

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
    bindValue?: string;
    @Input()
    bindLabel?: string;
    @Input()
    formControlName?: string;
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
    disabled?: boolean;
    @Output()
    onClick = new EventEmitter<void>();
}

function makeCandidate(): Candidate {
    return { id: 99 } as Candidate;
}

function makeExam(overrides: Partial<CandidateExam> = {}): CandidateExam {
    return {
        id: 1,
        exam: 'IELTSGen' as any,
        otherExam: null,
        score: '7.5',
        year: 2024,
        notes: 'Strong result',
        ...overrides
    };
}

describe('CandidateExamFormComponent', () => {
    let component: CandidateExamFormComponent;
    let fixture: ComponentFixture<CandidateExamFormComponent>;

    let candidateServiceSpy: MockedObject<CandidateService>;
    let candidateExamServiceSpy: MockedObject<CandidateExamService>;
    let registrationServiceSpy: MockedObject<RegistrationService>;

    async function configureAndCreate(options?: {
        exam?: CandidateExam | null;
        candidateError?: unknown;
        createError?: unknown;
        updateError?: unknown;
    }) {
        candidateServiceSpy = {
            getCandidateAdditionalInfo: vi.fn().mockName("CandidateService.getCandidateAdditionalInfo")
        } as unknown as MockedObject<CandidateService>;
        candidateExamServiceSpy = {
            createCandidateExam: vi.fn().mockName("CandidateExamService.createCandidateExam"),
            updateCandidateExam: vi.fn().mockName("CandidateExamService.updateCandidateExam")
        } as unknown as MockedObject<CandidateExamService>;
        registrationServiceSpy = {
            next: vi.fn().mockName("RegistrationService.next"),
            back: vi.fn().mockName("RegistrationService.back")
        } as unknown as MockedObject<RegistrationService>;

        if (options?.candidateError) {
            candidateServiceSpy.getCandidateAdditionalInfo.mockReturnValue(throwError(options.candidateError));
        }
        else {
            candidateServiceSpy.getCandidateAdditionalInfo.mockReturnValue(of(makeCandidate()));
        }

        if (options?.createError) {
            candidateExamServiceSpy.createCandidateExam.mockReturnValue(throwError(options.createError));
        }
        else {
            candidateExamServiceSpy.createCandidateExam.mockReturnValue(of(makeExam({ id: 2 })));
        }

        if (options?.updateError) {
            candidateExamServiceSpy.updateCandidateExam.mockReturnValue(throwError(options.updateError));
        }
        else {
            candidateExamServiceSpy.updateCandidateExam.mockReturnValue(of(makeExam()));
        }

        await TestBed.configureTestingModule({
            declarations: [
                CandidateExamFormComponent,
                TcInputStubComponent,
                TcTextareaStubComponent,
                NgSelectStubComponent,
                TcButtonStubComponent
            ],
            imports: [ReactiveFormsModule, TranslateModule.forRoot()],
            providers: [
                { provide: CandidateService, useValue: candidateServiceSpy },
                { provide: CandidateExamService, useValue: candidateExamServiceSpy },
                { provide: RegistrationService, useValue: registrationServiceSpy }
            ],
            schemas: [NO_ERRORS_SCHEMA]
        }).compileComponents();

        fixture = TestBed.createComponent(CandidateExamFormComponent);
        component = fixture.componentInstance;
        component.exam = options?.exam ?? null;

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
            expect(component.form.contains('exam')).toBe(true);
            expect(component.form.contains('otherExam')).toBe(true);
            expect(component.form.contains('score')).toBe(true);
            expect(component.form.contains('year')).toBe(true);
            expect(component.form.contains('notes')).toBe(true);
        });

        it('should load candidate additional info', () => {
            expect(candidateServiceSpy.getCandidateAdditionalInfo).toHaveBeenCalled();
            expect(component.candidate.id).toBe(99);
        });

        it('should patch an existing exam into the form', async () => {
            // This test needs a different input setup than the shared beforeEach fixture,
            // so reset the TestBed before creating a fresh component instance.
            TestBed.resetTestingModule();
            await configureAndCreate({ exam: makeExam({ exam: 'Other' as any, otherExam: 'PTE' }) });

            expect(component.form.value.id).toBe(1);
            expect(component.form.value.exam).toBe('Other');
            expect(component.form.value.otherExam).toBe('PTE');
            expect(component.form.value.score).toBe('7.5');
        });
    });

    describe('template tc components', () => {
        beforeEach(async () => configureAndCreate());

        it('should render tc-input fields for migrated text inputs', () => {
            const inputIds = fixture.debugElement.queryAll(By.directive(TcInputStubComponent))
                .map(debugEl => debugEl.componentInstance.id);

            expect(inputIds).toContain('score');
        });

        it('should render ng-select controls with the tc-select class', () => {
            const selectEls = fixture.debugElement.queryAll(By.directive(NgSelectStubComponent));
            const selectIds = selectEls.map(debugEl => debugEl.componentInstance.id);

            expect(selectIds).toContain('examType');
            expect(selectIds).toContain('year');
            selectEls.forEach(debugEl => {
                expect(debugEl.nativeElement.classList).toContain('tc-select');
            });
        });

        it('should render tc-label and tc-button elements', () => {
            const nativeElement = fixture.nativeElement as HTMLElement;

            expect(nativeElement.querySelector('tc-label[for="examType"]')).toBeTruthy();
            expect(nativeElement.querySelectorAll('tc-button').length).toBe(1);
        });

        it('should render tc-textarea for notes', () => {
            expect(fixture.debugElement.queryAll(By.directive(TcTextareaStubComponent)).length).toBe(1);
        });

        it('should render otherExam input when Other is selected', () => {
            component.form.patchValue({ exam: 'Other' });
            fixture.detectChanges();

            expect((fixture.nativeElement as HTMLElement).querySelector('tc-input[id="otherExam"]')).toBeTruthy();
        });
    });

    describe('helpers', () => {
        beforeEach(async () => configureAndCreate());

        it('should return true when Other exam is selected', () => {
            component.form.patchValue({ exam: 'Other' });
            expect(component.isOtherExamSelected()).toBe(true);
        });

        it('should add required validator when Other exam is selected', () => {
            component.toggleOtherExamValidator('Other');
            component.form.get('otherExam').setValue(null);

            expect(component.form.get('otherExam').hasError('required')).toBe(true);
        });

        it('should clear the validator when Other exam is not selected', () => {
            component.toggleOtherExamValidator('IELTSGen');
            component.form.get('otherExam').setValue(null);

            expect(component.form.get('otherExam').hasError('required')).toBe(false);
        });
    });

    describe('save', () => {
        beforeEach(async () => configureAndCreate());

        it('should create an exam when the form has no id', () => {
            const savedSpy = vi.spyOn(component.saved, 'emit').mockReturnValue(undefined);
            component.form.patchValue({ exam: 'IELTSGen', score: '8', year: 2024 });
            component.form.markAsDirty();

            component.save();

            expect(candidateExamServiceSpy.createCandidateExam).toHaveBeenCalledWith(99, component.form.value);
            expect(savedSpy).toHaveBeenCalled();
        });

        it('should update an exam when the form has an id', async () => {
            // This test needs a different input setup than the shared beforeEach fixture,
            // so reset the TestBed before creating a fresh component instance.
            TestBed.resetTestingModule();
            await configureAndCreate({ exam: makeExam() });
            component.form.patchValue({ score: '8' });
            component.form.markAsDirty();

            component.save();

            expect(candidateExamServiceSpy.updateCandidateExam).toHaveBeenCalledWith(1, component.form.value);
        });

        it('should emit the existing exam when the form is pristine', async () => {
            // This test needs a different input setup than the shared beforeEach fixture,
            // so reset the TestBed before creating a fresh component instance.
            TestBed.resetTestingModule();
            const exam = makeExam();
            await configureAndCreate({ exam });
            const savedSpy = vi.spyOn(component.saved, 'emit').mockReturnValue(undefined);

            component.save();

            expect(savedSpy).toHaveBeenCalledWith(exam);
        });

        it('should set error and clear saving when create fails', async () => {
            // This test needs a different service setup than the shared beforeEach fixture,
            // so reset the TestBed before creating a fresh component instance.
            TestBed.resetTestingModule();
            const serverError = { status: 500 };
            await configureAndCreate({ createError: serverError });
            component.form.patchValue({ exam: 'IELTSGen', score: '8', year: 2024 });
            component.form.markAsDirty();

            component.save();

            expect(component.error).toEqual(serverError);
            expect(component.saving).toBe(false);
        });

        it('should set error and clear saving when update fails', async () => {
            // This test needs a different input and service setup than the shared beforeEach fixture,
            // so reset the TestBed before creating a fresh component instance.
            TestBed.resetTestingModule();
            const serverError = { status: 503 };
            await configureAndCreate({ exam: makeExam(), updateError: serverError });
            component.form.patchValue({ score: '8' });
            component.form.markAsDirty();

            component.save();

            expect(component.error).toEqual(serverError);
            expect(component.saving).toBe(false);
        });
    });

});
