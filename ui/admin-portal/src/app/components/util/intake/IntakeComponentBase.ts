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

import {AfterViewInit, Directive, Input, OnDestroy, OnInit} from '@angular/core';
import {catchError, debounceTime, map, switchMap, takeUntil, tap} from 'rxjs/operators';
import {Subject} from 'rxjs';
import {FormBuilder, FormGroup} from '@angular/forms';
import {Candidate, CandidateIntakeData} from '../../../model/candidate';
import {CandidateService} from '../../../services/candidate.service';

/**
 * Base class for all candidate intake components.
 * <p/>
 * Provides following standard functionality
 * <ul>
 *   <li>Implements autosave of form data after x seconds of inactivity</li>
 *   <li>Declares standard component @Inputs</li>
 *   <li>Provides standard "error" and "saving" attributes for display to user</li>
 *   <li>Provides FormGroup form variable for subclass to create and populate.
 *   The form should be created in the subclass's onInit method
 *   </li>
 * </ul>
 * @author John Cameron
 */
@Directive()
export abstract class IntakeComponentBase implements AfterViewInit, OnDestroy, OnInit {
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
   * Error which should be displayed to user if not null.
   * Typically an error connecting to the Spring server.
   */
  error: string;

  /**
   * Form containing the component's field(s).
   * This should be created and initialized in the subclass's ngOnInit method.
   */
  form: FormGroup;

  /**
   * Index into a array member of candidateIntakeData if that is what is
   * being updated.
   */
  @Input() myRecordIndex: number;

  /**
   * True when a save is underway. Should be used to show the user when a save
   * is happening.
   */
  saving: boolean;

  /**
   * True when a field is being entered before save. Should be used to show the user difference between typing and save.
   */
  typing: boolean;

  /**
   * Used to signal that subscription to form values should be dropped.
   * @see ngOnDestroy
   */
  private unsubscribe = new Subject<void>()

  @Input() editable: boolean = true;

  /**
   * Inject in a FormBuilder to create the form and CandidateService
   * to perform the saves.
   * @param fb FormBuilder
   * @param candidateService CandidateService which saves the intake data
   */
  protected constructor(
    protected fb: FormBuilder,
    private candidateService: CandidateService,
  ) {}

  /**
   * This must be implemented by subclass which should create and initialize
   * the form in this method.
   * <p/>
   * The names of form controls are used to send the data to the server so they
   * must match the field names in CandidateIntakeDataUpdate.java, otherwise
   * they will be ignored and will not update the database.
   */
  abstract ngOnInit(): void;

  /**
   * This is called after ngOnInit - ie after the form has been set up.
   * <p/>
   * It sets up the autosave.
   */
  ngAfterViewInit(): void {
    //1 second timeout
    this.setupAutosave(1000);
  }

  /**
   * Subscribes to changes in form data, saving form data after a period of
   * inactivity.
   * @param timeout Data will be saved after this many milliseconds of
   * inactivity
   */
  private setupAutosave(timeout: number) {
    this.form.valueChanges?.pipe(

      tap(() => this.typing = true),

      //Only pass values on if there has been inactivity for the given timeout
      debounceTime(timeout),

      //Convert any possible multiselected enums
      map(formValue => IntakeComponentBase.convertEnumOptions(formValue)),

      //Do a save of the received form values.
      switchMap(formValue => {
          this.typing = false;
          this.error = null;
          this.saving = true;
          return this.candidateService.updateIntakeData(this.candidate.id, formValue);
        }
      ),

      //We catch errors, copying them to this.error, but then just continuing
      catchError((error, caught) => {
        this.saving = false;
        this.error = error;
        return caught;
      }),

      //Subscription will continue until the given Observable emits.
      //See ngOnDestroy
      takeUntil(this.unsubscribe)
    ).subscribe(

      //Save has completed successfully
      () => this.saving = false,

      //Theoretically never get here because we catch errors in the pipe
      (error) => {
        this.saving = false;
        this.error = error;
      }
    )
  }

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
      } else {
        // If not enum it will be a list of ids.
        return formValue;
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

  /**
   * When the component is destroyed we need to stop subscribing
   * (otherwise we get a memory leak)
   */
  ngOnDestroy(): void {
    //Stop subscribing by emitting a value from the Unsubscribe Observable
    //See takeUntil in the above pipe.
    this.unsubscribe.next();
  }
}
