import {Component, Input, OnInit} from '@angular/core';
import {
  Candidate,
  CandidateCitizenship,
  CandidateIntakeData
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

  constructor(){}

  ngOnInit(): void {
  }

  deleteRecord(i: number) {
    //todo
    //todo Refresh existing records
  }

  updateRecord() {
    const citizenship: CandidateCitizenship = {};
    this.existingRecords.push(citizenship)
    //todo
  }
}
