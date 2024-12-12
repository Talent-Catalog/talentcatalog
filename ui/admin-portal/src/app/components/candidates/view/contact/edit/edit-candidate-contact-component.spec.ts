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
import {EditCandidateContactComponent} from "./edit-candidate-contact.component";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateService} from "../../../../../services/candidate.service";
import {CountryService} from "../../../../../services/country.service";
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {ComponentFixture, TestBed, waitForAsync} from "@angular/core/testing";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {of} from "rxjs";
import {MockCandidate} from "../../../../../MockData/MockCandidate";
import {Candidate} from "../../../../../model/candidate";

describe('EditCandidateContactComponent', () => {
  let component: EditCandidateContactComponent;
  let fixture: ComponentFixture<EditCandidateContactComponent>;
  let activeModalSpy: jasmine.SpyObj<NgbActiveModal>;
  let candidateServiceSpy: jasmine.SpyObj<CandidateService>;
  let countryServiceSpy: jasmine.SpyObj<CountryService>;
  let formBuilder: UntypedFormBuilder;

  beforeEach(waitForAsync(() => {
    const activeModalSpyObj = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);
    const candidateServiceSpyObj = jasmine.createSpyObj('CandidateService', ['get', 'update']);
    const countryServiceSpyObj = jasmine.createSpyObj('CountryService', ['listCountries']);

    TestBed.configureTestingModule({
      declarations: [EditCandidateContactComponent],
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule],
      providers: [
        { provide: NgbActiveModal, useValue: activeModalSpyObj },
        { provide: CandidateService, useValue: candidateServiceSpyObj },
        { provide: CountryService, useValue: countryServiceSpyObj },
        UntypedFormBuilder,
      ],
    }).compileComponents();

    activeModalSpy = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;
    candidateServiceSpy = TestBed.inject(CandidateService) as jasmine.SpyObj<CandidateService>;
    countryServiceSpy = TestBed.inject(CountryService) as jasmine.SpyObj<CountryService>;
    formBuilder = TestBed.inject(UntypedFormBuilder);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EditCandidateContactComponent);
    component = fixture.componentInstance;
    component.candidateId = 1; // Mock candidate ID
    countryServiceSpy.listCountries.and.returnValue(of([]));
    candidateServiceSpy.get.and.returnValue(of());
    // component.candidateForm = formBuilder.group(new MockCandidate());
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with candidate details', () => {
    // Mock candidate data
    const mockCandidate: Candidate = new MockCandidate();
    // Mock the candidate service to return the mock candidate
    candidateServiceSpy.get.and.returnValue(of(mockCandidate));

    // Trigger ngOnInit
    component.ngOnInit();
    // Expect the form controls to be initialized with the candidate details
    expect(component.loading).toBeFalsy();
    expect(component.error).toBeUndefined();
    expect(component.candidateForm.value).toEqual({
      firstName: mockCandidate.user.firstName,
      lastName: mockCandidate.user.lastName,
      gender: mockCandidate.gender,
      address1: mockCandidate.address1,
      city: mockCandidate.city,
      state: mockCandidate.state,
      countryId: mockCandidate.country.id,
      yearOfArrival: mockCandidate.yearOfArrival,
      phone: mockCandidate.phone,
      whatsapp: mockCandidate.whatsapp,
      email: mockCandidate.user.email,
      dob: mockCandidate.dob,
      nationalityId: mockCandidate.nationality.id,
    });  });
});
