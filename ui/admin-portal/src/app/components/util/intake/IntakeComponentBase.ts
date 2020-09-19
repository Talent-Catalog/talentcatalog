/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

import {AfterViewInit, Input, OnDestroy, OnInit} from "@angular/core";
import {catchError, debounceTime, switchMap, takeUntil} from "rxjs/operators";
import {Subject} from "rxjs";
import {FormBuilder, FormGroup} from "@angular/forms";
import {Candidate, CandidateIntakeData} from "../../../model/candidate";
import {CandidateService} from "../../../services/candidate.service";

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
   * True when a save is underway. Should be used to show the user when a save
   * is happening.
   */
  saving: boolean;

  /**
   * Used to signal that subscription to form values should be dropped.
   * @see ngOnDestroy
   */
  private unsubscribe = new Subject<void>()

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
   */
  abstract ngOnInit(): void;

  /**
   * This is called after ngOnInit - ie after the form has been set up.
   * <p/>
   * It sets up the autosave.
   */
  ngAfterViewInit(): void {
    //3 second timeout
    this.setupAutosave(3000);
  }

  /**
   * Subscribes to changes in form data, saving form data after a period of
   * inactivity.
   * @param timeout Data will be saved after this many milliseconds of
   * inactivity
   */
  private setupAutosave(timeout: number) {
    this.form.valueChanges.pipe(

      //Only pass values on if there has been inactivity for the given timeout
      debounceTime(timeout),

      //Do a save of the received form values.
      switchMap(formValue => {
          //Update the candidateIntakeData to keep it in sync with the server
          //saved version.
          //Object assign just copies the formValue fields across, leaving any
          //other fields in candidateIntakeData unchanged.
          Object.assign(this.candidateIntakeData, formValue);
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
   * When the component is destroyed we need to stop subscribing
   * (otherwise we get a memory leak)
   */
  ngOnDestroy(): void {
    //Stop subscribing by emitting a value from the Unsubscribe Observable
    //See takeUntil in the above pipe.
    this.unsubscribe.next();
  }

}
