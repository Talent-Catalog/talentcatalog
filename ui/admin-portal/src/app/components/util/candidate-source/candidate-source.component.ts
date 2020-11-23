import {
  Component,
  EventEmitter,
  Input,
  OnChanges,
  OnInit,
  Output,
  SimpleChanges
} from '@angular/core';
import {
  getCandidateSourceExternalHref,
  getSavedSourceNavigation,
  isSavedSearch,
  SavedSearch,
  SavedSearchRef
} from '../../../model/saved-search';
import {SavedSearchService} from '../../../services/saved-search.service';
import {AuthService} from '../../../services/auth.service';
import {User} from '../../../model/user';
import {CandidateSource, canEditSource, isMine} from '../../../model/base';
import {Router} from '@angular/router';
import {Location} from '@angular/common';
import {copyToClipboard} from '../../../util/clipboard';
import {isSavedList} from "../../../model/saved-list";

@Component({
  selector: 'app-candidate-source',
  templateUrl: './candidate-source.component.html',
  styleUrls: ['./candidate-source.component.scss']
})
export class CandidateSourceComponent implements OnInit, OnChanges {

  // The data processing is dependant on these variables
  // The candidate source passed in
  @Input() candidateSource: CandidateSource;
  // If the extra data can be loaded for the dropdowns (get request)
  @Input() canLoad: boolean = true;
  // Handles the toggle state of the candidate source (show less/show more)
  @Input() seeMore: boolean;

  // The font awesome icon buttons are dependant on these variables
  @Input() showLink: boolean = true;
  @Input() showMore: boolean = true;
  @Input() showOpen: boolean = true;
  @Input() showWatch: boolean = true;
  @Input() showSelect: boolean = false;
  @Input() showCopy: boolean = false;
  @Input() showEdit: boolean = false;
  @Input() showDelete: boolean = false;
  @Input() showAudit: boolean = false;

  @Output() openSource = new EventEmitter<CandidateSource>();
  @Output() selectSource = new EventEmitter<CandidateSource>();
  @Output() copySource = new EventEmitter<CandidateSource>();
  @Output() editSource = new EventEmitter<CandidateSource>();
  @Output() deleteSource = new EventEmitter<CandidateSource>();
  @Output() selectColumns = new EventEmitter<CandidateSource>();
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

  ngOnChanges (changes: SimpleChanges){
    // WHEN candidateSource changes IF showAll fetch the savedSearch object
    // which has the multi select Names to display (not just Ids).
    if (this.seeMore && changes && changes.candidateSource
      && changes.candidateSource.previousValue !== changes.candidateSource.currentValue) {

      //Only fetch if we have an id - otherwise changes will just be local
      //modifications of a candidate source which has not been created yet.
      if (this.candidateSource.id) {
        this.getSavedSearch(this.candidateSource.id);
      }
    }
  }

  toggleShowMore() {
    this.seeMore = !this.seeMore;
    // Get extra data from saved search if needed (canLoad:true)
    if (this.canLoad) {
      this.loading = true;
      this.getSavedSearch(this.candidateSource.id);
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
    copyToClipboard(getCandidateSourceExternalHref(
      this.router, this.location, this.candidateSource));
  }

  doSelectColumns() {
    this.selectColumns.emit(this.candidateSource);
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
    return canEditSource(this.candidateSource, this.authService);
  }

  isRemovable() {
    // Can't delete global searches
    if (isSavedSearch(this.candidateSource)){
      return !this.candidateSource.global;
    } else {
      return true;
    }
  }

  getSavedSearch(savedSearchId: number) {
    this.savedSearchService.get(savedSearchId).subscribe(result => {
      this.candidateSource = result;
      this.loading = false;
    }, err => {
      this.loading = false;
      this.error = err;
    })
  }

  private getSavedSearchSource(): SavedSearchRef {
    if (isSavedList(this.candidateSource)) {
      return this.candidateSource.savedSearchSource;
    } else {
      return null;
    }
  }


  hasSavedSearchSource(): boolean {
    return this.getSavedSearchSource() != null;
  }

  doShowSearch() {
    const savedSearchSource = this.getSavedSearchSource();
    if (savedSearchSource != null) {
      this.router.navigate(getSavedSourceNavigation(savedSearchSource));
    }
  }
}
