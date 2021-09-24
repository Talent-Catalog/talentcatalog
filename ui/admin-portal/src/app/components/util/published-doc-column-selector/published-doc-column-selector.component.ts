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
  private _selectedColumns: PublishedDocColumnConfig[];
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
  }

  ngOnInit(): void {
    const dragulaGroup = this.dragulaService.find(this.dragulaGroupName);
    if (!dragulaGroup) {
      this.dragulaService.createGroup(this.dragulaGroupName, {
        copy: (el, source) => {
          return source.id === 'availableColumns';
        },
        copyItem: (item: PublishedDocColumnConfig) => {
          const copy = new PublishedDocColumnConfig();
          copy.columnDef = item.columnDef;
          copy.columnProps = new PublishedDocColumnProps();
          copy.columnProps.header = null;
          copy.columnProps.constant = null;
          return copy;
        },
        accepts: (el, target, source, sibling) => {
          // To avoid dragging from right to left container
          return target.id !== 'availableColumns';
        },
        removeOnSpill: true
      });
    }
    // Pull out empty column and put at bottom + alphabetise columns
    const emptyCol = this.availableColumns.shift();
    this.availableColumns = this.availableColumns.sort(
      (field1, field2) =>
        field1.columnDef.header.localeCompare(field2.columnDef.header));
    this.availableColumns.push(emptyCol);
  }

  get selectedColumns(): PublishedDocColumnConfig[] {
    return this._selectedColumns;
  }

  set selectedColumns(fields: PublishedDocColumnConfig[]) {
    this._selectedColumns = fields;
  }

  cancel() {
    this.activeModal.dismiss(false);
  }

  submit() {
    this.activeModal.close(this.selectedColumns);
  }

  default() {
    this.selectedColumns = [];
  }

  update(field: PublishedDocColumnConfig) {
    console.log(field);
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
}
