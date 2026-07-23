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
import {ViewCandidateJobExperienceComponent} from "./view-candidate-job-experience.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {
  CandidateJobExperienceService
} from "../../../../../services/candidate-job-experience.service";
import {
  FormsModule,
  ReactiveFormsModule,
  UntypedFormBuilder,
  UntypedFormControl,
  UntypedFormGroup
} from "@angular/forms";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {NgxWigModule} from "ngx-wig";
import {NO_ERRORS_SCHEMA, SimpleChange} from "@angular/core";
import {MockCandidate} from "../../../../../MockData/MockCandidate";
import {CandidateJobExperience} from "../../../../../model/candidate-job-experience";
import {CandidateService} from "../../../../../services/candidate.service";
import {of} from "rxjs";
import {SkillsService} from "../../../../../services/skills.service";
import {EditCandidateOccupationComponent} from "../edit/edit-candidate-occupation.component";
import {
  CreateCandidateJobExperienceComponent
} from "./create/create-candidate-job-experience.component";
import {EditCandidateJobExperienceComponent} from "./edit/edit-candidate-job-experience.component";
import {ConfirmationComponent} from "../../../../util/confirm/confirmation.component";

describe('ViewCandidateJobExperienceComponent', () => {
  let component: ViewCandidateJobExperienceComponent;
  let fixture: ComponentFixture<ViewCandidateJobExperienceComponent>;
  let mockCandidateJobExperienceService: jasmine.SpyObj<CandidateJobExperienceService>;
  let mockCandidateService: jasmine.SpyObj<CandidateService>;
  let mockNgbModal: jasmine.SpyObj<NgbModal>;
  let mockSkillsService: jasmine.SpyObj<SkillsService>;
  let formBuilder: UntypedFormBuilder;

  const mockCandidate = new MockCandidate();

  const mockExperiences: CandidateJobExperience[] = mockCandidate.candidateJobExperiences;
  const candidateOccupation = mockCandidate.candidateOccupations;

  beforeEach(async () => {
    const candidateJobExperienceServiceSpy = jasmine.createSpyObj('CandidateJobExperienceService', ['delete']);
    const candidateServiceSpy = jasmine.createSpyObj('CandidateService', ['updateCandidate']);
    const ngbModalSpy = jasmine.createSpyObj('NgbModal', ['open']);
    const skillsServiceSpy = jasmine.createSpyObj('SkillsService', ['extractSkills']);

    await TestBed.configureTestingModule({
      declarations: [ViewCandidateJobExperienceComponent],
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule,NgxWigModule],
      providers: [
        UntypedFormBuilder,
        { provide: CandidateJobExperienceService, useValue: candidateJobExperienceServiceSpy },
        { provide: CandidateService, useValue: candidateServiceSpy },
        { provide: NgbModal, useValue: ngbModalSpy },
        { provide: SkillsService, useValue: skillsServiceSpy }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    mockCandidateJobExperienceService = TestBed.inject(CandidateJobExperienceService) as jasmine.SpyObj<CandidateJobExperienceService>;
    mockCandidateService = TestBed.inject(CandidateService) as jasmine.SpyObj<CandidateService>;
    mockNgbModal = TestBed.inject(NgbModal) as jasmine.SpyObj<NgbModal>;
    mockSkillsService = TestBed.inject(SkillsService) as jasmine.SpyObj<SkillsService>;
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
      candidateOccupation: new SimpleChange(
        null,
        component.candidateOccupation,
        false
      )
    });

    expect(component.expanded).toBeFalse();
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

    mockNgbModal.open.and.returnValue(modalRef);

    component.editOccupation();
    tick();

    expect(mockNgbModal.open).toHaveBeenCalledWith(
      EditCandidateOccupationComponent,
      {
        centered: true,
        backdrop: 'static'
      }
    );
    expect(modalRef.componentInstance.candidateOccupation)
    .toBe(component.candidateOccupation);
    expect(
      component.candidateJobExperienceForm
        .controls['candidateOccupationId'].value
    ).toBe(component.candidateOccupation.id);
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

    mockNgbModal.open.and.returnValue(modalRef);

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

    mockNgbModal.open.and.returnValue(modalRef);

    component.createCandidateJobExperience();
    tick();

    expect(mockNgbModal.open).toHaveBeenCalledWith(
      CreateCandidateJobExperienceComponent,
      {
        centered: true,
        backdrop: 'static'
      }
    );
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

    mockNgbModal.open.and.returnValue(modalRef);

    component.createCandidateJobExperience();
    tick();

    expect(mockCandidateService.updateCandidate)
    .not.toHaveBeenCalled();
  }));

  it('should edit experience, extract skills and refresh after success', fakeAsync(() => {
    const experience = {
      id: 5,
      description: JSON.stringify({
        parts: {
          original: 'Original text',
          tidied: 'Tidied text',
          keywords: ['Java', 'Spring']
        }
      })
    } as any;

    const modalRef = {
      componentInstance: {},
      result: Promise.resolve(experience)
    } as any;

    mockNgbModal.open.and.returnValue(modalRef);

    mockSkillsService.extractSkills.and.returnValue(
      of([
        {name: 'Java'},
        {name: 'Spring'}
      ] as any)
    );

    component.editCandidateJobExperience(experience);
    tick();

    expect(mockNgbModal.open).toHaveBeenCalledWith(
      EditCandidateJobExperienceComponent,
      {
        centered: true,
        backdrop: 'static'
      }
    );

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
      description: JSON.stringify({
        parts: {
          original: '',
          tidied: '',
          keywords: []
        }
      })
    } as any;

    const modalRef = {
      componentInstance: {},
      result: Promise.reject('dismissed')
    } as any;

    mockNgbModal.open.and.returnValue(modalRef);
    mockSkillsService.extractSkills.and.returnValue(of([]));

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

  it('should emit delete occupation immediately when there are no experiences', () => {
    const emitSpy = spyOn(component.deleteOccupation, 'emit');
    component.experiences = [];

    component.deleteCandidateOccupation();

    expect(emitSpy).toHaveBeenCalledWith(
      component.candidateOccupation
    );
    expect(mockNgbModal.open).not.toHaveBeenCalled();
  });

  it('should confirm before deleting occupation with experiences', fakeAsync(() => {
    const emitSpy = spyOn(component.deleteOccupation, 'emit');
    component.experiences = [{} as any];

    const modalRef = {
      componentInstance: {},
      result: Promise.resolve(true)
    } as any;

    mockNgbModal.open.and.returnValue(modalRef);

    component.deleteCandidateOccupation();
    tick();

    expect(mockNgbModal.open).toHaveBeenCalledWith(
      ConfirmationComponent,
      {
        centered: true,
        backdrop: 'static'
      }
    );
    expect(modalRef.componentInstance.message).toBe(
      'Are you sure you want to delete this occupation? ' +
      'All associated job experiences will also be deleted.'
    );
    expect(emitSpy).toHaveBeenCalledWith(
      component.candidateOccupation
    );
  }));

  it('should ignore occupation delete modal dismissal', fakeAsync(() => {
    const emitSpy = spyOn(component.deleteOccupation, 'emit');
    component.experiences = [{} as any];

    mockNgbModal.open.and.returnValue({
      componentInstance: {},
      result: Promise.reject('dismissed')
    } as any);

    component.deleteCandidateOccupation();
    tick();

    expect(emitSpy).not.toHaveBeenCalled();
  }));

  it('should delete a job experience after confirmation', fakeAsync(() => {
    const experience = {id: 7} as any;

    const modalRef = {
      componentInstance: {},
      result: Promise.resolve(true)
    } as any;

    mockNgbModal.open.and.returnValue(modalRef);
    mockCandidateJobExperienceService.delete
    .and.returnValue(of({} as any));

    component.loading = true;
    component.deleteCandidateJobExperience(experience);
    tick();

    expect(modalRef.componentInstance.message).toBe(
      'Are you sure you want to delete this job experience?'
    );
    expect(mockCandidateJobExperienceService.delete)
    .toHaveBeenCalledWith(experience.id);
    expect(component.loading).toBeFalse();
    expect(mockCandidateService.updateCandidate)
    .toHaveBeenCalled();
  }));

  it('should not delete a job experience when confirmation is false', fakeAsync(() => {
    const experience = {id: 7} as any;

    mockNgbModal.open.and.returnValue({
      componentInstance: {},
      result: Promise.resolve(false)
    } as any);

    component.deleteCandidateJobExperience(experience);
    tick();

    expect(mockCandidateJobExperienceService.delete)
    .not.toHaveBeenCalled();
  }));

  it('should ignore job experience delete modal dismissal', fakeAsync(() => {
    const experience = {id: 7} as any;

    mockNgbModal.open.and.returnValue({
      componentInstance: {},
      result: Promise.reject('dismissed')
    } as any);

    component.deleteCandidateJobExperience(experience);
    tick();

    expect(mockCandidateJobExperienceService.delete)
    .not.toHaveBeenCalled();
  }));

  it('should expose the isHtml helper', () => {
    expect(component.isHtml('<p>Hello</p>')).toBeTrue();
    expect(component.isHtml('Plain text')).toBeFalse();
  });

});
