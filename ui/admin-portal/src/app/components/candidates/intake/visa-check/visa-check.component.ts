import {Component, Input, OnInit} from '@angular/core';
import {Candidate, CandidateIntakeData} from "../../../../model/candidate";
import {FormBuilder, FormGroup} from "@angular/forms";

@Component({
  selector: 'app-visa-check',
  templateUrl: './visa-check.component.html',
  styleUrls: ['./visa-check.component.scss']
})
export class VisaCheckComponent implements OnInit {
  @Input() candidate: Candidate;
  @Input() candidateIntakeData: CandidateIntakeData;
  error: boolean;
  form: FormGroup;
  saving: boolean;
  selectedIndex: number;

  constructor(private fb: FormBuilder) { }

  ngOnInit(): void {

    //If we have some visa checks, select the first one
    if (this.candidateIntakeData?.candidateVisaChecks.length > 0) {
      this.selectedIndex = 0;
    }
    this.form = this.fb.group({
      visaCountry: [this.selectedIndex]
    });
  }

  addRecord() {
    //todo
    // this.saving = true;
    // const candidateCitizenship: CandidateCitizenship = {};
    // this.candidateCitizenshipService.create(this.candidate.id, candidateCitizenship).subscribe(
    //   (citizenship) => {
    //     this.candidateIntakeData.candidateCitizenships.push(citizenship)
    //     this.saving = false;
    //   },
    //   (error) => {
    //     this.error = error;
    //     this.saving = false;
    //   });
  }

  deleteRecord(i: number) {
    this.candidateIntakeData.candidateVisaChecks.splice(i, 1);
  }

  changeVisaCountry(event: Event) {
    this.selectedIndex = this.form.controls.visaCountry.value;
  }

  onRefreshRequest() {

  }
}
