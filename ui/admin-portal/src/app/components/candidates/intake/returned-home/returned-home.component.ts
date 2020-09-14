import {Component, OnInit} from '@angular/core';
import {FormBuilder} from "@angular/forms";
import {IntakeComponentBase} from "../../../util/intake/IntakeComponentBase";
import {CandidateService} from "../../../../services/candidate.service";

@Component({
  selector: 'app-returned-home',
  templateUrl: './returned-home.component.html',
  styleUrls: ['./returned-home.component.scss']
})
export class ReturnedHomeComponent extends IntakeComponentBase implements OnInit {

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      returnedHome: [this.candidateIntakeData?.returnedHome],
      returnedHomeReason: [this.candidateIntakeData?.returnedHomeReason],
      returnedHomeNotes: [this.candidateIntakeData?.returnedHomeNotes],
    });
  }

  get returnedHome(): boolean {
    const val: string = this.form.value?.returnedHome;
    return val === null ? false : val === 'true';
  }
}
