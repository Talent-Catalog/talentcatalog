import {Component, OnInit} from '@angular/core';
import {NgbTabChangeEvent} from "@ng-bootstrap/ng-bootstrap";
import {SavedSearchType} from "../../model/saved-search";
import {LocalStorageService} from "angular-2-local-storage";

interface TabInfo {
  savedSearchType?: SavedSearchType;
  title: string;
}

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {

  activeId: string;
  private lastTabKey: string = 'HomeLastTab';

  tabInfos: TabInfo[] = [
    {savedSearchType: SavedSearchType.profession,
      title: 'Professions'},
    {savedSearchType: SavedSearchType.job,
      title: 'Jobs'},
    {savedSearchType: SavedSearchType.other,
      title: 'Other'},
    {title: 'All'},
  ];

  constructor(
    private localStorageService: LocalStorageService
  ) { }

  ngOnInit() {
    this.selectDefaultTab();
  }

  private selectDefaultTab() {
    this.activeId = this.localStorageService.get(this.lastTabKey);
  }

  onTabChanged(event: NgbTabChangeEvent) {
    this.activeId = event.nextId;
    this.localStorageService.set(this.lastTabKey, this.activeId);
  }
}
