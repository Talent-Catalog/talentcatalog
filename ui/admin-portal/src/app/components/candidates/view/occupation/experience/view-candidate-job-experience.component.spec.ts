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
import {ViewCandidateJobExperienceComponent} from "./view-candidate-job-experience.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateJobExperienceService} from "../../../../../services/candidate-job-experience.service";
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder, UntypedFormControl, UntypedFormGroup} from "@angular/forms";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {NgSelectModule} from "@ng-select/ng-select";
import {NgxWigModule} from "ngx-wig";
import {NO_ERRORS_SCHEMA, SimpleChange} from "@angular/core";
import {MockCandidate} from "../../../../../MockData/MockCandidate";
import {CandidateJobExperience} from "../../../../../model/candidate-job-experience";
import {CandidateService} from "../../../../../services/candidate.service";
import {of, throwError} from "rxjs";
import {SkillsService} from "../../../../../services/skills.service";
import {EditCandidateOccupationComponent} from "../edit/edit-candidate-occupation.component";
import {CreateCandidateJobExperienceComponent} from "./create/create-candidate-job-experience.component";
import {EditCandidateJobExperienceComponent} from "./edit/edit-candidate-job-experience.component";
import {ConfirmationComponent} from "../../../../util/confirm/confirmation.component";
import {CandidateJobExperienceComponent} from "./candidate-job-experience/candidate-job-experience.component";
import {ExtendDatePipe} from "../../../../../util/date-adapter/extend-date-pipe";
import {By} from "@angular/platform-browser";
import {expect, vi} from 'vitest';

