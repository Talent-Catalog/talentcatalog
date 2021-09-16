import {Component, OnInit} from '@angular/core';
import {CandidateSource} from "../../../model/base";
import {CandidateSourceService} from "../../../services/candidate-source.service";
import {DragulaService} from "ng2-dragula";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {PublishedDocColumnInfo, PublishListRequest} from "../../../model/saved-list";
import {PublishedDocColumnService} from "../../../services/published-doc-column.service";

@Component({
  selector: 'app-published-doc-column-selector',
  templateUrl: './published-doc-column-selector.component.html',
  styleUrls: ['./published-doc-column-selector.component.scss']
})
export class PublishedDocColumnSelectorComponent implements OnInit {

  availableColumns: PublishedDocColumnInfo[];
  dragulaGroupName: string = "COLUMNS";
  error: string;
  private _selectedColumns: PublishedDocColumnInfo[];
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
      this.dragulaService.createGroup(this.dragulaGroupName, {});
    }
  }

  get selectedColumns(): PublishedDocColumnInfo[] {
    return this._selectedColumns;
  }

  set selectedColumns(fields: PublishedDocColumnInfo[]) {
    this._selectedColumns = fields;

    //Calculate remaining available fields.
    //Start by taking local copy of all displayable fields
    const availableFieldsMap = new Map<string, PublishedDocColumnInfo>(
      this.publishedDocColumnService.allColumnInfosMap);

    //Now pull out the ones that are already selected
    for (const field of fields) {
      availableFieldsMap.delete(field.key);
    }

    //Copy remaining values to array
    this.availableColumns = [...availableFieldsMap.values()];

    //Start by sorting available fields in alpha order of display name
    this.availableColumns.sort(
      (field1, field2) =>
        field1.name.localeCompare(field2.name));
  }

  dismiss() {
    this.activeModal.dismiss(false);
  }

  close() {
    const request: PublishListRequest = new PublishListRequest();
    request.columns = this.selectedColumns;

    this.activeModal.close(request);
  //   //Extract field paths.
  //   const fieldPaths: string[] = [];
  //   for (const field of this.selectedColumns) {
  //     fieldPaths.push(field.fieldPath);
  //   }
  //
  //   //Update candidateSource and construct update request
  //   const request: UpdateDisplayedFieldPathsRequest = {};
  //   if (this.longFormat) {
  //     this.candidateSource.displayedFieldsLong = fieldPaths
  //     request.displayedFieldsLong = fieldPaths;
  //   } else {
  //     this.candidateSource.displayedFieldsShort = fieldPaths
  //     request.displayedFieldsShort = fieldPaths;
  //   }
  //
  //   //Save to server
  //   this.error = null;
  //   this.updating = true;
  //
  //   this.candidateSourceService
  //     .updateDisplayedFieldPaths(this.candidateSource, request).subscribe(
  //     () => {
  //       this.updating = false;
  //       this.activeModal.close();
  //     },
  //     error => {
  //       this.error = error;
  //       this.updating = false;
  //     }
  //   );
  }

  default() {
    //this.selectedColumns = this.availableColumns;
  }
}
