import {Component, Input, OnInit} from '@angular/core';
import {JobIntakeComponentBase} from "../../../util/intake/JobIntakeComponentBase";
import {FormBuilder} from "@angular/forms";
import {JobService} from "../../../../services/job.service";

@Component({
  selector: 'app-joi-component',
  templateUrl: './joi-component.component.html',
  styleUrls: ['./joi-component.component.scss']
})
export class JoiComponentComponent extends JobIntakeComponentBase implements OnInit {
  @Input() formFieldName: string;
  @Input() richText = false;
  @Input() inputType = "text";
  @Input() required = false;

  constructor(fb: FormBuilder, jobService: JobService) {
    super(fb, jobService);
  }

  ngOnInit(): void {
    let value = this.jobIntakeData == null ? null : this.jobIntakeData[this.formFieldName];
    let controlsConfig = {};
    controlsConfig[this.formFieldName] = [{value: value, disabled: !this.editable}];
    this.form = this.fb.group(controlsConfig);
  }
}
