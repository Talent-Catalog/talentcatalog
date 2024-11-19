import {Component, Input, OnInit} from '@angular/core';
import {JobIntakeComponentBase} from "../../../util/intake/JobIntakeComponentBase";
import {UntypedFormBuilder} from "@angular/forms";
import {JobService} from "../../../../services/job.service";
import {BUTTONS, NgxWigToolbarService} from "ngx-wig";
import {CUSTOM_CLEAR_FORMAT_BUTTON} from "../../../../util/clear-format";

@Component({
  selector: 'app-joi-component',
  templateUrl: './joi-data.component.html',
  styleUrls: ['./joi-data.component.scss'],
  providers: [
    {
      provide: BUTTONS,
      multi: true,
      useFactory: (toolbar: NgxWigToolbarService) => {
        // Get the default buttons
        const defaultButtons = toolbar.getToolbarButtons(); // Use the service to get existing buttons
        // Merge the custom button with the default ones
        return { ...defaultButtons, ...CUSTOM_CLEAR_FORMAT_BUTTON };
      },
      deps: [NgxWigToolbarService],
    },
  ]
})
export class JoiDataComponent extends JobIntakeComponentBase implements OnInit {
  @Input() formFieldName: string;
  @Input() richText = false;
  @Input() inputType = "text";
  @Input() required = false;

  constructor(fb: UntypedFormBuilder, jobService: JobService) {
    super(fb, jobService);
  }

  ngOnInit(): void {
    let value = this.jobIntakeData == null ? null : this.jobIntakeData[this.formFieldName];
    let controlsConfig = {};
    controlsConfig[this.formFieldName] = [{value: value, disabled: !this.editable}];
    this.form = this.fb.group(controlsConfig);

  }


}
