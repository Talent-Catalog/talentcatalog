import type { MockedObject } from "vitest";
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

import { Component, EventEmitter, forwardRef, Input, NO_ERRORS_SCHEMA, Output, ChangeDetectionStrategy } from '@angular/core';
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { By } from '@angular/platform-browser';
import { ControlValueAccessor, FormsModule, NG_VALUE_ACCESSOR, ReactiveFormsModule } from '@angular/forms';
import { Router } from '@angular/router';
import { NgbModal } from '@ng-bootstrap/ng-bootstrap';
import { TranslateModule, TranslateService } from '@ngx-translate/core';
import { of, Subject, throwError } from 'rxjs';

import { RegistrationCandidateOccupationComponent } from './registration-candidate-occupation.component';
import { CandidateOccupation } from '../../../model/candidate-occupation';
import { CandidateJobExperience } from '../../../model/candidate-job-experience';
import { Occupation } from '../../../model/occupation';
import { CandidateService } from '../../../services/candidate.service';
import { CandidateOccupationService } from '../../../services/candidate-occupation.service';
import { OccupationService } from '../../../services/occupation.service';
import { RegistrationService } from '../../../services/registration.service';

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
    placeholder?: string;
    @Input()
    formControlName?: string;
    @Input()
    min?: number;

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
    searchable?: boolean;
    @Input()
    placeholder?: string;
    @Input()
    formControlName?: string;
    @Input()
    bindValue?: string;
    @Input()
    bindLabel?: string;
    @Input()
    multiple?: boolean | string;
    @Output()
    ngModelChange = new EventEmitter<unknown>();

    writeValue(): void { }
    registerOnChange(): void { }
    registerOnTouched(): void { }
}

@Component({
    selector: 'tc-radio',
    template: '<ng-content></ng-content>',
    providers: [{
            provide: NG_VALUE_ACCESSOR,
            useExisting: forwardRef(() => TcRadioStubComponent),
            multi: true
        }],
    changeDetection: ChangeDetectionStrategy.Eager,
    standalone: false
})
class TcRadioStubComponent implements ControlValueAccessor {
    @Input()
    id?: string;
    @Input()
    name?: string;
    @Input()
    value?: unknown;
    @Input()
    label?: string;
    @Input()
    ariaLabel?: string;
    @Input()
    ariaLabelledby?: string;
    writeValue(): void { }
    registerOnChange(): void { }
    registerOnTouched(): void { }
}

function makeOccupation(id: number, name: string): Occupation {
    return { id, name };
}

function makeCandidateOccupation(id: number, occupationId: number, yearsExperience = 3, migrationOccupation?: string): CandidateOccupation {
    return {
        id,
        occupation: makeOccupation(occupationId, `Occupation ${occupationId}`),
        occupationId,
        yearsExperience,
        migrationOccupation
    };
}

function makeJobExperience(occupationId: number): CandidateJobExperience {
    return {
        id: 11,
        companyName: 'ACME',
        role: 'Engineer',
        startDate: '2020-01-01',
        endDate: '2021-01-01',
        fullTime: true,
        paid: true,
        description: 'desc',
        candidateOccupation: {
            id: 99,
            occupation: makeOccupation(occupationId, `Occupation ${occupationId}`),
            yearsExperience: 4
        }
    };
}

