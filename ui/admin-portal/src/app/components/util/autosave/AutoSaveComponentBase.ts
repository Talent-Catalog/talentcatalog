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
import {AfterViewInit, Directive, OnDestroy, OnInit} from "@angular/core";
import {Observable, Subject} from "rxjs";
import {catchError, debounceTime, switchMap, takeUntil, tap} from "rxjs/operators";
import {FormGroup} from "@angular/forms";

/**
 * Base class for autosave components.
 * <p/>
 * Provides following standard functionality
 * <ul>
 *   <li>Implements autosave of form data after x seconds of inactivity</li>
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
   * This must be implemented by subclass which should create and initialize
   * the form in this method.
   */
  abstract ngOnInit(): void;

  /**
   * This must be implemented by subclass to save the current contents of the form.
   */
  abstract doSave(formValue: any): Observable<any>;

  /**
   * This must be implemented to do any processing following a successful save.
   * Typically that will involve updating the locally stored copy of the data that the form
   * is being used to update.
   */
  abstract onSuccessfulSave(): void;

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
  }
}
