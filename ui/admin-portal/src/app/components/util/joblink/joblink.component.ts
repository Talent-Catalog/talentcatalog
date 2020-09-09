import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {AbstractControl, AsyncValidatorFn, FormBuilder, FormGroup, ValidationErrors, Validators} from '@angular/forms';
import {salesforceUrlPattern} from '../../../model/base';
import {Observable, of} from 'rxjs';
import {catchError, map, tap} from 'rxjs/operators';
import {SalesforceService} from '../../../services/salesforce.service';

@Component({
  selector: 'app-joblink',
  templateUrl: './joblink.component.html',
  styleUrls: ['./joblink.component.scss']
})
export class JoblinkComponent implements OnInit {
  form: FormGroup;
  @Input() joblink: string;
  @Output() updateError =  new EventEmitter();
  @Output() joblinkValidation =  new EventEmitter();

  constructor(
    private fb: FormBuilder,
    private salesforceService: SalesforceService
  ) { }

  ngOnInit(): void {

    this.form = this.fb.group({
      sfJoblink: [this.joblink,
        [Validators.pattern(salesforceUrlPattern)], //Sync validators
        [this.sfJoblinkValidator()] //Async validators
      ],
    });
  }

  get sfJoblink() { return this.form.get('sfJoblink'); }

  private sfJoblinkValidator(): AsyncValidatorFn {
    return (control: AbstractControl): Observable<ValidationErrors | null> => {
      const url: string = control.value;
      let retval;

      this.updateError.emit(null);

      if (url == null || url.length === 0) {
        //Empty url always validates
        retval = of(null)
      } else {
        //See if we have name for a job corresponding to this url
        retval = this.salesforceService.findSfJobName(url).pipe(
          //As side effect populate the job details
          tap(opportunity => {
            const valid = opportunity && opportunity.name !== null;
            const validationEvent = new JoblinkValidationEvent(valid);
            if (valid) {
              validationEvent.sfJoblink = this.sfJoblink.value;
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
