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
import {EditCandidateRegistrationComponent} from "./edit-candidate-registration.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {CandidateService} from "../../../../../services/candidate.service";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {UntypedFormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {NgxWigModule} from "ngx-wig";
import {NgbActiveModal, NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {CountryService} from "../../../../../services/country.service";
import {MockCandidate} from "../../../../../MockData/MockCandidate";
import {of} from "rxjs";
import {UnhcrStatus, YesNo, YesNoUnsure} from "../../../../../model/candidate";

describe('EditCandidateRegistrationComponent', () => {
  let component: EditCandidateRegistrationComponent;
  let fixture: ComponentFixture<EditCandidateRegistrationComponent>;
  let mockCandidateService: jasmine.SpyObj<CandidateService>;
  let modalService: jasmine.SpyObj<NgbActiveModal>;

  const mockCandidate = new MockCandidate();
  beforeEach(async () => {
    mockCandidateService = jasmine.createSpyObj('CandidateService', ['get', 'updateRegistration']);
    const modalServiceSpy = jasmine.createSpyObj('NgbActiveModal', ['open','close']);

    await TestBed.configureTestingModule({
      declarations: [EditCandidateRegistrationComponent],
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule,NgxWigModule],
      providers: [
        UntypedFormBuilder,
        { provide: NgbActiveModal, useValue: modalServiceSpy },
        { provide: CandidateService, useValue: mockCandidateService },
        { provide: CountryService, useValue: {} }
      ]
    }).compileComponents();
    modalService = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;

  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EditCandidateRegistrationComponent);
    component = fixture.componentInstance;

    mockCandidateService.get.and.returnValue(of(mockCandidate));

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch candidate details on init', () => {
    const candidateData = mockCandidate;

    mockCandidateService.get.and.returnValue(of(candidateData));

    component.ngOnInit();

    expect(mockCandidateService.get).toHaveBeenCalled();
    expect(component.candidateForm.value).toEqual({
      externalId: 'ABC123',
      externalIdSource: 'Source',
      partnerRef: 'Ref',
      unhcrNumber: 'UNHCR123',
      unhcrStatus: UnhcrStatus.RegisteredAsylum,
      unhcrConsent: YesNo.Yes,
      unrwaRegistered: YesNoUnsure.No,
      unrwaNumber: '123'
    });
  });

  it('should update candidate registration on save', () => {
    const updatedCandidateData = mockCandidate;

    mockCandidateService.updateRegistration.and.returnValue(of(updatedCandidateData));

    component.candidateId = 1;
    component.candidateForm.setValue({
      externalId: '456',
      externalIdSource: 'newSource',
      partnerRef: 'newPartner456',
      unhcrNumber: 'UNHCR456',
      unhcrStatus: 'pending',
      unhcrConsent: 'no',
      unrwaRegistered: 'no',
      unrwaNumber: 'UNRWA456'
    });

    component.onSave();

    expect(mockCandidateService.updateRegistration).toHaveBeenCalledWith(1, component.candidateForm.value);
    expect(component.saving).toBe(false);
  });
});
