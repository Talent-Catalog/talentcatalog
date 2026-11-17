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
import {Component, EventEmitter, Input, NO_ERRORS_SCHEMA, Output, ChangeDetectionStrategy} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {TranslateModule, TranslateService} from '@ngx-translate/core';
import {of, throwError} from 'rxjs';

import {RegistrationCertificationsComponent} from './registration-certifications.component';
import {CandidateCertification} from '../../../model/candidate-certification';
import {CandidateCertificationService} from '../../../services/candidate-certification.service';
import {CandidateService} from '../../../services/candidate.service';
import {RegistrationService} from '../../../services/registration.service';

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

function makeCertification(overrides: Partial<CandidateCertification> = {}): CandidateCertification {
    return {
        id: 1,
        name: 'AWS Certified',
        institution: 'Amazon',
        dateCompleted: '2024-01-15',
        ...overrides
    };
}

describe('RegistrationCertificationsComponent', () => {
    let component: RegistrationCertificationsComponent;
    let fixture: ComponentFixture<RegistrationCertificationsComponent>;

    let candidateServiceSpy: MockedObject<CandidateService>;
    let candidateCertificationServiceSpy: MockedObject<CandidateCertificationService>;
    let registrationServiceSpy: MockedObject<RegistrationService>;

    async function configureAndCreate(options?: {
        candidateCertifications?: CandidateCertification[];
        candidateError?: unknown;
        deleteError?: unknown;
    }) {
        candidateServiceSpy = {
            getCandidateCertifications: vi.fn().mockName("CandidateService.getCandidateCertifications")
        } as unknown as MockedObject<CandidateService>;
        candidateCertificationServiceSpy = {
            deleteCandidateCertification: vi.fn().mockName("CandidateCertificationService.deleteCandidateCertification")
        } as unknown as MockedObject<CandidateCertificationService>;
        registrationServiceSpy = {
            next: vi.fn().mockName("RegistrationService.next"),
            back: vi.fn().mockName("RegistrationService.back")
        } as unknown as MockedObject<RegistrationService>;

        const candidateCertifications = options?.candidateCertifications ?? [makeCertification()];

        if (options?.candidateError) {
            candidateServiceSpy.getCandidateCertifications.mockReturnValue(throwError(options.candidateError));
        }
        else {
            candidateServiceSpy.getCandidateCertifications.mockReturnValue(of({ candidateCertifications } as any));
        }

        if (options?.deleteError) {
            candidateCertificationServiceSpy.deleteCandidateCertification.mockReturnValue(throwError(options.deleteError));
        }
        else {
            candidateCertificationServiceSpy.deleteCandidateCertification.mockReturnValue(of({} as any));
        }

        await TestBed.configureTestingModule({
            declarations: [RegistrationCertificationsComponent, TcButtonStubComponent],
            imports: [ReactiveFormsModule, TranslateModule.forRoot()],
            providers: [
                { provide: Router, useValue: {
                        navigate: vi.fn().mockName("Router.navigate")
                    } },
                { provide: CandidateService, useValue: candidateServiceSpy },
                { provide: CandidateCertificationService, useValue: candidateCertificationServiceSpy },
                { provide: RegistrationService, useValue: registrationServiceSpy }
            ],
            schemas: [NO_ERRORS_SCHEMA]
        }).compileComponents();

        fixture = TestBed.createComponent(RegistrationCertificationsComponent);
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
            expect(component.form.contains('name')).toBe(true);
            expect(component.form.contains('institution')).toBe(true);
            expect(component.form.contains('dateCompleted')).toBe(true);
        });

        it('should load candidate certifications', () => {
            expect(candidateServiceSpy.getCandidateCertifications).toHaveBeenCalled();
            expect(component.candidateCertifications.length).toBe(1);
            expect(component.addingCertification).toBe(false);
        });

        it('should show the form when no certifications exist', async () => {
            // This test needs a different input setup than the shared beforeEach fixture,
            // so reset the TestBed before creating a fresh component instance.
            TestBed.resetTestingModule();
            await configureAndCreate({ candidateCertifications: [] });

            expect(component.addingCertification).toBe(true);
        });
    });

    describe('template tc components', () => {
        it('should render the add tc-button when not adding a certification', async () => {
            await configureAndCreate();
            const buttons = (fixture.nativeElement as HTMLElement).querySelectorAll('tc-button');

            expect(buttons.length).toBe(1);
        });
    });

    describe('behaviour', () => {
        beforeEach(async () => configureAndCreate());

        it('should handle a created certification by adding it and closing the form', () => {
            component.handleCandidateCertificationCreated(makeCertification({ id: 2 }));

            expect(component.candidateCertifications.length).toBe(2);
            expect(component.addingCertification).toBe(false);
        });

        it('should set the edit target when editing a certification', () => {
            component.editCandidateCertification(component.candidateCertifications[0]);

            expect(component.editTarget).toEqual(component.candidateCertifications[0]);
        });

        it('should replace the saved certification and clear the edit target', () => {
            component.editTarget = component.candidateCertifications[0];
            const updatedCertification = makeCertification({ name: 'Azure Certified' });

            component.handleCertificationSaved(updatedCertification, 0);

            expect(component.candidateCertifications[0].name).toBe('Azure Certified');
            expect(component.editTarget).toBeNull();
        });

        it('should delete a certification and clear saving on success', () => {
            component.deleteCertificate(component.candidateCertifications[0]);

            expect(candidateCertificationServiceSpy.deleteCandidateCertification).toHaveBeenCalledWith(1);
            expect(component.candidateCertifications.length).toBe(0);
            expect(component.saving).toBe(false);
        });

        it('should set error and clear saving when delete fails', async () => {
            // This test needs a different service setup than the shared beforeEach fixture,
            // so reset the TestBed before creating a fresh component instance.
            TestBed.resetTestingModule();
            const serverError = { status: 500 };
            await configureAndCreate({ deleteError: serverError });

            component.deleteCertificate(component.candidateCertifications[0]);

            expect(component.error).toEqual(serverError);
            expect(component.candidateCertifications.length).toBe(1);
            expect(component.saving).toBe(false);
        });
    });

    describe('navigation', () => {
        beforeEach(async () => configureAndCreate());

        it('should emit onSave and navigate next', () => {
            const onSaveSpy = vi.spyOn(component.onSave, 'emit').mockReturnValue(undefined);

            component.next();

            expect(onSaveSpy).toHaveBeenCalled();
            expect(registrationServiceSpy.next).toHaveBeenCalled();
        });

        it('should navigate back', () => {
            component.back();

            expect(registrationServiceSpy.back).toHaveBeenCalled();
        });

        it('should emit onSave when finishEditing is called', () => {
            const onSaveSpy = vi.spyOn(component.onSave, 'emit').mockReturnValue(undefined);

            component.finishEditing();

            expect(onSaveSpy).toHaveBeenCalled();
        });

        it('should emit onSave when cancel is called', () => {
            const onSaveSpy = vi.spyOn(component.onSave, 'emit').mockReturnValue(undefined);

            component.cancel();

            expect(onSaveSpy).toHaveBeenCalled();
        });
    });

    describe('error paths', () => {
        it('should set error when candidate certifications fail to load', async () => {
            const serverError = { status: 503 };
            await configureAndCreate({ candidateError: serverError });

            expect(component.error).toEqual(serverError);
            expect(component.loading).toBe(false);
        });
    });
});
