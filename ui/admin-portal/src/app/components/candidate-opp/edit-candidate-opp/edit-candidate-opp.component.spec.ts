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

import {EditCandidateOppComponent} from "./edit-candidate-opp.component";
import {NgbActiveModal, NgbDatepickerModule, NgbTooltipModule} from "@ng-bootstrap/ng-bootstrap";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {UntypedFormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {CandidateOpportunity} from "../../../model/candidate-opportunity";
import {mockCandidateOpportunity} from "../../../MockData/MockCandidateOpportunity";
import {HelpComponent} from "../../help/help.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {CommonModule} from "@angular/common";
import {DatePickerComponent} from "../../util/date-picker/date-picker.component";
import {NgxWigComponent, NgxWigModule} from "ngx-wig";
import {NgSelectModule} from "@ng-select/ng-select";

describe('EditCandidateOppComponent', () => {
  let component: EditCandidateOppComponent;
  let fixture: ComponentFixture<EditCandidateOppComponent>;
  let mockActiveModal: jasmine.SpyObj<NgbActiveModal>;

  beforeEach(async () => {
    const modalSpy = jasmine.createSpyObj('NgbActiveModal', ['dismiss', 'close']);

    await TestBed.configureTestingModule({
      declarations: [EditCandidateOppComponent,HelpComponent,DatePickerComponent],
      imports: [HttpClientTestingModule,ReactiveFormsModule,FormsModule,CommonModule,NgbDatepickerModule,NgxWigModule,NgSelectModule,NgbTooltipModule],
      providers: [
        UntypedFormBuilder,
        { provide: NgbActiveModal, useValue: modalSpy }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EditCandidateOppComponent);
    component = fixture.componentInstance;
    mockActiveModal = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;
    fixture.detectChanges();
  });
  it('should create', () => {
    expect(component).toBeTruthy();
  });
  it('should initialize the form with default values based on the provided opportunity data', () => {
    const opportunity: CandidateOpportunity = mockCandidateOpportunity;

    component.opp = opportunity;
    component.ngOnInit();

    expect(component.salesforceStageForm.value).toEqual({
      stage: opportunity.stage,
      nextStep: opportunity.nextStep,
      nextStepDueDate: opportunity.nextStepDueDate,
      closingComments: opportunity.closingComments,
      closingCommentsForCandidate: opportunity.closingCommentsForCandidate,
      employerFeedback: opportunity.employerFeedback
    });
  });
  //
  it('should mark the form as valid when form fields are filled with valid data', () => {
    // Set values for all form controls
    component.salesforceStageForm.setValue({
      stage: 'ExampleStage',
      nextStep: 'ExampleNextStep',
      nextStepDueDate: '2024-05-15',
      closingComments: 'ExampleClosingComments',
      closingCommentsForCandidate: 'ExampleClosingCommentsForCandidate',
      employerFeedback: 'ExampleEmployerFeedback',
    });
    expect(component.salesforceStageForm.valid).toBe(true);
  });

  it('should dismiss the modal without saving any changes when cancel button is clicked', () => {
    component.cancel();
    expect(mockActiveModal.dismiss).toHaveBeenCalledWith(false);
  });

  it('should close the modal with form values when onSave is called', () => {
    component.salesforceStageForm.setValue({
      stage: 'Interview',
      nextStep: 'Send feedback',
      nextStepDueDate: '2024-06-01',
      closingComments: 'All went well.',
      closingCommentsForCandidate: 'You did great!',
      employerFeedback: 'Positive response from employer.'
    });

    component.onSave();

    expect(mockActiveModal.close).toHaveBeenCalledWith({
      stage: 'Interview',
      nextStep: 'Send feedback',
      nextStepDueDate: '2024-06-01',
      closingComments: 'All went well.',
      closingCommentsForCandidate: 'You did great!',
      employerFeedback: 'Positive response from employer.'
    });
  });

  it('should update stageHelpRequest when stage selection changes', () => {
    const stage = { key: 'Closed_Hired' };
    component.onStageSelectionChange(stage);
    expect(component.stageHelpRequest).toEqual({ caseStage: 'Closed_Hired' });
  });

  it('should filter stage options to only closed ones if closing is true', () => {
    component.opp = mockCandidateOpportunity;
    component.closing = true;
    component.ngOnInit();

    const allStagesClosed = component.candidateOpportunityStageOptions.every(opt => opt.stringValue.startsWith('Closed'));
    expect(allStagesClosed).toBeTrue();
  });

  it('form control getters should return correct values', () => {
    const formValues = {
      stage: 'Screening',
      nextStep: 'Schedule call',
      nextStepDueDate: '2025-01-01',
      closingComments: 'Test comment',
      closingCommentsForCandidate: 'Candidate comment',
      employerFeedback: 'Great'
    };

    component.salesforceStageForm.setValue(formValues);

    expect(component.stage).toBe(formValues.stage);
    expect(component.nextStep).toBe(formValues.nextStep);
    expect(component.nextStepDueDate).toBe(formValues.nextStepDueDate);
    expect(component.closingComments).toBe(formValues.closingComments);
    expect(component.closingCommentsForCandidate).toBe(formValues.closingCommentsForCandidate);
    expect(component.employerFeedback).toBe(formValues.employerFeedback);
  });


});
