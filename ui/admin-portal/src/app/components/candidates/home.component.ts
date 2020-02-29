import {Component, OnInit} from '@angular/core';
import {NgbTabChangeEvent} from "@ng-bootstrap/ng-bootstrap";
import {SavedSearchSubtype, SavedSearchType} from "../../model/saved-search";
import {LocalStorageService} from "angular-2-local-storage";
import {
  SavedSearchService,
  SavedSearchTypeInfo, SavedSearchTypeSubInfo
} from "../../services/saved-search.service";
import {FormBuilder, FormGroup} from "@angular/forms";

@Component({
  selector: 'app-home',
  templateUrl: './home.component.html',
  styleUrls: ['./home.component.scss']
})
export class HomeComponent implements OnInit {

  activeId: string;
  categoryForm: FormGroup;
  private lastTabKey: string = 'HomeLastTab';
  private lastCategoryTabKey: string = 'HomeLastCategoryTab';
  savedSearchTypeInfos: SavedSearchTypeInfo[];
  savedSearchTypeSubInfos: SavedSearchTypeSubInfo[];
  selectedSavedSearchSubtype: SavedSearchSubtype;

  constructor(
    private fb: FormBuilder,
    private localStorageService: LocalStorageService,
    private savedSearchService: SavedSearchService
  ) {
    this.savedSearchTypeInfos = savedSearchService.getSavedSearchTypeInfos();
  }

  ngOnInit() {

    this.categoryForm = this.fb.group({
      savedSearchSubtype: [this.selectedSavedSearchSubtype]
    });

    this.selectDefaultTab()
  }

  onTabChanged(event: NgbTabChangeEvent) {
    this.setActiveId(event.nextId);

    this.localStorageService.set(this.lastTabKey, this.activeId);
  }

  onSavedSearchSubtypeChange($event: Event) {
    const formValues = this.categoryForm.value;
    this.setSelectedSavedSearchSubtype(formValues.savedSearchSubtype);
  }

  private selectDefaultTab() {
    const defaultActiveID: string = this.localStorageService.get(this.lastTabKey);
    this.setActiveId(defaultActiveID == null ? "0" : defaultActiveID);

    if (defaultActiveID == null) {
      this.setSelectedSavedSearchSubtype(this.savedSearchTypeSubInfos[0].savedSearchSubtype);
    } else {
      const defaultCategory: string = this.localStorageService.get(this.lastCategoryTabKey);
      this.setSelectedSavedSearchSubtype(defaultCategory == null ? 0 : +defaultCategory);
    }
  }

  private setActiveId(activeId: string) {
    this.activeId = activeId;

    //todo this won't work if there are non category tabs because activeId is a
    // tab number which is not the same as an enum value
    if (activeId === null) {
      this.savedSearchTypeSubInfos = null;
    } else {
      this.savedSearchTypeSubInfos =
        this.savedSearchTypeInfos[+activeId].categories;
    }
  }

  private setSelectedSavedSearchSubtype(selectedSavedSearchSubtype: number) {
    this.selectedSavedSearchSubtype = selectedSavedSearchSubtype;
    this.categoryForm.controls['savedSearchSubtype'].patchValue(selectedSavedSearchSubtype);

    this.localStorageService.set(this.lastCategoryTabKey, this.selectedSavedSearchSubtype);
  }
}
