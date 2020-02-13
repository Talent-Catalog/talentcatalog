import {Component, OnInit} from '@angular/core';
import {NgbTabChangeEvent} from "@ng-bootstrap/ng-bootstrap";
import {SavedSearchType} from "../../model/saved-search";
import {LocalStorageService} from "angular-2-local-storage";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {

  private activeId: string;
  private lastTabKey: string = 'HomeLastTab';

  savedSearchType: SavedSearchType;

  savedSearchTypeTabs: SavedSearchType[] = [
    SavedSearchType.profession,
    SavedSearchType.job,
    SavedSearchType.other
  ];

  constructor(
    private localStorageService: LocalStorageService
  ) { }

  //todo process url for any hint of what tab shoudl be selected.
  ngOnInit() {
    this.selectDefaultTab();
  }

  private selectDefaultTab() {
    this.activeId = this.localStorageService.get(this.lastTabKey);
  }

  onTabChanged(event: NgbTabChangeEvent) {
    this.savedSearchType = SavedSearchType[event.nextId];
    this.activeId = event.nextId;
    //todo Save savedSearchType to local storage and default to that tab if
    //no other tab specifically requested (eg on URL)
    this.localStorageService.set(this.lastTabKey, this.savedSearchType);
  }
}
