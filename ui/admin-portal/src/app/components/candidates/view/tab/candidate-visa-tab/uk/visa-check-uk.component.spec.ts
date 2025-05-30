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
import {MockCandidate} from "../../../../../../MockData/MockCandidate";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {mockCandidateIntakeData} from "../../candidate-intake-tab/candidate-intake-tab.component.spec";
import {By} from '@angular/platform-browser';
import {VisaCheckUkComponent} from "./visa-check-uk.component";
import {MockCandidateVisa} from "../../../../../../MockData/MockCandidateVisa";
import {MockCandidateVisaJobCheck} from "../../../../../../MockData/MockCandidateVisaCheck";
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {NgbAccordionModule, NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {NgSelectModule} from "@ng-select/ng-select";
import {AutosaveStatusComponent} from "../../../../../util/autosave-status/autosave-status.component";
import {RouterLinkStubDirective} from "../../../../../login/login.component.spec";
import {DependantsComponent} from "../../../../intake/dependants/dependants.component";
import {Component, CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";
import {AuthorizationService} from "../../../../../../services/authorization.service";
import {LocalStorageService} from "../../../../../../services/local-storage.service";
import {ReadOnlyInputsDirective} from "../../../../../../directives/read-only-inputs.directive";

// Mock IntProtectionComponent to include input elements for testing
@Component({
  selector: 'app-destination-family',
  template: `
    <ng-select></ng-select>
    <input type="text"/>
    <textarea></textarea>
    <app-date-picker></app-date-picker>
    <ngx-wig></ngx-wig>
  `
})
class MockDestinationFamilyComponent {
  // No isEditable method; inputs are controlled by parent directive
}

describe('VisaCheckUkComponent', () => {
  let component: VisaCheckUkComponent;
  let fixture: ComponentFixture<VisaCheckUkComponent>;
  let authorizationServiceSpy: jasmine.SpyObj<AuthorizationService>;
  const mockCandidate = new MockCandidate();

  beforeEach(async () => {
    const authServiceSpyObj = jasmine.createSpyObj('AuthorizationService', ['isEditableCandidate']);

    await TestBed.configureTestingModule({
      declarations: [
        VisaCheckUkComponent,
        AutosaveStatusComponent,
        ReadOnlyInputsDirective,
        MockDestinationFamilyComponent,
        RouterLinkStubDirective,
        DependantsComponent],
      imports: [NgSelectModule,FormsModule,ReactiveFormsModule,HttpClientTestingModule,NgbAccordionModule],
      providers: [UntypedFormBuilder,
        {provide: NgbModal, useValue: {}},
        {provide: LocalStorageService, useValue: {}},
        {provide: AuthorizationService, useValue: authServiceSpyObj}],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaCheckUkComponent);
    component = fixture.componentInstance;
    authorizationServiceSpy = TestBed.inject(AuthorizationService) as jasmine.SpyObj<AuthorizationService>;

    // Assign mock data to component inputs
    component.candidate = mockCandidate;
    component.candidateIntakeData = {...mockCandidateIntakeData};
    component.visaCheckRecord = {...MockCandidateVisa};
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should default select the first job in the array on init', () => {
    component.ngOnInit();
    expect(component.selectedJob).toEqual(MockCandidateVisaJobCheck);
  });
  it('should pass the correct inputs to app-candidate-visa-job component', () => {
    const candidateVisaJobComponent = fixture.debugElement.query(By.css('app-candidate-visa-job'));
    expect(candidateVisaJobComponent).toBeTruthy();
    expect(candidateVisaJobComponent.componentInstance.candidate).toEqual(mockCandidate);
    expect(candidateVisaJobComponent.componentInstance.candidateIntakeData).toEqual(mockCandidateIntakeData);
    expect(candidateVisaJobComponent.componentInstance.visaCheckRecord).toEqual(MockCandidateVisa);
    expect(candidateVisaJobComponent.componentInstance.selectedJob).toEqual(MockCandidateVisaJobCheck);
  });

  it('should render app-visa-job-check-uk component for the selected job', () => {
    component.ngOnInit();
    fixture.detectChanges();

    const visaJobCheckUkComponent = fixture.debugElement.query(By.css('app-visa-job-check-uk'));
    expect(visaJobCheckUkComponent).toBeTruthy();
    expect(visaJobCheckUkComponent.componentInstance.candidate).toEqual(mockCandidate);
    expect(visaJobCheckUkComponent.componentInstance.candidateIntakeData).toEqual(mockCandidateIntakeData);
    expect(visaJobCheckUkComponent.componentInstance.visaCheckRecord).toEqual(MockCandidateVisa);
  });


  it('should not render app-visa-job-check-uk component for non-selected jobs', () => {
    const otherJobCheck = { id: 2, jobTitle: 'Data Scientist' } as any;
    component.visaCheckRecord.candidateVisaJobChecks.push(otherJobCheck);
    component.ngOnInit();
    fixture.detectChanges();

    const visaJobCheckUkComponents = fixture.debugElement.queryAll(By.css('app-visa-job-check-uk'));
    expect(visaJobCheckUkComponents.length).toBeLessThanOrEqual(2);
  });

  it('should set inputs to read only if isEditable is false', (done) => {
    authorizationServiceSpy.isEditableCandidate.and.returnValue(false);
    component.isEditable();
    fixture.detectChanges();

    // Wait for ReadOnlyInputsDirective's setTimeout
    setTimeout(() => {
      // Query inputs within the ngb-accordion where the directive is applied
      const accordion = fixture.debugElement.query(By.css('ngb-accordion'));
      const inputElements = accordion.queryAll(
        By.css('ng-select, input, textarea, app-date-picker, ngx-wig')
      );

      inputElements.forEach((element) => {
        expect(element.nativeElement.hasAttribute('disabled')).toBeTrue();
      });

      const ngSelect = accordion.query(By.css('ng-select'));
      const ngxWig = accordion.query(By.css('ngx-wig'));
      expect(ngSelect.nativeElement.classList.contains('read-only')).toBeTrue();
      expect(ngxWig.nativeElement.classList.contains('read-only')).toBeTrue();
      expect(component.isEditable()).toBeFalse();
      done();
    }, 0);
  });
});
