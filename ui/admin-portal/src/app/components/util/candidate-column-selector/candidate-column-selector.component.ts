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

import {Component, OnInit} from '@angular/core';
import {DragulaService} from "ng2-dragula";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateFieldInfo} from "../../../model/candidate-field-info";
import {CandidateFieldService} from "../../../services/candidate-field.service";
import {
  CandidateSource,
  UpdateDisplayedFieldPathsRequest
} from "../../../model/base";
import {CandidateSourceService} from "../../../services/candidate-source.service";

@Component({
  selector: 'app-candidate-column-selector',
  templateUrl: './candidate-column-selector.component.html',
  styleUrls: ['./candidate-column-selector.component.scss']
})
export class CandidateColumnSelectorComponent implements OnInit {

  availableFields: CandidateFieldInfo[] = [];
  dragulaGroupName: string = "FIELDS";
  error: string;
  private _selectedFields: CandidateFieldInfo[] = [];
  protected candidateSource: CandidateSource;
  private longFormat: boolean;
  updating: boolean;

  constructor(
    private candidateFieldService: CandidateFieldService,
    private candidateSourceService: CandidateSourceService,
    private dragulaService: DragulaService,
    private activeModal: NgbActiveModal) { }

  ngOnInit(): void {
    const dragulaGroup = this.dragulaService.find(this.dragulaGroupName);
    if (!dragulaGroup) {
      this.dragulaService.createGroup(this.dragulaGroupName, {});
    }
  }

  setSourceAndFormat(source: CandidateSource, longFormat: boolean) {
    this.candidateSource = source;
    this.longFormat = longFormat;

    this.selectedFields = this.candidateFieldService
      .getCandidateSourceFields(source, longFormat);
  }

  get selectedFields(): CandidateFieldInfo[] {
    return this._selectedFields;
  }

  set selectedFields(fields: CandidateFieldInfo[]) {
    this._selectedFields = fields;

    //Calculate remaining available fields.
    //Start by taking local copy of all displayable fields
    const availableFieldsMap = new Map<string, CandidateFieldInfo>(
      this.candidateFieldService.getDisplayableFieldsMap(this.candidateSource));

    //Now pull out the ones that are already selected
    for (const field of fields) {
      availableFieldsMap.delete(field.fieldPath);
    }

    //Copy remaining values to array
    this.availableFields = [...availableFieldsMap.values()];

    //Start by sorting available fields in alpha order of display name
    this.availableFields.sort(
      (field1, field2) =>
        field1.displayName.localeCompare(field2.displayName));
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }

  close() {
    //Extract field paths.
    const fieldPaths: string[] = [];
    for (const field of this.selectedFields) {
      fieldPaths.push(field.fieldPath);
    }

    //Update candidateSource and construct update request
    const request: UpdateDisplayedFieldPathsRequest = {};
    if (this.longFormat) {
      this.candidateSource.displayedFieldsLong = fieldPaths
      request.displayedFieldsLong = fieldPaths;
    } else {
      this.candidateSource.displayedFieldsShort = fieldPaths
      request.displayedFieldsShort = fieldPaths;
    }

    //Save to server
    this.error = null;
    this.updating = true;

    this.candidateSourceService
      .updateDisplayedFieldPaths(this.candidateSource, request).subscribe(
      () => {
        this.updating = false;
        this.activeModal.close();
      },
      error => {
        this.error = error;
        this.updating = false;
      }
    );
  }

  default(source: CandidateSource) {
    this.selectedFields = this.longFormat
      ? this.candidateFieldService.getDefaultDisplayableFieldsLong(source)
      : this.candidateFieldService.getDefaultDisplayableFieldsShort(source);
  }
}
