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
import {CandidateSource} from "../../../model/base";
import {CandidateSourceService} from "../../../services/candidate-source.service";
import {DragulaService} from "ng2-dragula";
import {NgbActiveModal, NgbDropdownConfig} from "@ng-bootstrap/ng-bootstrap";
import {PublishedDocColumnConfig, PublishedDocColumnProps} from "../../../model/saved-list";
import {PublishedDocColumnService} from "../../../services/published-doc-column.service";

@Component({
  selector: 'app-published-doc-column-selector',
  templateUrl: './published-doc-column-selector.component.html',
  styleUrls: ['./published-doc-column-selector.component.scss']
})
export class PublishedDocColumnSelectorComponent implements OnInit {

  availableColumns: PublishedDocColumnConfig[];
  dragulaGroupName: string = "COLUMNS";
  error: string;
  selectedColumns: PublishedDocColumnConfig[];
  private candidateSource: CandidateSource;
  private longFormat: boolean;
  updating: boolean;
  edit: boolean = false;

  constructor(
    private publishedDocColumnService: PublishedDocColumnService,
    private candidateSourceService: CandidateSourceService,
    private dragulaService: DragulaService,
    private activeModal: NgbActiveModal,
    config: NgbDropdownConfig) {
    config.placement = 'bottom-left';
    let group = dragulaService.find(this.dragulaGroupName);
    if (!group) {
      dragulaService.createGroup(this.dragulaGroupName, {removeOnSpill: true})
    }
  }

  ngOnInit(): void {
    // Set default columns if no columns selected.
    if (this.selectedColumns.length === 0) {
      this.default();
    }
  }

  cancel() {
    this.activeModal.dismiss(false);
  }

  submit() {
    this.activeModal.close(this.selectedColumns);
  }

  default() {
    this.selectedColumns = this.publishedDocColumnService.getDefaultColumns();
  }

  update(field: PublishedDocColumnConfig) {
    return field;
  }

  reset(field: PublishedDocColumnConfig) {
    field.columnProps.header = null;
    field.columnProps.constant = null;
  }

  hasFieldName(field: PublishedDocColumnConfig): boolean {
    let hasFieldName: boolean = true;
    if (field.columnDef.content.value != null) {
      if (field.columnDef.content.value.fieldName == null) {
        hasFieldName = false;
      }
    } else {
      hasFieldName = false;
    }
    return hasFieldName;
  }

  addColumn(field: PublishedDocColumnConfig) {
    field.columnProps = new PublishedDocColumnProps();
    field.columnProps.header = null;
    field.columnProps.constant = null;
    this.selectedColumns.push(field);
  }

  removeColumn($event) {
    let col : PublishedDocColumnConfig = $event.value;
    let index = this.selectedColumns.findIndex(c => c.columnDef.key === col.columnDef.key)
    this.selectedColumns.splice(index, 1);
  }
}
