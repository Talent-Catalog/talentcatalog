import {Component, EventEmitter, Input, OnInit, Output} from '@angular/core';
import {SavedSearch} from "../../../model/saved-search";
import {SavedSearchService} from "../../../services/saved-search.service";

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
  @Input() showEdit: boolean = false;
  @Input() showDelete: boolean = false;
  @Output() openSearch = new EventEmitter<SavedSearch>();
  @Output() editSearch = new EventEmitter<SavedSearch>();
  @Output() deleteSearch = new EventEmitter<SavedSearch>();

  loading;
  error;

  constructor(private savedSearchService: SavedSearchService) {
  }

  ngOnInit() {


  }

  loadSavedSearch(){
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

  doOpenSearch(){
    this.openSearch.emit(this.savedSearch);
  }

  doEditSearch(){
    this.editSearch.emit(this.savedSearch);
  }

  doDeleteSearch(){
    this.deleteSearch.emit(this.savedSearch);
  }

}
