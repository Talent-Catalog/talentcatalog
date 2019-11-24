import {Component, Input, OnInit} from '@angular/core';
import {SavedSearch} from "../../../model/saved-search";
import {SavedSearchService} from "../../../services/saved-search.service";

@Component({
  selector: 'app-saved-search',
  templateUrl: './saved-search.component.html',
  styleUrls: ['./saved-search.component.scss']
})
export class SavedSearchComponent implements OnInit {

  @Input() savedSearch: SavedSearch;

  loading;
  error;
  showAll;

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

}
