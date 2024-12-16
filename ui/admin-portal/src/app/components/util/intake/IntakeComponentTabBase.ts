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

import {Directive, Input, OnInit} from '@angular/core';
import {forkJoin} from 'rxjs';
import {Candidate, CandidateIntakeData} from '../../../model/candidate';
import {CandidateService, IntakeAuditRequest} from '../../../services/candidate.service';
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
import {dateString} from '../../../util/date-adapter/date-adapter';
import {AuthenticationService} from "../../../services/authentication.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {ConfirmationComponent} from "../confirm/confirmation.component";
import {OldIntakeInputComponent} from "../old-intake-input-modal/old-intake-input.component";

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
  tcDestinations: Country[];

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
    protected modalService: NgbModal
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
      'tcDestinations': this.countryService.listTCDestinations(),
      'nationalities': this.countryService.listCountries(),
      'educationLevels': this.educationLevelService.listEducationLevels(),
      'occupations': this.occupationService.listOccupations(),
      'languageLevels': this.languageLevelService.listLanguageLevels(),
      'intakeData':  this.candidateService.getIntakeData(this.candidate.id),
      'candidate': this.candidateService.get(this.candidate.id)
    }).subscribe(results => {
      this.loading = false;
      this.countries = results['countries'];
      this.tcDestinations = results['tcDestinations'];
      this.nationalities = results['nationalities'];
      this.educationLevels = results['educationLevels'];
      this.occupations = results['occupations'];
      this.languageLevels = results['languageLevels'];
      this.candidateIntakeData = results['intakeData'];
      this.candidate = results['candidate'];
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
   * @param btnType is either update or complete.
   */
  public createIntakeNote(formName: string, btnType: string) {
    this.saving = true;
    this.loggedInUser = this.authenticationService.getLoggedInUser();
    if (btnType === "update") {
       this.noteRequest = {
        candidateId: this.candidate.id,
        title: formName + ' interview updated by ' + this.makeUserName(this.loggedInUser)
          + ' on ' + dateString(new Date()) + '.',
      };
    } else if (btnType === "complete") {
      this.noteRequest = {
        candidateId: this.candidate.id,
        title: formName + ' interview completed by ' + this.makeUserName(this.loggedInUser)
          + ' on ' + dateString(new Date()) + '.',
      };
    }
    this.noteService.create(this.noteRequest).subscribe(
      (candidateNote) => {
        this.saving = false;
      }, (error) => {
        this.error = error;
        this.saving = false;
      })
  };

  private makeUserName(user: User): string {
    let name = user.firstName + ' ' + user.lastName;
    if (user.partner?.abbreviation) {
      name += '(' + user.partner.abbreviation + ')';
    }
    return name;
  }

  /**
   * Populates the completedBy completedDate fields for a particular intake (mini or full)
   * @param full boolean. If true, completing full intake. If false, completing mini intake.
   */
  completeIntake(full: boolean) {
    const completeIntakeModal = this.modalService.open(ConfirmationComponent, {
      centered: true,
      backdrop: 'static'
    })

    let intake = full ? 'Full' : 'Mini'
    completeIntakeModal.componentInstance.title = "Mark the " + intake + " Intake as complete?";
    completeIntakeModal.componentInstance.message =
      "This will mark the candidate as having had the intake completed and store the audit data of " +
      "completion (who & when). Note: An intake can only be completed ONCE. " +
      "Once completed, updates can be made to the intakes anytime, just click the Update Intake " +
      "button to keep track of who/when completed the update.";

    // Completed date is null, as this is a new intake and it will be set server side.
    let request: IntakeAuditRequest = {
      completedDate: null,
      fullIntake: full
    }

    completeIntakeModal.result
    .then((result) => {
      if (result == true) {
        this.saving = true;
        this.candidateService.completeIntake(this.candidate.id, request).subscribe(
          (candidate)=> {
            this.candidate = candidate;
            this.refreshIntakeData();
            //todo look at refactoring this note method (I don't need to update the button text, I can use new fields to show/disabled or not.)
            let intakeType: string = full ? 'Full Intake' : 'Mini Intake'
            this.createIntakeNote(intakeType, 'complete');
            this.saving = false;
          }, (error) => {
            this.error = error;
            this.saving = false;
          }
        )
      }
    })
    .catch(() => {});
  }

  public inputOldIntake(full: boolean) {
    // Popup modal to gather who and when.
    const oldIntakeInputModal = this.modalService.open(OldIntakeInputComponent, {
      centered: true,
      backdrop: 'static'
    });

    oldIntakeInputModal.componentInstance.fullIntake = full;
    oldIntakeInputModal.componentInstance.candidate = this.candidate;

    oldIntakeInputModal.result
      .then((candidate) => {
        this.candidate = candidate;
        this.refreshIntakeData();
        this.saving = false;
      }, (error) => {
        this.error = error;
        this.saving = false;
      })
      .catch(() => { /* Isn't possible */
      });
  }

}
