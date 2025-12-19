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
import {EditCandidateEducationComponent} from './edit-candidate-education.component';
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from '@angular/forms';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {of, throwError} from 'rxjs';
import {CandidateEducationService} from '../../../../../services/candidate-education.service';
import {CountryService} from '../../../../../services/country.service';
import {EducationMajorService} from '../../../../../services/education-major.service';
import {MockCandidate} from "../../../../../MockData/MockCandidate";
import {MockJob} from "../../../../../MockData/MockJob";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";

describe('EditCandidateEducationComponent', () => {
  let component: EditCandidateEducationComponent;
  let fixture: ComponentFixture<EditCandidateEducationComponent>;
  let mockActiveModal: jasmine.SpyObj<NgbActiveModal>;
  let mockCandidateEducationService: jasmine.SpyObj<CandidateEducationService>;
  let mockCountryService: jasmine.SpyObj<CountryService>;
  let mockEducationMajorService: jasmine.SpyObj<EducationMajorService>;

  const mockCandidate = new MockCandidate();
  beforeEach(async () => {
    mockActiveModal = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);
    mockCandidateEducationService = jasmine.createSpyObj('CandidateEducationService', ['update']);
    mockCountryService = jasmine.createSpyObj('CountryService', ['listCountries']);
    mockEducationMajorService = jasmine.createSpyObj('EducationMajorService', ['listMajors']);

    await TestBed.configureTestingModule({
      declarations: [EditCandidateEducationComponent],
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule],
      providers: [
        UntypedFormBuilder,
        {provide: NgbActiveModal, useValue: mockActiveModal},
        {provide: CandidateEducationService, useValue: mockCandidateEducationService},
        {provide: CountryService, useValue: mockCountryService},
        {provide: EducationMajorService, useValue: mockEducationMajorService},
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EditCandidateEducationComponent);
    component = fixture.componentInstance;

    // Provide initial candidate education data
    component.candidateEducation = mockCandidate.candidateEducations[0];

    // Mock service responses
    mockCountryService.listCountries.and.returnValue(of([MockJob.country]));
    mockEducationMajorService.listMajors.and.returnValue(of([mockCandidate.candidateEducations[0].educationMajor]));

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the form with candidate education data', () => {
    expect(component.candidateForm.value.courseName).toBe('B.Sc. in Computer Science');
    expect(component.candidateForm.value.institution).toBe('University of Example');
    expect(component.candidateForm.value.countryId).toBe(1);
    expect(component.candidateForm.value.majorId).toBe(1);
    expect(component.candidateForm.value.lengthOfCourseYears).toBe(4);
    expect(component.candidateForm.value.yearCompleted).toBe('2012');
    expect(component.candidateForm.value.educationType).toBe('Bachelor\'s Degree');
    expect(component.candidateForm.value.incomplete).toBeFalse();
  });

  it('should call update service on save', () => {
    mockCandidateEducationService.update.and.returnValue(of(component.candidateEducation));

    component.onSave();

    expect(mockCandidateEducationService.update).toHaveBeenCalledWith({
      id: 1,
      courseName: 'B.Sc. in Computer Science',
      institution: 'University of Example',
      countryId: 1,
      majorId: 1,
      yearCompleted: '2012',
      lengthOfCourseYears: 4,
      educationType: 'Bachelor\'s Degree',
      incomplete: false
    });
    expect(mockActiveModal.close).toHaveBeenCalledWith(component.candidateEducation);
  });

  it('should handle update service error', () => {
    const errorResponse = new Error('Update failed');
    mockCandidateEducationService.update.and.returnValue(throwError(errorResponse));

    component.onSave();

    expect(mockCandidateEducationService.update).toHaveBeenCalled();
    expect(component.error).toBe(errorResponse);
    expect(component.saving).toBeFalse();
  });
});
