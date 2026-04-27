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

import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {FormComponentBase} from "../form/FormComponentBase";
import {
  AbstractControl,
  AsyncValidatorFn,
  UntypedFormBuilder,
  UntypedFormGroup, ValidationErrors,
  Validators
} from "@angular/forms";
import {salesforceSandboxUrlPattern, salesforceUrlPattern} from "../../../model/base";
import {Observable, of} from "rxjs";
import {catchError, map, tap} from "rxjs/operators";
import {SalesforceService} from "../../../services/salesforce.service";

@Component({
  selector: 'app-sf-joblink',
  templateUrl: './sf-joblink.component.html',
  styleUrls: ['./sf-joblink.component.scss']
})
export class SfJoblinkComponent extends FormComponentBase implements OnInit {
  form: UntypedFormGroup;
  @Output() updateError =  new EventEmitter();
  @Output() sfJoblinkValidation =  new EventEmitter();

  constructor(fb: UntypedFormBuilder,
              private salesforceService: SalesforceService) {
    super(fb);
  }

  ngOnInit(): void {
    this.form = this.fb.group({
      sfJoblink: [null,
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
        this.sfJoblinkValidation.emit("");
      } else {
        //See if we have name for a job corresponding to this url
        retval = this.salesforceService.getOpportunity(url).pipe(
          //As side effect populate the job details
          tap(opportunity => {
            const valid = opportunity && opportunity.name !== null;
            const validationEvent = new SfJoblinkValidationEvent(valid);
            if (valid) {
              validationEvent.sfJoblink = this.form.controls.sfJoblink.value;
              validationEvent.jobname = opportunity.name;
            }
            this.sfJoblinkValidation.emit(validationEvent);
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

export class SfJoblinkValidationEvent {
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
