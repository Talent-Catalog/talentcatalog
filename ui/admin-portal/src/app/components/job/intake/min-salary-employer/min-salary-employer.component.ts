import {Component, OnInit} from '@angular/core';
import {FormBuilder} from "@angular/forms";
import {JobService} from "../../../../services/job.service";
import {JobIntakeComponentBase} from "../../../util/intake/JobIntakeComponentBase";
import {EnumOption, enumOptions} from "../../../../util/enum";
import {YesNo} from "../../../../model/candidate";

@Component({
  selector: 'app-min-salary-employer',
  templateUrl: './min-salary-employer.component.html',
  styleUrls: ['./min-salary-employer.component.scss']
})
export class MinSalaryEmployerComponent extends JobIntakeComponentBase implements OnInit {
  public minSalaryEmployerOptions: EnumOption[] = enumOptions(YesNo);
  constructor(fb: FormBuilder, jobService: JobService) {
    super(fb, jobService);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      minSalaryEmployer: [{value: this.jobIntakeData?.minSalaryEmployer, disabled: !this.editable}],
    });
  }

}
