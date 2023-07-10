import {AfterViewChecked, Component, Input, OnInit, ViewChild} from '@angular/core';
import {NgbActiveOffcanvas, NgbNav, NgbNavChangeEvent} from "@ng-bootstrap/ng-bootstrap";
import {Candidate} from "../../../model/candidate";
import {LocalStorageService} from "angular-2-local-storage";
import {isSavedList} from "../../../model/saved-list";
import {CandidateSource} from "../../../model/base";
import {isSavedSearch} from "../../../model/saved-search";
import {AuthService} from "../../../services/auth.service";

@Component({
  selector: 'app-candidate-side-profile',
  templateUrl: './candidate-side-profile.component.html',
  styleUrls: ['./candidate-side-profile.component.scss']
})
export class CandidateSideProfileComponent implements OnInit, AfterViewChecked {

  @Input() candidate: Candidate;
  @Input() candidateSource: CandidateSource;

  showAttachments: boolean = false;
  showNotes: boolean = true;

  //Get reference to the nav element
  @ViewChild(NgbNav)
  nav: NgbNav;
  activeTabId: string;
  private lastTabKey: string = 'SelectedCandidateLastTab';


  constructor(public activeOffcanvas: NgbActiveOffcanvas,
              private localStorageService: LocalStorageService,
              private authService: AuthService) {
  }

  ngOnInit(): void {
  }

  toggleNotes() {
    this.showNotes = !this.showNotes;
  }

  toggleAttachments() {
    this.showAttachments = !this.showAttachments;
  }

  ngAfterViewChecked(): void {
    //This is called in order for the navigation tabs, this.nav, to be set.
    this.selectDefaultTab()
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

  canViewPrivateInfo() {
    return this.authService.canViewPrivateCandidateInfo(this.candidate);
  }
}
