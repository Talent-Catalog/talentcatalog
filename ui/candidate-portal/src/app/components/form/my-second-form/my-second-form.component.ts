import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {MySecondFormData} from "../../../model/form";
import {FormBuilder, FormGroup, Validators} from "@angular/forms";
import {CandidateFormService} from "../../../services/candidate-form.service";
import {ICandidateFormComponent} from "../../../model/candidate-form";
import {CandidateService} from "../../../services/candidate.service";

/**
 * This is effectively a duplicate of MyFirstFormComponent
 */
@Component({
  selector: 'app-my-second-form',
  templateUrl: './my-second-form.component.html',
  styleUrls: ['./my-second-form.component.scss']
})
export class MySecondFormComponent implements OnInit, ICandidateFormComponent<MySecondFormData> {
  //When present and true, the form can't be modified or submitted
  @Input() readOnly = false;

  /** Candidate object passed */
  @Input() candidate: any | null = null;
  
  //Output event supplying the submitted data
  @Output() submitted = new EventEmitter<MySecondFormData>();

  //Note the use of FormGroup instead of UntypedFormGroup
  form: FormGroup;

  //Used to display any errors on submission
  error: any = null;

  //Set to true after the form has been submitted, and back to false when completed
  submitting: boolean = false;

  //Note that Angular now has an alternative to the above error and submitting variables which
  //are used to record the state of the component. See https://angular.dev/guide/signals.
  //They help Angular to optimize rendering updates - but add complexity so not used here.

  constructor(
    private candidateService: CandidateService,
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
    this.candidateFormService.getMySecondForm().subscribe({
      //this.form.reset is the best way to set form values in typed forms
      next: data => this.form.reset(data),
      error: () => {
        //There will be an error if the form does not yet exist. The form can be blank.
        this.form.reset();
      }
    })
  }

  //Check whether the form can be submitted - otherwise the submit button can be disabled
  canSubmit(): boolean {
    //Pending is true while validators are running
    return this.form.valid && !this.form.pending && !this.submitting && !this.readOnly;
  }

  /**
   * Checks whether the given control has passed the given validation
   * @param ctrlName Name of a control
   * @param validationName Name of validation
   */
  hasError(ctrlName: keyof MySecondFormData, validationName: string): boolean {
    const c = this.form.get(ctrlName);
    return !this.readOnly && !!c && c.touched && c.hasError(validationName);
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
    const formData: MySecondFormData = this.form.getRawValue();

    //Note that subscribe is called with an object {} containing the next and error fields
    this.candidateFormService.createOrUpdateMySecondForm(formData).subscribe({
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
