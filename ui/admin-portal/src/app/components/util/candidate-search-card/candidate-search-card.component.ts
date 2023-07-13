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
import {CandidateAttachment} from "../../../model/candidate-attachment";

@Component({
  selector: 'app-candidate-search-card',
  templateUrl: './candidate-search-card.component.html',
  styleUrls: ['./candidate-search-card.component.scss']
})
export class CandidateSearchCardComponent implements OnInit, AfterViewChecked, OnChanges {

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

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.candidate?.previousValue !== changes.candidate?.currentValue) {
      this.previewCVLink();
    }
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

  ngAfterViewChecked(): void {
    //This is called in order for the navigation tabs, this.nav, to be set.
    this.selectDefaultTab();
  }

  onTabChanged(event: NgbNavChangeEvent) {
    this.setActiveTabId(event.nextId);
  }

  private setActiveTabId(id: string) {
    this.nav?.select(id);
    this.localStorageService.set(this.lastTabKey, id);
  }

  private selectDefaultTab() {
    const defaultActiveTabID: string = this.localStorageService.get(this.lastTabKey);
    this.setActiveTabId(defaultActiveTabID == null ? "general" : defaultActiveTabID);
  }

  canViewPrivateInfo() {
    return this.authService.canViewPrivateCandidateInfo(this.candidate);
  }

  updateShareableCVPreview() {
    this.previewCVLink();
  }

  previewCVLink() {
    /**
     * Sanitise the shareable CV link to avoid XSS errors when showing in iframe.
     * See more here:
     */
    if (this.candidate?.listShareableCv) {
      this.processCVForIframe(this.candidate?.listShareableCv);
    } else if (this.candidate?.shareableCv) {
      this.processCVForIframe(this.candidate?.shareableCv);
    } else {
      this.cvUrl = null
    }
  }

  /**
   * Useful Stack Overflow about using iframe with word docs. Currently word docs in S3 buckets are being downloaded.
   * However, the s3 buckets are depreciated so only contain older documents, word docs stored the Google drive are displaying.
   * See here: https://stackoverflow.com/a/27958186
   * @param cv
   */
  processCVForIframe(cv: CandidateAttachment) {
    /**
     * If it is a Google file, we need to alter the link by replacing anything after the file id in the link with /preview.
     * This is so it will work in the iframe.
     */
    if (cv?.type == 'googlefile') {
      this.cvUrl = cv?.url.substring(0, cv?.url.lastIndexOf('/')) + '/preview'
    } else if (this.candidate?.listShareableCv?.type == 'file') {
      this.cvUrl = cv?.url;
    }
  }

}
