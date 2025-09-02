import {Component, EventEmitter, OnInit, Output} from '@angular/core';
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {CandidateFormService} from "../../../services/candidate-form.service";
import {MyFirstFormData} from "../../../model/form";

/*
MODEL: Best practice Angular form
 */

@Component({
  selector: 'app-my-first-form',
  templateUrl: './my-first-form.component.html',
  styleUrls: ['./my-first-form.component.scss']
})
export class MyFirstFormComponent implements OnInit {
  //Output event supplying the submitted data
  @Output() submitted = new EventEmitter<MyFirstFormData>();

  //Note use of FormGroup instead of UntypedFormGroup
  form: FormGroup;

  //Used to display any errors on submission
  error: any = null;

  //Set to true after the form has been submitted, and back to false when completed
  submitting: boolean = false;

  //Note that Angular now has an alternative to the above error and submitting variables which
  //are used to record the state of the component. See https://angular.dev/guide/signals.
  //They help Angular to optimize rendering updates - but add complexity so not used here.

  constructor(
    private candidateFormService: CandidateFormService,
    //Note that UntypedFormBuilder is not used.
    private fb: FormBuilder
  ) {}

  ngOnInit(): void {
    //Note the recommended use of nonNullable. Form reset will return fields to initial values.
    this.form = this.fb.nonNullable.group({
      city: ['', [Validators.required]],
      hairColour: ['', [Validators.required]]
    })

    //Load the current form contents if any
    this.error = null;
    this.candidateFormService.createOrUpdateMyFirstForm({}).subscribe({
      //this.form.reset is the best way to set form values in typed forms
      next: myFirstFormData => this.form.reset(myFirstFormData),
      error: err => this.error = err
    })
  }

  //Check whether the form can be submitted - otherwise submit button can be disabled
  canSubmit(): boolean {
    //Pending is true while validators are running
    return this.form.valid && !this.form.pending && !this.submitting;
  }

  /**
   * Checks whether the given control has passed the given validation
   * @param ctrlName Name of a control
   * @param validationName Name of validation
   */
  hasError(ctrlName: keyof MyFirstFormData, validationName: string): boolean {
    const c = this.form.get(ctrlName);
    return !!c && c.touched && c.hasError(validationName);
  }

  onSubmit(): void {
    if (this.form.invalid) {
      //Note that errors only display for fields that have been touched. So touch them all.
      this.form.markAllAsTouched();
      return;
    }

    //Record that we are in the process of submitting
    this.submitting = true;

    //Clear any existing error
    this.error = null;

    //Copy form data to fully typed payload.
    //getRawValue instead of value returns all fields - even those which may be readonly or
    //where user input is disabled. This usually is what you want.
    const formData: MyFirstFormData = this.form.getRawValue();

    //Note that subscribe is called with an object {} containing the next and error fields
    this.candidateFormService.createOrUpdateMyFirstForm(formData).subscribe({
      next: value => {
        this.submitted.emit(value);

        //Reset the touched/dirty states
        this.form.markAsPristine();
        this.form.markAsUntouched();

        this.submitting = false;
      },
      error: err => {
        this.error = err;
        this.submitting = false;
      }
    })
  }
}
