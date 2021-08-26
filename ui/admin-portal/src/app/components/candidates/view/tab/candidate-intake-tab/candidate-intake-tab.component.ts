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

import {Component, QueryList, ViewChildren} from '@angular/core';
import {IntakeComponentTabBase} from "../../../../util/intake/IntakeComponentTabBase";
import {Subject} from "rxjs/index";
import {NgbAccordion, NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {OldIntakeInputComponent} from "../../../../util/old-intake-input-modal/old-intake-input.component";
import {CandidateService} from "../../../../../services/candidate.service";
import {CountryService} from "../../../../../services/country.service";
import {EducationLevelService} from "../../../../../services/education-level.service";
import {OccupationService} from "../../../../../services/occupation.service";
import {LanguageLevelService} from "../../../../../services/language-level.service";
import {CandidateNoteService} from "../../../../../services/candidate-note.service";
import {AuthService} from "../../../../../services/auth.service";

@Component({
  selector: 'app-candidate-intake-tab',
  templateUrl: './candidate-intake-tab.component.html',
  styleUrls: ['./candidate-intake-tab.component.scss']
})
export class CandidateIntakeTabComponent extends IntakeComponentTabBase {
  toggleAll: Subject<any> = new Subject();
  activeIds: string[] = ['intake-confirm', 'intake-int-recruit', 'intake-english-assessment', 'intake-residency',
    'intake-host-country', 'intake-registration', 'intake-partner-info', 'intake-additional-eligibility', 'intake-final-agreement']

  @ViewChildren(NgbAccordion) accs: QueryList<NgbAccordion>;

  clickedOldIntake: boolean;

  constructor(candidateService: CandidateService,
              countryService: CountryService,
              educationLevelService: EducationLevelService,
              occupationService: OccupationService,
              languageLevelService: LanguageLevelService,
              noteService: CandidateNoteService,
              authService: AuthService,
              private modalService: NgbModal) {
    super(candidateService, countryService, educationLevelService, occupationService, languageLevelService, noteService, authService)
  }

  togglePanels(openAll: boolean) {
    this.toggleAll.next(openAll);
    if (openAll) {
      this.accs.forEach(acc => {
        acc.expandAll();
      })
    } else {
      this.accs.forEach(acc => {
        acc.collapseAll();
      })
    }
  }

  public inputOldIntakeNote(formName: string, button) {
    this.clickedOldIntake = true;
    // Popup modal to gather who and when.
    const oldIntakeInputModal = this.modalService.open(OldIntakeInputComponent, {
      centered: true,
      backdrop: 'static'
    });

    oldIntakeInputModal.componentInstance.candidateId = this.candidate.id;
    oldIntakeInputModal.componentInstance.formName = formName;

    oldIntakeInputModal.result
      .then((country) => button.textContent = 'Note created!')
      .catch(() => { /* Isn't possible */
      });
  }

}
