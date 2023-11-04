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
import {forkJoin} from 'rxjs';
import {Candidate, CandidateIntakeData} from '../../../model/candidate';
import {CandidateService} from '../../../services/candidate.service';
import {CountryService} from '../../../services/country.service';
import {Country} from '../../../model/country';
import {EducationLevel} from '../../../model/education-level';
import {EducationLevelService} from '../../../services/education-level.service';
import {Occupation} from '../../../model/occupation';
import {OccupationService} from '../../../services/occupation.service';
import {LanguageLevelService} from '../../../services/language-level.service';
import {LanguageLevel} from '../../../model/language-level';

import html2canvas from 'html2canvas';
import jsPDF from 'jspdf';
import {
  CandidateNoteService,
  CreateCandidateNoteRequest
} from '../../../services/candidate-note.service';
import {User} from '../../../model/user';
import {AuthService} from '../../../services/auth.service';
import {dateString} from '../../../util/date-adapter/date-adapter';
import {AuthenticationService} from "../../../services/authentication.service";

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
   * True when saving is underway. Should be used to show the user when a save
   * is happening.
   */
  saving: boolean;

  /**
   * All standard nationalities
   */
  nationalities: Country[];

  /**
   * All TBB destinations
   */
  tbbDestinations: Country[];

  /**
   * All Education Levels
   */
  educationLevels: EducationLevel[];

  /**
   * All Occupations
   */
  occupations: Occupation[];

  /**
   * All Occupations
   */
  languageLevels: LanguageLevel[];

  /**
   * Logged in User for audit
   */
  loggedInUser: User;

  /**
   * Note request for intake start and update.
   */
  noteRequest: CreateCandidateNoteRequest;

  public constructor(
    protected candidateService: CandidateService,
    protected countryService: CountryService,
    protected educationLevelService: EducationLevelService,
    protected occupationService: OccupationService,
    protected languageLevelService: LanguageLevelService,
    protected noteService: CandidateNoteService,
    protected authenticationService: AuthenticationService,
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
      'tbbDestinations': this.countryService.listTBBDestinations(),
      'nationalities': this.countryService.listCountries(),
      'educationLevels': this.educationLevelService.listEducationLevels(),
      'occupations': this.occupationService.listOccupations(),
      'languageLevels': this.languageLevelService.listLanguageLevels(),
      'intakeData':  this.candidateService.getIntakeData(this.candidate.id),
    }).subscribe(results => {
      this.loading = false;
      this.countries = results['countries'];
      this.tbbDestinations = results['tbbDestinations'];
      this.nationalities = results['nationalities'];
      this.educationLevels = results['educationLevels'];
      this.occupations = results['occupations'];
      this.languageLevels = results['languageLevels'];
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

  /**
   * Called when export button on intake forms is clicked. Exports the div containing
   * the forms and downloads the file.
   * @param formName is the id of the div container explain which form relates to.
   */
  public exportAsPdf(formName: string) {
    // parent div is the html element which has to be converted to PDF
    this.saving = true;
    const element = document.getElementById(formName);
    html2canvas(element, {scrollY: -window.scrollY, scale: 1}).then(canvas => {
      const heightRatio = canvas.height / canvas.width;
      //const widthRadio = canvas.width / canvas.height;
      //jsPdf has a max height of 14440 so set the height less than this
      let pdf;
      let height;
      let width;
      if (canvas.height > canvas.width) {
        // Make the PDF the same size as the canvas content
        height = 14000;
        width = height / heightRatio;
          pdf = new jsPDF('p', 'pt', [width, height]);
      } else {
        // Make the PDF a landscape size.
        width = canvas.width / heightRatio;
        height = width * heightRatio;
        pdf = new jsPDF('l', 'pt', [width, height]);
      }
      const imgData  = canvas.toDataURL("image/jpeg", 1.0);
      pdf.addImage(imgData, 0, 0, width, height);
      pdf.save(formName + '_' + this.candidate.user.firstName + '_' + this.candidate.user.lastName + '.pdf');
      this.saving = false;
    })
  };

  /**
   * Called when Start, update or complete buttons on intake forms are clicked.
   * Creates an appropriate note with user/time.
   * @param formName is the type of intake interview used for the comment/title.
   * @param btnType is either start, update or complete.
   * @param button is the button that's clicked, used to change the button text on click.
   */
  public createIntakeNote(formName: string, btnType: string, button) {
    this.loggedInUser = this.authenticationService.getLoggedInUser();
    let btnText: string;
    if (btnType === "update") {
       this.noteRequest = {
        candidateId: this.candidate.id,
        title: formName + ' interview updated by ' + this.makeUserName(this.loggedInUser)
          + ' on ' + dateString(new Date()) + '.',
      };
       btnText = 'Updated!';
    } else if (btnType === "complete") {
      this.noteRequest = {
        candidateId: this.candidate.id,
        title: formName + ' interview completed by ' + this.makeUserName(this.loggedInUser)
          + ' on ' + dateString(new Date()) + '.',
      };
      btnText = 'Completed!';
    } else {
      this.noteRequest = {
        candidateId: this.candidate.id,
        title: formName + ' interview started by ' + this.makeUserName(this.loggedInUser)
          + ' on ' + dateString(new Date()) + '.',
      };
      btnText = 'Started!';
    }
    this.noteService.create(this.noteRequest).subscribe(
      (candidateNote) => {
        button.textContent = btnText;
      }, (error) => {
        this.error = error;
      })
  };

  private makeUserName(user: User): string {
    let name = user.firstName + ' ' + user.lastName;
    if (user.partner?.abbreviation) {
      name += '(' + user.partner.abbreviation + ')';
    }
    return name;
  }

}
