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

import {OpportunityStageNextStepComponent} from "./opportunity-stage-next-step.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {AuthorizationService} from "../../../services/authorization.service";
import {JobService} from "../../../services/job.service";
import {NgbModal, NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {CandidateOpportunityService} from "../../../services/candidate-opportunity.service";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {By} from "@angular/platform-browser";
import {mockCandidateOpportunity} from "../../../MockData/MockCandidateOpportunity";
import {OpportunityProgressParams} from "../../../model/opportunity";
import {EditOppComponent} from "../../opportunity/edit-opp/edit-opp.component";
import {of, throwError} from "rxjs";

describe('OpportunityStageNextStepComponent', () => {
  let component: OpportunityStageNextStepComponent;
  let fixture: ComponentFixture<OpportunityStageNextStepComponent>;
  let authServiceSpy: jasmine.SpyObj<AuthorizationService>;
  let candidateOpportunityServiceSpy: jasmine.SpyObj<CandidateOpportunityService>;
  let jobServiceSpy: jasmine.SpyObj<JobService>;
  let modalServiceSpy: jasmine.SpyObj<NgbModal>;

  beforeEach(async () => {
    const authSpy = jasmine.createSpyObj('AuthorizationService', ['canEditCandidateOpp', 'canChangeJobStage']);
    const candidateOppSpy = jasmine.createSpyObj('CandidateOpportunityService', ['updateCandidateOpportunity']);
    const jobSpy = jasmine.createSpyObj('JobService', ['update']);
    const modalSpy = jasmine.createSpyObj('NgbModal', ['open']);

    await TestBed.configureTestingModule({
      declarations: [OpportunityStageNextStepComponent],
      imports: [FormsModule, ReactiveFormsModule, NgbModule, HttpClientTestingModule],
      providers: [
        { provide: AuthorizationService, useValue: authSpy },
        { provide: CandidateOpportunityService, useValue: candidateOppSpy },
        { provide: JobService, useValue: jobSpy },
        { provide: NgbModal, useValue: modalSpy }
      ]
    }).compileComponents();

    authServiceSpy = TestBed.inject(AuthorizationService) as jasmine.SpyObj<AuthorizationService>;
    candidateOpportunityServiceSpy = TestBed.inject(CandidateOpportunityService) as jasmine.SpyObj<CandidateOpportunityService>;
    jobServiceSpy = TestBed.inject(JobService) as jasmine.SpyObj<JobService>;
    modalServiceSpy = TestBed.inject(NgbModal) as jasmine.SpyObj<NgbModal>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(OpportunityStageNextStepComponent);
    component = fixture.componentInstance;
    component.opp = mockCandidateOpportunity;
    fixture.detectChanges();
  });
  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display progress card when opp is provided', () => {
    component.opp = mockCandidateOpportunity;
    fixture.detectChanges();
    const card = fixture.debugElement.query(By.css('.card'));
    expect(card).toBeTruthy();
  });

  it('should display spinner when updating', () => {
    component.updating = true;
    authServiceSpy.canEditCandidateOpp.and.returnValue(true);
    fixture.detectChanges();
    const spinner = fixture.debugElement.query(By.css('.fa-spinner'));
    expect(spinner).toBeTruthy();
  });

  it('should display error message when error is set', () => {
    component.error = 'Some error';
    fixture.detectChanges();
    const errorElement = fixture.debugElement.query(By.css('.alert-danger'));
    expect(errorElement).toBeTruthy();
    expect(errorElement.nativeElement.textContent).toContain('Some error');
  });

  it('should call editOppProgress when edit button is clicked', () => {
    component.opp = mockCandidateOpportunity;
    spyOn(component, 'editOppProgress');
    component.updating = true;
    authServiceSpy.canEditCandidateOpp.and.returnValue(true);
    fixture.detectChanges();
    const button = fixture.debugElement.query(By.css('.btn-secondary'));
    button.nativeElement.click();
    expect(component.editOppProgress).toHaveBeenCalled();
  });

  it('should open EditOppComponent modal on editOppProgress', () => {
    component.opp = mockCandidateOpportunity;
    const modalRef = {
      componentInstance: { opp: null },
      result: Promise.resolve({} as OpportunityProgressParams)
    };
    modalServiceSpy.open.and.returnValue(modalRef as any);

    component.editOppProgress();
    expect(modalServiceSpy.open).toHaveBeenCalledWith(EditOppComponent, { size: 'lg' });
  });

  it('should update opportunity on successful modal result', async () => {
    component.opp = mockCandidateOpportunity;
    const modalRef = {
      componentInstance: { opp: null },
      result: Promise.resolve({} as OpportunityProgressParams)
    };
    modalServiceSpy.open.and.returnValue(modalRef as any);
    spyOn(component as any, 'doUpdate'); // Use 'any' type to access private method

    component.editOppProgress();
    await modalRef.result;

    expect((component as any).doUpdate).toHaveBeenCalledWith({});
  });

  it('should set updating and error on failed doUpdate', () => {
    component.opp = mockCandidateOpportunity;
    candidateOpportunityServiceSpy.updateCandidateOpportunity.and.returnValue(throwError('Error'));

    (component as any).doUpdate({} as OpportunityProgressParams);
    expect(component.updating).toBeFalse();
    expect(component.error).toBe('Error');
  });

  it('should emit oppProgressUpdated on successful update', () => {
    component.opp = mockCandidateOpportunity;
    const updatedOpp = mockCandidateOpportunity;
    updatedOpp.id=2;
    candidateOpportunityServiceSpy.updateCandidateOpportunity.and.returnValue(of(updatedOpp));
    spyOn(component.oppProgressUpdated, 'emit');

    (component as any).doUpdate({} as OpportunityProgressParams);
    expect(component.oppProgressUpdated.emit).toHaveBeenCalledWith(updatedOpp);
    expect(component.updating).toBeFalse();
  });

  it('should determine editable based on permissions', () => {
    component.opp = mockCandidateOpportunity
    authServiceSpy.canEditCandidateOpp.and.returnValue(true);
    authServiceSpy.canChangeJobStage.and.returnValue(false);

    expect(component.editable).toBeTrue();
  });
});
