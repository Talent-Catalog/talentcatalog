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

  ngAfterViewChecked(): void {
    //This is called in order for the navigation tabs, this.nav, to be set.
    this.selectDefaultTab();
  }

  ngOnChanges(changes: SimpleChanges): void {
    if (changes.candidate?.previousValue !== changes.candidate?.currentValue) {
      this.processUrlForIframe(this.cvForPreview);
    }
  }

  /**
   * Return the CV we want to display in the iframe - need to prioritise the listShareableCv &&
   * make sure it is a file that can be displayed and not automatically downloaded (see canPreviewCV method)
   */
  get cvForPreview(): CandidateAttachment {
    if (this.candidate?.listShareableCv && this.canPreviewCv(this.candidate?.listShareableCv)) {
      return this.candidate.listShareableCv;
    } else if (this.candidate?.shareableCv && this.canPreviewCv(this.candidate?.shareableCv)) {
      return this.candidate.shareableCv;
    } else {
      return null;
    }
  }

  /**
   * If it is a Google file, we need to alter the link by replacing anything after the file id in the link with /preview.
   * This is so it will work in the iframe.
   * @param cv
   */
  processUrlForIframe(cv: CandidateAttachment) {
    if (cv) {
      if (cv?.type == 'googlefile') {
        this.cvUrl = cv?.url.substring(0, cv?.url.lastIndexOf('/')) + '/preview'
      } else {
        this.cvUrl = cv?.url;
      }
    } else {
      // If a CV is unselected, update the selected CV for preview.
      this.cvForPreview;
    }
  }

  /**
   * If the CV is hosted on Amazon s3 bucket (older file uploads only) and the file is a doc/docx it
   * automatically is downloaded and not displayed in the iframe. We want to skip previewing these CVs
   * so need to filter these out.
   * @param cv
   */
  canPreviewCv(cv: CandidateAttachment): boolean {
    if(cv.type == AttachmentType.file) {
      return cv.fileType != ('doc' && 'docx');
    } else {
      return true;
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

}
