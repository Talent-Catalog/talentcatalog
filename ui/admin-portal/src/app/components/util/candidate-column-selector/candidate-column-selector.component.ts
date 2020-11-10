import {Component, OnInit} from '@angular/core';
import {DragulaService} from "ng2-dragula";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateFieldInfo} from "../../../model/candidate-field-info";
import {CandidateFieldService} from "../../../services/candidate-field.service";

@Component({
  selector: 'app-candidate-column-selector',
  templateUrl: './candidate-column-selector.component.html',
  styleUrls: ['./candidate-column-selector.component.scss']
})
export class CandidateColumnSelectorComponent implements OnInit {

  availableFields: CandidateFieldInfo[] = [];
  dragulaGroupName: string = "FIELDS";
  private _selectedFields: CandidateFieldInfo[] = [];

  constructor(
    private candidateFieldService: CandidateFieldService,
    private dragulaService: DragulaService,
    private activeModal: NgbActiveModal) { }

  ngOnInit(): void {
    const dragulaGroup = this.dragulaService.find(this.dragulaGroupName);
    if (!dragulaGroup) {
      this.dragulaService.createGroup(this.dragulaGroupName, {});
    }
  }

  get selectedFields(): CandidateFieldInfo[] {
    return this._selectedFields;
  }

  set selectedFields(fields: CandidateFieldInfo[]) {
    this._selectedFields = fields;

    //Calculate remaining available fields.
    //Start by taking local copy of all displayable fields
    const availableFieldsMap = new Map<string, CandidateFieldInfo>(
      this.candidateFieldService.displayableFieldsMap);

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
    this.activeModal.close(this.selectedFields);

  }
}
