/*
 * Copyright (c) 2024 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

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
