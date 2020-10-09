/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

import {Directive, Input, OnInit} from '@angular/core';
import {forkJoin} from 'rxjs';
import {Candidate, CandidateIntakeData} from '../../../model/candidate';
import {CandidateService} from '../../../services/candidate.service';
import {Nationality} from "../../../model/nationality";
import {NationalityService} from "../../../services/nationality.service";

/**
 * Base class for all candidate intake tab components.
 * <p/>
 * Provides following standard functionality
 * <ul>
 *   <li>Loads standard data required by intake components
 *   <li>Declares standard candidate @Input</li>
 *   <li>Provides standard "error" and "loading" attributes for display to user</li>
 * </ul>
 * @author John Cameron
 */
@Directive()
export abstract class IntakeComponentTabBase implements OnInit {
  /**
   * This is the candidate whose intake data we are entering
   */
  @Input() candidate: Candidate;

  /**
   * This is the existing candidate intake data (if any) which is used to
   * initialize the form data.
   */
  candidateIntakeData: CandidateIntakeData;

  /**
   * Error which should be displayed to user if not null.
   * Typically an error connecting to the Spring server.
   */
  error: string;

  /**
   * True when loading is underway. Should be used to show the user when a load
   * is happening.
   */
  loading: boolean;

  /**
   * All standard nationalities
   */
  nationalities: Nationality[];

  protected constructor(
    private candidateService: CandidateService,
    private nationalityService: NationalityService
  ) { }

  ngOnInit(): void {
    //Load existing candidateIntakeData and other data needed by intake
    this.error = null;
    this.loading = true;
    forkJoin({
      'nationalities': this.nationalityService.listNationalities(),
      'intakeData':  this.candidateService.getIntakeData(this.candidate.id),
    }).subscribe(results => {
      this.loading = false;
      this.nationalities = results['nationalities'];
      this.candidateIntakeData = results['intakeData'];
    }, error => {
      this.loading = false;
      this.error = error;
    });
  }

}
