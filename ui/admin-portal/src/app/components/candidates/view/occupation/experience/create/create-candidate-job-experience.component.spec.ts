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
import {NgxWigModule} from "ngx-wig";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {CreateCandidateJobExperienceComponent} from "./create-candidate-job-experience.component";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CountryService} from "../../../../../../services/country.service";
import {
  CandidateJobExperienceService
} from "../../../../../../services/candidate-job-experience.service";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {of, throwError} from "rxjs";

describe('CreateCandidateJobExperienceComponent', () => {
  let component: CreateCandidateJobExperienceComponent;
  let fixture: ComponentFixture<CreateCandidateJobExperienceComponent>;
  let mockCandidateJobExperienceService: jasmine.SpyObj<CandidateJobExperienceService>;
  let mockCountryService: jasmine.SpyObj<CountryService>;
  let mockActiveModal: jasmine.SpyObj<NgbActiveModal>;

  beforeEach(async () => {
    const candidateJobExperienceServiceSpy = jasmine.createSpyObj('CandidateJobExperienceService', ['create']);
    const countryServiceSpy = jasmine.createSpyObj('CountryService', ['listCountries']);
    const activeModalSpy = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);

    await TestBed.configureTestingModule({
      declarations: [CreateCandidateJobExperienceComponent],
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule,NgxWigModule],
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

    fixture = TestBed.createComponent(CreateCandidateJobExperienceComponent);
    component = fixture.componentInstance;
  });

  beforeEach(() => {
    mockCountryService.listCountries.and.returnValue(of([]));

    component.candidateOccupationId = 1;
    component.candidateId = 1;
    component.ngOnInit();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with default values', () => {
    expect(component.candidateForm.value).toEqual({
      countryId: null,
      companyName: null,
      candidateOccupationId: component.candidateOccupationId,
      role: null,
      startDate: null,
      endDate: null,
      fullTime: null,
      paid: null,
      description: null
    });
  });

  it('should load countries on init', () => {
    expect(mockCountryService.listCountries).toHaveBeenCalled();
    expect(component.countries.length).toBe(0); // Since we returned an empty array in the mock
  });

  it('should save candidate job experience on valid form submission', () => {
    const mockJobExperience = { id: 1, role: 'Developer' } as any;
    mockCandidateJobExperienceService.create.and.returnValue(of(mockJobExperience));

    component.candidateForm.patchValue({
      countryId: 1,
      companyName: 'Company',
      role: 'Developer',
      startDate: new Date(),
      endDate: new Date(),
      fullTime: true,
      paid: true,
      description: 'Job description'
    });

    component.onSave();

    expect(component.saving).toBe(false);
    expect(mockCandidateJobExperienceService.create).toHaveBeenCalledWith(component.candidateId, component.candidateForm.value);
    expect(mockActiveModal.close).toHaveBeenCalledWith(mockJobExperience);
  });

  it('should handle save error', () => {
    const error = 'Error saving job experience';
    mockCandidateJobExperienceService.create.and.returnValue(throwError(error));

    component.candidateForm.patchValue({
      countryId: 1,
      companyName: 'Company',
      role: 'Developer',
      startDate: new Date(),
      endDate: new Date(),
      fullTime: true,
      paid: true,
      description: 'Job description'
    });

    component.onSave();

    expect(component.saving).toBe(false);
    expect(component.error).toBe(error);
    expect(mockActiveModal.close).not.toHaveBeenCalled();
  });
});
