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
import {VisaCheckCaComponent} from "./visa-check-ca.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {Component, CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";
import {MockCandidate} from "../../../../../../MockData/MockCandidate";
import {mockCandidateIntakeData} from "../../candidate-intake-tab/candidate-intake-tab.component.spec";
import {CandidateVisa, CandidateVisaJobCheck} from "../../../../../../model/candidate";
import {NgbAccordionModule, NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {By} from "@angular/platform-browser";
import {AuthorizationService} from "../../../../../../services/authorization.service";
import {ReadOnlyInputsDirective} from "../../../../../../directives/read-only-inputs.directive";
import {LocalStorageService} from "../../../../../../services/local-storage.service";

// Mock IntProtectionComponent to include input elements for testing
@Component({
  selector: 'app-int-protection',
  template: `
    <ng-select></ng-select>
    <input type="text"/>
    <textarea></textarea>
    <app-date-picker></app-date-picker>
    <ngx-wig></ngx-wig>
  `
})
class MockIntProtectionComponent {
  // No isEditable method; inputs are controlled by parent directive
}

describe('VisaCheckCaComponent', () => {
  let component: VisaCheckCaComponent;
  let fixture: ComponentFixture<VisaCheckCaComponent>;
  let authorizationServiceSpy: jasmine.SpyObj<AuthorizationService>;
  const mockCandidate = new MockCandidate();

  beforeEach(async () => {
    const authServiceSpyObj = jasmine.createSpyObj('AuthorizationService', ['isEditableCandidate']);
    await TestBed.configureTestingModule({
      imports: [NgbAccordionModule, HttpClientTestingModule],
      declarations: [VisaCheckCaComponent, ReadOnlyInputsDirective, MockIntProtectionComponent],
      providers: [
        {provide: NgbModal, useValue: {}},
        {provide: LocalStorageService, useValue: {}},
        {provide: AuthorizationService, useValue: authServiceSpyObj}
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaCheckCaComponent);
    component = fixture.componentInstance;
    authorizationServiceSpy = TestBed.inject(AuthorizationService) as jasmine.SpyObj<AuthorizationService>;

    component.candidate = mockCandidate;
    component.candidateIntakeData = mockCandidateIntakeData;
    component.visaCheckRecord = {
      candidateVisaJobChecks: [
        { id: 1, occupation: { id: 1, name: 'Job 1' } } as CandidateVisaJobCheck,
        { id: 2, occupation: { id: 2, name: 'Job 2' } } as CandidateVisaJobCheck
      ]
    } as CandidateVisa;

    fixture.detectChanges(); // trigger data binding
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should select the first job by default', () => {
    expect(component.selectedJob).toEqual(component.visaCheckRecord.candidateVisaJobChecks[0]);
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