describe('RegistrationCandidateOccupationComponent', () => {
    let component: RegistrationCandidateOccupationComponent;
    let fixture: ComponentFixture<RegistrationCandidateOccupationComponent>;

    let candidateServiceSpy: MockedObject<CandidateService>;
    let occupationServiceSpy: MockedObject<OccupationService>;
    let candidateOccupationServiceSpy: MockedObject<CandidateOccupationService>;
    let registrationServiceSpy: MockedObject<RegistrationService>;
    let routerSpy: MockedObject<Router>;
    let modalServiceSpy: MockedObject<NgbModal>;

    async function configureAndCreate(options?: {
        candidateOccupations?: CandidateOccupation[];
        occupations?: Occupation[];
        candidateOccupationError?: unknown;
        occupationListError?: unknown;
        jobExperiences?: CandidateJobExperience[];
        modalResult?: boolean;
    }) {
        candidateServiceSpy = {
            getCandidateCandidateOccupations: vi.fn().mockName("CandidateService.getCandidateCandidateOccupations"),
            getCandidateJobExperiences: vi.fn().mockName("CandidateService.getCandidateJobExperiences")
        } as unknown as MockedObject<CandidateService>;
        occupationServiceSpy = {
            listOccupations: vi.fn().mockName("OccupationService.listOccupations")
        } as unknown as MockedObject<OccupationService>;
        candidateOccupationServiceSpy = {
            updateCandidateOccupations: vi.fn().mockName("CandidateOccupationService.updateCandidateOccupations")
        } as unknown as MockedObject<CandidateOccupationService>;
        registrationServiceSpy = {
            next: vi.fn().mockName("RegistrationService.next"),
            back: vi.fn().mockName("RegistrationService.back")
        } as unknown as MockedObject<RegistrationService>;
        routerSpy = {
            navigate: vi.fn().mockName("Router.navigate")
        } as unknown as MockedObject<Router>;
        modalServiceSpy = {
            open: vi.fn().mockName("NgbModal.open")
        } as unknown as MockedObject<NgbModal>;

        const candidateOccupations = options?.candidateOccupations ?? [];
        const occupations = options?.occupations ?? [
            makeOccupation(1, 'Teacher'),
            makeOccupation(2, 'Engineer'),
            makeOccupation(0, 'Unknown')
        ];

        if (options?.candidateOccupationError) {
            candidateServiceSpy.getCandidateCandidateOccupations.mockReturnValue(throwError(options.candidateOccupationError));
        }
        else {
            candidateServiceSpy.getCandidateCandidateOccupations.mockReturnValue(of({
                candidateOccupations
            } as any));
        }

        if (options?.occupationListError) {
            occupationServiceSpy.listOccupations.mockReturnValue(throwError(options.occupationListError));
        }
        else {
            occupationServiceSpy.listOccupations.mockReturnValue(of(occupations));
        }

        candidateServiceSpy.getCandidateJobExperiences.mockReturnValue(of({
            candidateJobExperiences: options?.jobExperiences ?? []
        } as any));

        candidateOccupationServiceSpy.updateCandidateOccupations.mockReturnValue(of({} as any));
        modalServiceSpy.open.mockReturnValue({
            result: Promise.resolve(options?.modalResult ?? true)
        } as any);

        await TestBed.configureTestingModule({
            declarations: [
                RegistrationCandidateOccupationComponent,
                TcInputStubComponent,
                NgSelectStubComponent,
                TcRadioStubComponent
            ],
            imports: [
                FormsModule,
                ReactiveFormsModule,
                TranslateModule.forRoot()
            ],
            providers: [
                { provide: CandidateService, useValue: candidateServiceSpy },
                { provide: OccupationService, useValue: occupationServiceSpy },
                { provide: CandidateOccupationService, useValue: candidateOccupationServiceSpy },
                { provide: RegistrationService, useValue: registrationServiceSpy },
                { provide: Router, useValue: routerSpy },
                { provide: NgbModal, useValue: modalServiceSpy }
            ],
            schemas: [NO_ERRORS_SCHEMA]
        }).compileComponents();

        fixture = TestBed.createComponent(RegistrationCandidateOccupationComponent);
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
        beforeEach(async () => configureAndCreate({
            candidateOccupations: [makeCandidateOccupation(1, 2, 5)]
        }));

        it('should build the expected form controls', () => {
            expect(component.form.contains('id')).toBe(true);
            expect(component.form.contains('occupationId')).toBe(true);
            expect(component.form.contains('yearsExperience')).toBe(true);
        });

        it('should load occupations and candidate occupations', () => {
            expect(candidateServiceSpy.getCandidateCandidateOccupations).toHaveBeenCalled();
            expect(occupationServiceSpy.listOccupations).toHaveBeenCalled();
            expect(component.candidateOccupations.length).toBe(1);
            expect(component.candidateOccupations[0].occupationId).toBe(2);
        });

        it('should hide the create form when occupations already exist', () => {
            expect(component.showForm).toBe(false);
        });

        it('should set loading to false after initial data loads', () => {
            expect(component.loading).toBe(false);
        });
    });

    describe('template tc components', () => {
        beforeEach(async () => configureAndCreate());

        it('should render tc-input for yearsExperience', () => {
            const inputIds = fixture.debugElement
                .queryAll(By.directive(TcInputStubComponent))
                .map(debugEl => debugEl.componentInstance.id);

            expect(inputIds).toContain('yearsExperience');
        });

        it('should render ng-select controls with the tc-select class', () => {
            const selectEls = fixture.debugElement.queryAll(By.directive(NgSelectStubComponent));
            const selectIds = selectEls.map(debugEl => debugEl.componentInstance.id);

            expect(selectIds).toContain('occupationId');
            selectEls.forEach(debugEl => {
                expect(debugEl.nativeElement.classList).toContain('tc-select');
            });
        });

        it('should render tc-label for the migrated fields', () => {
            const nativeElement = fixture.nativeElement as HTMLElement;

            expect(nativeElement.querySelector('tc-label[for="occupationId"]')).toBeTruthy();
            expect(nativeElement.querySelector('tc-label[for="yearsExperience"]')).toBeTruthy();
        });
    });

    describe('filteredOccupations', () => {
        it('should exclude already selected occupations and the unknown occupation', async () => {
            await configureAndCreate({
                candidateOccupations: [makeCandidateOccupation(1, 2, 5)],
                occupations: [
                    makeOccupation(1, 'Teacher'),
                    makeOccupation(2, 'Engineer'),
                    makeOccupation(0, 'Unknown')
                ]
            });

            const filteredIds = component.filteredOccupations.map(occupation => occupation.id);

            expect(filteredIds).toContain(1);
            expect(filteredIds).not.toContain(2);
            expect(filteredIds).not.toContain(0);
        });

        it('should return all occupations when none are selected', async () => {
            const occupations = [makeOccupation(1, 'Teacher'), makeOccupation(2, 'Engineer')];
            await configureAndCreate({ occupations });

            expect(component.filteredOccupations).toEqual(occupations);
        });

        it('should return all occupations when candidateOccupations is not yet set', async () => {
            const occupations = [makeOccupation(1, 'Teacher'), makeOccupation(2, 'Engineer')];
            await configureAndCreate({ occupations });
            component.candidateOccupations = undefined;

            expect(component.filteredOccupations).toEqual(occupations);
        });

        it('should fall back to occupation.id when a selected candidateOccupation has no occupationId', async () => {
            const engineer = makeOccupation(2, 'Engineer');
            await configureAndCreate({
                occupations: [makeOccupation(1, 'Teacher'), engineer, makeOccupation(0, 'Unknown')]
            });
            // Bypass ngOnInit's remap (which always derives occupationId from occ.occupation?.id)
            // to exercise the getter's own fallback for a record with no occupationId set.
            component.candidateOccupations = [
                { id: 1, occupation: engineer, occupationId: null, yearsExperience: 5, principal: false }
            ];

            const filteredIds = component.filteredOccupations.map(occupation => occupation.id);

            expect(filteredIds).toContain(1);
            expect(filteredIds).not.toContain(2);
        });

        it('should not offer a removed saved occupation again until changes are saved', async () => {
            await configureAndCreate({
                candidateOccupations: [makeCandidateOccupation(1, 2, 5)],
                jobExperiences: []
            });

            component.deleteOccupation(0, 2);

            expect(component.candidateOccupations.length).toBe(0);
            expect(component.filteredOccupations.map(occupation => occupation.id)).not.toContain(2);
        });

        it('should not offer a removed saved occupation when editing another saved row', async () => {
            await configureAndCreate({
                candidateOccupations: [makeCandidateOccupation(1, 1, 5), makeCandidateOccupation(2, 2, 3)],
                jobExperiences: []
            });

            component.deleteOccupation(1, 2);
            component.editOccupation(0);

            const filteredIds = component.filteredOccupations.map(occupation => occupation.id);
            // The row being edited keeps its own occupation; the removed one stays unavailable.
            expect(filteredIds).toContain(1);
            expect(filteredIds).not.toContain(2);
        });

        it('should not offer the original occupation of a saved row that was changed', async () => {
            await configureAndCreate({
                candidateOccupations: [makeCandidateOccupation(1, 1, 5)],
                occupations: [
                    makeOccupation(1, 'Teacher'),
                    makeOccupation(2, 'Engineer'),
                    makeOccupation(3, 'Nurse')
                ]
            });

            component.editOccupation(0);
            component.form.patchValue({ occupationId: 3 });
            component.saveDraft();

            const filteredIds = component.filteredOccupations.map(occupation => occupation.id);
            expect(filteredIds).toEqual([2]);
        });

        it('should offer a removed occupation again if it had not been saved yet', async () => {
            await configureAndCreate();
            component.form.setValue({ id: null, occupationId: 2, yearsExperience: 4 });
            component.saveDraft();

            component.deleteOccupation(0, 2);

            expect(component.filteredOccupations.map(occupation => occupation.id)).toContain(2);
        });
    });

    describe('draft add/edit/save/discard', () => {
        beforeEach(async () => configureAndCreate());

        it('should commit the draft and close the form when saveDraft is called with a valid form', () => {
            component.form.patchValue({
                occupationId: 1,
                yearsExperience: 4
            });

            component.saveDraft();

            expect(component.candidateOccupations.length).toBe(1);
            expect(component.candidateOccupations[0].occupationId).toBe(1);
            expect(component.candidateOccupations[0].principal).toBe(false);
            expect(component.showForm).toBe(false);
            expect(component.editingIndex).toBeNull();
        });

        it('should not commit a draft when the form is invalid', () => {
            component.form.patchValue({
                occupationId: null,
                yearsExperience: null
            });

            component.saveDraft();

            expect(component.candidateOccupations.length).toBe(0);
            expect(component.showForm).toBe(true);
        });

        it('should open a fresh blank form when openAddForm is called', () => {
            component.showForm = false;

            component.openAddForm();

            expect(component.showForm).toBe(true);
            expect(component.editingIndex).toBeNull();
            expect(component.form.value.occupationId).toBeNull();
        });

        it('should discard the draft without adding it', () => {
            component.form.patchValue({ occupationId: 1, yearsExperience: 4 });

            component.discardDraft();

            expect(component.candidateOccupations.length).toBe(0);
            expect(component.showForm).toBe(false);
        });

        it('should populate the form and switch to edit mode when editOccupation is called', () => {
            component.candidateOccupations = [
                { id: 1, occupation: makeOccupation(2, 'Engineer'), occupationId: 2, yearsExperience: 5, principal: false }
            ];

            component.editOccupation(0);

            expect(component.editingIndex).toBe(0);
            expect(component.showForm).toBe(true);
            expect(component.form.value).toEqual({ id: 1, occupationId: 2, yearsExperience: 5 });
        });

        it('should update the existing entry in place when saveDraft is called while editing, preserving its principal flag', () => {
            component.candidateOccupations = [
                { id: 1, occupation: makeOccupation(2, 'Engineer'), occupationId: 2, yearsExperience: 5, principal: true }
            ];

            component.editOccupation(0);
            component.form.patchValue({ yearsExperience: 9 });
            component.saveDraft();

            expect(component.candidateOccupations.length).toBe(1);
            expect(component.candidateOccupations[0].yearsExperience).toBe(9);
            expect(component.candidateOccupations[0].principal).toBe(true);
            expect(component.showForm).toBe(false);
            expect(component.editingIndex).toBeNull();
        });

        it('should scroll to and focus the "select principal" heading once, after saving the first occupation', () => {
            vi.spyOn(window, 'matchMedia').mockReturnValue({ matches: false } as MediaQueryList);
            const scrollIntoViewSpy = vi.fn().mockName('scrollIntoView');
            const focusSpy = vi.fn().mockName('focus');
            component.selectPrincipalHeadingRef = {
                nativeElement: { scrollIntoView: scrollIntoViewSpy, focus: focusSpy }
            } as any;

            component.form.patchValue({ occupationId: 1, yearsExperience: 4 });
            component.saveDraft();
            component.ngAfterViewChecked();

            expect(scrollIntoViewSpy).toHaveBeenCalledTimes(1);
            expect(focusSpy).toHaveBeenCalledTimes(1);
            expect(scrollIntoViewSpy).toHaveBeenCalledWith({ behavior: 'smooth', block: 'start' });

            // A second view-check with nothing new pending shouldn't repeat the scroll/focus.
            component.ngAfterViewChecked();
            expect(scrollIntoViewSpy).toHaveBeenCalledTimes(1);
            expect(focusSpy).toHaveBeenCalledTimes(1);
        });

        it('should scroll instantly (no animation) when the user prefers reduced motion', () => {
            vi.spyOn(window, 'matchMedia').mockReturnValue({ matches: true } as MediaQueryList);
            const scrollIntoViewSpy = vi.fn().mockName('scrollIntoView');
            const focusSpy = vi.fn().mockName('focus');
            component.selectPrincipalHeadingRef = {
                nativeElement: { scrollIntoView: scrollIntoViewSpy, focus: focusSpy }
            } as any;

            component.form.patchValue({ occupationId: 1, yearsExperience: 4 });
            component.saveDraft();
            component.ngAfterViewChecked();

            expect(scrollIntoViewSpy).toHaveBeenCalledWith({ behavior: 'auto', block: 'start' });
        });

        it('should not scroll/focus when saving a second occupation (already past the first-save transition)', () => {
            component.candidateOccupations = [
                { id: 1, occupation: makeOccupation(2, 'Engineer'), occupationId: 2, yearsExperience: 5, principal: true }
            ];
            const scrollIntoViewSpy = vi.fn().mockName('scrollIntoView');
            const focusSpy = vi.fn().mockName('focus');
            component.selectPrincipalHeadingRef = {
                nativeElement: { scrollIntoView: scrollIntoViewSpy, focus: focusSpy }
            } as any;

            component.form.patchValue({ occupationId: 1, yearsExperience: 2 });
            component.saveDraft();
            component.ngAfterViewChecked();

            expect(scrollIntoViewSpy).not.toHaveBeenCalled();
            expect(focusSpy).not.toHaveBeenCalled();
        });

        it('should keep the occupation being edited visible in candidateOccupations, at its original position', () => {
            component.candidateOccupations = [
                { id: 1, occupation: makeOccupation(2, 'Engineer'), occupationId: 2, yearsExperience: 5, principal: false },
                { id: 2, occupation: makeOccupation(1, 'Teacher'), occupationId: 1, yearsExperience: 3, principal: false }
            ];

            component.editOccupation(0);

            // The row must stay put (showing its pre-edit values) rather than disappear
            // from the list while its draft form is open elsewhere - otherwise it looks
            // like it "jumps" to the bottom of the page and back on save.
            expect(component.candidateOccupations.map(occ => occ.id)).toEqual([1, 2]);
            expect(component.editingIndex).toBe(0);
        });

        it('should return a stable trackBy key, preferring the persisted id over occupationId', () => {
            const occupation = { id: 1, occupation: makeOccupation(2, 'Engineer'), occupationId: 2, yearsExperience: 5, principal: false };

            // trackBy must key off something stable so *ngFor doesn't tear down and
            // recreate the row (and its tc-radio/ngModel) on every change detection cycle.
            expect(component.trackByOccupationId(0, occupation)).toBe(1);
        });

        it('should fall back to occupationId for a newly added row with no persisted id yet', () => {
            const occupation = { id: null, occupation: makeOccupation(2, 'Engineer'), occupationId: 2, yearsExperience: 5, principal: false };

            expect(component.trackByOccupationId(0, occupation)).toBe(2);
        });

        it('should treat a string "1" (as produced by the native number input before a save) as singular', () => {
            // A freshly-added/edited draft holds yearsExperience as a string until the next
            // server round-trip, since the native number input's value is always a string.
            expect(component.isSingularYear('1' as any)).toBe(true);
        });

        it('should treat the number 1 as singular and any other value as plural', () => {
            expect(component.isSingularYear(1)).toBe(true);
            expect(component.isSingularYear(0)).toBe(false);
            expect(component.isSingularYear(5)).toBe(false);
        });

    });

    describe('save via next()', () => {
        beforeEach(async () => configureAndCreate());

        it('should add the current form occupation before saving when the form is valid', () => {
            component.form.patchValue({
                occupationId: 1,
                yearsExperience: 4
            });

            component.next();

            expect(candidateOccupationServiceSpy.updateCandidateOccupations).toHaveBeenCalledWith({
                updates: expect.arrayContaining([
                    expect.objectContaining({ occupationId: 1, yearsExperience: 4 })
                ])
            });
        });

        it('should emit onSave and call registrationService.next() on success', () => {
            const onSaveSpy = vi.spyOn(component.onSave, 'emit').mockReturnValue(undefined);

            component.form.patchValue({
                occupationId: 1,
                yearsExperience: 4
            });

            component.next();

            expect(onSaveSpy).toHaveBeenCalled();
            expect(registrationServiceSpy.next).toHaveBeenCalled();
        });

        it('should set an error and not save when an occupation has invalid yearsExperience', () => {
            component.candidateOccupations = [{
                    id: 1,
                    occupation: makeOccupation(2, 'Engineer'),
                    occupationId: 2,
                    yearsExperience: null
                }];

            component.next();

            expect(candidateOccupationServiceSpy.updateCandidateOccupations).not.toHaveBeenCalled();
            expect(component.error).toBe('You need to put in a years experience value (from 0 upwards).');
        });

        it('should set error on update failure', () => {
            const serverError = { status: 500 };
            candidateOccupationServiceSpy.updateCandidateOccupations.mockReturnValue(throwError(serverError));

            component.form.patchValue({
                occupationId: 1,
                yearsExperience: 4
            });

            component.next();

            expect(component.error).toEqual(serverError);
            expect(registrationServiceSpy.next).not.toHaveBeenCalled();
            expect(component.saving).toBe(false);
        });

        it('should set saving true while the request is in flight, then false on success', () => {
            const subject = new Subject<any>();
            candidateOccupationServiceSpy.updateCandidateOccupations.mockReturnValue(subject);

            component.form.patchValue({
                occupationId: 1,
                yearsExperience: 4
            });

            component.next();
            expect(component.saving).toBe(true);

            subject.next({});
            expect(component.saving).toBe(false);
        });
    });

    describe('back()', () => {
        beforeEach(async () => configureAndCreate());

        // Going back never requires (or persists) a principal selection - unlike next(),
        // which is gated on hasPrincipal and enforces the backend's principal invariant.
        it('should navigate back without saving, even with unsaved/invalid draft state', () => {
            component.form.patchValue({
                occupationId: 1,
                yearsExperience: 4
            });

            component.back();

            expect(candidateOccupationServiceSpy.updateCandidateOccupations).not.toHaveBeenCalled();
            expect(registrationServiceSpy.back).toHaveBeenCalled();
        });
    });

    describe('cancel()', () => {
        it('should emit onSave without saving, for the profile-edit "Cancel" flow', async () => {
            await configureAndCreate();
            const onSaveSpy = vi.spyOn(component.onSave, 'emit').mockReturnValue(undefined);

            component.cancel();

            expect(onSaveSpy).toHaveBeenCalled();
            expect(candidateOccupationServiceSpy.updateCandidateOccupations).not.toHaveBeenCalled();
        });
    });

    describe('deleteOccupation', () => {
        it('should remove the occupation immediately when no job experiences are linked', async () => {
            await configureAndCreate({
                candidateOccupations: [makeCandidateOccupation(1, 2, 5)],
                jobExperiences: []
            });

            component.deleteOccupation(0, 2);

            expect(candidateServiceSpy.getCandidateJobExperiences).toHaveBeenCalled();
            expect(component.candidateOccupations.length).toBe(0);
            expect(modalServiceSpy.open).not.toHaveBeenCalled();
        });

        it('should open the delete modal when job experiences are linked', async () => {
            await configureAndCreate({
                candidateOccupations: [makeCandidateOccupation(1, 2, 5)],
                jobExperiences: [makeJobExperience(2)],
                modalResult: true
            });

            component.deleteOccupation(0, 2);
            await Promise.resolve();

            expect(modalServiceSpy.open).toHaveBeenCalled();
            expect(component.candidateOccupations.length).toBe(0);
        });

        it('should keep the occupation when the delete modal resolves false', async () => {
            await configureAndCreate({
                candidateOccupations: [makeCandidateOccupation(1, 2, 5)],
                jobExperiences: [makeJobExperience(2)],
                modalResult: false
            });

            component.deleteOccupation(0, 2);
            await Promise.resolve();

            expect(modalServiceSpy.open).toHaveBeenCalled();
            expect(component.candidateOccupations.length).toBe(1);
        });

        it('should discard the open draft when deleting the row currently being edited', async () => {
            await configureAndCreate({
                candidateOccupations: [makeCandidateOccupation(1, 2, 5), makeCandidateOccupation(2, 1, 3)],
                jobExperiences: []
            });
            component.editOccupation(1);

            component.deleteOccupation(1, 1);

            expect(component.editingIndex).toBeNull();
            expect(component.showForm).toBe(false);
        });

        it('should shift editingIndex down when deleting a row before the one being edited', async () => {
            await configureAndCreate({
                candidateOccupations: [
                    makeCandidateOccupation(1, 2, 5), makeCandidateOccupation(2, 1, 3), makeCandidateOccupation(3, 3, 2)
                ],
                jobExperiences: []
            });
            component.editOccupation(2);

            component.deleteOccupation(0, 2);

            expect(component.editingIndex).toBe(1);
        });
    });

    describe('principal occupation selection', () => {
        it('should mark the occupation matching candidate.principalOccupation as principal on load', async () => {
            const occ1 = makeCandidateOccupation(1, 2, 5);
            const occ2 = makeCandidateOccupation(2, 1, 3);
            candidateServiceSpy = {
                getCandidateCandidateOccupations: vi.fn().mockName("CandidateService.getCandidateCandidateOccupations"),
                getCandidateJobExperiences: vi.fn().mockName("CandidateService.getCandidateJobExperiences")
            } as unknown as MockedObject<CandidateService>;
            occupationServiceSpy = {
                listOccupations: vi.fn().mockName("OccupationService.listOccupations")
            } as unknown as MockedObject<OccupationService>;
            candidateOccupationServiceSpy = {
                updateCandidateOccupations: vi.fn().mockName("CandidateOccupationService.updateCandidateOccupations")
            } as unknown as MockedObject<CandidateOccupationService>;
            registrationServiceSpy = {
                next: vi.fn().mockName("RegistrationService.next"),
                back: vi.fn().mockName("RegistrationService.back")
            } as unknown as MockedObject<RegistrationService>;
            routerSpy = {
                navigate: vi.fn().mockName("Router.navigate")
            } as unknown as MockedObject<Router>;
            modalServiceSpy = {
                open: vi.fn().mockName("NgbModal.open")
            } as unknown as MockedObject<NgbModal>;

            candidateServiceSpy.getCandidateCandidateOccupations.mockReturnValue(of({
                candidateOccupations: [occ1, occ2],
                principalOccupation: { id: 2 }
            } as any));
            occupationServiceSpy.listOccupations.mockReturnValue(of([]));
            candidateServiceSpy.getCandidateJobExperiences.mockReturnValue(of({ candidateJobExperiences: [] } as any));
            candidateOccupationServiceSpy.updateCandidateOccupations.mockReturnValue(of({} as any));

            await TestBed.configureTestingModule({
                declarations: [
                    RegistrationCandidateOccupationComponent,
                    TcInputStubComponent,
                    NgSelectStubComponent,
                    TcRadioStubComponent
                ],
                imports: [FormsModule, ReactiveFormsModule, TranslateModule.forRoot()],
                providers: [
                    { provide: CandidateService, useValue: candidateServiceSpy },
                    { provide: OccupationService, useValue: occupationServiceSpy },
                    { provide: CandidateOccupationService, useValue: candidateOccupationServiceSpy },
                    { provide: RegistrationService, useValue: registrationServiceSpy },
                    { provide: Router, useValue: routerSpy },
                    { provide: NgbModal, useValue: modalServiceSpy }
                ],
                schemas: [NO_ERRORS_SCHEMA]
            }).compileComponents();

            fixture = TestBed.createComponent(RegistrationCandidateOccupationComponent);
            component = fixture.componentInstance;
            TestBed.inject(TranslateService).use('en');
            fixture.detectChanges();

            expect(component.candidateOccupations.find(o => o.id === 1).principal).toBeFalsy();
            expect(component.candidateOccupations.find(o => o.id === 2).principal).toBe(true);
            expect(component.hasPrincipal).toBe(true);
        });

        it('should switch the principal occupation and clear the previous one', async () => {
            await configureAndCreate({
                candidateOccupations: [makeCandidateOccupation(1, 2, 5), makeCandidateOccupation(2, 1, 3)]
            });

            component.selectPrincipal(component.candidateOccupations[1]);

            expect(component.candidateOccupations[0].principal).toBe(false);
            expect(component.candidateOccupations[1].principal).toBe(true);
            expect(component.hasPrincipal).toBe(true);
        });

        it('should report no principal before any selection', async () => {
            await configureAndCreate({
                candidateOccupations: [makeCandidateOccupation(1, 2, 5)]
            });

            expect(component.hasPrincipal).toBe(false);
        });

        it('should disable Next when no principal is selected', async () => {
            await configureAndCreate({
                candidateOccupations: [makeCandidateOccupation(1, 2, 5)]
            });

            expect(component.nextDisabled).toBe(true);
        });

        it('should disable Next while an open draft is invalid, even with a principal already selected', async () => {
            await configureAndCreate({
                candidateOccupations: [makeCandidateOccupation(1, 2, 5)]
            });
            component.selectPrincipal(component.candidateOccupations[0]);
            component.openAddForm();

            expect(component.nextDisabled).toBe(true);
        });

        it('should enable Next once a principal is selected and no invalid draft is open', async () => {
            await configureAndCreate({
                candidateOccupations: [makeCandidateOccupation(1, 2, 5)]
            });
            component.selectPrincipal(component.candidateOccupations[0]);

            expect(component.nextDisabled).toBe(false);
        });

        it('should flag justAddedOccupation after saving a new occupation while none is principal', async () => {
            await configureAndCreate();

            component.form.patchValue({ occupationId: 1, yearsExperience: 4 });
            component.saveDraft();

            expect(component.justAddedOccupation).toBe(true);
        });

        it('should not flag justAddedOccupation when a principal is already selected', async () => {
            await configureAndCreate();
            component.candidateOccupations = [{ ...makeCandidateOccupation(1, 2, 5), principal: true }];

            component.form.patchValue({ occupationId: 1, yearsExperience: 3 });
            component.saveDraft();

            expect(component.justAddedOccupation).toBe(false);
        });

        it('should clear justAddedOccupation once a principal is selected', async () => {
            await configureAndCreate();
            component.form.patchValue({ occupationId: 1, yearsExperience: 4 });
            component.saveDraft();
            expect(component.justAddedOccupation).toBe(true);

            component.selectPrincipal(component.candidateOccupations[0]);

            expect(component.justAddedOccupation).toBe(false);
        });

        it('should not flag justAddedOccupation when saving an edit to an existing occupation', async () => {
            await configureAndCreate({
                candidateOccupations: [makeCandidateOccupation(1, 2, 5)]
            });

            component.editOccupation(0);
            component.form.patchValue({ yearsExperience: 9 });
            component.saveDraft();

            expect(component.justAddedOccupation).toBe(false);
        });

        it('should keep justAddedOccupation set through further adds and edits until a principal is chosen', async () => {
            await configureAndCreate();
            component.form.patchValue({ occupationId: 1, yearsExperience: 4 });
            component.saveDraft();
            expect(component.justAddedOccupation).toBe(true);

            // The confirmation is not dismissible, so none of these interim actions
            // should make it disappear before a principal occupation is actually chosen.
            component.openAddForm();
            expect(component.justAddedOccupation).toBe(true);

            component.form.patchValue({ occupationId: 2, yearsExperience: 6 });
            component.saveDraft();
            expect(component.justAddedOccupation).toBe(true);

            component.editOccupation(0);
            expect(component.justAddedOccupation).toBe(true);
            component.discardDraft();

            component.deleteOccupation(1, 2);
            expect(component.justAddedOccupation).toBe(true);

            component.selectPrincipal(component.candidateOccupations[0]);
            expect(component.justAddedOccupation).toBe(false);
        });

        it('should flag principalOccupationRemoved when the principal occupation is deleted', async () => {
            await configureAndCreate({
                candidateOccupations: [makeCandidateOccupation(1, 2, 5)],
                jobExperiences: []
            });
            component.selectPrincipal(component.candidateOccupations[0]);

            component.deleteOccupation(0, 2);

            expect(component.principalOccupationRemoved).toBe(true);
            expect(component.candidateOccupations.length).toBe(0);
        });

        it('should not flag principalOccupationRemoved when a non-principal occupation is deleted', async () => {
            await configureAndCreate({
                candidateOccupations: [makeCandidateOccupation(1, 2, 5), makeCandidateOccupation(2, 1, 3)],
                jobExperiences: []
            });
            component.selectPrincipal(component.candidateOccupations[0]);

            component.deleteOccupation(1, 1);

            expect(component.principalOccupationRemoved).toBe(false);
        });

        it('should clear principalOccupationRemoved once a new principal is selected, but not before', async () => {
            await configureAndCreate({
                candidateOccupations: [makeCandidateOccupation(1, 2, 5), makeCandidateOccupation(2, 1, 3)],
                jobExperiences: []
            });
            component.selectPrincipal(component.candidateOccupations[0]);
            component.deleteOccupation(0, 2);
            expect(component.principalOccupationRemoved).toBe(true);

            // Adding/editing another occupation shouldn't dismiss the warning early.
            component.openAddForm();
            component.discardDraft();
            expect(component.principalOccupationRemoved).toBe(true);

            component.selectPrincipal(component.candidateOccupations[0]);
            expect(component.principalOccupationRemoved).toBe(false);
        });

        it('should keep principalOccupationRemoved set after saving a newly added occupation, until a principal is chosen', async () => {
            await configureAndCreate({
                candidateOccupations: [makeCandidateOccupation(1, 2, 5)],
                jobExperiences: []
            });
            component.selectPrincipal(component.candidateOccupations[0]);
            component.deleteOccupation(0, 2);
            expect(component.principalOccupationRemoved).toBe(true);

            // Adding (and actually saving) another occupation while no principal is
            // selected must not silently dismiss the removal warning.
            component.form.patchValue({ occupationId: 1, yearsExperience: 3 });
            component.saveDraft();

            expect(component.principalOccupationRemoved).toBe(true);
        });

    });

    describe('error paths', () => {
        it('should set error and stop loading if candidate occupations fail to load', async () => {
            const serverError = { status: 404 };
            await configureAndCreate({ candidateOccupationError: serverError });

            expect(component.error).toEqual(serverError);
            expect(component.loading).toBe(false);
        });

        it('should set error and stop loading if occupation list fails to load', async () => {
            const serverError = { status: 503 };
            await configureAndCreate({ occupationListError: serverError });

            expect(component.error).toEqual(serverError);
            expect(component.loading).toBe(false);
        });
    });
});
