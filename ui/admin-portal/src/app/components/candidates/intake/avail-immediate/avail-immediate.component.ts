import {Component, OnInit} from '@angular/core';
import {IntakeComponentBase} from "../../../util/intake/IntakeComponentBase";
import {EnumOption, enumOptions} from "../../../../util/enum";
import {AvailImmediate, AvailImmediateReason} from "../../../../model/candidate";
import {FormBuilder} from "@angular/forms";
import {CandidateService} from "../../../../services/candidate.service";

@Component({
  selector: 'app-avail-immediate',
  templateUrl: './avail-immediate.component.html',
  styleUrls: ['./avail-immediate.component.scss']
})

export class AvailImmediateComponent extends IntakeComponentBase implements OnInit {

  public availImmediateOptions: EnumOption[] = enumOptions(AvailImmediate);
  public availImmediateReasonOptions: EnumOption[] = enumOptions(AvailImmediateReason);

  constructor(fb: FormBuilder, candidateService: CandidateService) {
    super(fb, candidateService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      availImmediate: [this.candidateIntakeData?.availImmediate],
      availImmediateReason: [this.candidateIntakeData?.availImmediateReason],
      availImmediateNotes: [this.candidateIntakeData?.availImmediateNotes],
    });
  }

  get availImmediate(): string {
    return this.form.value?.availImmediate;
  }

}
