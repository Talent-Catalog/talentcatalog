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
import {VisaCheckAuComponent} from "./visa-check-au.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {NgbAccordionModule, NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";
import {MockCandidate} from "../../../../../../MockData/MockCandidate";
import {CandidateVisa, CandidateVisaJobCheck} from "../../../../../../model/candidate";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {MockJob} from "../../../../../../MockData/MockJob";
import {mockCandidateIntakeData} from "../../candidate-intake-tab/candidate-intake-tab.component.spec";
import {LocalStorageService} from "../../../../../../services/local-storage.service";
import {AuthorizationService} from "../../../../../../services/authorization.service";

describe('VisaCheckAuComponent', () => {
  let component: VisaCheckAuComponent;
  let fixture: ComponentFixture<VisaCheckAuComponent>;
  const mockCandidate = new MockCandidate();
  let authServiceMock: jasmine.SpyObj<AuthorizationService>;

  beforeEach(async () => {
    authServiceMock = jasmine.createSpyObj('AuthorizationService', ['isEditableCandidate']);
    await TestBed.configureTestingModule({
      declarations: [ VisaCheckAuComponent ],
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule,NgbAccordionModule ,NgSelectModule],
      providers: [
        { provide: NgbModal, useValue: {} },
        { provide: LocalStorageService, useValue: {} },
        { provide: AuthorizationService, useValue: authServiceMock },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaCheckAuComponent);
    component = fixture.componentInstance;

    // Initialize input properties
    component.candidate = mockCandidate;
    component.candidateIntakeData = {...mockCandidateIntakeData,candidateDestinations:[MockJob.country]}
    component.visaCheckRecord = {
      candidateVisaJobChecks: [
        { id: 1 } as CandidateVisaJobCheck,
        { id: 2 } as CandidateVisaJobCheck
      ]
    } as CandidateVisa;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize currentYear and birthYear correctly', () => {
    const currentYear = new Date().getFullYear().toString();
    expect(component.currentYear).toBe(currentYear);
    expect(component.birthYear).toBe('Mon ');
  });

  it('should select the first job by default', () => {
    expect(component.selectedJob).toBe(component.visaCheckRecord.candidateVisaJobChecks[0]);
  });

});
