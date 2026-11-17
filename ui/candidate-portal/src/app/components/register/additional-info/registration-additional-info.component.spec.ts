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
import {Component, EventEmitter, forwardRef, Input, Output, ChangeDetectionStrategy} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {ControlValueAccessor, NG_VALUE_ACCESSOR, ReactiveFormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {TranslateModule} from '@ngx-translate/core';
import {of, throwError} from 'rxjs';

import {RegistrationAdditionalInfoComponent} from './registration-additional-info.component';
import {CandidateService} from '../../../services/candidate.service';
import {RegistrationService} from '../../../services/registration.service';
import {SurveyTypeService} from '../../../services/survey-type.service';
import {SurveyType, US_AFGHAN_SURVEY_TYPE} from '../../../model/survey-type';
import {AuthenticationService} from "../../../services/authentication.service";
import {TcInstanceType} from "../../../model/tc-instance-type";

@Component({
    selector: 'tc-loading',
    template: '',
    changeDetection: ChangeDetectionStrategy.Eager,
    standalone: false
})
class TcLoadingStubComponent {
    @Input()
    loading?: boolean;
}

@Component({
    selector: 'app-error',
    template: '',
    changeDetection: ChangeDetectionStrategy.Eager,
    standalone: false
})
class ErrorStubComponent {
    @Input()
    error?: unknown;
}

@Component({
    selector: 'app-registration-footer',
    template: '',
    changeDetection: ChangeDetectionStrategy.Eager,
    standalone: false
})
class RegistrationFooterStubComponent {
    @Input()
    nextDisabled?: boolean;
    @Input()
    type?: string;
    @Output()
    backClicked = new EventEmitter<void>();
    @Output()
    nextClicked = new EventEmitter<void>();
}

@Component({
    selector: 'tc-label',
    template: '<ng-content></ng-content>',
    changeDetection: ChangeDetectionStrategy.Eager,
    standalone: false
})
class TcLabelStubComponent {
    @Input()
    for?: string;
}

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
    rows?: string;
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
    bindLabel?: string;
    @Input()
    bindValue?: string;
    @Input()
    placeholder?: string;
    @Input()
    formControlName?: string;

    writeValue(): void { }
    registerOnChange(): void { }
    registerOnTouched(): void { }
}

function makeSurveyType(id: number, name: string): SurveyType {
    return { id, name } as SurveyType;
}

