/*
 * Copyright (c) 2025 Talent Catalog.
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

// profile-match.validator.ts
import {AbstractControl, FormGroup, ValidationErrors, ValidatorFn} from '@angular/forms';

/**
 * Returns a validator that checks whether specified form controls match
 * corresponding fields on a candidate object. The fieldMap keys are
 * form control names, and the values are dot‑notation paths into the
 * candidate object (e.g. 'user.firstName' or 'dob').
 *
 * If a mismatch is found, the validator sets a `mismatch` error on the form
 * group containing per‑field flags, and also adds a `mismatch` error on the
 * individual controls so you can style them accordingly.
 */
export function profileMatchValidator(
  candidate: any,
  fieldMap: { [controlName: string]: string },
): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    const group = control as FormGroup;
    const mismatch: any = {};

    const getCandidateValue = (path: string): any =>
      path.split('.').reduce((obj, key) => (obj ? obj[key] : undefined), candidate);

    const toComparable = (val: any): string => {
      if (val === null || val === undefined) return '';
      // if already a string, trim and lowercase
      if (typeof val === 'string') return val.trim().toLowerCase();
      // if it's an object with a name prop, use that
      if (typeof val === 'object' && 'name' in val) {
        return String((val as any).id).trim().toLowerCase();
      }
      // otherwise, stringify
      return String(val).trim().toLowerCase();
    };

    // Clear previous mismatch errors
    Object.keys(fieldMap).forEach((field) => {
      const ctrl = group.get(field);
      if (ctrl && ctrl.errors?.['mismatch']) {
        const { mismatch, ...others } = ctrl.errors;
        ctrl.setErrors(Object.keys(others).length ? others : null);
      }
    });

    // Compare each mapped field
    Object.keys(fieldMap).forEach((field) => {
      const ctrl = group.get(field);
      const candidateVal = getCandidateValue(fieldMap[field]);
      const enteredVal = ctrl?.value;
      const normCandidate = toComparable(candidateVal);
      const normEntered = toComparable(enteredVal);
      if (normCandidate && normEntered && normCandidate !== normEntered) {
        mismatch[field] = true;
        ctrl?.setErrors({ ...ctrl.errors, mismatch: true });
      }
    });

    return Object.keys(mismatch).length ? { mismatch } : null;
  };
}
