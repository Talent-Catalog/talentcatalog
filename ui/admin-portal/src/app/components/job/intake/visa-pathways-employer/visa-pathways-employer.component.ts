import {Component, OnInit} from '@angular/core';
import {FormBuilder} from "@angular/forms";
import {JobService} from "../../../../services/job.service";
import {JobIntakeComponentBase} from "../../../util/intake/JobIntakeComponentBase";
import {EnumOption, enumOptions} from "../../../../util/enum";
import {YesNo} from "../../../../model/candidate";

@Component({
  selector: 'app-visa-pathways-employer',
  templateUrl: './visa-pathways-employer.component.html',
  styleUrls: ['./visa-pathways-employer.component.scss']
})
export class VisaPathwaysEmployerComponent extends JobIntakeComponentBase implements OnInit {

  public visaPathwaysEmployerOptions: EnumOption[] = enumOptions(YesNo);
  constructor(fb: FormBuilder, jobService: JobService) {
    super(fb, jobService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      visaPathwaysEmployer: [{value: this.jobIntakeData?.visaPathwaysEmployer, disabled: !this.editable}],
    });
  }

}
