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

import {Component, OnInit} from '@angular/core';
import {UntypedFormBuilder} from "@angular/forms";
import {JobService} from "../../../../services/job.service";
import {
  JoiDataComponent
} from "../joi-data/joi-data.component";
import {BUTTONS, NgxWigToolbarService} from "ngx-wig";
import {CUSTOM_CLEAR_FORMAT_BUTTON} from "../../../../util/clear-format";

@Component({
  selector: 'app-cost-commit-employer',
  templateUrl: './cost-commit-employer.component.html',
  styleUrls: ['./cost-commit-employer.component.scss'],
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
export class CostCommitEmployerComponent extends JoiDataComponent implements OnInit {

  constructor(fb: UntypedFormBuilder, jobService: JobService) {
    super(fb, jobService);

    //These inputs are predefined for this component
    this.formFieldName = "employerCostCommitment";
    this.componentKey="JOI.COST_COMMITMENT"
    this.richText=true
    this.required=true
  }

  onSuccessfulSave() {
    //Update employerCostCommitment in this.jobIntakeData with the form data.
    //Note that we have to this because the save returns void, not the updated intake.
    //This is for performance reasons when doing autosaves - we don't want the server to be
    //continually sending back updated intakes.
    let commitment = this.form.controls.employerCostCommitment.value;
    this.jobIntakeData.employerCostCommitment = commitment;

    //We need to output an event with this new intake data so that the parent job can be updated
    //and also so that the logic which checks whether an intake is complete can be triggered
    //and displayed. See JobPrepJOI in the JobPrepItems of ViewJobComponent.
    //Note that we don't do this for all intake components. We probably should, but currently just
    //employer cost commitment is used in JobPrepJOI to determine intake completeness.
    this.intakeChanged.emit(this.jobIntakeData);
  }
}
