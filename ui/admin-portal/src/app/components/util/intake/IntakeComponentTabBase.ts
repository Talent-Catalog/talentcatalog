/*
 * Copyright (c) 2020 Talent Beyond Boundaries. All rights reserved.
 */

import {Directive, Input, OnInit} from '@angular/core';
import {forkJoin} from 'rxjs';
import {Candidate, CandidateIntakeData} from '../../../model/candidate';
import {CandidateService} from '../../../services/candidate.service';
import {Nationality} from "../../../model/nationality";
import {NationalityService} from "../../../services/nationality.service";
import {CountryService} from "../../../services/country.service";
import {Country} from "../../../model/country";

/**
 * Base class for all candidate intake tab components.
 * <p/>
 * Provides following standard functionality
 * <ul>
 *   <li>Loads standard data required by all intake tabs
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
   * All standard countries
   */
  countries: Country[];

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
    private countryService: CountryService,
    private nationalityService: NationalityService
  ) { }

  ngOnInit(): void {
    this.refreshIntakeDataInternal(true);
  }

  /**
   * Loads all the candidate's intake data from the server, as well as
   * standard data used for intake data entry, such as lists of all countries
   * and nationalities.
   */
  refreshIntakeData(): void {
    this.refreshIntakeDataInternal(false)
  }

  private refreshIntakeDataInternal(init: boolean): void {
    //Load existing candidateIntakeData and other data needed by intake
    this.error = null;
    this.loading = true;
    forkJoin({
      'countries': this.countryService.listCountries(),
      'nationalities': this.nationalityService.listNationalities(),
      'intakeData':  this.candidateService.getIntakeData(this.candidate.id),
    }).subscribe(results => {
      this.loading = false;
      this.countries = results['countries'];
      this.nationalities = results['nationalities'];
      this.candidateIntakeData = results['intakeData'];
      this.onDataLoaded(init);
    }, error => {
      this.loading = false;
      this.error = error;
    });
  }

  /**
   * Called when all intake data has been loaded (by refreshIntakeData).
   * <p/>
   * Default implementation does nothing but can be overridden in subclasses
   * to perform extra processing once data has been loaded.
   * @param init True if data was loaded as part of ngOnInit. If false
   * the data loading was triggered by directly calling refreshIntakeData
   * externally to do a data refresh.
   */
  protected onDataLoaded(init: boolean) {}

}
