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

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {EditCandidateExamComponent} from './edit-candidate-exam.component';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgbActiveModal, NgbModalModule} from "@ng-bootstrap/ng-bootstrap";
import {ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";
import {MockCandidate} from "../../../../../MockData/MockCandidate";
import {CandidateExamService} from "../../../../../services/candidate-exam.service";
import {of, throwError} from 'rxjs';

describe('EditCandidateExamComponent', () => {
  let component: EditCandidateExamComponent;
  let fixture: ComponentFixture<EditCandidateExamComponent>;
  let mockCandidateExamService: jasmine.SpyObj<CandidateExamService>;
  let mockActiveModal: jasmine.SpyObj<NgbActiveModal>;

  beforeEach(async () => {
    // Mock CandidateExamService
    mockCandidateExamService = jasmine.createSpyObj('CandidateExamService', ['update']);
    // Mock NgbActiveModal
    mockActiveModal = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);

    await TestBed.configureTestingModule({
      declarations: [ EditCandidateExamComponent ],
      imports: [HttpClientTestingModule, NgbModalModule, ReactiveFormsModule, NgSelectModule],
      providers: [
        UntypedFormBuilder,
        { provide: NgbActiveModal, useValue: mockActiveModal },
        { provide: CandidateExamService, useValue: mockCandidateExamService }
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EditCandidateExamComponent);
    component = fixture.componentInstance;
    component.candidateExam = new MockCandidate().candidateExams[0];
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with candidate exam data', () => {
    expect(component.candidateForm).toBeTruthy();
    expect(component.candidateForm.controls['exam'].value).toEqual(component.candidateExam.exam);
    expect(component.candidateForm.controls['score'].value).toEqual(component.candidateExam.score);
    expect(component.candidateForm.controls['year'].value).toEqual(component.candidateExam.year);
    expect(component.candidateForm.controls['notes'].value).toBeNull();
  });

  it('should show other exam input field if exam is "Other"', () => {
    component.candidateForm.controls['exam'].setValue('Other');
    fixture.detectChanges();
    expect(component.isOtherExam).toBeTrue();
  });

  it('should hide other exam input field if exam is not "Other"', () => {
    component.candidateForm.controls['exam'].setValue('IELTS');
    fixture.detectChanges();
    expect(component.isOtherExam).toBeFalse();
  });

  it('should enable the score field if an exam is selected', () => {
    component.candidateForm.controls['exam'].setValue('IELTS');
    fixture.detectChanges();
    expect(component.hasSelectedExam).toBeTrue();
  });

  it('should disable the save button if form is invalid', () => {
    component.candidateForm.controls['score'].setValue(null);
    fixture.detectChanges();
    const saveButton = fixture.nativeElement.querySelector('.modal-footer button[type="button"]');
    expect(saveButton.disabled).toBeTrue();
  });

  it('should call the update service when saving', () => {
    const mockRequest = {
      id: component.candidateExam.id,
      exam: component.candidateForm.value.exam,
      otherExam: component.candidateForm.value.otherExam,
      score: component.candidateForm.value.score,
      year: component.candidateForm.value.year,
      notes: component.candidateForm.value.notes
    };
    mockCandidateExamService.update.and.returnValue(of(mockRequest));

    component.onSave();
    expect(mockCandidateExamService.update).toHaveBeenCalledWith(component.candidateExam.id, mockRequest);
    expect(component.saving).toBeFalse();
  });

  it('should show error message if saving fails', () => {
    mockCandidateExamService.update.and.returnValue(throwError('Error occurred'));

    component.onSave();
    expect(component.error).toBe('Error occurred');
    expect(component.saving).toBeFalse();
  });

  it('should close the modal on successful save', () => {
    const mockRequest = {
      id: component.candidateExam.id,
      exam: component.candidateForm.value.exam,
      otherExam: component.candidateForm.value.otherExam,
      score: component.candidateForm.value.score,
      year: component.candidateForm.value.year,
      notes: component.candidateForm.value.notes
    };
    mockCandidateExamService.update.and.returnValue(of(mockRequest));

    component.onSave();
    expect(mockActiveModal.close).toHaveBeenCalledWith(mockRequest);
  });

  it('should dismiss the modal', () => {
    component.dismiss();
    expect(mockActiveModal.dismiss).toHaveBeenCalledWith(false);
  });
});
