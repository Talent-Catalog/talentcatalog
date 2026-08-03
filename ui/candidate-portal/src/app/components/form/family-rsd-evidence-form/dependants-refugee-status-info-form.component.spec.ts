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

import {FormBuilder} from '@angular/forms';
import {of, throwError} from 'rxjs';

import {DependantsInfoFormData, RelocatingDependant} from '../../../model/form';
import {CandidateFormService} from '../../../services/candidate-form.service';
import {
  DependantsRefugeeStatusInfoFormComponent
} from './dependants-refugee-status-info-form.component';

describe('DependantsRefugeeStatusInfoFormComponent', () => {
  let component:
    DependantsRefugeeStatusInfoFormComponent;
  let candidateFormServiceSpy:
    jasmine.SpyObj<CandidateFormService>;

  const emptyFormData: DependantsInfoFormData = {
    noEligibleDependants: false,
    noEligibleNotes: '',
    dependantsInfoJson: '[]'
  };

  beforeEach(() => {
    candidateFormServiceSpy =
      jasmine.createSpyObj<CandidateFormService>(
        'CandidateFormService',
        [
          'getDependantsInfoForm',
          'createOrUpdateDependantsInfoForm'
        ]
      );

    candidateFormServiceSpy
    .getDependantsInfoForm
    .and.returnValue(of(emptyFormData));

    candidateFormServiceSpy
    .createOrUpdateDependantsInfoForm
    .and.returnValue(of(emptyFormData));

    component = createComponent();
    component.ngOnInit();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the form and load dependant information', () => {
    expect(component.form).toBeTruthy();
    expect(component.members.length).toBe(0);
    expect(component.loadingMembers).toBeFalse();

    expect(
      candidateFormServiceSpy.getDependantsInfoForm
    ).toHaveBeenCalledTimes(1);
  });

  it('should expose the members FormArray', () => {
    expect(component.members)
    .toBe(component.form.get('members') as any);
  });

  describe('noEligibleDependants', () => {
    it('should default to true before data is loaded', () => {
      (component as any).dependantsInfoFormData =
        undefined;

      expect(component.noEligibleDependants)
      .toBeTrue();
    });

    it('should return the value from loaded data', () => {
      (component as any).dependantsInfoFormData = {
        ...emptyFormData,
        noEligibleDependants: false
      };

      expect(component.noEligibleDependants)
      .toBeFalse();

      (component as any).dependantsInfoFormData = {
        ...emptyFormData,
        noEligibleDependants: true
      };

      expect(component.noEligibleDependants)
      .toBeTrue();
    });
  });

  describe('canSubmit', () => {
    it('should return true for a valid editable form', () => {
      expect(component.form.valid).toBeTrue();
      expect(component.canSubmit()).toBeTrue();
    });

    it('should return false while submitting', () => {
      component.submitting = true;

      expect(component.canSubmit()).toBeFalse();
    });

    it('should return false in read-only mode', () => {
      component.readOnly = true;

      expect(component.canSubmit()).toBeFalse();
    });

    it('should return false when the form is invalid', () => {
      component.form.setErrors({
        invalid: true
      });

      expect(component.canSubmit()).toBeFalse();
    });

    it('should return false while validation is pending', () => {
      component.form.markAsPending();

      expect(component.canSubmit()).toBeFalse();
    });
  });

  describe('loading data', () => {
    it('should create a form group for every dependant', () => {
      const formData = createDependantsFormData([
        createDependant({
          'user.firstName': 'Ehsan',
          'user.lastName': 'Ehrari'
        }),
        createDependant({
          'user.firstName': 'Second',
          'user.lastName': 'Member'
        })
      ]);

      candidateFormServiceSpy
      .getDependantsInfoForm
      .and.returnValue(of(formData));

      component = createComponent();
      component.ngOnInit();

      expect(component.relocatingDependants.length)
      .toBe(2);

      expect(component.members.length).toBe(2);

      const firstGroup = component.members.at(0);
      const secondGroup = component.members.at(1);

      expect(
        firstGroup.controls['user.firstName'].value
      ).toBe('Ehsan');

      expect(
        firstGroup.controls['user.lastName'].value
      ).toBe('Ehrari');

      expect(
        secondGroup.controls['user.firstName'].value
      ).toBe('Second');

      expect(
        secondGroup.controls['user.lastName'].value
      ).toBe('Member');

      expect(component.loadingMembers).toBeFalse();
    });

    it('should create controls with empty defaults', () => {
      const formData = createDependantsFormData([
        {} as RelocatingDependant
      ]);

      candidateFormServiceSpy
      .getDependantsInfoForm
      .and.returnValue(of(formData));

      component = createComponent();
      component.ngOnInit();

      const group = component.members.at(0);

      expect(
        group.controls['user.firstName'].value
      ).toBe('');

      expect(
        group.controls['user.lastName'].value
      ).toBe('');

      expect(
        group.controls['dob'].value
      ).toBe('');

      expect(
        group.controls[
          'TTH_IT$REFUGEE_STATUS'
          ].value
      ).toBe('');

      expect(
        group.controls[
          'TTH_IT$REFUGEE_STATUS_EVIDENCE_DOCUMENT_TYPE'
          ].value
      ).toBe('');

      expect(
        group.controls[
          'TTH_IT$REFUGEE_STATUS_EVIDENCE_DOCUMENT_NUMBER'
          ].value
      ).toBe('');

      expect(
        group.controls[
          'TTH_IT$REFUGEE_STATUS_COMMENT'
          ].value
      ).toBe('');

      expect(group.invalid).toBeTrue();
    });

    it('should apply required validators', () => {
      const formData = createDependantsFormData([
        createDependant({
          TTH_IT$REFUGEE_STATUS: '',
          TTH_IT$REFUGEE_STATUS_EVIDENCE_DOCUMENT_TYPE:
            ''
        })
      ]);

      candidateFormServiceSpy
      .getDependantsInfoForm
      .and.returnValue(of(formData));

      component = createComponent();
      component.ngOnInit();

      const group = component.members.at(0);

      expect(
        group.controls[
          'TTH_IT$REFUGEE_STATUS'
          ].hasError('required')
      ).toBeTrue();

      expect(
        group.controls[
          'TTH_IT$REFUGEE_STATUS_EVIDENCE_DOCUMENT_TYPE'
          ].hasError('required')
      ).toBeTrue();
    });

    it('should enforce the document-number maximum length', () => {
      const formData = createDependantsFormData([
        createDependant({
          TTH_IT$REFUGEE_STATUS_EVIDENCE_DOCUMENT_NUMBER:
            '1'.repeat(31)
        })
      ]);

      candidateFormServiceSpy
      .getDependantsInfoForm
      .and.returnValue(of(formData));

      component = createComponent();
      component.ngOnInit();

      expect(
        component.members.at(0).controls[
          'TTH_IT$REFUGEE_STATUS_EVIDENCE_DOCUMENT_NUMBER'
          ].hasError('maxlength')
      ).toBeTrue();
    });

    it('should leave members empty when JSON is missing', () => {
      candidateFormServiceSpy
      .getDependantsInfoForm
      .and.returnValue(
        of({
          noEligibleDependants: false,
          noEligibleNotes: '',
          dependantsInfoJson: ''
        })
      );

      component = createComponent();
      component.ngOnInit();

      expect(component.members.length).toBe(0);
      expect(component.relocatingDependants)
      .toEqual([]);
      expect(component.loadingMembers).toBeFalse();
    });

    it('should handle invalid dependant JSON', () => {
      const consoleSpy = spyOn(console, 'error');

      candidateFormServiceSpy
      .getDependantsInfoForm
      .and.returnValue(
        of({
          noEligibleDependants: false,
          noEligibleNotes: '',
          dependantsInfoJson: 'invalid JSON'
        })
      );

      component = createComponent();

      expect(() => component.ngOnInit())
      .not.toThrow();

      expect(consoleSpy).toHaveBeenCalledOnceWith(
        'Failed to parse relocating family members JSON'
      );

      expect(component.members.length).toBe(0);
      expect(component.loadingMembers).toBeFalse();
    });

    it('should handle loading errors', () => {
      const error = new Error(
        'Unable to load dependants'
      );

      candidateFormServiceSpy
      .getDependantsInfoForm
      .and.returnValue(
        throwError(error)
      );

      component = createComponent();
      component.ngOnInit();

      expect(component.error).toBe(error);
      expect(component.loadingMembers).toBeFalse();
    });
  });

  describe('automatic submission', () => {
    it('should automatically submit when there are no eligible dependants', () => {
      const formData: DependantsInfoFormData = {
        noEligibleDependants: true,
        noEligibleNotes: 'No eligible dependants',
        dependantsInfoJson: '[]'
      };

      const savedData = {
        ...formData
      };

      candidateFormServiceSpy
      .getDependantsInfoForm
      .and.returnValue(of(formData));

      candidateFormServiceSpy
      .createOrUpdateDependantsInfoForm
      .and.returnValue(of(savedData));

      component = createComponent();

      const submittedSpy = spyOn(
        component.submitted,
        'emit'
      );

      component.ngOnInit();

      expect(
        candidateFormServiceSpy
          .createOrUpdateDependantsInfoForm
      ).toHaveBeenCalledOnceWith(formData);

      expect(submittedSpy)
      .toHaveBeenCalledOnceWith(savedData);

      expect(component.submitting).toBeFalse();
      expect(component.error).toBeNull();
      expect(component.form.pristine).toBeTrue();
      expect(component.form.untouched).toBeTrue();
      expect(component.loadingMembers).toBeFalse();
    });

    it('should handle automatic submission errors', () => {
      const formData: DependantsInfoFormData = {
        noEligibleDependants: true,
        noEligibleNotes: '',
        dependantsInfoJson: '[]'
      };

      const error = new Error(
        'Automatic submission failed'
      );

      candidateFormServiceSpy
      .getDependantsInfoForm
      .and.returnValue(of(formData));

      candidateFormServiceSpy
      .createOrUpdateDependantsInfoForm
      .and.returnValue(
        throwError(error)
      );

      component = createComponent();
      component.ngOnInit();

      expect(component.error).toBe(error);
      expect(component.submitting).toBeFalse();
      expect(component.loadingMembers).toBeFalse();
    });
  });

  describe('onSubmit', () => {
    it('should mark invalid member controls as touched', () => {
      const formData = createDependantsFormData([
        createDependant({
          TTH_IT$REFUGEE_STATUS: '',
          TTH_IT$REFUGEE_STATUS_EVIDENCE_DOCUMENT_TYPE:
            ''
        })
      ]);

      candidateFormServiceSpy
      .getDependantsInfoForm
      .and.returnValue(of(formData));

      component = createComponent();
      component.ngOnInit();

      component.onSubmit();

      const group = component.members.at(0);

      expect(group.touched).toBeTrue();

      expect(
        group.controls[
          'TTH_IT$REFUGEE_STATUS'
          ].touched
      ).toBeTrue();

      expect(
        candidateFormServiceSpy
          .createOrUpdateDependantsInfoForm
      ).not.toHaveBeenCalled();
    });

    it('should update and submit dependant information', () => {
      const originalDependant = createDependant({
        'user.firstName': 'Original',
        'user.lastName': 'Name',
        TTH_IT$RELATIONSHIP_TO_PRIMARY_APPLICANT:
          'Child' as any
      });

      const formData = createDependantsFormData([
        originalDependant
      ]);

      candidateFormServiceSpy
      .getDependantsInfoForm
      .and.returnValue(of(formData));

      component = createComponent();
      component.ngOnInit();

      component.members.at(0).patchValue({
        'user.firstName': 'Updated',
        'user.lastName': 'Member',
        TTH_IT$REFUGEE_STATUS: 'PENDING',
        TTH_IT$REFUGEE_STATUS_EVIDENCE_DOCUMENT_TYPE:
          'UNHCR_CERTIFICATE',
        TTH_IT$REFUGEE_STATUS_EVIDENCE_DOCUMENT_NUMBER:
          'DOC-123',
        TTH_IT$REFUGEE_STATUS_COMMENT:
          'Updated comment'
      });

      component.form.markAsDirty();
      component.form.markAllAsTouched();

      let submittedPayload:
        DependantsInfoFormData;

      candidateFormServiceSpy
      .createOrUpdateDependantsInfoForm
      .and.callFake((payload) => {
        submittedPayload = payload;
        return of(payload);
      });

      const submittedSpy = spyOn(
        component.submitted,
        'emit'
      );

      component.onSubmit();

      expect(
        submittedPayload!.noEligibleDependants
      ).toBeFalse();

      expect(
        submittedPayload!.noEligibleNotes
      ).toBe('');

      const submittedDependants =
        JSON.parse(
          submittedPayload!.dependantsInfoJson
        ) as RelocatingDependant[];

      expect(submittedDependants.length).toBe(1);

      expect(
        submittedDependants[0]['user.firstName']
      ).toBe('Updated');

      expect(
        submittedDependants[0]['user.lastName']
      ).toBe('Member');

      expect(
        submittedDependants[0]
          .TTH_IT$RELATIONSHIP_TO_PRIMARY_APPLICANT
      ).toBe('Child');

      expect(submittedSpy)
      .toHaveBeenCalledOnceWith(
        submittedPayload!
      );

      expect(component.submitting).toBeFalse();
      expect(component.error).toBeNull();
      expect(component.form.pristine).toBeTrue();
      expect(component.form.untouched).toBeTrue();
    });

    it('should use fallback payload values when stored data is unavailable', () => {
      const dependant = createDependant();

      candidateFormServiceSpy
      .getDependantsInfoForm
      .and.returnValue(
        of(createDependantsFormData([dependant]))
      );

      component = createComponent();
      component.ngOnInit();

      (component as any).dependantsInfoFormData =
        undefined;

      let submittedPayload:
        DependantsInfoFormData;

      candidateFormServiceSpy
      .createOrUpdateDependantsInfoForm
      .and.callFake((payload) => {
        submittedPayload = payload;
        return of(payload);
      });

      component.onSubmit();

      expect(
        submittedPayload!.noEligibleDependants
      ).toBeTrue();

      expect(
        submittedPayload!.noEligibleNotes
      ).toBe('');
    });

    it('should handle submission errors', () => {
      const formData = createDependantsFormData([
        createDependant()
      ]);

      const error = new Error(
        'Submission failed'
      );

      candidateFormServiceSpy
      .getDependantsInfoForm
      .and.returnValue(of(formData));

      candidateFormServiceSpy
      .createOrUpdateDependantsInfoForm
      .and.returnValue(
        throwError(error)
      );

      component = createComponent();
      component.ngOnInit();

      component.onSubmit();

      expect(component.error).toBe(error);
      expect(component.submitting).toBeFalse();
    });
  });

  describe('composeDisplayName', () => {
    it('should compose and trim first and last names', () => {
      const member = createDependant({
        'user.firstName': '  Ehsan ',
        'user.lastName': ' Ehrari  '
      });

      expect(component.composeDisplayName(member))
      .toBe('Ehsan Ehrari');
    });

    it('should return only the available name', () => {
      expect(
        component.composeDisplayName(
          createDependant({
            'user.firstName': 'Ehsan',
            'user.lastName': ''
          })
        )
      ).toBe('Ehsan');

      expect(
        component.composeDisplayName(
          createDependant({
            'user.firstName': '',
            'user.lastName': 'Ehrari'
          })
        )
      ).toBe('Ehrari');
    });

    it('should fall back to the relationship', () => {
      const member = createDependant({
        'user.firstName': '',
        'user.lastName': '',
        TTH_IT$RELATIONSHIP_TO_PRIMARY_APPLICANT:
          'Child' as any
      });

      expect(component.composeDisplayName(member))
      .toBe('Child');
    });

    it('should return Unnamed Member when no name or relationship exists', () => {
      const member = createDependant({
        'user.firstName': '',
        'user.lastName': '',
        TTH_IT$RELATIONSHIP_TO_PRIMARY_APPLICANT:
        undefined
      });

      expect(component.composeDisplayName(member))
      .toBe('Unnamed Member');
    });
  });

  function createComponent():
    DependantsRefugeeStatusInfoFormComponent {
    return new DependantsRefugeeStatusInfoFormComponent(
      new FormBuilder(),
      candidateFormServiceSpy
    );
  }
});

function createDependantsFormData(
  dependants: RelocatingDependant[]
): DependantsInfoFormData {
  return {
    noEligibleDependants: false,
    noEligibleNotes: '',
    dependantsInfoJson: JSON.stringify(dependants)
  };
}

function createDependant(
  overrides: Partial<RelocatingDependant> = {}
): RelocatingDependant {
  return {
    'user.firstName': 'First',
    'user.lastName': 'Member',
    dob: '2000-01-01',
    TTH_IT$REFUGEE_STATUS: 'PENDING',
    TTH_IT$REFUGEE_STATUS_EVIDENCE_DOCUMENT_TYPE:
      'UNHCR_CERTIFICATE',
    TTH_IT$REFUGEE_STATUS_EVIDENCE_DOCUMENT_NUMBER:
      'DOC-001',
    TTH_IT$REFUGEE_STATUS_COMMENT: '',
    ...overrides
  };
}
