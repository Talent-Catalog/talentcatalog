import {Directive, Input, OnInit} from '@angular/core';
import {FormBuilder} from '@angular/forms';
import {AutoSaveComponentBase} from "../autosave/AutoSaveComponentBase";
import {CandidateVisaCheckService} from "../../../services/candidate-visa-check.service";
import {CandidateVisa, CandidateVisaJobCheck} from "../../../model/candidate";

/**
 * Base class for all job intake components.
 * <p/>
 * See IntakeComponentBase doc
 * @author John Cameron
 */
@Directive()
export abstract class VisaCheckComponentBase extends AutoSaveComponentBase implements OnInit {

  /**
   * Index into job check array member of data if that is what is being updated.
   */
  @Input() visaJobCheck: CandidateVisaJobCheck;

  @Input() editable: boolean = true;

  /**
   * Inject in a FormBuilder to create the form and an IntakeService to perform the saves.
   * @param fb FormBuilder
   * @param candidateVisaCheckService Service which saves the intake data
   */
  protected constructor(protected fb: FormBuilder,
                        private candidateVisaCheckService: CandidateVisaCheckService) {
    super(candidateVisaCheckService);
  }

  /**
   * The entity that we are working with in this class is a Job - this returns the entity
   * cast as a Job so that anyone referencing "job" will see the entity with all
   * its Job properties.
   */
  get visaCheck(): CandidateVisa {
    return <CandidateVisa>this.entity;
  }

  /**
   * Convert any multiselected enums
   */
  preprocessFormValues(formValue: Object): Object {
    return AutoSaveComponentBase.convertEnumOptions(formValue);
  }

  /**
   * Sets a standard no response value to the named form control.
   * @param formControlName Name of control in the intake form.
   */
  setNoResponse(formControlName: string) {
    this.form.controls[formControlName].setValue('NoResponse');
  };


}
