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
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {MockJob} from "../../../../../../MockData/MockJob";
import {mockCandidateIntakeData} from "../../candidate-intake-tab/candidate-intake-tab.component.spec";
import {LocalStorageService} from "../../../../../../services/local-storage.service";
import {AuthorizationService} from "../../../../../../services/authorization.service";
import {ReadOnlyInputsDirective} from "../../../../../../directives/read-only-inputs.directive";
import {IntProtectionComponent} from "../../../../visa/int-protection/int-protection.component";
import {MockCandidateVisa} from "../../../../../../MockData/MockCandidateVisa";
import {DestinationFamilyComponent} from "../../../../visa/destination-family/destination-family.component";
import {HealthAssessmentComponent} from "../../../../visa/health-assessment/health-assessment.component";

describe('VisaCheckAuComponent', () => {
  let component: VisaCheckAuComponent;
  let fixture: ComponentFixture<VisaCheckAuComponent>;
  const mockCandidate = new MockCandidate();
  let authorizationServiceSpy: jasmine.SpyObj<AuthorizationService>;

  beforeEach(async () => {
    const authServiceSpyObj = jasmine.createSpyObj('AuthorizationService', ['isEditableCandidate']);
    TestBed.configureTestingModule({
      declarations: [ VisaCheckAuComponent, ReadOnlyInputsDirective, IntProtectionComponent, DestinationFamilyComponent, HealthAssessmentComponent ],
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule,NgbAccordionModule ,NgSelectModule],
      providers: [
        { provide: NgbModal, useValue: {} },
        { provide: LocalStorageService, useValue: {} },
        { provide: AuthorizationService, useValue: authServiceSpyObj },
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    });

    TestBed.overrideComponent(IntProtectionComponent, {
      set: {
        template: '<ng-select [class.readonly]="!isEditable()"></ng-select>'
      }
    });

    TestBed.compileComponents().then(() => {
      fixture = TestBed.createComponent(VisaCheckAuComponent);
      component = fixture.componentInstance;
      authorizationServiceSpy = TestBed.inject(AuthorizationService) as jasmine.SpyObj<AuthorizationService>;
    });
  });

  beforeEach(() => {

    // Initialize input properties
    component.candidate = mockCandidate;
    component.candidateIntakeData = {...mockCandidateIntakeData,candidateDestinations:[MockJob.country]}
    component.visaCheckRecord = MockCandidateVisa;

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

  it('should set inputs to read only if isEditable is false', () => {
    authorizationServiceSpy.isEditableCandidate.and.returnValue(false);
    component.isEditable()
    const compiled = fixture.nativeElement;
    const readOnly = compiled.querySelector('ng-select');
    expect(component.isEditable()).toBeFalse();
    expect(readOnly.classList.contains('read-only')).toBeTrue();
  });
});
