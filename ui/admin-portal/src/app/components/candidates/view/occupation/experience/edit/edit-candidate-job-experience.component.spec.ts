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

import {EditCandidateJobExperienceComponent} from "./edit-candidate-job-experience.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {CountryService} from "../../../../../../services/country.service";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {
  CandidateJobExperienceService
} from "../../../../../../services/candidate-job-experience.service";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {NgxWigModule} from "ngx-wig";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {of, throwError} from "rxjs";
import {MockJob} from "../../../../../../MockData/MockJob";
import {MockCandidate} from "../../../../../../MockData/MockCandidate";
import {CandidateJobExperience} from "../../../../../../model/candidate-job-experience";

describe('EditCandidateJobExperienceComponent', () => {
  let component: EditCandidateJobExperienceComponent;
  let fixture: ComponentFixture<EditCandidateJobExperienceComponent>;
  let mockCandidateJobExperienceService: jasmine.SpyObj<CandidateJobExperienceService>;
  let mockCountryService: jasmine.SpyObj<CountryService>;
  let mockActiveModal: jasmine.SpyObj<NgbActiveModal>;

  const mockCandidate = new MockCandidate();
  beforeEach(async () => {
    const candidateJobExperienceServiceSpy = jasmine.createSpyObj('CandidateJobExperienceService', ['update']);
    const countryServiceSpy = jasmine.createSpyObj('CountryService', ['listCountries']);
    const activeModalSpy = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);

    await TestBed.configureTestingModule({
      declarations: [EditCandidateJobExperienceComponent],
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule,
        NgxWigModule],
      providers: [
        UntypedFormBuilder,
        { provide: CandidateJobExperienceService, useValue: candidateJobExperienceServiceSpy },
        { provide: CountryService, useValue: countryServiceSpy },
        { provide: NgbActiveModal, useValue: activeModalSpy }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    mockCandidateJobExperienceService = TestBed.inject(CandidateJobExperienceService) as jasmine.SpyObj<CandidateJobExperienceService>;
    mockCountryService = TestBed.inject(CountryService) as jasmine.SpyObj<CountryService>;
    mockActiveModal = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;

    fixture = TestBed.createComponent(EditCandidateJobExperienceComponent);
    component = fixture.componentInstance;
  });

  beforeEach(() => {
    component.candidate = mockCandidate;
    component.candidateJobExperience = mockCandidate.candidateJobExperiences[0];
    mockCountryService.listCountries.and.returnValue(of([MockJob.country]));

    component.ngOnInit();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with provided job experience values', () => {
    expect(component.candidateForm.value).toEqual({
      countryId: component.candidateJobExperience.country.id,
      companyName: component.candidateJobExperience.companyName,
      role: component.candidateJobExperience.role,
      startDate: component.candidateJobExperience.startDate,
      endDate: component.candidateJobExperience.endDate,
      fullTime: component.candidateJobExperience.fullTime,
      paid: component.candidateJobExperience.paid,
      description: component.candidateJobExperience.description,
      tidiedDescription: null,
      keywordsInDescription: null
    });
  });

  it('should save the edited job experience successfully', () => {
    const updatedJobExperience = { ...component.candidateJobExperience, role: 'Senior Developer' };
    mockCandidateJobExperienceService.update.and.returnValue(of(updatedJobExperience));

    component.candidateForm.patchValue({
      role: 'Senior Developer'
    });

    component.onSave();

    expect(component.saving).toBe(false);
    expect(mockCandidateJobExperienceService.update).
    toHaveBeenCalledWith(component.candidateJobExperience.id, component.candidateForm.value);
    expect(mockActiveModal.close).toHaveBeenCalledWith(updatedJobExperience);
  });

  it('should handle save error', () => {
    const error = 'Error saving job experience';
    mockCandidateJobExperienceService.update.and.returnValue(throwError(error));

    component.candidateForm.patchValue({
      role: 'Senior Developer'
    });

    component.onSave();

    expect(component.saving).toBe(false);
    expect(component.error).toBe(error);
    expect(mockActiveModal.close).not.toHaveBeenCalled();
  });

  describe('keywordsInDescription processing', () => {

    function withKeywords(keywordsInDescription: any): CandidateJobExperience {
      return {
        ...mockCandidate.candidateJobExperiences[0],
        keywordsInDescription
      };
    }

    describe('ngOnInit', () => {

      it('should join a populated keywordsInDescription array into a comma-separated string', () => {
        component.candidateJobExperience = withKeywords(['Java', 'Spring', 'SQL']);

        component.ngOnInit();

        expect(component.candidateForm.value.keywordsInDescription).toBe('Java, Spring, SQL');
      });

      it('should leave keywordsInDescription as null when it is undefined', () => {
        component.candidateJobExperience = withKeywords(undefined);

        component.ngOnInit();

        expect(component.candidateForm.value.keywordsInDescription).toBeNull();
      });

      it('should leave keywordsInDescription as null when it is null', () => {
        component.candidateJobExperience = withKeywords(null);

        component.ngOnInit();

        expect(component.candidateForm.value.keywordsInDescription).toBeNull();
      });

      it('should leave keywordsInDescription as null when it is not an array', () => {
        component.candidateJobExperience = withKeywords('not-an-array');

        component.ngOnInit();

        expect(component.candidateForm.value.keywordsInDescription).toBeNull();
      });

      it('should produce an empty string when keywordsInDescription is an empty array', () => {
        component.candidateJobExperience = withKeywords([]);

        component.ngOnInit();

        expect(component.candidateForm.value.keywordsInDescription).toBe('');
      });
    });

    describe('onSave', () => {

      it('should convert a comma-separated keywordsInDescription string back into a trimmed array', () => {
        const updatedJobExperience = { ...component.candidateJobExperience };
        mockCandidateJobExperienceService.update.and.returnValue(of(updatedJobExperience));

        component.candidateForm.patchValue({
          keywordsInDescription: 'Java, Spring , SQL'
        });

        component.onSave();

        expect(mockCandidateJobExperienceService.update).toHaveBeenCalledWith(
          component.candidateJobExperience.id,
          jasmine.objectContaining({keywordsInDescription: ['Java', 'Spring', 'SQL']})
        );
      });

      it('should leave keywordsInDescription unchanged when it is null', () => {
        const updatedJobExperience = { ...component.candidateJobExperience };
        mockCandidateJobExperienceService.update.and.returnValue(of(updatedJobExperience));

        component.candidateForm.patchValue({
          keywordsInDescription: null
        });

        component.onSave();

        expect(mockCandidateJobExperienceService.update).toHaveBeenCalledWith(
          component.candidateJobExperience.id,
          jasmine.objectContaining({keywordsInDescription: null})
        );
      });

      it('should leave keywordsInDescription unchanged when it is an empty string', () => {
        const updatedJobExperience = { ...component.candidateJobExperience };
        mockCandidateJobExperienceService.update.and.returnValue(of(updatedJobExperience));

        component.candidateForm.patchValue({
          keywordsInDescription: ''
        });

        component.onSave();

        expect(mockCandidateJobExperienceService.update).toHaveBeenCalledWith(
          component.candidateJobExperience.id,
          jasmine.objectContaining({keywordsInDescription: ''})
        );
      });
    });
  });
});
