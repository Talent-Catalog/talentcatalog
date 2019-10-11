import {Component, Input, OnInit} from '@angular/core';
import {SavedSearch} from "../../../model/saved-search";

@Component({
  selector: 'app-saved-search',
  templateUrl: './saved-search.component.html',
  styleUrls: ['./saved-search.component.scss']
})
export class SavedSearchComponent implements OnInit {

  @Input() savedSearch: SavedSearch


  constructor() {
  }

  ngOnInit() {


  }

}
