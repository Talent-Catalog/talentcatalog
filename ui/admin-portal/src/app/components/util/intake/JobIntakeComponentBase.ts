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
import {FormBuilder} from '@angular/forms';
import {AutoSaveComponentBase} from "../autosave/AutoSaveComponentBase";
import {Job, JobIntakeData} from "../../../model/job";
import {IntakeService} from "./IntakeService";

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
export abstract class JobIntakeComponentBase extends AutoSaveComponentBase implements OnInit {

  /**
   * This is the existing candidate data (if any) which is used to
   * initialize the form data.
   */
  @Input() jobIntakeData: JobIntakeData;

  /**
   * Index into a array member of data if that is what is being updated.
   */
  @Input() myRecordIndex: number;

  @Input() editable: boolean = true;

  /**
   * Inject in a FormBuilder to create the form and an IntakeService to perform the saves.
   * @param fb FormBuilder
   * @param intakeService IntakeService which saves the intake data
   */
  protected constructor(protected fb: FormBuilder, intakeService: IntakeService) {
    super(intakeService);
  }

  /**
   * The entity that we are working with in this class is a Job - this returns the entity
   * cast as a Job so that anyone referencing "job" will see the entity with all
   * its Job properties.
   */
  get job(): Job {
    return <Job>this.entity;
  }

  /**
   * This must be implemented by subclass which should create and initialize
   * the form in this method using the FormBuilder inherited from here.
   * <p/>
   * The names of form controls are used to send the data to the server so they
   * must match the field names in JobIntakeDataUpdate.java, otherwise
   * they will be ignored and will not update the database.
   */
  abstract ngOnInit(): void;

  /**
   * This must be implemented to do any processing following a successful save.
   * Typically, that will involve updating the locally stored copy of the data that the form
   * is being used to update.
   */
  onSuccessfulSave(): void {
    //Nothing special to do
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