describe('ViewCandidateJobExperienceComponent', () => {
    let component: ViewCandidateJobExperienceComponent;
    let fixture: ComponentFixture<ViewCandidateJobExperienceComponent>;
    let mockCandidateJobExperienceService: MockedObject<CandidateJobExperienceService>;
    let mockCandidateService: MockedObject<CandidateService>;
    let mockNgbModal: MockedObject<NgbModal>;
    let mockSkillsService: MockedObject<SkillsService>;
    let formBuilder: UntypedFormBuilder;

    const mockCandidate = new MockCandidate();

    const mockExperiences: CandidateJobExperience[] = mockCandidate.candidateJobExperiences;
    const candidateOccupation = mockCandidate.candidateOccupations;

    beforeEach(async () => {
        const candidateJobExperienceServiceSpy = {
            delete: vi.fn().mockName("CandidateJobExperienceService.delete")
        };
        const candidateServiceSpy = {
            updateCandidate: vi.fn().mockName("CandidateService.updateCandidate")
        };
        const ngbModalSpy = {
            open: vi.fn().mockName("NgbModal.open")
        };
        const skillsServiceSpy = {
            extractSkills: vi.fn().mockName("SkillsService.extractSkills")
        };

        await TestBed.configureTestingModule({
            declarations: [ViewCandidateJobExperienceComponent, CandidateJobExperienceComponent, ExtendDatePipe],
            imports: [FormsModule, ReactiveFormsModule, NgSelectModule, NgxWigModule],
            providers: [
                UntypedFormBuilder,
                { provide: CandidateJobExperienceService, useValue: candidateJobExperienceServiceSpy },
                { provide: CandidateService, useValue: candidateServiceSpy },
                { provide: NgbModal, useValue: ngbModalSpy },
                { provide: SkillsService, useValue: skillsServiceSpy },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ],
            schemas: [NO_ERRORS_SCHEMA]
        }).compileComponents();

        mockCandidateJobExperienceService = TestBed.inject(CandidateJobExperienceService) as MockedObject<CandidateJobExperienceService>;
        mockCandidateService = TestBed.inject(CandidateService) as MockedObject<CandidateService>;
        mockNgbModal = TestBed.inject(NgbModal) as MockedObject<NgbModal>;
        mockSkillsService = TestBed.inject(SkillsService) as MockedObject<SkillsService>;
        formBuilder = TestBed.inject(UntypedFormBuilder);

        fixture = TestBed.createComponent(ViewCandidateJobExperienceComponent);
        component = fixture.componentInstance;
    });

    beforeEach(() => {


        component.candidate = mockCandidate;
        component.candidateOccupation = candidateOccupation[0];
        component.editable = true;
        component.adminUser = true;

        component.ngOnChanges({
            candidate: {
                previousValue: null,
                currentValue: mockCandidate,
                firstChange: true,
                isFirstChange: () => true
            }
        });
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should run ngOnInit', () => {
        expect(() => component.ngOnInit()).not.toThrow();
    });

    it('should update expanded state and experiences on changes', () => {
        component.expanded = true;
        component.experiences = [];

        component.ngOnChanges({
            candidateOccupation: new SimpleChange(null, component.candidateOccupation, false)
        });

        expect(component.expanded).toBe(false);
        expect(component.experiences)
            .toBe(component.candidateOccupation.candidateJobExperiences);
    });

    it('should open edit occupation modal, patch the form and refresh after success', fakeAsync(() => {
        component.candidateJobExperienceForm = new UntypedFormGroup({
            candidateOccupationId: new UntypedFormControl(null)
        });

        const modalRef = {
            componentInstance: {},
            result: Promise.resolve(component.candidateOccupation)
        } as any;

        mockNgbModal.open.mockReturnValue(modalRef);

        component.editOccupation();
        tick();

        expect(mockNgbModal.open).toHaveBeenCalledWith(EditCandidateOccupationComponent, {
            centered: true,
            backdrop: 'static'
        });
        expect(modalRef.componentInstance.candidateOccupation)
            .toBe(component.candidateOccupation);
        expect(component.candidateJobExperienceForm
            .controls['candidateOccupationId'].value).toBe(component.candidateOccupation.id);
        expect(mockCandidateService.updateCandidate)
            .toHaveBeenCalled();
    }));

    it('should ignore edit occupation modal dismissal', fakeAsync(() => {
        component.candidateJobExperienceForm = new UntypedFormGroup({
            candidateOccupationId: new UntypedFormControl(null)
        });

        const modalRef = {
            componentInstance: {},
            result: Promise.reject('dismissed')
        } as any;

        mockNgbModal.open.mockReturnValue(modalRef);

        component.editOccupation();
        tick();

        expect(mockCandidateService.updateCandidate)
            .not.toHaveBeenCalled();
    }));

    it('should open create experience modal and refresh after success', fakeAsync(() => {
        const modalRef = {
            componentInstance: {},
            result: Promise.resolve({})
        } as any;

        mockNgbModal.open.mockReturnValue(modalRef);

        component.createCandidateJobExperience();
        tick();

        expect(mockNgbModal.open).toHaveBeenCalledWith(CreateCandidateJobExperienceComponent, {
            centered: true,
            backdrop: 'static'
        });
        expect(modalRef.componentInstance.candidateOccupationId)
            .toBe(component.candidateOccupation.id);
        expect(modalRef.componentInstance.candidateId)
            .toBe(component.candidate.id);
        expect(mockCandidateService.updateCandidate)
            .toHaveBeenCalled();
    }));

    it('should ignore create experience modal dismissal', fakeAsync(() => {
        const modalRef = {
            componentInstance: {},
            result: Promise.reject('dismissed')
        } as any;

        mockNgbModal.open.mockReturnValue(modalRef);

        component.createCandidateJobExperience();
        tick();

        expect(mockCandidateService.updateCandidate)
            .not.toHaveBeenCalled();
    }));

    it('should edit experience, extract skills and refresh after success', fakeAsync(() => {
        const experience = {
            id: 5,
            description: 'Original text',
            tidiedDescription: 'Tidied text',
            keywordsInDescription: ['Java', 'Spring']
        } as any;

        const modalRef = {
            componentInstance: {},
            result: Promise.resolve(experience)
        } as any;

        mockNgbModal.open.mockReturnValue(modalRef);

        mockSkillsService.extractSkills.mockReturnValue(of([
            { name: 'Java' },
            { name: 'Spring' }
        ] as any));

        component.editCandidateJobExperience(experience);
        tick();

        expect(mockNgbModal.open).toHaveBeenCalledWith(EditCandidateJobExperienceComponent, {
            centered: true,
            backdrop: 'static'
        });

        expect(modalRef.componentInstance.candidateJobExperience)
            .toBe(experience);

        expect(mockSkillsService.extractSkills).toHaveBeenCalledWith({
            lang: 'en',
            text: 'Original text Tidied text Java Spring'
        });

        expect(modalRef.componentInstance.extractedSkills)
            .toEqual(['Java', 'Spring']);

        expect(mockCandidateService.updateCandidate)
            .toHaveBeenCalled();
    }));

    it('should ignore edit experience modal dismissal', fakeAsync(() => {
        const experience = {
            id: 5,
            description: '',
            tidiedDescription: '',
            keywordsInDescription: []
        } as any;

        const modalRef = {
            componentInstance: {},
            result: Promise.reject('dismissed')
        } as any;

        mockNgbModal.open.mockReturnValue(modalRef);
        mockSkillsService.extractSkills.mockReturnValue(of([]));

        component.editCandidateJobExperience(experience);
        tick();

        expect(mockSkillsService.extractSkills).toHaveBeenCalledWith({
            lang: 'en',
            text: '  '
        });

        expect(modalRef.componentInstance.extractedSkills)
            .toEqual([]);

        expect(mockCandidateService.updateCandidate)
            .not.toHaveBeenCalled();
    }));

    it('should safely build skill extraction text when description fields are missing', fakeAsync(() => {
        const experience = {
            id: 9,
            description: undefined,
            tidiedDescription: undefined,
            keywordsInDescription: undefined
        } as any;

        const modalRef = {
            componentInstance: {},
            result: Promise.resolve(experience)
        } as any;

        mockNgbModal.open.mockReturnValue(modalRef);
        mockSkillsService.extractSkills.mockReturnValue(of([]));

        expect(() => component.editCandidateJobExperience(experience)).not.toThrow();
        tick();

        expect(mockSkillsService.extractSkills).toHaveBeenCalledWith({
            lang: 'en',
            text: '  '
        });

        expect(mockCandidateService.updateCandidate)
            .toHaveBeenCalled();
    }));

    it('should emit delete occupation immediately when there are no experiences', () => {
        const emitSpy = vi.spyOn(component.deleteOccupation, 'emit').mockReturnValue(undefined);
        component.experiences = [];

        component.deleteCandidateOccupation();

        expect(emitSpy).toHaveBeenCalledWith(component.candidateOccupation);
        expect(mockNgbModal.open).not.toHaveBeenCalled();
    });

    it('should confirm before deleting occupation with experiences', fakeAsync(() => {
        const emitSpy = vi.spyOn(component.deleteOccupation, 'emit').mockReturnValue(undefined);
        component.experiences = [{} as any];

        const modalRef = {
            componentInstance: {},
            result: Promise.resolve(true)
        } as any;

        mockNgbModal.open.mockReturnValue(modalRef);

        component.deleteCandidateOccupation();
        tick();

        expect(mockNgbModal.open).toHaveBeenCalledWith(ConfirmationComponent, {
            centered: true,
            backdrop: 'static'
        });
        expect(modalRef.componentInstance.message).toBe('Are you sure you want to delete this occupation? ' +
            'All associated job experiences will also be deleted.');
        expect(emitSpy).toHaveBeenCalledWith(component.candidateOccupation);
    }));

    it('should ignore occupation delete modal dismissal', fakeAsync(() => {
        const emitSpy = vi.spyOn(component.deleteOccupation, 'emit').mockReturnValue(undefined);
        component.experiences = [{} as any];

        mockNgbModal.open.mockReturnValue({
            componentInstance: {},
            result: Promise.reject('dismissed')
        } as any);

        component.deleteCandidateOccupation();
        tick();

        expect(emitSpy).not.toHaveBeenCalled();
    }));

    it('should delete a job experience after confirmation', fakeAsync(() => {
        const experience = { id: 7 } as any;

        const modalRef = {
            componentInstance: {},
            result: Promise.resolve(true)
        } as any;

        mockNgbModal.open.mockReturnValue(modalRef);
        mockCandidateJobExperienceService.delete.mockReturnValue(of({} as any));

        component.loading = true;
        component.deleteCandidateJobExperience(experience);
        tick();

        expect(modalRef.componentInstance.message).toBe('Are you sure you want to delete this job experience?');
        expect(mockCandidateJobExperienceService.delete)
            .toHaveBeenCalledWith(experience.id);
        expect(component.loading).toBe(false);
        expect(mockCandidateService.updateCandidate)
            .toHaveBeenCalled();
    }));

    it('should set error and stop loading when deleting a job experience fails', fakeAsync(() => {
        const experience = { id: 7 } as any;
        const error = 'Delete failed';

        const modalRef = {
            componentInstance: {},
            result: Promise.resolve(true)
        } as any;

        mockNgbModal.open.mockReturnValue(modalRef);
        mockCandidateJobExperienceService.delete.mockReturnValue(throwError(error));

        component.loading = true;
        component.deleteCandidateJobExperience(experience);
        tick();

        expect(component.error).toBe(error);
        expect(component.loading).toBe(false);
        expect(mockCandidateService.updateCandidate)
            .not.toHaveBeenCalled();
    }));

    it('should not delete a job experience when confirmation is false', fakeAsync(() => {
        const experience = { id: 7 } as any;

        mockNgbModal.open.mockReturnValue({
            componentInstance: {},
            result: Promise.resolve(false)
        } as any);

        component.deleteCandidateJobExperience(experience);
        tick();

        expect(mockCandidateJobExperienceService.delete)
            .not.toHaveBeenCalled();
    }));

    it('should ignore job experience delete modal dismissal', fakeAsync(() => {
        const experience = { id: 7 } as any;

        mockNgbModal.open.mockReturnValue({
            componentInstance: {},
            result: Promise.reject('dismissed')
        } as any);

        component.deleteCandidateJobExperience(experience);
        tick();

        expect(mockCandidateJobExperienceService.delete)
            .not.toHaveBeenCalled();
    }));

    it('should pass isPrincipal through to the edit occupation modal', fakeAsync(() => {
        component.candidateJobExperienceForm = new UntypedFormGroup({
            candidateOccupationId: new UntypedFormControl(null)
        });
        component.isPrincipal = true;

        const modalRef = {
            componentInstance: {},
            result: Promise.resolve(component.candidateOccupation)
        } as any;

        mockNgbModal.open.mockReturnValue(modalRef);

        component.editOccupation();
        tick();

        expect(modalRef.componentInstance.isPrincipal).toBe(true);
    }));

    it('should expose the isHtml helper', () => {
        expect(component.isHtml('<p>Hello</p>')).toBe(true);
        expect(component.isHtml('Plain text')).toBe(false);
    });

    describe('rendering (via the reusable CandidateJobExperienceComponent)', () => {

        function experienceComponents() {
            return fixture.debugElement.queryAll(By.directive(CandidateJobExperienceComponent));
        }

        it('should render one reusable experience component per experience, in order', () => {
            fixture.detectChanges();

            const rendered = experienceComponents()
                .map(de => (de.componentInstance as CandidateJobExperienceComponent).experience);
            expect(rendered).toEqual(component.candidateOccupation.candidateJobExperiences);
            expect(fixture.nativeElement.textContent).toContain('Developer');
            expect(fixture.nativeElement.textContent).toContain('Project Manager');
        });

        it('should still render the occupation heading', () => {
            fixture.detectChanges();

            expect(fixture.nativeElement.textContent).toContain('Software Engineer (5 years)');
        });

        it('should render separators only between experiences', () => {
            fixture.detectChanges();

            expect(fixture.nativeElement.querySelectorAll('hr').length).toBe(experienceComponents().length - 1);
        });

        it('should keep edit and delete controls in the parent alongside each experience when editable', () => {
            fixture.detectChanges();

            experienceComponents().forEach(de => {
                const buttons = de.queryAll(By.css('[experienceActions] tc-button'));
                expect(buttons.length).toBe(2);
            });
        });

        it('should call the parent edit/delete handlers for the corresponding experience', () => {
            vi.spyOn(component, 'editCandidateJobExperience').mockReturnValue(undefined);
            vi.spyOn(component, 'deleteCandidateJobExperience').mockReturnValue(undefined);
            fixture.detectChanges();

            const second = experienceComponents()[1];
            const [editButton, deleteButton] = second.queryAll(By.css('[experienceActions] tc-button'));
            editButton.triggerEventHandler('onClick', null);
            deleteButton.triggerEventHandler('onClick', null);

            const secondExperience = component.candidateOccupation.candidateJobExperiences[1];
            expect(component.editCandidateJobExperience).toHaveBeenCalledWith(secondExperience);
            expect(component.deleteCandidateJobExperience).toHaveBeenCalledWith(secondExperience);
        });

        it('should show edit but not delete for non-admin users', () => {
            component.adminUser = false;
            fixture.detectChanges();

            experienceComponents().forEach(de => {
                expect(de.queryAll(By.css('[experienceActions] tc-button')).length).toBe(1);
            });
        });

        it('should show no experience controls when not editable', () => {
            component.editable = false;
            fixture.detectChanges();

            expect(fixture.nativeElement.querySelector('[experienceActions]')).toBeNull();
            expect(experienceComponents().length).toBe(2);
        });

        it('should show the empty state when there are no experiences', () => {
            component.experiences = [];
            fixture.detectChanges();

            expect(experienceComponents().length).toBe(0);
            expect(fixture.nativeElement.textContent)
                .toContain('No job experience data has been entered by this candidate.');
        });
    });

});
