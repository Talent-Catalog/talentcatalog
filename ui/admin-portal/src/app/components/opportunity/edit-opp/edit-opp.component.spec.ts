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

import {EditOppComponent} from "./edit-opp.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {NgbActiveModal, NgbDatepickerModule, NgbTooltipModule} from "@ng-bootstrap/ng-bootstrap";
import {SavedListService} from "../../../services/saved-list.service";
import {
  CandidateSourceCandidateService
} from "../../../services/candidate-source-candidate.service";
import {JobService} from "../../../services/job.service";
import {OpportunityProgressParams} from "../../../model/opportunity";
import {UntypedFormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {of, throwError} from "rxjs";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {RouterTestingModule} from "@angular/router/testing";
import {DatePickerComponent} from "../../util/date-picker/date-picker.component";
import {NgSelectModule} from "@ng-select/ng-select";
import {HelpComponent} from "../../help/help.component";
import {MockUser} from "../../../MockData/MockUser";
import {
  CandidateOpportunity,
  CandidateOpportunityStage
} from "../../../model/candidate-opportunity";
import {MockCandidate} from "../../../MockData/MockCandidate";
import {MockJob} from "../../../MockData/MockJob";
import {ShortJob} from "../../../model/job";
import {MockSavedList} from "../../../MockData/MockSavedList";

describe('EditOppComponent', () => {
  let component: EditOppComponent;
  let fixture: ComponentFixture<EditOppComponent>;
  let modalService: NgbActiveModal;
  let savedListServiceSpy: jasmine.SpyObj<SavedListService>;
  let candidateSourceCandidateServiceSpy: jasmine.SpyObj<CandidateSourceCandidateService>;
  let jobServiceSpy: jasmine.SpyObj<JobService>;


  // Mock data for CandidateOpportunity
  const mockCandidateOpportunity: CandidateOpportunity = {
    id: 1,
    closed: false,
    name: 'Mock Candidate Opportunity',
    nextStep: 'Complete the next step',
    nextStepDueDate: new Date('2099-06-30'),
    won: false,
    candidate: new MockCandidate(),
    jobOpp: MockJob as ShortJob,
    stage: CandidateOpportunityStage.cvReview,
    closingCommentsForCandidate: 'Candidate closing comments',
    employerFeedback: 'Employer feedback',
    fileOfferLink: 'https://example.com/offer.pdf',
    fileOfferName: 'Offer Letter.pdf',
    createdBy: new MockUser(),
    createdDate: new Date('2023-01-01'),
    updatedBy: new MockUser(),
    updatedDate: new Date('2023-01-02')
  };
  beforeEach(async () => {
    const modalSpy = jasmine.createSpyObj('NgbActiveModal', ['dismiss', 'close']);
    const savedListService = jasmine.createSpyObj('SavedListService', ['get']);
    const candidateSourceCandidateService = jasmine.createSpyObj('CandidateSourceCandidateService', ['search']);
    const jobService = jasmine.createSpyObj('JobService', ['update']);

    await TestBed.configureTestingModule({
      declarations: [ EditOppComponent, DatePickerComponent, HelpComponent ],
      imports: [HttpClientTestingModule,NgSelectModule,RouterTestingModule,NgbTooltipModule,NgbDatepickerModule,ReactiveFormsModule,FormsModule],

      providers: [
        UntypedFormBuilder,
        { provide: NgbActiveModal, useValue: modalSpy },
        { provide: SavedListService, useValue: savedListService },
        { provide: CandidateSourceCandidateService, useValue: candidateSourceCandidateService },
        { provide: JobService, useValue: jobService }
      ]
    }).compileComponents();

    modalService = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;
    savedListServiceSpy = TestBed.inject(SavedListService) as jasmine.SpyObj<SavedListService>;
    candidateSourceCandidateServiceSpy = TestBed.inject(CandidateSourceCandidateService) as jasmine.SpyObj<CandidateSourceCandidateService>;
    jobServiceSpy = TestBed.inject(JobService) as jasmine.SpyObj<JobService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EditOppComponent);
    component = fixture.componentInstance;
    component.opp = mockCandidateOpportunity;
    savedListServiceSpy.get.and.returnValue(of(MockSavedList));
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize Salesforce stage form with correct values for Candidate Opportunity', () => {
    component.ngOnInit();
    expect(component.salesforceStageForm.value.stage).toBe(CandidateOpportunityStage.cvReview);
    expect(component.salesforceStageForm.value.nextStep).toBe('Complete the next step');
    expect(component.salesforceStageForm.value.nextStepDueDate.getDate()).toBe(new Date('2099-06-30').getDate());
    expect(component.salesforceStageForm.value.copyToParentJob).toBeFalse();
    expect(component.oppType).toBe('Candidate Opportunity');
    expect(component.stageHelpRequest).toEqual({ caseOppId: 1, caseStage: '5. CV review' });
  });

  it('should dismiss modal on cancel', () => {
    component.cancel();
    expect(modalService.dismiss).toHaveBeenCalledWith(false);
  });

  it('should close modal with opportunity progress params on save', () => {
    const info: OpportunityProgressParams = {
      stage: 'New Stage',
      nextStep: 'New Step',
      nextStepDueDate: '2099-07-01'
    };
    component.salesforceStageForm.patchValue({
      stage: 'New Stage',
      nextStep: 'New Step',
      nextStepDueDate: '2099-07-01'
    });
    component.onSave();
    expect(jobServiceSpy.update).not.toHaveBeenCalled(); // since copyToParentJob is false
    expect(modalService.close).toHaveBeenCalledWith(info);
  });

  it('should update parent job and close modal on save when copyToParentJob is true', () => {
    const info: OpportunityProgressParams = {
      nextStep: 'New Step',
      nextStepDueDate: '2099-07-01'
    };
    component.salesforceStageForm.patchValue({
      nextStep: 'New Step',
      nextStepDueDate: '2099-07-01',
      copyToParentJob: true
    });
    jobServiceSpy.update.and.returnValue(of(MockJob));
    component.onSave();
    expect(jobServiceSpy.update).toHaveBeenCalledWith(1, info); // assuming the parent job id is 1
    expect(modalService.close).toHaveBeenCalledWith({
      stage: '5. CV review', // assuming the current stage is not changed
      nextStep: 'New Step',
      nextStepDueDate: '2099-07-01'
    });
  });

  it('should handle error in checkOpenCases', () => {
    savedListServiceSpy.get.and.returnValue(of(MockSavedList));
    candidateSourceCandidateServiceSpy.search.and.returnValue(throwError('Error'));
    component.checkOpenCases(mockCandidateOpportunity);
    expect(component.error).toBe('Error');
  });

  it('should set isOnlyOpenCaseOfParentJob to true if there is only one open case of parent job', () => {
    const mockResult = [new MockCandidate()];
    savedListServiceSpy.get.and.returnValue(of(MockSavedList));
    candidateSourceCandidateServiceSpy.search.and.returnValue(of(mockResult));
    component.checkOpenCases(mockCandidateOpportunity);
    expect(component.isOnlyOpenCaseOfParentJob).toBeTrue();
  });

  it('should set isOnlyOpenCaseOfParentJob to false if there are multiple open cases of parent job', () => {
    const mockResult = [new MockCandidate(), new MockCandidate()];
    savedListServiceSpy.get.and.returnValue(of(MockSavedList));
    candidateSourceCandidateServiceSpy.search.and.returnValue(of(mockResult));
    component.checkOpenCases(mockCandidateOpportunity); // cast to any for simplicity in testing
    expect(component.isOnlyOpenCaseOfParentJob).toBeFalse();
  });

  it('should set isOnlyOpenCaseOfParentJob to false if the opportunity is closed', () => {
    const closedOpportunity = { ...mockCandidateOpportunity, closed: true };
    component.checkOpenCases(closedOpportunity as any); // cast to any for simplicity in testing
    expect(component.isOnlyOpenCaseOfParentJob).toBeUndefined();
  });

  it('should update stageHelpRequest on stage selection change for Candidate Opportunity', () => {
    component.onStageSelectionChange({ key: 'NewStage' });
    expect(component.stageHelpRequest).toEqual({ caseOppId: 1, caseStage: 'NewStage' });
  });

});

