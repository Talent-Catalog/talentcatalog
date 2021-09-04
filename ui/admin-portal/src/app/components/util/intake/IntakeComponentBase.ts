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

import {Directive, Input, OnInit} from '@angular/core';
import {Observable} from 'rxjs';
import {FormBuilder} from '@angular/forms';
import {Candidate, CandidateIntakeData, CandidateVisa} from '../../../model/candidate';
import {CandidateService} from '../../../services/candidate.service';
import {AutoSaveComponentBase} from "../autosave/AutoSaveComponentBase";

/**
 * Base class for all candidate intake components.
 * <p/>
 * Provides following standard functionality
 * <ul>
 *   <li>Declares standard component @Inputs. Note that these inputs are not all used in this
 *   code but by declaring them here we standardize their naming and purpose, which will
 *   be used by subclasses.</li>
 *   <li>Provide a FormBuilder instance {@link fb} that subclasses can use to create the form</li>
 *   <li>Provides a standard setNoResponse method for form fields</li>
 *   <li>Implements the saving of updated intake data</li>
 *   <li>Adds some special preprocessing of form values</li>
 *   <li>Inherits the standard autosave functionality from {@link AutoSaveComponentBase} </li>
 * </ul>
 * @author John Cameron
 */
@Directive()
export abstract class IntakeComponentBase extends AutoSaveComponentBase implements OnInit {
  /**
   * This is the candidate whose intake data we are entering
   */
  @Input() candidate: Candidate;

  /**
   * This is the existing candidate data (if any) which is used to
   * initialize the form data.
   */
  @Input() candidateIntakeData: CandidateIntakeData;

  /**
   * Index into a array member of candidateIntakeData if that is what is
   * being updated.
   */
  @Input() myRecordIndex: number;

  /**
   * Visa Check Object for selected country.
   */
  @Input() visaCheckRecord: CandidateVisa;

  @Input() editable: boolean = true;

  /**
   * Inject in a FormBuilder to create the form and CandidateService
   * to perform the saves.
   * @param fb FormBuilder
   * @param candidateService CandidateService which saves the intake data
   */
  protected constructor(protected fb: FormBuilder, private candidateService: CandidateService) {
    super();
  }

  /**
   * This must be implemented by subclass which should create and initialize
   * the form in this method using the FormBuilder inherited from here.
   * <p/>
   * The names of form controls are used to send the data to the server so they
   * must match the field names in CandidateIntakeDataUpdate.java, otherwise
   * they will be ignored and will not update the database.
   */
  abstract ngOnInit(): void;

  /**
   * Save the form data
   */
  doSave(formValue: any): Observable<any> {
    return this.candidateService.updateIntakeData(this.candidate.id, formValue);
  }

  /**
   * This must be implemented to do any processing following a successful save.
   * Typically that will involve updating the locally stored copy of the data that the form
   * is being used to update.
   */
  onSuccessfulSave(): void {
    //Nothing special to do
  }

  /**
   * Convert any multiselected enums
   */
  preprocessFormValues(formValue: Object): Object {
    return IntakeComponentBase.convertEnumOptions(formValue);
  }

  /**
   * Sets a standard no response value to the named form control.
   * @param formControlName Name of control in the intake form.
   */
  setNoResponse(formControlName: string) {
    this.form.controls[formControlName].setValue('NoResponse');
  };

  /**
   * Converts the data returned by multiselected enums to a simple array of
   * enum names suitable for sending to the server.
   * <p/>
   * We use ng-multiselect-dropdown for multiselect dropdowns, and given the
   * way that we have configured it for selecting enums, that component returns
   * arrays of EnumOption objects. This method converts that data to arrays of
   * strings corresponding to the enums.
   * <p/>
   * Note that the normal single select dropdown - where we use a standard
   * html <select> and options - returns a single string corresponding to the
   * selected enum - so not a problem there.
   * @param formValue Values returned from a form.
   * @private
   */
  private static convertEnumOptions(formValue: Object): Object {
    //Look through all the formValue object properties looking for a
    //property with a EnumOption array as a value.
    for (const [key, value] of Object.entries(formValue)) {
      if (IntakeComponentBase.isEnumOptionArray(value)) {
        //Convert EnumOption array to a simple string array.
        const enums: string[] = [];
        for (const item of value) {
          enums.push(item.value);
        }
        formValue[key] = enums;
      }
    }
    return formValue;
  }

  private static isEnumOptionArray(value: Object): boolean {
    let gotOne: boolean = false;
    //Needs to be an array
    if (Array.isArray(value)) {
      //With something in it
      if (value.length > 0) {
        //Look at first item in array and check its type
        const item = value[0];
        //EnumOption objects have a value and a displayText property.
        gotOne = ("value" in item && "displayText" in item);
      }
    }
    return gotOne;
  }
}
