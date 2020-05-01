import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {SavedSearch} from "../../../model/saved-search";
import {SavedSearchService} from "../../../services/saved-search.service";
import {AuthService} from "../../../services/auth.service";
import {User} from "../../../model/user";

@Component({
  selector: 'app-saved-search',
  templateUrl: './saved-search.component.html',
  styleUrls: ['./saved-search.component.scss']
})
export class SavedSearchComponent implements OnInit {

  @Input() savedSearch: SavedSearch;
  @Input() showAll: boolean;
  @Input() showMore: boolean = true;
  @Input() showOpen: boolean = true;
  @Input() showWatch: boolean = true;
  @Input() showSearch: boolean = false;
  @Input() showEdit: boolean = false;
  @Input() showDelete: boolean = false;
  @Output() openSearch = new EventEmitter<SavedSearch>();
  @Output() selectSearch = new EventEmitter<SavedSearch>();
  @Output() editSearch = new EventEmitter<SavedSearch>();
  @Output() deleteSearch = new EventEmitter<SavedSearch>();
  @Output() toggleWatch = new EventEmitter<SavedSearch>();

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

  loadSavedSearch(){
    if (this.showAll == true){
      this.showAll = false;
    } else {
      this.loading = true;
      this.savedSearchService.get(this.savedSearch.id).subscribe(result => {
        this.savedSearch = result;
        this.showAll = true;
        this.loading = false;
      }, err => {
        this.loading = false;
        this.error = err;
      })
    }

  }

  doOpenSearch(){
    this.openSearch.emit(this.savedSearch);
  }

  doSelectSearch(){
    this.selectSearch.emit(this.savedSearch);
  }

  doEditSearch(){
    this.editSearch.emit(this.savedSearch);
  }

  doDeleteSearch(){
    this.deleteSearch.emit(this.savedSearch);
  }

  doToggleWatch() {
    this.toggleWatch.emit(this.savedSearch);
  }

  isWatching(): boolean {
    return this.savedSearch.watcherUserIds === undefined ? false :
      this.savedSearch.watcherUserIds.indexOf(this.loggedInUser.id) >= 0;
  }
}