describe('RegistrationAdditionalInfoComponent', () => {
    let component: RegistrationAdditionalInfoComponent;
    let fixture: ComponentFixture<RegistrationAdditionalInfoComponent>;

    let candidateServiceSpy: MockedObject<CandidateService>;
    let registrationServiceSpy: MockedObject<RegistrationService>;
    let surveyTypeServiceSpy: MockedObject<SurveyTypeService>;
    let authenticationServiceSpy: MockedObject<AuthenticationService>;
    async function configureAndCreate(options?: {
        surveyResponse?: any;
        additionalInfoResponse?: any;
        surveyTypes?: SurveyType[];
        candidateSurveyError?: unknown;
        additionalInfoError?: unknown;
        surveyTypesError?: unknown;
        updateSurveyError?: unknown;
        updateOtherInfoError?: unknown;
        edit?: boolean;
    }) {
        candidateServiceSpy = {
            getCandidateSurvey: vi.fn().mockName("CandidateService.getCandidateSurvey"),
            getCandidateAdditionalInfo: vi.fn().mockName("CandidateService.getCandidateAdditionalInfo"),
            updateCandidateSurvey: vi.fn().mockName("CandidateService.updateCandidateSurvey"),
            updateCandidateOtherInfo: vi.fn().mockName("CandidateService.updateCandidateOtherInfo")
        } as unknown as MockedObject<CandidateService>;
        registrationServiceSpy = {
            next: vi.fn().mockName("RegistrationService.next"),
            back: vi.fn().mockName("RegistrationService.back")
        } as unknown as MockedObject<RegistrationService>;
        surveyTypeServiceSpy = {
            listActiveSurveyTypes: vi.fn().mockName("SurveyTypeService.listActiveSurveyTypes")
        } as unknown as MockedObject<SurveyTypeService>;
        authenticationServiceSpy = {
            getTcInstanceType: vi.fn().mockName("AuthenticationService.getTcInstanceType")
        } as unknown as MockedObject<AuthenticationService>;

        authenticationServiceSpy.getTcInstanceType.mockReturnValue(TcInstanceType.TBB);
        const surveyTypes = options?.surveyTypes ?? [
            makeSurveyType(1, 'Friend'),
            makeSurveyType(8, 'Other'),
            makeSurveyType(2, 'Agency')
        ];
        const surveyResponse = options?.surveyResponse ?? {
            surveyType: { id: 2 },
            surveyComment: 'From an agency'
        };
        const additionalInfoResponse = options?.additionalInfoResponse ?? {
            additionalInfo: 'More context',
            linkedInLink: 'https://www.linkedin.com/in/example',
            allNotifications: false
        };

        if (options?.candidateSurveyError) {
            candidateServiceSpy.getCandidateSurvey.mockReturnValue(throwError(options.candidateSurveyError));
        }
        else {
            candidateServiceSpy.getCandidateSurvey.mockReturnValue(of(surveyResponse));
        }

        if (options?.additionalInfoError) {
            candidateServiceSpy.getCandidateAdditionalInfo.mockReturnValue(throwError(options.additionalInfoError));
        }
        else {
            candidateServiceSpy.getCandidateAdditionalInfo.mockReturnValue(of(additionalInfoResponse));
        }

        if (options?.surveyTypesError) {
            surveyTypeServiceSpy.listActiveSurveyTypes.mockReturnValue(throwError(options.surveyTypesError));
        }
        else {
            surveyTypeServiceSpy.listActiveSurveyTypes.mockReturnValue(of(surveyTypes));
        }

        if (options?.updateSurveyError) {
            candidateServiceSpy.updateCandidateSurvey.mockReturnValue(throwError(options.updateSurveyError));
        }
        else {
            candidateServiceSpy.updateCandidateSurvey.mockReturnValue(of({} as any));
        }

        if (options?.updateOtherInfoError) {
            candidateServiceSpy.updateCandidateOtherInfo.mockReturnValue(throwError(options.updateOtherInfoError));
        }
        else {
            candidateServiceSpy.updateCandidateOtherInfo.mockReturnValue(of({} as any));
        }

        await TestBed.configureTestingModule({
            declarations: [
                RegistrationAdditionalInfoComponent,
                TcLoadingStubComponent,
                ErrorStubComponent,
                RegistrationFooterStubComponent,
                TcLabelStubComponent,
                TcInputStubComponent,
                TcTextareaStubComponent,
                NgSelectStubComponent
            ],
            imports: [ReactiveFormsModule, TranslateModule.forRoot()],
            providers: [
                { provide: Router, useValue: {
                        navigate: vi.fn().mockName("Router.navigate")
                    } },
                { provide: CandidateService, useValue: candidateServiceSpy },
                { provide: RegistrationService, useValue: registrationServiceSpy },
                { provide: SurveyTypeService, useValue: surveyTypeServiceSpy },
                { provide: AuthenticationService, useValue: authenticationServiceSpy }
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(RegistrationAdditionalInfoComponent);
        component = fixture.componentInstance;
        component.edit = options?.edit ?? false;

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
            expect(component.form.contains('additionalInfo')).toBe(true);
            expect(component.form.contains('surveyTypeId')).toBe(true);
            expect(component.form.contains('surveyComment')).toBe(true);
            expect(component.form.contains('linkedInLink')).toBe(true);
            expect(component.form.contains('allNotifications')).toBe(true);
        });

        it('should load survey types and candidate additional info for non-US-Afghan flow', () => {
            expect(candidateServiceSpy.getCandidateSurvey).toHaveBeenCalled();
            expect(candidateServiceSpy.getCandidateAdditionalInfo).toHaveBeenCalled();
            expect(surveyTypeServiceSpy.listActiveSurveyTypes).toHaveBeenCalled();
            expect(component.usAfghan).toBeFalsy();
            expect(component.form.value.surveyTypeId).toBe(2);
            expect(component.form.value.surveyComment).toBe('From an agency');
            expect(component.form.value.additionalInfo).toBe('More context');
            expect(component.form.value.linkedInLink).toBe('https://www.linkedin.com/in/example');
            expect(component.loading).toBe(false);
        });

        it('should remove survey controls for the US-Afghan flow', async () => {
            TestBed.resetTestingModule();
            await configureAndCreate({
                surveyResponse: {
                    surveyType: { id: US_AFGHAN_SURVEY_TYPE }
                }
            });

            expect(component.usAfghan).toBe(true);
            expect(component.form.contains('surveyTypeId')).toBe(false);
            expect(component.form.contains('surveyComment')).toBe(false);
            expect(surveyTypeServiceSpy.listActiveSurveyTypes).not.toHaveBeenCalled();
            expect(component.loading).toBe(false);
        });

        it('should sort survey types with Other last', () => {
            expect(component.surveyTypes.map(surveyType => surveyType.id)).toEqual([1, 2, 8]);
        });
    });

    describe('template tc components', () => {
        beforeEach(async () => configureAndCreate());

        it('should render tc-loading while using the migrated loading component', () => {
            const loadingEl = fixture.debugElement.query(By.directive(TcLoadingStubComponent));

            expect(loadingEl).toBeTruthy();
            expect(loadingEl.componentInstance.loading).toBe(component.loading);
        });

        it('should render tc-label elements for the migrated fields', () => {
            const labelFors = fixture.debugElement
                .queryAll(By.directive(TcLabelStubComponent))
                .map(debugEl => debugEl.componentInstance.for);

            expect(labelFors).toContain('additionalInfo');
            expect(labelFors).toContain('surveyTypeId');
            expect(labelFors).toContain('surveyComment');
            expect(labelFors).toContain('linkedInLink');
        });

        it('should render the migrated textarea, inputs, and select', () => {
            const textareaEls = fixture.debugElement.queryAll(By.directive(TcTextareaStubComponent));
            const inputIds = fixture.debugElement
                .queryAll(By.directive(TcInputStubComponent))
                .map(debugEl => debugEl.componentInstance.id);
            const selectEls = fixture.debugElement.queryAll(By.directive(NgSelectStubComponent));

            expect(textareaEls.length).toBe(1);
            expect(textareaEls[0].componentInstance.id).toBe('additionalInfo');
            expect(inputIds).toContain('surveyComment');
            expect(inputIds).toContain('linkedInLink');
            expect(selectEls.length).toBe(1);
            expect(selectEls[0].componentInstance.id).toBe('surveyTypeId');
            expect(selectEls[0].nativeElement.classList).toContain('tc-select');
        });

        it('should hide survey fields for the US-Afghan flow', async () => {
            TestBed.resetTestingModule();
            await configureAndCreate({
                surveyResponse: {
                    surveyType: { id: US_AFGHAN_SURVEY_TYPE }
                }
            });

            expect(fixture.debugElement.queryAll(By.directive(NgSelectStubComponent)).length).toBe(0);
            const inputIds = fixture.debugElement
                .queryAll(By.directive(TcInputStubComponent))
                .map(debugEl => debugEl.componentInstance.id);
            expect(inputIds).not.toContain('surveyComment');
            expect(inputIds).toContain('linkedInLink');
        });
    });

    describe('validation', () => {
        beforeEach(async () => configureAndCreate());

        it('should expose allNotifications from the getter', () => {
            component.form.patchValue({ allNotifications: true });

            expect(component.allNotifications).toBe(true);
        });

        it('should validate the LinkedIn pattern', () => {
            component.form.patchValue({ linkedInLink: 'https://example.com/not-linkedin' });

            expect(component.form.controls.linkedInLink.invalid).toBe(true);
        });
    });

    describe('save flows', () => {
        beforeEach(async () => configureAndCreate());

        it('should update survey and other info then navigate next', () => {
            const onSaveSpy = vi.spyOn(component.onSave, 'emit').mockReturnValue(undefined);

            component.save('next');

            expect(candidateServiceSpy.updateCandidateSurvey).toHaveBeenCalledWith(component.form.value);
            expect(candidateServiceSpy.updateCandidateOtherInfo).toHaveBeenCalledWith(component.form.value);
            expect(onSaveSpy).toHaveBeenCalled();
            expect(registrationServiceSpy.next).toHaveBeenCalled();
            expect(component.saving).toBe(false);
        });

        it('should update survey and other info then navigate back', () => {
            component.save('back');

            expect(candidateServiceSpy.updateCandidateSurvey).toHaveBeenCalledWith(component.form.value);
            expect(candidateServiceSpy.updateCandidateOtherInfo).toHaveBeenCalledWith(component.form.value);
            expect(registrationServiceSpy.back).toHaveBeenCalled();
        });

        it('should only update other info in the US-Afghan flow', async () => {
            TestBed.resetTestingModule();
            await configureAndCreate({
                surveyResponse: {
                    surveyType: { id: US_AFGHAN_SURVEY_TYPE }
                }
            });

            component.save('next');

            expect(candidateServiceSpy.updateCandidateSurvey).not.toHaveBeenCalled();
            expect(candidateServiceSpy.updateCandidateOtherInfo).toHaveBeenCalledWith(component.form.value);
            expect(registrationServiceSpy.next).toHaveBeenCalled();
        });

        it('should set error and clear saving when survey save fails', async () => {
            TestBed.resetTestingModule();
            const serverError = { status: 500 };
            await configureAndCreate({ updateSurveyError: serverError });

            component.save('next');

            expect(component.error).toEqual(serverError);
            expect(component.saving).toBe(false);
            expect(candidateServiceSpy.updateCandidateOtherInfo).not.toHaveBeenCalled();
            expect(registrationServiceSpy.next).not.toHaveBeenCalled();
        });

        it('should set error and clear saving when other info save fails', async () => {
            TestBed.resetTestingModule();
            const serverError = { status: 503 };
            await configureAndCreate({ updateOtherInfoError: serverError });

            component.save('next');

            expect(component.error).toEqual(serverError);
            expect(component.saving).toBe(false);
            expect(registrationServiceSpy.next).not.toHaveBeenCalled();
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

    describe('error paths', () => {
        it('should set error when candidate survey fails to load', async () => {
            const serverError = { status: 500 };
            await configureAndCreate({ candidateSurveyError: serverError });

            expect(component.error).toEqual(serverError);
            expect(component.loading).toBe(true);
        });

        it('should set error when additional info fails to load', async () => {
            const serverError = { status: 503 };
            await configureAndCreate({ additionalInfoError: serverError });

            expect(component.error).toEqual(serverError);
            expect(component.loading).toBe(false);
        });

        it('should set error when survey types fail to load', async () => {
            const serverError = { status: 502 };
            await configureAndCreate({ surveyTypesError: serverError });

            expect(component.error).toEqual(serverError);
            expect(component.loading).toBe(false);
        });
    });
});
