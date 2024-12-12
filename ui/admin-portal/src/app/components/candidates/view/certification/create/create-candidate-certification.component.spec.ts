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
import {ComponentFixture, TestBed, waitForAsync} from "@angular/core/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {of, throwError} from "rxjs";
import {CreateCandidateCertificationComponent} from "./create-candidate-certification.component";
import {
  CandidateCertificationService
} from "../../../../../services/candidate-certification.service";
import {CountryService} from "../../../../../services/country.service";
import {NgbActiveModal, NgbDatepickerModule} from "@ng-bootstrap/ng-bootstrap";
import {DatePickerComponent} from "../../../../util/date-picker/date-picker.component";

describe('CreateCandidateCertificationComponent', () => {
  let component: CreateCandidateCertificationComponent;
  let fixture: ComponentFixture<CreateCandidateCertificationComponent>;
  let candidateCertificationServiceSpy: jasmine.SpyObj<CandidateCertificationService>;
  let countryServiceSpy: jasmine.SpyObj<CountryService>;

  beforeEach(waitForAsync(() => {
    const candidateCertificationSpy = jasmine.createSpyObj('CandidateCertificationService', ['create']);
    const countrySpy = jasmine.createSpyObj('CountryService', ['listCountries']);

    TestBed.configureTestingModule({
      declarations: [CreateCandidateCertificationComponent,DatePickerComponent],
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule, NgbDatepickerModule],
      providers: [
        NgbActiveModal,
        { provide: CandidateCertificationService, useValue: candidateCertificationSpy },
        { provide: CountryService, useValue: countrySpy }
      ]
    }).compileComponents();

    candidateCertificationServiceSpy = TestBed.inject(CandidateCertificationService) as jasmine.SpyObj<CandidateCertificationService>;
    countryServiceSpy = TestBed.inject(CountryService) as jasmine.SpyObj<CountryService>;
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateCandidateCertificationComponent);
    component = fixture.componentInstance;
    countryServiceSpy.listCountries.and.returnValue(of([]));
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the form', () => {
    expect(component.candidateForm).toBeDefined();
    expect(component.candidateForm.controls['name']).toBeDefined();
    expect(component.candidateForm.controls['institution']).toBeDefined();
    expect(component.candidateForm.controls['dateCompleted']).toBeDefined();
  });

  it('form should be invalid when empty', () => {
    expect(component.candidateForm.valid).toBeFalsy();
  });

  it('form should be valid when filled', () => {
    component.candidateForm.controls['name'].setValue('Course Name');
    component.candidateForm.controls['institution'].setValue('Institution');
    component.candidateForm.controls['dateCompleted'].setValue('2024-05-29');
    expect(component.candidateForm.valid).toBeTruthy();
  });

  it('should call the create method on the service when form is valid and submitted', () => {
    const mockCertification = { id: 1, name: 'Course Name', institution: 'Institution', dateCompleted: '2024-05-29' };
    candidateCertificationServiceSpy.create.and.returnValue(of(mockCertification));

    component.candidateId = 1;
    component.candidateForm.controls['name'].setValue('Course Name');
    component.candidateForm.controls['institution'].setValue('Institution');
    component.candidateForm.controls['dateCompleted'].setValue('2024-05-29');

    component.onSave();

    expect(candidateCertificationServiceSpy.create.calls.count()).toBe(1);
  });

  it('should handle errors on form submission', () => {
    const mockError = 'An error occurred';
    candidateCertificationServiceSpy.create.and.returnValue(throwError(mockError));

    component.candidateId = 1;
    component.candidateForm.controls['name'].setValue('Course Name');
    component.candidateForm.controls['institution'].setValue('Institution');
    component.candidateForm.controls['dateCompleted'].setValue('2024-05-29');

    component.onSave();

    expect(component.error).toBe(mockError);
    expect(candidateCertificationServiceSpy.create.calls.count()).toBe(1);
  });
});
