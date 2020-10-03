import {Component, Input, OnInit} from '@angular/core';
import {EnumOption, enumOptions} from "../../../../util/enum";
import {
  Candidate,
  CandidateCitizenship,
  CandidateIntakeData,
  HasPassport
} from "../../../../model/candidate";
import {Nationality} from "../../../../model/nationality";

@Component({
  selector: 'app-citizenships',
  templateUrl: './citizenships.component.html',
  styleUrls: ['./citizenships.component.scss']
})
export class CitizenshipsComponent implements OnInit {
  @Input() candidate: Candidate;
  @Input() candidateIntakeData: CandidateIntakeData;
  @Input() existingRecords: CandidateCitizenship[];
  @Input() nationalities: Nationality[];
  hasPassportOptions: EnumOption[] = enumOptions(HasPassport);
  updatingRecord: boolean;

  constructor(){}

  ngOnInit(): void {
  }

  deleteRecord(i: number) {
    //todo
    //todo Refresh existing records
  }

  updateRecord() {
    //todo
    this.updatingRecord = true;
  }

  finishUpdatingRecord() {
    //todo Refresh existing records
    this.updatingRecord = false;
  }
}
