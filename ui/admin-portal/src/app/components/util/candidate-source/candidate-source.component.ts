import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {isSavedSearch, SavedSearch} from "../../../model/saved-search";
import {SavedSearchService} from "../../../services/saved-search.service";
import {AuthService} from "../../../services/auth.service";
import {User} from "../../../model/user";
import {CandidateSource} from "../../../model/base";

@Component({
  selector: 'app-candidate-source',
  templateUrl: './candidate-source.component.html',
  styleUrls: ['./candidate-source.component.scss']
})
export class CandidateSourceComponent implements OnInit {

  @Input() candidateSource: CandidateSource;
  @Input() showAll: boolean;
  @Input() showMore: boolean = true;
  @Input() showOpen: boolean = true;
  @Input() showWatch: boolean = true;
  @Input() showSelect: boolean = false;
  @Input() showEdit: boolean = false;
  @Input() showDelete: boolean = false;
  @Output() openSource = new EventEmitter<CandidateSource>();
  @Output() selectSource = new EventEmitter<CandidateSource>();
  @Output() editSource = new EventEmitter<CandidateSource>();
  @Output() deleteSource = new EventEmitter<CandidateSource>();
  @Output() toggleWatch = new EventEmitter<CandidateSource>();

  loading;
  error;
  private loggedInUser: User;

  constructor(
    private savedSearchService: SavedSearchService,
    private authService: AuthService
  ) {
  }

  ngOnInit() {
    this.loggedInUser = this.authService.getLoggedInUser();
  }

  loadSavedSearch() {
    if (this.showAll === true){
      this.showAll = false;
    } else {
      this.loading = true;
      this.savedSearchService.get(this.candidateSource.id).subscribe(result => {
        this.candidateSource = result;
        this.showAll = true;
        this.loading = false;
      }, err => {
        this.loading = false;
        this.error = err;
      })
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

  doDeleteSource(){
    this.deleteSource.emit(this.candidateSource);
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
}
