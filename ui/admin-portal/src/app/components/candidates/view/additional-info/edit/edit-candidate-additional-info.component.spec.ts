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
import {EditCandidateAdditionalInfoComponent} from "./edit-candidate-additional-info.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {CandidateService} from "../../../../../services/candidate.service";
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {Candidate} from "../../../../../model/candidate";
import {MockCandidate} from "../../../../../MockData/MockCandidate";
import {of, throwError} from "rxjs";
import {CreatedByComponent} from "../../../../util/user/created-by/created-by.component";

describe('EditCandidateAdditionalInfoComponent', () => {
  let component: EditCandidateAdditionalInfoComponent;
  let fixture: ComponentFixture<EditCandidateAdditionalInfoComponent>;
  let candidateServiceSpy: jasmine.SpyObj<CandidateService>;
  let fb: UntypedFormBuilder;

  const mockCandidate: Candidate = new MockCandidate();

  beforeEach(async () => {
    const candidateServiceSpyObj = jasmine.createSpyObj('CandidateService', ['get', 'updateInfo']);

    await TestBed.configureTestingModule({
      declarations: [EditCandidateAdditionalInfoComponent, CreatedByComponent],
      imports: [HttpClientTestingModule, FormsModule, ReactiveFormsModule],
      providers: [
        UntypedFormBuilder,
        NgbActiveModal,
        { provide: CandidateService, useValue: candidateServiceSpyObj }
      ]
    }).compileComponents();
    fb = TestBed.inject(UntypedFormBuilder); // Inject FormBuilder
    candidateServiceSpy = TestBed.inject(CandidateService) as jasmine.SpyObj<CandidateService>;
    candidateServiceSpy.get.and.returnValue(of());
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EditCandidateAdditionalInfoComponent);
    component = fixture.componentInstance;
    component.candidateId = 1; // Set a mock candidate id

    // Initialize the form
    component.candidateForm = fb.group({
      additionalInfo: [mockCandidate.additionalInfo]
    });
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with loading state', () => {
    expect(component.loading).toBeTrue();
  });

  it('should fetch candidate data on init', () => {
    const candidate: Candidate = mockCandidate;
    candidateServiceSpy.get.and.returnValue(of(candidate));

    fixture.detectChanges();

    expect(component.candidateForm.value.additionalInfo).toEqual(candidate.additionalInfo);
    expect(candidateServiceSpy.get).toHaveBeenCalledWith(component.candidateId);
  });

  it('should handle error when fetching candidate data', () => {
    const errorMessage = 'Error fetching candidate data';
    candidateServiceSpy.updateInfo.and.returnValue(throwError(errorMessage)); // Use returnValue instead of and.throwError

    component.onSave();

    expect(component.error).toEqual(errorMessage);
  });
});
