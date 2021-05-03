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

import {Component, Input, OnChanges, OnInit, SimpleChanges} from '@angular/core';
import {NgbModal} from '@ng-bootstrap/ng-bootstrap';
import {Candidate} from '../../../../model/candidate';
import {CandidateLanguage} from '../../../../model/candidate-language';
import {CandidateLanguageService} from '../../../../services/candidate-language.service';
import {EditCandidateLanguageComponent} from '../language/edit/edit-candidate-language.component';
import {Subject} from "rxjs";

@Component({
  selector: 'app-view-candidate-language',
  templateUrl: './view-candidate-language.component.html',
  styleUrls: ['./view-candidate-language.component.scss']
})
export class ViewCandidateLanguageComponent implements OnInit, OnChanges {

  @Input() candidate: Candidate;
  @Input() editable: boolean;
  @Input() accordion: boolean = false;

  candidateLanguages: CandidateLanguage[];
  loading: boolean;
  error;

  activeIds: string;
  open: boolean;
  @Input() toggleAll: Subject<any>;

  constructor(private candidateLanguageService: CandidateLanguageService,
              private modalService: NgbModal ) {
  }

  ngOnInit() {
    /*
      If an accordion in intake set the subscribe to the toggle all buttons in intake candidate component
     */
    if (this.accordion) {
      this.activeIds = 'intake-language';
      this.open = true;
      // called when the toggleAll method is called in the parent component
      this.toggleAll.subscribe(isOpen => {
        this.open = isOpen;
        this.setActiveIds();
      })
    }
  }

  ngOnChanges(changes: SimpleChanges) {
    if (changes && changes.candidate && changes.candidate.previousValue !== changes.candidate.currentValue) {
      this.search();
    }
  }

  search() {
    this.loading = true;
    this.candidateLanguageService.list(this.candidate.id).subscribe(
      candidateLanguages => {
        this.candidateLanguages = candidateLanguages;
        this.loading = false;
      },
      error => {
        this.error = error;
        this.loading = false;
      })
    ;
  }

  editCandidateLanguage(candidateLanguage: CandidateLanguage) {
    const editCandidateLanguageModal = this.modalService.open(EditCandidateLanguageComponent, {
      centered: true,
      backdrop: 'static'
    });

    editCandidateLanguageModal.componentInstance.candidateLanguage = candidateLanguage;

    editCandidateLanguageModal.result
      .then((candidateLanguage) => this.search())
      .catch(() => { /* Isn't possible */ });

  }

  /*
    Methods related to the accordion toggle, and the toggle all parent observable
   */

  toggleOpen() {
    this.open = !this.open
    this.setActiveIds();
  }

  setActiveIds(){
    if (this.open) {
      this.activeIds = 'intake-language';
    } else {
      this.activeIds = '';
    }
  }


}
