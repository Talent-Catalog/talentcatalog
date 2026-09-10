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

import {FormControl, FormGroup} from '@angular/forms';
import {profileMatchValidator} from './profile-match-validator';

describe('profileMatchValidator', () => {

  it('should return null when nested candidate values match', () => {
    const candidate = {
      user: {
        firstName: '  Ehsan  '
      }
    };

    const group = new FormGroup({
      firstName: new FormControl('ehsan')
    });

    const validator = profileMatchValidator(candidate, {
      firstName: 'user.firstName'
    });

    expect(validator(group)).toBeNull();
    expect(group.get('firstName')?.errors).toBeNull();
  });

  it('should ignore whitespace and letter casing', () => {
    const candidate = {
      firstName: 'EHsan'
    };

    const group = new FormGroup({
      firstName: new FormControl('  ehsan  ')
    });

    const validator = profileMatchValidator(candidate, {
      firstName: 'firstName'
    });

    expect(validator(group)).toBeNull();
  });

  it('should return mismatch errors for different values', () => {
    const candidate = {
      user: {
        firstName: 'Ehsan'
      },
      dob: '1995-07-27'
    };

    const group = new FormGroup({
      firstName: new FormControl('John'),
      dob: new FormControl('1995-07-27')
    });

    const validator = profileMatchValidator(candidate, {
      firstName: 'user.firstName',
      dob: 'dob'
    });

    expect(validator(group)).toEqual({
      mismatch: {
        firstName: true
      }
    });

    expect(group.get('firstName')?.errors).toEqual({
      mismatch: true
    });

    expect(group.get('dob')?.errors).toBeNull();
  });

  it('should compare an object containing a name property using its id', () => {
    const candidate = {
      country: {
        id: 7,
        name: 'Afghanistan'
      }
    };

    const group = new FormGroup({
      country: new FormControl('7')
    });

    const validator = profileMatchValidator(candidate, {
      country: 'country'
    });

    expect(validator(group)).toBeNull();
  });

  it('should stringify and compare non-string primitive values', () => {
    const candidate = {
      age: 30,
      active: true
    };

    const group = new FormGroup({
      age: new FormControl('30'),
      active: new FormControl('true')
    });

    const validator = profileMatchValidator(candidate, {
      age: 'age',
      active: 'active'
    });

    expect(validator(group)).toBeNull();
  });

  it('should not report a mismatch when a candidate value is null', () => {
    const candidate = {
      firstName: null
    };

    const group = new FormGroup({
      firstName: new FormControl('Ehsan')
    });

    const validator = profileMatchValidator(candidate, {
      firstName: 'firstName'
    });

    expect(validator(group)).toBeNull();
  });

  it('should handle an undefined nested candidate path', () => {
    const candidate = {
      user: null
    };

    const group = new FormGroup({
      firstName: new FormControl('Ehsan')
    });

    const validator = profileMatchValidator(candidate, {
      firstName: 'user.firstName'
    });

    expect(validator(group)).toBeNull();
  });

  it('should not report a mismatch when the entered value is empty', () => {
    const candidate = {
      firstName: 'Ehsan'
    };

    const group = new FormGroup({
      firstName: new FormControl(null)
    });

    const validator = profileMatchValidator(candidate, {
      firstName: 'firstName'
    });

    expect(validator(group)).toBeNull();
  });

  it('should safely handle a mapped control that does not exist', () => {
    const candidate = {
      firstName: 'Ehsan'
    };

    const group = new FormGroup({});

    const validator = profileMatchValidator(candidate, {
      firstName: 'firstName'
    });

    expect(() => validator(group)).not.toThrow();
    expect(validator(group)).toBeNull();
  });

  it('should clear a previous mismatch error when values now match', () => {
    const candidate = {
      firstName: 'Ehsan'
    };

    const firstNameControl = new FormControl('Ehsan');
    firstNameControl.setErrors({mismatch: true});

    const group = new FormGroup({
      firstName: firstNameControl
    });

    const validator = profileMatchValidator(candidate, {
      firstName: 'firstName'
    });

    expect(validator(group)).toBeNull();
    expect(firstNameControl.errors).toBeNull();
  });

  it('should preserve unrelated errors when clearing a mismatch', () => {
    const candidate = {
      firstName: 'Ehsan'
    };

    const firstNameControl = new FormControl('Ehsan');
    firstNameControl.setErrors({
      required: true,
      mismatch: true
    });

    const group = new FormGroup({
      firstName: firstNameControl
    });

    const validator = profileMatchValidator(candidate, {
      firstName: 'firstName'
    });

    expect(validator(group)).toBeNull();
    expect(firstNameControl.errors).toEqual({
      required: true
    });
  });

  it('should preserve existing errors when adding a mismatch', () => {
    const candidate = {
      firstName: 'Ehsan'
    };

    const firstNameControl = new FormControl('John');
    firstNameControl.setErrors({required: true});

    const group = new FormGroup({
      firstName: firstNameControl
    });

    const validator = profileMatchValidator(candidate, {
      firstName: 'firstName'
    });

    expect(validator(group)).toEqual({
      mismatch: {
        firstName: true
      }
    });

    expect(firstNameControl.errors).toEqual({
      required: true,
      mismatch: true
    });
  });

  it('should return null when the field map is empty', () => {
    const group = new FormGroup({
      firstName: new FormControl('Ehsan')
    });

    const validator = profileMatchValidator(
      {firstName: 'Different'},
      {}
    );

    expect(validator(group)).toBeNull();
  });
});
