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
import {AfterViewInit, Directive, Input, OnDestroy, OnInit} from "@angular/core";
import {Observable, Subject} from "rxjs";
import {catchError, debounceTime, map, switchMap, takeUntil, tap} from "rxjs/operators";
import {UntypedFormGroup} from "@angular/forms";
import {isEnumOptionArray} from "../../../util/enum";
import {HasId} from "../../../model/base";

//See https://stackoverflow.com/questions/40841641/cannot-import-exported-interface-export-not-found
import type {IntakeService} from "../intake/IntakeService";

/**
 * Base class for autosave components.
 * <p/>
 * Provides following standard functionality
 * <ul>
 *   <li>Implements autosaving of form data after x seconds of inactivity</li>
 *   <li>Provides standard "error" and "saving" attributes for display to user</li>
 *   <li>Provides FormGroup form variable for subclass to create and populate.
 *   The form should be created in the subclass's onInit method
 *   </li>
 *   <li>Provides a call back for successful saves, allowing the subclass to update local data</li>
 * </ul>
 * @author John Cameron
 */
@Directive()
export abstract class AutoSaveComponentBase implements AfterViewInit, OnDestroy, OnInit {

  @Input() entity: HasId;

  /**
   * Error which should be displayed to user if not null.
   * Typically, an error connecting to the Spring server.
   */
  error: string;

  /**
   * Form containing the component's field(s).
   * This should be created and initialized in the subclass's ngOnInit method.
   */
  form: UntypedFormGroup;

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

  /**
   * If using the default save implementation, a non-null service that implements IntakeService
   * must be passed in.
   * If the subclass is overriding doSave to manage the saving themselves, they can pass in a
   * null service.
   * @param intakeService If non-null, will be used to perform saves using the default doSave.
   * @protected
   */
  protected constructor(private intakeService: IntakeService) {
  }

  /**
   * This must be implemented by subclass which should create and initialize
   * the form in this method using the FormBuilder inherited from here.
   * <p/>
   * The names of form controls are used to send the data to the server so they
   * must match the field names in the corresponding server IntakeDataUpdate.java class, otherwise
   * they will be ignored and will not update the database.
   */
  abstract ngOnInit(): void;

  /**
   * Default is to save the form data using the intakeService
   */
  doSave(formValue: any): Observable<any>{
    if (this.intakeService) {
      return this.intakeService.updateIntakeData(this.entity.id, formValue);
    }
    //todo Return an error or throw an exception if intakeService has not been set.
  }

  /**
   * This can be overridden to do any processing following a successful save.
   * Typically, that will involve updating the locally stored copy of the data that the form
   * is being used to update.
   */
  onSuccessfulSave(): void {
    //Nothing special to do
  }

  /**
   * Override this if you want to change the formValue before processing it.
   * <p/>
   * This gets called by the "map" method in the autosave pipe - see {@link #setupAutosave} below.
   * <p/>
   * The default implementation is just to return the formValue unchanged.
   * @param formValue Form values as currently entered
   */
  preprocessFormValues(formValue: Object): Object {
    //Default is no preprocessing - just pass form values on unchanged
    return formValue;
  }

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

      //Hook into form values preprocessor
      map(formValue => this.preprocessFormValues(formValue)),

      //Do a save of the received form values.
      switchMap(formValue => {
          this.typing = false;
          this.error = null;
          this.saving = true;
          return this.doSave(formValue);
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
      () => {
        this.saving = false;
        this.onSuccessfulSave();
      },

      //Theoretically never get here because we catch errors in the pipe
      (error) => {
        this.saving = false;
        this.error = error;
      }
    )
  }

  /**
   * When the component is destroyed we need to stop subscribing
   * (otherwise we get a memory leak)
   */
  ngOnDestroy(): void {
    //Stop subscribing by emitting a value from the Unsubscribe Observable
    //See takeUntil in the above pipe.
    this.unsubscribe.next();
    this.unsubscribe.complete();
  }


  /**
   * Converts the data returned by multiselected enums to a simple array of
   * enum keys suitable for sending to the server.
   * <p/>
   * We use ng-select for multiselect dropdowns, and given the
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
  protected static convertEnumOptions(formValue: Object): Object {
    //Look through all the formValue object properties looking for a
    //property with a EnumOption array as a value.
    for (const [key, value] of Object.entries(formValue)) {
      if (isEnumOptionArray(value)) {
        //Convert EnumOption array to a simple string array.
        const enums: string[] = [];
        for (const enumOption of value) {
          enums.push(enumOption.key);
        }
        formValue[key] = enums;
      }
    }
    return formValue;
  }

}
