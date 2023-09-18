/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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

import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {
  AbstractControl,
  AsyncValidatorFn,
  FormBuilder,
  FormGroup,
  ValidationErrors,
  Validators
} from '@angular/forms';
import {salesforceSandboxUrlPattern, salesforceUrlPattern} from '../../../model/base';
import {Observable, of} from 'rxjs';
import {catchError, map, tap} from 'rxjs/operators';
import {SalesforceService} from "../../../services/salesforce.service";
import {FormComponentBase} from "../form/FormComponentBase";

/*
  MODEL - subclass FormComponentBase, invalid values, sync and async validators
  - use of subclassing FormComponentBase to inherit common standard form functionality
  - shows how to check for invalid control values using inherited isInvalid method.
  - use of both sync and async form validators
 */

@Component({
  selector: 'app-joblink',
  templateUrl: './joblink.component.html',
  styleUrls: ['./joblink.component.scss']
})
export class JoblinkComponent extends FormComponentBase implements OnInit {
  form: FormGroup;
  @Input() joblink: string;
  @Output() updateError =  new EventEmitter();
  @Output() joblinkValidation =  new EventEmitter();

  constructor(fb: FormBuilder,
    private salesforceService: SalesforceService) {
    super(fb);
  }

  ngOnInit(): void {

    this.form = this.fb.group({
      sfJoblink: [this.joblink,
        [Validators.pattern(`${salesforceUrlPattern}|${salesforceSandboxUrlPattern}`)], //Sync validators
        [this.sfJoblinkValidator()] //Async validators
      ],
    });
  }

  private sfJoblinkValidator(): AsyncValidatorFn {
    return (control: AbstractControl): Observable<ValidationErrors | null> => {
      const url: string = control.value;
      let retval;

      this.updateError.emit(null);

      if (url == null || url.length === 0) {
        //Empty url always validates
        retval = of(null)
        this.joblinkValidation.emit("");
      } else {
        //See if we have name for a job corresponding to this url
        retval = this.salesforceService.getOpportunity(url).pipe(
          //As side effect populate the job details
          tap(opportunity => {
            const valid = opportunity && opportunity.name !== null;
            const validationEvent = new JoblinkValidationEvent(valid);
            if (valid) {
              validationEvent.sfJoblink = this.form.controls.sfJoblink.value;
              validationEvent.jobname = opportunity.name;
            }
            this.joblinkValidation.emit(validationEvent);
          }),

          //Null names turn into validation error - otherwise no error
          map(opportunity => opportunity === null ? {'invalidSfJoblink': true} : null),

          //Problems connecting to server will be displayed but we won't
          //treat it as a validation error
          catchError(err => {
            this.updateError.emit(err);
            return of(null);
          })
        );
      }

      return retval;
    };
  }

}

export class JoblinkValidationEvent {
  jobname: string;
  sfJoblink: string;
  valid: boolean;

  constructor(valid: boolean) {
    this.valid = valid;
    if (!valid) {
      this.jobname = null;
      this.sfJoblink = null;
    }
  }
}
