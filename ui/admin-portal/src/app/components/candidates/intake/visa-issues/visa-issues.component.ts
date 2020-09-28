import {Component, OnInit} from '@angular/core';
import {IntakeComponentBase} from "../../../util/intake/IntakeComponentBase";
import {FormBuilder} from "@angular/forms";
import {CandidateService} from "../../../../services/candidate.service";
import {enumOptions} from "../../../../util/enum";
import {PotentialVisaIssues} from "../../../../model/candidate";

@Component({
  selector: 'app-visa-issues',
  templateUrl: './visa-issues.component.html',
  styleUrls: ['./visa-issues.component.scss']
})
export class VisaIssuesComponent extends IntakeComponentBase implements OnInit {

  public visaIssueOptions = enumOptions(PotentialVisaIssues);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService)
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaIssues: [this.candidateIntakeData?.visaIssues],
      visaIssueNotes: [this.candidateIntakeData?.visaIssueNotes],
    });
  }

  get haveIssues(): boolean {
    return true; //todo
  }
}
