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

import {EditCandidateCertificationComponent} from "./edit-candidate-certification.component";
import {ComponentFixture, TestBed, waitForAsync} from "@angular/core/testing";
import {CountryService} from "../../../../../services/country.service";
import {NgbActiveModal, NgbDatepickerModule} from "@ng-bootstrap/ng-bootstrap";
import {
  CandidateCertificationService
} from "../../../../../services/candidate-certification.service";
import {DatePickerComponent} from "../../../../util/date-picker/date-picker.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {of, throwError} from "rxjs";
import {CandidateCertification} from "../../../../../model/candidate-certification";

describe('EditCandidateCertificationComponent', () => {
  let component: EditCandidateCertificationComponent;
  let fixture: ComponentFixture<EditCandidateCertificationComponent>;
  let candidateCertificationServiceSpy: jasmine.SpyObj<CandidateCertificationService>;
  let countryServiceSpy: jasmine.SpyObj<CountryService>;
  let activeModalSpy: jasmine.SpyObj<NgbActiveModal>;

  beforeEach(waitForAsync(() => {
    const candidateCertificationSpy = jasmine.createSpyObj('CandidateCertificationService', ['update']);
    const countrySpy = jasmine.createSpyObj('CountryService', ['listCountries']);
    const activeModalMock = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);

    TestBed.configureTestingModule({
      declarations: [EditCandidateCertificationComponent, DatePickerComponent],
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule, NgbDatepickerModule],
      providers: [
        { provide: NgbActiveModal, useValue: activeModalMock },
        { provide: CandidateCertificationService, useValue: candidateCertificationSpy },
        { provide: CountryService, useValue: countrySpy }
      ]
    }).compileComponents();

    candidateCertificationServiceSpy = TestBed.inject(CandidateCertificationService) as jasmine.SpyObj<CandidateCertificationService>;
    countryServiceSpy = TestBed.inject(CountryService) as jasmine.SpyObj<CountryService>;
    activeModalSpy = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EditCandidateCertificationComponent);
    component = fixture.componentInstance;
    component.candidateCertification = { id: 1, name: 'Course Name', institution: 'Institution', dateCompleted: '2024-05-29' } as CandidateCertification;
    countryServiceSpy.listCountries.and.returnValue(of([]));
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the form with candidateCertification data', () => {
    expect(component.candidateForm).toBeDefined();
    expect(component.candidateForm.controls['name'].value).toBe('Course Name');
    expect(component.candidateForm.controls['institution'].value).toBe('Institution');
    expect(component.candidateForm.controls['dateCompleted'].value).toBe('2024-05-29');
  });

  it('form should be valid when filled correctly', () => {
    expect(component.candidateForm.valid).toBeTruthy();
  });

  it('should call the update method on the service when form is valid and submitted', () => {
    const mockCertification = { id: 1, name: 'Course Name', institution: 'Institution', dateCompleted: '2024-05-29' };
    candidateCertificationServiceSpy.update.and.returnValue(of(mockCertification));

    component.onSave();

    expect(candidateCertificationServiceSpy.update.calls.count()).toBe(1);
    expect(activeModalSpy.close.calls.count()).toBe(1);
  });

  it('should handle errors on form submission', () => {
    const mockError = 'An error occurred';
    candidateCertificationServiceSpy.update.and.returnValue(throwError(mockError));

    component.onSave();

    expect(component.error).toBe(mockError);
    expect(candidateCertificationServiceSpy.update.calls.count()).toBe(1);
    expect(activeModalSpy.close.calls.count()).toBe(0);
  });

  it('should dismiss the modal without saving changes', () => {
    component.dismiss();
    expect(activeModalSpy.dismiss.calls.count()).toBe(1);
  });
});
