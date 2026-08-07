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

import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {DragulaService} from 'ng2-dragula';
import {of, throwError} from 'rxjs';

import {CandidateSource} from '../../../model/base';
import {CandidateFieldInfo} from '../../../model/candidate-field-info';
import {CandidateFieldService} from '../../../services/candidate-field.service';
import {CandidateSourceService} from '../../../services/candidate-source.service';
import {CandidateColumnSelectorComponent} from './candidate-column-selector.component';

describe('CandidateColumnSelectorComponent', () => {
  let component: CandidateColumnSelectorComponent;
  let candidateFieldService: jasmine.SpyObj<CandidateFieldService>;
  let candidateSourceService: jasmine.SpyObj<CandidateSourceService>;
  let dragulaService: jasmine.SpyObj<DragulaService>;
  let activeModal: jasmine.SpyObj<NgbActiveModal>;
  let source: CandidateSource;

  const firstNameField = {
    fieldPath: 'user.firstName',
    displayName: 'First name'
  } as CandidateFieldInfo;

  const countryField = {
    fieldPath: 'country.name',
    displayName: 'Country'
  } as CandidateFieldInfo;

  const emailField = {
    fieldPath: 'user.email',
    displayName: 'Email'
  } as CandidateFieldInfo;

  beforeEach(() => {
    candidateFieldService =
      jasmine.createSpyObj<CandidateFieldService>(
        'CandidateFieldService',
        [
          'getCandidateSourceFields',
          'getDisplayableFieldsMap',
          'getDefaultDisplayableFieldsLong',
          'getDefaultDisplayableFieldsShort'
        ]
      );

    candidateSourceService =
      jasmine.createSpyObj<CandidateSourceService>(
        'CandidateSourceService',
        ['updateDisplayedFieldPaths']
      );

    dragulaService = jasmine.createSpyObj<DragulaService>(
      'DragulaService',
      ['find', 'createGroup']
    );

    activeModal = jasmine.createSpyObj<NgbActiveModal>(
      'NgbActiveModal',
      ['close', 'dismiss']
    );

    source = {
      id: 1,
      name: 'Test source',
      fixed: false,
      global: false,
      displayedFieldsLong: [],
      displayedFieldsShort: []
    } as CandidateSource;

    candidateFieldService.getCandidateSourceFields.and.returnValue(
      [firstNameField]
    );

    candidateFieldService.getDisplayableFieldsMap.and.returnValue(
      new Map<string, CandidateFieldInfo>([
        [firstNameField.fieldPath, firstNameField],
        [emailField.fieldPath, emailField],
        [countryField.fieldPath, countryField]
      ])
    );

    candidateFieldService
    .getDefaultDisplayableFieldsLong
    .and.returnValue([firstNameField, emailField]);

    candidateFieldService
    .getDefaultDisplayableFieldsShort
    .and.returnValue([countryField]);

    candidateSourceService
    .updateDisplayedFieldPaths
    .and.returnValue(of(null));

    component = new CandidateColumnSelectorComponent(
      candidateFieldService,
      candidateSourceService,
      dragulaService,
      activeModal
    );
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should create the Dragula group when it does not exist', () => {
    dragulaService.find.and.returnValue(null);

    component.ngOnInit();

    expect(dragulaService.find)
    .toHaveBeenCalledWith(component.dragulaGroupName);

    expect(dragulaService.createGroup)
    .toHaveBeenCalledWith(component.dragulaGroupName, {});
  });

  it('should not create the Dragula group when it already exists', () => {
    dragulaService.find.and.returnValue({} as any);

    component.ngOnInit();

    expect(dragulaService.find)
    .toHaveBeenCalledWith(component.dragulaGroupName);

    expect(dragulaService.createGroup).not.toHaveBeenCalled();
  });

  it('should initialize source, format and selected fields', () => {
    component.setSourceAndFormat(source, true);

    expect(candidateFieldService.getCandidateSourceFields)
    .toHaveBeenCalledWith(source, true);

    expect(component.selectedFields).toEqual([firstNameField]);
  });

  it('should calculate and sort available fields', () => {
    component.setSourceAndFormat(source, true);

    expect(component.availableFields).toEqual([
      countryField,
      emailField
    ]);
  });

  it('should return selected fields from the getter', () => {
    component.setSourceAndFormat(source, true);

    expect(component.selectedFields).toEqual([firstNameField]);
  });

  it('should dismiss the modal', () => {
    component.dismiss();

    expect(activeModal.dismiss).toHaveBeenCalledWith(false);
  });

  it('should save long-format fields and close the modal', () => {
    component.setSourceAndFormat(source, true);
    component.selectedFields = [
      firstNameField,
      emailField
    ];

    component.close();

    expect(source.displayedFieldsLong).toEqual([
      firstNameField.fieldPath,
      emailField.fieldPath
    ]);

    expect(candidateSourceService.updateDisplayedFieldPaths)
    .toHaveBeenCalledWith(source, {
      displayedFieldsLong: [
        firstNameField.fieldPath,
        emailField.fieldPath
      ]
    });

    expect(component.error).toBeNull();
    expect(component.updating).toBeFalse();
    expect(activeModal.close).toHaveBeenCalled();
  });

  it('should save short-format fields and close the modal', () => {
    component.setSourceAndFormat(source, false);
    component.selectedFields = [countryField];

    component.close();

    expect(source.displayedFieldsShort).toEqual([
      countryField.fieldPath
    ]);

    expect(candidateSourceService.updateDisplayedFieldPaths)
    .toHaveBeenCalledWith(source, {
      displayedFieldsShort: [
        countryField.fieldPath
      ]
    });

    expect(component.updating).toBeFalse();
    expect(activeModal.close).toHaveBeenCalled();
  });

  it('should save an empty field-path array', () => {
    component.setSourceAndFormat(source, true);
    component.selectedFields = [];

    component.close();

    expect(candidateSourceService.updateDisplayedFieldPaths)
    .toHaveBeenCalledWith(source, {
      displayedFieldsLong: []
    });

    expect(activeModal.close).toHaveBeenCalled();
  });

  it('should store an error when saving fields fails', () => {
    const error = new Error('Update failed');

    candidateSourceService
    .updateDisplayedFieldPaths
    .and.returnValue(throwError(error));

    component.setSourceAndFormat(source, true);

    component.close();

    expect(component.error).toBe(error as any);
    expect(component.updating).toBeFalse();
    expect(activeModal.close).not.toHaveBeenCalled();
  });

  it('should restore long-format default fields', () => {
    component.setSourceAndFormat(source, true);

    component.default(source);

    expect(
      candidateFieldService.getDefaultDisplayableFieldsLong
    ).toHaveBeenCalledWith(source);

    expect(component.selectedFields).toEqual([
      firstNameField,
      emailField
    ]);
  });

  it('should restore short-format default fields', () => {
    component.setSourceAndFormat(source, false);

    component.default(source);

    expect(
      candidateFieldService.getDefaultDisplayableFieldsShort
    ).toHaveBeenCalledWith(source);

    expect(component.selectedFields).toEqual([
      countryField
    ]);
  });
});
