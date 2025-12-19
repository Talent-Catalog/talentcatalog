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
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {UntypedFormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {CreateCandidateEducationComponent} from "./create-candidate-education.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {CountryService} from "../../../../../services/country.service";
import {EducationMajorService} from "../../../../../services/education-major.service";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {
  CandidateEducationService,
  CreateCandidateEducationRequest
} from "../../../../../services/candidate-education.service";
import {of, throwError} from "rxjs";
import {MockCandidate} from "../../../../../MockData/MockCandidate";

describe('CreateCandidateEducationComponent', () => {
  let component: CreateCandidateEducationComponent;
  let fixture: ComponentFixture<CreateCandidateEducationComponent>;
  let mockCandidateEducationService: jasmine.SpyObj<CandidateEducationService>;
  let mockCountryService: jasmine.SpyObj<CountryService>;
  let mockEducationMajorService: jasmine.SpyObj<EducationMajorService>;
  let mockActiveModal: jasmine.SpyObj<NgbActiveModal>;

  beforeEach(async () => {
    const candidateEducationServiceSpy = jasmine.createSpyObj('CandidateEducationService', ['create']);
    const countryServiceSpy = jasmine.createSpyObj('CountryService', ['listCountries']);
    const educationMajorServiceSpy = jasmine.createSpyObj('EducationMajorService', ['listMajors']);
    const activeModalSpy = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);

    await TestBed.configureTestingModule({
      declarations: [CreateCandidateEducationComponent],
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule],
      providers: [
        { provide: CandidateEducationService, useValue: candidateEducationServiceSpy },
        { provide: CountryService, useValue: countryServiceSpy },
        { provide: EducationMajorService, useValue: educationMajorServiceSpy },
        { provide: NgbActiveModal, useValue: activeModalSpy },
        UntypedFormBuilder
      ]
    })
    .compileComponents();

    mockCandidateEducationService = TestBed.inject(CandidateEducationService) as jasmine.SpyObj<CandidateEducationService>;
    mockCountryService = TestBed.inject(CountryService) as jasmine.SpyObj<CountryService>;
    mockEducationMajorService = TestBed.inject(EducationMajorService) as jasmine.SpyObj<EducationMajorService>;
    mockActiveModal = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateCandidateEducationComponent);
    component = fixture.componentInstance;
    mockCountryService.listCountries.and.returnValue(of([]));
    mockEducationMajorService.listMajors.and.returnValue(of([]));
    component.candidateId = 1;
    fixture.detectChanges();
  });

  it('should ', () => {
    expect(component).toBeTruthy();
  });

  it('should save candidate education details', () => {
    const mockEducation =  new MockCandidate().candidateEducations[0];


    mockCandidateEducationService.create.and.returnValue(of(mockEducation));

    component.candidateForm.setValue({
      courseName: 'CS101',
      institution: 'MIT',
      countryId: 1,
      educationMajorId: 1,
      yearCompleted: 2010,
      lengthOfCourseYears: 4,
      educationType: 'Bachelor',
      incomplete: false
    });

    component.onSave();

    expect(mockCandidateEducationService.create).toHaveBeenCalledWith(jasmine.objectContaining({
      candidateId: component.candidateId,
      courseName: 'CS101',
      institution: 'MIT',
      countryId: 1,
      educationMajorId: 1,
      yearCompleted: 2010,
      lengthOfCourseYears: 4,
      educationType: 'Bachelor',
      incomplete: false
    } as unknown as CreateCandidateEducationRequest));

    expect(mockActiveModal.close).toHaveBeenCalledWith(mockEducation);
    expect(component.saving).toBeFalse();
  });

  it('should handle error when saving candidate education details', () => {
    const error = 'Failed to save education details';
    mockCandidateEducationService.create.and.returnValue(throwError(error));

    component.candidateForm.setValue({
      courseName: 'CS101',
      institution: 'MIT',
      countryId: 1,
      educationMajorId: 1,
      yearCompleted: 2010,
      lengthOfCourseYears: 4,
      educationType: 'Bachelor',
      incomplete: false
    });

    component.onSave();

    expect(mockCandidateEducationService.create).toHaveBeenCalled();
    expect(mockActiveModal.close).not.toHaveBeenCalled();
    expect(component.error).toBe(error);
    expect(component.saving).toBeFalse();
  });
});
