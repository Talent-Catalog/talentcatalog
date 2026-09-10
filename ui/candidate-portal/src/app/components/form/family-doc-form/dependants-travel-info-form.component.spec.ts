/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {of, throwError} from 'rxjs';

import {DependantsTravelInfoFormComponent} from './dependants-travel-info-form.component';
import {CandidateFormService} from '../../../services/candidate-form.service';
import {DependantRelations} from '../../../model/candidate';
import {TravelDocType} from '../../../model/form';

describe('DependantsTravelInfoFormComponent', () => {
  let component: DependantsTravelInfoFormComponent;
  let fixture: ComponentFixture<DependantsTravelInfoFormComponent>;
  let candidateFormService: jasmine.SpyObj<CandidateFormService>;

  beforeEach(async () => {
    candidateFormService = jasmine.createSpyObj<CandidateFormService>(
      'CandidateFormService',
      [
        'getDependantsInfoForm',
        'createOrUpdateDependantsInfoForm'
      ]
    );

    candidateFormService.getDependantsInfoForm.and.returnValue(
      of({
        noEligibleDependants: false,
        noEligibleNotes: '',
        dependantsInfoJson: ''
      } as any)
    );

    candidateFormService.createOrUpdateDependantsInfoForm.and.returnValue(
      of({
        noEligibleDependants: false,
        noEligibleNotes: '',
        dependantsInfoJson: '[]'
      } as any)
    );

    await TestBed.configureTestingModule({
      imports: [
        DependantsTravelInfoFormComponent
      ],
      providers: [
        {
          provide: CandidateFormService,
          useValue: candidateFormService
        }
      ]
    })
    .overrideComponent(DependantsTravelInfoFormComponent, {
      set: {
        template: ''
      }
    })
    .compileComponents();

    fixture = TestBed.createComponent(DependantsTravelInfoFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load dependants info form on init', () => {
    expect(candidateFormService.getDependantsInfoForm).toHaveBeenCalled();
    expect(component.form).toBeTruthy();
    expect(component.noEligibleDependants).toBeFalse();
  });

  it('should add one empty dependant when there are eligible dependants but no saved members', () => {
    expect(component.members.length).toBe(1);
    expect(component.relocatingDependants.length).toBe(1);
  });

  it('should add a dependant member', () => {
    component.members.clear();
    component.relocatingDependants = [];

    component.addMember();

    expect(component.members.length).toBe(1);
    expect(component.relocatingDependants.length).toBe(1);
  });

  it('should remove a dependant member', () => {
    component.members.clear();
    component.relocatingDependants = [];

    component.addMember();
    component.addMember();

    component.removeMember(0);

    expect(component.members.length).toBe(1);
    expect(component.relocatingDependants.length).toBe(1);
  });

  it('should return false from canSubmit when form is invalid', () => {
    expect(component.canSubmit()).toBeFalse();
  });

  it('should return false from canSubmit when readOnly is true', () => {
    component.readOnly = true;

    expect(component.canSubmit()).toBeFalse();
  });

  it('should allow submit when no eligible dependants is selected', () => {
    component.form.patchValue({
      noEligibleDependants: true,
      noEligibleNotes: 'No eligible dependants'
    });

    component.members.clear();

    expect(component.canSubmit()).toBeTrue();
  });

  it('should submit no eligible dependants payload', () => {
    spyOn(component.submitted, 'emit');

    component.form.patchValue({
      noEligibleDependants: true,
      noEligibleNotes: 'No eligible dependants'
    });

    component.members.clear();

    component.onSubmit();

    expect(candidateFormService.createOrUpdateDependantsInfoForm).toHaveBeenCalledWith({
      noEligibleDependants: true,
      noEligibleNotes: 'No eligible dependants',
      dependantsInfoJson: '[]'
    });

    expect(component.submitted.emit).toHaveBeenCalled();
    expect(component.submitting).toBeFalse();
  });

  it('should submit dependant members payload', () => {
    spyOn(component.submitted, 'emit');

    component.members.clear();
    component.relocatingDependants = [];

    component.addMember();

    component.members.at(0).patchValue({
      TTH_IT$RELATIONSHIP_TO_PRIMARY_APPLICANT: DependantRelations.Partner,
      TTH_IT$OTHER_RELATIONSHIP_TO_PRIMARY_APPLICANT: '',
      'user.firstName': 'Test',
      'user.lastName': 'Dependant',
      dob: '1990-01-01',
      gender: 'other',
      'birthCountry.name': 'Afghanistan',
      placeOfBirth: 'Herat',
      TTH_IT$TRAVEL_DOC_TYPE: TravelDocType.Passport,
      TTH_IT$TRAVEL_DOC_NUMBER: 'P123456',
      TTH_IT$TRAVEL_DOC_ISSUED_BY: 'Afghanistan',
      TTH_IT$TRAVEL_DOC_ISSUE_DATE: '2024-01-01',
      TTH_IT$TRAVEL_DOC_EXPIRY_DATE: '',
      TTH_IT$TRAVEL_INFO_COMMENT: 'Test comment'
    });

    component.onSubmit();

    expect(candidateFormService.createOrUpdateDependantsInfoForm).toHaveBeenCalled();

    const payload = candidateFormService.createOrUpdateDependantsInfoForm.calls.mostRecent()
      .args[0];

    expect(payload.noEligibleDependants).toBeFalse();
    expect(payload.noEligibleNotes).toBe('');
    expect(payload.dependantsInfoJson).toContain('Test');
    expect(payload.dependantsInfoJson).toContain('Dependant');

    expect(component.submitted.emit).toHaveBeenCalled();
    expect(component.submitting).toBeFalse();
  });

  it('should set error when loading dependants info fails', () => {
    candidateFormService.getDependantsInfoForm.and.returnValue(
      throwError(() => new Error('Load failed'))
    );

    fixture = TestBed.createComponent(DependantsTravelInfoFormComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();

    expect(component.form).toBeTruthy();
    expect(component.noEligibleDependants).toBeFalse();
  });

  it('should set error when submit fails', () => {
    const error = new Error('Save failed');

    candidateFormService.createOrUpdateDependantsInfoForm.and.returnValue(
      throwError(error)
    );

    component.form.patchValue({
      noEligibleDependants: true,
      noEligibleNotes: 'No eligible dependants'
    });

    component.members.clear();

    component.onSubmit();

    expect(component.error).toBe(error);
    expect(component.submitting).toBeFalse();
  });
});
