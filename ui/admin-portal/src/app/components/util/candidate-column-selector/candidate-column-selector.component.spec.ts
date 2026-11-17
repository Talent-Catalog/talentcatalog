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

import type {MockedObject} from "vitest";
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {DragulaService} from 'ng2-dragula';
import {of, throwError} from 'rxjs';

import {CandidateSource} from '../../../model/base';
import {CandidateFieldInfo} from '../../../model/candidate-field-info';
import {CandidateFieldService} from '../../../services/candidate-field.service';
import {CandidateSourceService} from '../../../services/candidate-source.service';
import {CandidateColumnSelectorComponent} from './candidate-column-selector.component';
import {expect, vi} from 'vitest';

describe('CandidateColumnSelectorComponent', () => {
    let component: CandidateColumnSelectorComponent;
    let candidateFieldService: MockedObject<CandidateFieldService>;
    let candidateSourceService: MockedObject<CandidateSourceService>;
    let dragulaService: MockedObject<DragulaService>;
    let activeModal: MockedObject<NgbActiveModal>;
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
        candidateFieldService = ({
            getCandidateSourceFields: vi.fn().mockName("CandidateFieldService.getCandidateSourceFields"),
            getDisplayableFieldsMap: vi.fn().mockName("CandidateFieldService.getDisplayableFieldsMap"),
            getDefaultDisplayableFieldsLong: vi.fn().mockName("CandidateFieldService.getDefaultDisplayableFieldsLong"),
            getDefaultDisplayableFieldsShort: vi.fn().mockName("CandidateFieldService.getDefaultDisplayableFieldsShort")
        } as unknown as typeof candidateFieldService);

        candidateSourceService = ({
            updateDisplayedFieldPaths: vi.fn().mockName("CandidateSourceService.updateDisplayedFieldPaths")
        } as unknown as typeof candidateSourceService);

        dragulaService = ({
            find: vi.fn().mockName("DragulaService.find"),
            createGroup: vi.fn().mockName("DragulaService.createGroup")
        } as unknown as typeof dragulaService);

        activeModal = ({
            close: vi.fn().mockName("NgbActiveModal.close"),
            dismiss: vi.fn().mockName("NgbActiveModal.dismiss")
        } as unknown as typeof activeModal);

        source = {
            id: 1,
            name: 'Test source',
            fixed: false,
            global: false,
            displayedFieldsLong: [],
            displayedFieldsShort: []
        } as CandidateSource;

        candidateFieldService.getCandidateSourceFields.mockReturnValue([firstNameField]);

        candidateFieldService.getDisplayableFieldsMap.mockReturnValue(new Map<string, CandidateFieldInfo>([
            [firstNameField.fieldPath, firstNameField],
            [emailField.fieldPath, emailField],
            [countryField.fieldPath, countryField]
        ]));

        candidateFieldService
            .getDefaultDisplayableFieldsLong.mockReturnValue([firstNameField, emailField]);

        candidateFieldService
            .getDefaultDisplayableFieldsShort.mockReturnValue([countryField]);

        candidateSourceService
            .updateDisplayedFieldPaths.mockReturnValue(of(null));

        component = new CandidateColumnSelectorComponent(candidateFieldService, candidateSourceService, dragulaService, activeModal);
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should create the Dragula group when it does not exist', () => {
        dragulaService.find.mockReturnValue(null);

        component.ngOnInit();

        expect(dragulaService.find)
            .toHaveBeenCalledWith(component.dragulaGroupName);

        expect(dragulaService.createGroup)
            .toHaveBeenCalledWith(component.dragulaGroupName, {});
    });

    it('should not create the Dragula group when it already exists', () => {
        dragulaService.find.mockReturnValue({} as any);

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
        expect(component.updating).toBe(false);
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

        expect(component.updating).toBe(false);
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
            .updateDisplayedFieldPaths.mockReturnValue(throwError(error));

        component.setSourceAndFormat(source, true);

        component.close();

        expect(component.error).toBe(error as any);
        expect(component.updating).toBe(false);
        expect(activeModal.close).not.toHaveBeenCalled();
    });

    it('should restore long-format default fields', () => {
        component.setSourceAndFormat(source, true);

        component.default(source);

        expect(candidateFieldService.getDefaultDisplayableFieldsLong).toHaveBeenCalledWith(source);

        expect(component.selectedFields).toEqual([
            firstNameField,
            emailField
        ]);
    });

    it('should restore short-format default fields', () => {
        component.setSourceAndFormat(source, false);

        component.default(source);

        expect(candidateFieldService.getDefaultDisplayableFieldsShort).toHaveBeenCalledWith(source);

        expect(component.selectedFields).toEqual([
            countryField
        ]);
    });
});
