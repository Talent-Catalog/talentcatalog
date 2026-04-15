/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {Component, EventEmitter, Input, NO_ERRORS_SCHEMA, Output} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {Router} from '@angular/router';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {TranslateModule, TranslateService} from '@ngx-translate/core';
import {of, throwError} from 'rxjs';

import {RegistrationCandidateExamComponent} from './registration-candidate-exam.component';
import {CandidateExam, Exam} from '../../../model/candidate';
import {CandidateExamService} from '../../../services/candidate-exam.service';
import {CandidateService} from '../../../services/candidate.service';
import {RegistrationService} from '../../../services/registration.service';

@Component({
  selector: 'tc-button',
  template: '<ng-content></ng-content>'
})
class TcButtonStubComponent {
  @Input() disabled?: boolean;
  @Input() type?: string;
  @Output() onClick = new EventEmitter<void>();
}

function makeExam(overrides: Partial<CandidateExam> = {}): CandidateExam {
  return {
    id: 1,
    exam: Exam.IELTSGen,
    otherExam: null,
    score: '7.5',
    year: 2024,
    notes: 'Strong result',
    ...overrides
  };
}

describe('RegistrationCandidateExamComponent', () => {
  let component: RegistrationCandidateExamComponent;
  let fixture: ComponentFixture<RegistrationCandidateExamComponent>;

  let candidateServiceSpy: jasmine.SpyObj<CandidateService>;
  let candidateExamServiceSpy: jasmine.SpyObj<CandidateExamService>;
  let registrationServiceSpy: jasmine.SpyObj<RegistrationService>;
  let modalServiceSpy: jasmine.SpyObj<NgbModal>;

  async function configureAndCreate(options?: {
    candidateExams?: CandidateExam[];
    candidateError?: unknown;
    deleteError?: unknown;
    modalResult?: boolean;
  }) {
    candidateServiceSpy = jasmine.createSpyObj('CandidateService', ['getCandidateCandidateExams']);
    candidateExamServiceSpy = jasmine.createSpyObj('CandidateExamService', ['deleteCandidateExam']);
    registrationServiceSpy = jasmine.createSpyObj('RegistrationService', ['next', 'back']);
    modalServiceSpy = jasmine.createSpyObj('NgbModal', ['open']);

    const candidateExams = options?.candidateExams ?? [makeExam()];

    if (options?.candidateError) {
      candidateServiceSpy.getCandidateCandidateExams.and.returnValue(throwError(options.candidateError));
    } else {
      candidateServiceSpy.getCandidateCandidateExams.and.returnValue(of({candidateExams} as any));
    }

    if (options?.deleteError) {
      candidateExamServiceSpy.deleteCandidateExam.and.returnValue(throwError(options.deleteError));
    } else {
      candidateExamServiceSpy.deleteCandidateExam.and.returnValue(of({} as any));
    }

    modalServiceSpy.open.and.returnValue({
      result: Promise.resolve(options?.modalResult ?? true)
    } as any);

    await TestBed.configureTestingModule({
      declarations: [RegistrationCandidateExamComponent, TcButtonStubComponent],
      imports: [ReactiveFormsModule, TranslateModule.forRoot()],
      providers: [
        {provide: Router, useValue: jasmine.createSpyObj('Router', ['navigate'])},
        {provide: CandidateService, useValue: candidateServiceSpy},
        {provide: CandidateExamService, useValue: candidateExamServiceSpy},
        {provide: RegistrationService, useValue: registrationServiceSpy},
        {provide: NgbModal, useValue: modalServiceSpy}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(RegistrationCandidateExamComponent);
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

    it('should load candidate exams', () => {
      expect(candidateServiceSpy.getCandidateCandidateExams).toHaveBeenCalled();
      expect(component.candidateExams.length).toBe(1);
      expect(component.addingExam).toBeFalse();
    });

    it('should show the form when no exams exist', async () => {
      TestBed.resetTestingModule();
      await configureAndCreate({candidateExams: []});

      expect(component.addingExam).toBeTrue();
    });
  });

  describe('template tc components', () => {
    it('should render the add tc-button when not adding an exam', async () => {
      await configureAndCreate();
      const buttons = (fixture.nativeElement as HTMLElement).querySelectorAll('tc-button');

      expect(buttons.length).toBe(1);
    });
  });

  describe('behaviour', () => {
    beforeEach(async () => configureAndCreate());

    it('should handle a created exam by adding it and closing the form', () => {
      component.handleCandidateExamCreated(makeExam({id: 2}));

      expect(component.candidateExams.length).toBe(2);
      expect(component.addingExam).toBeFalse();
    });

    it('should set the edit target when editing an exam', () => {
      component.editCandidateExam(component.candidateExams[0]);

      expect(component.editTarget).toEqual(component.candidateExams[0]);
    });

    it('should replace the saved exam and clear the edit target', () => {
      component.editTarget = component.candidateExams[0];
      const updatedExam = makeExam({score: '8'});

      component.handleExamSaved(updatedExam, 0);

      expect(component.candidateExams[0].score).toBe('8');
      expect(component.editTarget).toBeNull();
    });
  });

  describe('navigation', () => {
    beforeEach(async () => configureAndCreate());

    it('should emit onSave and navigate next', () => {
      const onSaveSpy = spyOn(component.onSave, 'emit');

      component.next();

      expect(onSaveSpy).toHaveBeenCalled();
      expect(registrationServiceSpy.next).toHaveBeenCalled();
    });

    it('should navigate back', () => {
      component.back();

      expect(registrationServiceSpy.back).toHaveBeenCalled();
    });

    it('should emit onSave when finishEditing is called', () => {
      const onSaveSpy = spyOn(component.onSave, 'emit');

      component.finishEditing();

      expect(onSaveSpy).toHaveBeenCalled();
    });
  });

  describe('delete', () => {
    it('should delete the exam after modal confirmation', async () => {
      await configureAndCreate();

      component.deleteExam(component.candidateExams[0], 0);
      await Promise.resolve();

      expect(modalServiceSpy.open).toHaveBeenCalled();
      expect(candidateExamServiceSpy.deleteCandidateExam).toHaveBeenCalledWith(1);
    });

    it('should keep the exam when the modal resolves false', async () => {
      await configureAndCreate({modalResult: false});

      component.deleteExam(component.candidateExams[0], 0);
      await Promise.resolve();

      expect(candidateExamServiceSpy.deleteCandidateExam).not.toHaveBeenCalled();
      expect(component.candidateExams.length).toBe(1);
    });

    it('should set error and clear saving when delete fails', async () => {
      const serverError = {status: 500};
      await configureAndCreate({deleteError: serverError});

      component.deleteExam(component.candidateExams[0], 0);
      await Promise.resolve();

      expect(component.error).toEqual(serverError);
      expect(component.saving).toBeFalse();
    });
  });

  describe('error paths', () => {
    it('should set error when candidate exams fail to load', async () => {
      const serverError = {status: 503};
      await configureAndCreate({candidateError: serverError});

      expect(component.error).toEqual(serverError);
      expect(component.loading).toBeFalse();
    });
  });
});
