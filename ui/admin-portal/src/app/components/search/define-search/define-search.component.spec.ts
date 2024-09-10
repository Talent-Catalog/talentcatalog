/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License
 * for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */
import {DefineSearchComponent} from "./define-search.component";
import {SearchQueryService} from "../../../services/search-query.service";
import {AuthenticationService} from "../../../services/authentication.service";
import {ComponentFixture, TestBed, waitForAsync} from "@angular/core/testing";
import {FormBuilder, ReactiveFormsModule} from "@angular/forms";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {
  NgbDatepickerModule,
  NgbModal,
  NgbTooltipModule,
  NgbTypeaheadModule
} from "@ng-bootstrap/ng-bootstrap";
import {LocalStorageModule} from "angular-2-local-storage";
import {RouterTestingModule} from "@angular/router/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {
  LanguageLevelFormControlComponent
} from "../../util/form/language-proficiency/language-level-form-control.component";
import {JoinSavedSearchComponent} from "../join-search/join-saved-search.component";
import {
  DateRangePickerComponent
} from "../../util/form/date-range-picker/date-range-picker.component";
import {SavedSearchService} from "../../../services/saved-search.service";

describe('DefineSearchComponent', () => {
  let component: DefineSearchComponent;
  let fixture: ComponentFixture<DefineSearchComponent>;
  let searchQueryServiceSpy: jasmine.SpyObj<SearchQueryService>;
  let authenticationServiceSpy: jasmine.SpyObj<AuthenticationService>;
  let savedSearchServiceSpy: jasmine.SpyObj<SavedSearchService>;

  beforeEach(waitForAsync(() => {
    const searchQuerySpy = jasmine.createSpyObj('SearchQueryService', ['changeSearchQuery']);
    const savedSearchSpy = jasmine.createSpyObj('SavedSearchService', ['getSavedSearchTypeInfos','load']);
    const authSpy = jasmine.createSpyObj('AuthenticationService', ['getLoggedInUser','isSourcePartner', 'isDefaultPartner']);

    TestBed.configureTestingModule({
      declarations: [DefineSearchComponent,LanguageLevelFormControlComponent,JoinSavedSearchComponent,DateRangePickerComponent],
      imports: [ReactiveFormsModule,NgbTooltipModule,HttpClientTestingModule,NgbTypeaheadModule,NgbDatepickerModule,LocalStorageModule.forRoot({}),RouterTestingModule,NgSelectModule],
      providers: [
        FormBuilder,
        NgbModal,
        { provide: SearchQueryService, useValue: searchQuerySpy },
        { provide: AuthenticationService, useValue: authSpy },
        { provide: SavedSearchService, useValue: savedSearchSpy }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DefineSearchComponent);
    component = fixture.componentInstance;
    searchQueryServiceSpy = TestBed.inject(SearchQueryService) as jasmine.SpyObj<SearchQueryService>;
    authenticationServiceSpy = TestBed.inject(AuthenticationService) as jasmine.SpyObj<AuthenticationService>;
    savedSearchServiceSpy = TestBed.inject(SavedSearchService) as jasmine.SpyObj<SavedSearchService>;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with default values', () => {
    expect(component.searchForm).toBeDefined();
    expect(component.searchForm.get('savedSearchId').value).toBeNull();
  });

  it('should set simpleQueryString in searchQueryService on value changes', () => {
    component.searchForm.get('simpleQueryString').patchValue('test query');
    expect(searchQueryServiceSpy.changeSearchQuery).toHaveBeenCalledWith('test query');
  });

  it('should clear the form on clearForm() method call', () => {
    component.searchForm.get('simpleQueryString').patchValue('test query');
    component.searchForm.get('statuses').patchValue(['status1', 'status2']);
    component.clearForm();
    expect(component.searchForm.get('simpleQueryString').value).toBeNull();
    expect(component.searchForm.get('statuses').value).toEqual(null);
  });

  it('should call apply() method to initialize search request', () => {
    const applySpy = spyOn(component, 'apply');
    component.searchForm.get('simpleQueryString').patchValue('test query');
    component.apply();
    expect(applySpy).toHaveBeenCalled();
  });

  it('should delete saved search join on deleteBaseSearchJoin() method call', () => {
    component.selectedBaseJoin = { id: '123', name: 'Base Search', searchType: 'and' };
    component.deleteBaseSearchJoin();
    expect(component.selectedBaseJoin).toBeNull();
  });

});

