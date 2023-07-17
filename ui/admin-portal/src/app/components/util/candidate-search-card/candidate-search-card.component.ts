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

import {
  AfterViewChecked,
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges,
  ViewChild
} from '@angular/core';
import {Candidate} from '../../../model/candidate';
import {User} from '../../../model/user';
import {CandidateSource} from '../../../model/base';
import {isSavedSearch} from "../../../model/saved-search";
import {isSavedList} from "../../../model/saved-list";
import {NgbNav, NgbNavChangeEvent} from "@ng-bootstrap/ng-bootstrap";
import {LocalStorageService} from "angular-2-local-storage";
import {AuthService} from "../../../services/auth.service";
import {AttachmentType, CandidateAttachment} from "../../../model/candidate-attachment";

@Component({
  selector: 'app-candidate-search-card',
  templateUrl: './candidate-search-card.component.html',
  styleUrls: ['./candidate-search-card.component.scss']
})
export class CandidateSearchCardComponent implements OnInit, AfterViewChecked {

  @Input() candidate: Candidate;
  @Input() loggedInUser: User;
  @Input() candidateSource: CandidateSource;
  @Input() sourceType: String;
  @Input() defaultSearch: boolean;
  @Input() savedSearchSelectionChange: boolean;

  @Output() closeEvent = new EventEmitter();

  showAttachments: boolean = false;
  showNotes: boolean = true;

  activeTabId: string;
  private lastTabKey: string = 'SelectedCandidateLastTab';

  //Get reference to the nav element
  @ViewChild('nav')
  nav: NgbNav;
  cvUrl: string;

  constructor(private localStorageService: LocalStorageService,
              private authService: AuthService) { }

  ngOnInit() {
  }

  ngAfterViewChecked(): void {
    //This is called in order for the navigation tabs, this.nav, to be set.
    this.selectDefaultTab();
  }

  close() {
    this.closeEvent.emit();
  }

  toggleAttachments() {
    this.showAttachments = !this.showAttachments;
  }

  toggleNotes() {
    this.showNotes = !this.showNotes;
  }

  get isList() {
    return isSavedList(this.candidateSource);
  }

  get isCandidateSelected(): boolean {
    return this.candidate.selected;
  }

  isContextNoteDisplayed() {
    let display: boolean = true;
    if (isSavedSearch(this.candidateSource)) {
      if (this.candidateSource.defaultSearch || !this.isCandidateSelected) {
        display = false;
      }
    }
    return display;
  }

  onTabChanged(event: NgbNavChangeEvent) {
    this.setActiveTabId(event.nextId);
  }

  private setActiveTabId(id: string) {
    this.nav?.select(id);
    this.localStorageService.set(this.lastTabKey, id);
  }

  private setActiveContextTabId(id: string) {
    this.navContext?.select(id);
    this.localStorageService.set(this.lastContextTabKey, id);
  }

  private selectDefaultTab() {
    const defaultActiveTabID: string = this.localStorageService.get(this.lastTabKey);
    this.setActiveTabId(defaultActiveTabID == null ? "general" : defaultActiveTabID);
  }

  canViewPrivateInfo() {
    return this.authService.canViewPrivateCandidateInfo(this.candidate);
  }

}
