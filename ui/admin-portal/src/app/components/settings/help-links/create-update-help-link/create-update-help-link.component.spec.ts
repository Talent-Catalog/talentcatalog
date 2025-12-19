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

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {UntypedFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {of, throwError} from 'rxjs';
import {HelpLinkService} from '../../../../services/help-link.service';
import {CreateUpdateHelpLinkComponent} from './create-update-help-link.component';
import {CandidateOpportunityStage} from '../../../../model/candidate-opportunity';
import {Country} from "../../../../model/country";
import {JobOpportunityStage} from "../../../../model/job";
import {HelpFocus} from "../../../../model/help-link";
import {User} from "../../../../model/user";
import {MockUser} from "../../../../MockData/MockUser";
import {NgSelectModule} from "@ng-select/ng-select";

describe('CreateUpdateHelpLinkComponent', () => {
  let component: CreateUpdateHelpLinkComponent;
  let fixture: ComponentFixture<CreateUpdateHelpLinkComponent>;
  let mockHelpLinkService;
  let activeModal: NgbActiveModal;
  const mockUser = new MockUser();
  beforeEach(async () => {
    mockHelpLinkService = jasmine.createSpyObj(['create', 'update']);
    activeModal = jasmine.createSpyObj(['close', 'dismiss']);

    await TestBed.configureTestingModule({
      declarations: [CreateUpdateHelpLinkComponent],
      imports: [ReactiveFormsModule, NgSelectModule],
      providers: [
        UntypedFormBuilder,
        {provide: NgbActiveModal, useValue: activeModal},
        {provide: HelpLinkService, useValue: mockHelpLinkService}
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CreateUpdateHelpLinkComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with empty values when helpLink is not set', () => {
    component.ngOnInit();
    expect(component.form.value.label).toBeNull();
    expect(component.form.value.link).toBeNull();
  });

  it('should initialize form with values when helpLink is set', () => {
    component.helpLink = {
      id: 1,
      label: 'Sample Link',
      link: 'http://sample.com',
      country: {id: 1, name: 'Country A'} as Country,
      caseStage: CandidateOpportunityStage.candidateLeavesDestination,
      jobStage: JobOpportunityStage.jobOffer,
      focus: HelpFocus.updateNextStep,
      nextStepInfo: {
        nextStepName: 'Step 1',
        nextStepText: 'Do this',
        nextStepDays: 5
      },
      createdBy: mockUser,
      createdDate: new Date('10-10-2022'),
      updatedBy: mockUser,
      updatedDate: new Date('10-10-2023')
    };
    component.ngOnInit();

    expect(component.form.value.label).toBe('Sample Link');
    expect(component.form.value.link).toBe('http://sample.com');
    expect(component.form.value.countryId).toBe(1);
    expect(component.form.value.caseStage).toBe(CandidateOpportunityStage.candidateLeavesDestination);
    expect(component.form.value.nextStepName).toBe('Step 1');
  });

  it('should return true for create mode if helpLink is null', () => {
    component.helpLink = null;
    expect(component.create).toBeTrue();
  });

  it('should return false for create mode if helpLink is set', () => {
    component.helpLink = {id: 1} as any;
    expect(component.create).toBeFalse();
  });

  it('should call HelpLinkService.create when saving a new help link', () => {
    component.helpLink = null;
    mockHelpLinkService.create.and.returnValue(of({id: 1}));

    component.form.setValue({
      label: 'New Link',
      link: 'http://newlink.com',
      countryId: 1,
      caseStage: 'stage1',
      jobStage: 'stage2',
      focus: 'focus1',
      nextStepName: 'Next Step',
      nextStepText: 'Next Step Text',
      nextStepDays: 10
    });

    component.save();

    expect(mockHelpLinkService.create).toHaveBeenCalledWith({
      label: 'New Link',
      link: 'http://newlink.com',
      countryId: 1,
      caseStage: 'stage1',
      jobStage: 'stage2',
      focus: 'focus1',
      nextStepInfo: {
        nextStepName: 'Next Step',
        nextStepText: 'Next Step Text',
        nextStepDays: 10
      }
    });
    expect(activeModal.close).toHaveBeenCalled();
  });

  it('should call HelpLinkService.update when updating an existing help link', () => {
    component.helpLink = {id: 1} as any;
    mockHelpLinkService.update.and.returnValue(of({id: 1}));

    component.form.setValue({
      label: 'Updated Link',
      link: 'http://updatedlink.com',
      countryId: 2,
      caseStage: 'stage1',
      jobStage: 'stage2',
      focus: 'focus2',
      nextStepName: 'Updated Step',
      nextStepText: 'Updated Text',
      nextStepDays: 15
    });

    component.save();

    expect(mockHelpLinkService.update).toHaveBeenCalledWith(1, {
      label: 'Updated Link',
      link: 'http://updatedlink.com',
      countryId: 2,
      caseStage: 'stage1',
      jobStage: 'stage2',
      focus: 'focus2',
      nextStepInfo: {
        nextStepName: 'Updated Step',
        nextStepText: 'Updated Text',
        nextStepDays: 15
      }
    });
    expect(activeModal.close).toHaveBeenCalled();
  });

  it('should handle error on save', () => {
    mockHelpLinkService.create.and.returnValue(throwError('Error occurred'));
    component.helpLink = null;

    component.form.setValue({
      label: 'New Link',
      link: 'http://newlink.com',
      countryId: 1,
      caseStage: 'stage1',
      jobStage: 'stage2',
      focus: 'focus1',
      nextStepName: 'Next Step',
      nextStepText: 'Next Step Text',
      nextStepDays: 10
    });

    component.save();

    expect(component.error).toBe('Error occurred');
    expect(component.working).toBeFalse();
  });
});
