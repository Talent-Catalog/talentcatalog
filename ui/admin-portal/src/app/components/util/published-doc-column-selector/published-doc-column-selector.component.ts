import {Component, OnInit} from '@angular/core';
import {CandidateSource} from "../../../model/base";
import {CandidateSourceService} from "../../../services/candidate-source.service";
import {DragulaService} from "ng2-dragula";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {PublishedDocColumnConfig} from "../../../model/saved-list";
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

  constructor(
    private publishedDocColumnService: PublishedDocColumnService,
    private candidateSourceService: CandidateSourceService,
    private dragulaService: DragulaService,
    private activeModal: NgbActiveModal) { }

  ngOnInit(): void {
    const dragulaGroup = this.dragulaService.find(this.dragulaGroupName);
    if (!dragulaGroup) {
      this.dragulaService.createGroup(this.dragulaGroupName, {
        copy: true,
        copyItem: (item: PublishedDocColumnConfig) => {
          const copy = new PublishedDocColumnConfig();
          copy.columnDef = item.columnDef;
          return copy;
        }
      });
    }
    // todo pull out empty column and put at bottom + alphabetise columns
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

  dismiss() {
    this.activeModal.dismiss(false);
  }

  close() {
    console.log(this.selectedColumns);
    this.activeModal.close(this.selectedColumns);
  }

  default() {
    //this.selectedColumns = this.availableColumns;
  }
}
