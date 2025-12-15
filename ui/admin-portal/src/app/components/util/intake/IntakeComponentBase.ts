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

import {Directive, inject, Input, OnInit} from '@angular/core';
import {UntypedFormBuilder} from '@angular/forms';
import {Candidate, CandidateIntakeData, CandidateVisa} from '../../../model/candidate';
import {AutoSaveComponentBase} from "../autosave/AutoSaveComponentBase";
import {CandidateService} from "../../../services/candidate.service";
import {CrossTabSyncService} from "../../../services/cross-tab-sync.service";

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
  protected constructor(protected fb: UntypedFormBuilder, candidateService: CandidateService) {
    super(candidateService);
  }

  /**
   * The entity that we are working with in this class is a Candidate - this returns the entity
   * cast as a Candidate so that anyone referencing "candidate" will see the entity with all
   * its Candidate properties.
   */
  get candidate(): Candidate {
    return <Candidate>this.entity;
  }

  /**
   * Convert any multiselected enums
   */
  preprocessFormValues(formValue: Object): Object {
    return AutoSaveComponentBase.convertEnumOptions(formValue);
  }

  /**
   * Service used to notify other browser tabs when the candidate
   * intake data has been successfully saved.
   */
  protected crossTab = inject(CrossTabSyncService);

  /**
   * Called after a successful save.
   * Notifies other open tabs that newer intake data is available.
   */
  onSuccessfulSave(): void {
    this.crossTab.broadcastCandidateUpdated(this.candidate.id);
  }

  /**
   * Sets a standard no response value to the named form control.
   * @param formControlName Name of control in the intake form.
   */
  setNoResponse(formControlName: string) {
    this.form.controls[formControlName].setValue('NoResponse');
  };

  /**
   * Updates the CandidateIntakeData object with the changed form control value.
   * This is used to populate the values on the accordion panels.
   * @param formControlName Name of form control in intake form that we want to track changes of for update.
   */
  updateDataOnFieldChange(formControlName: string) {
    this.form.get(formControlName).valueChanges.subscribe((value) => {
      this.candidateIntakeData[formControlName] = value;
    })
  }
}
