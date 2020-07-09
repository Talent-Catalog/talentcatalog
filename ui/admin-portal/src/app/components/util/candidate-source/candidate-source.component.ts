import {Component, EventEmitter, Input, OnInit, Output, SimpleChanges} from '@angular/core';
import {
  copyCandidateSourceLinkToClipboard,
  isSavedSearch,
  SavedSearch
} from "../../../model/saved-search";
import {SavedSearchService} from "../../../services/saved-search.service";
import {AuthService} from "../../../services/auth.service";
import {User} from "../../../model/user";
import {CandidateSource, isMine} from "../../../model/base";
import {Router} from "@angular/router";
import {Location} from "@angular/common";

@Component({
  selector: 'app-candidate-source',
  templateUrl: './candidate-source.component.html',
  styleUrls: ['./candidate-source.component.scss']
})
export class CandidateSourceComponent implements OnInit {

  @Input() candidateSource: CandidateSource;
  @Input() showAll: boolean;
  @Input() showLink: boolean = true;
  @Input() showMore: boolean = true;
  @Input() showOpen: boolean = true;
  @Input() showWatch: boolean = true;
  @Input() showSelect: boolean = false;
  @Input() showCopy: boolean = false;
  @Input() showEdit: boolean = false;
  @Input() showDelete: boolean = false;
  @Output() openSource = new EventEmitter<CandidateSource>();
  @Output() selectSource = new EventEmitter<CandidateSource>();
  @Output() copySource = new EventEmitter<CandidateSource>();
  @Output() editSource = new EventEmitter<CandidateSource>();
  @Output() deleteSource = new EventEmitter<CandidateSource>();
  @Output() toggleWatch = new EventEmitter<CandidateSource>();

  loading;
  error;
  private loggedInUser: User;

  constructor(
    private savedSearchService: SavedSearchService,
    private location: Location,
    private router: Router,
    private authService: AuthService
  ) {
  }

  ngOnInit() {
    this.loggedInUser = this.authService.getLoggedInUser();
  }

  ngOnChanges(changes: SimpleChanges){
    // WHEN candidateSource changes IF showAll fetch the savedSearch object which has the multi select Names to display (not just Ids).
    if(this.showAll){
      this.getSavedSearch();
    }
  }

  toggleShowAll() {
    this.showAll = !this.showAll;
    if(this.showAll){
      this.loading = true;
      this.getSavedSearch();
    }
  }

  doOpenSource(){
    this.openSource.emit(this.candidateSource);
  }

  doSelectSource(){
    this.selectSource.emit(this.candidateSource);
  }

  doEditSource(){
    this.editSource.emit(this.candidateSource);
  }

  doCopySource(){
    this.copySource.emit(this.candidateSource);
  }

  doDeleteSource(){
    this.deleteSource.emit(this.candidateSource);
  }

  doCopyLink() {
    copyCandidateSourceLinkToClipboard(this.router, this.location, this.candidateSource);
  }

  doToggleWatch() {
    this.toggleWatch.emit(this.candidateSource);
  }

  isWatching(): boolean {
    return this.candidateSource.watcherUserIds === undefined ? false :
      this.candidateSource.watcherUserIds.indexOf(this.loggedInUser.id) >= 0;
  }

  get savedSearch(): SavedSearch {
    return isSavedSearch(this.candidateSource)
      ? this.candidateSource as SavedSearch : null;
  }

  isSavedSearch() {
    return isSavedSearch(this.candidateSource);
  }

  isShared() {
    return !isMine(this.candidateSource, this.authService);
  }

  isEditable() {
    return isMine(this.candidateSource, this.authService);
  }

  getSavedSearch() {
    this.savedSearchService.get(this.candidateSource.id).subscribe(result => {
      this.candidateSource = result;
      this.loading = false;
    }, err => {
      this.loading = false;
      this.error = err;
    })
  }

}
