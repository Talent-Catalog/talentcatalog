/*
 * Copyright (c) 2024 Talent Catalog.
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

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {NgbNavModule} from '@ng-bootstrap/ng-bootstrap';
import {SavedSearchService} from '../../../services/saved-search.service';
import {AuthenticationService} from '../../../services/authentication.service';
import {SearchHomeComponent} from './search-home.component';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import MockSavedSearchTypeInfo from "../../../MockData/MockSavedSearchTypeInfo";
import {MockUser} from "../../../MockData/MockUser";
import {LocalStorageService} from "../../../services/local-storage.service";
import {of} from "rxjs";
import {ActivatedRoute} from "@angular/router";

describe('SearchHomeComponent', () => {
  let component: SearchHomeComponent;
  let fixture: ComponentFixture<SearchHomeComponent>;
  let localStorageServiceSpy: jasmine.SpyObj<LocalStorageService>;
  let savedSearchServiceSpy: jasmine.SpyObj<SavedSearchService>;
  let authenticationServiceSpy: jasmine.SpyObj<AuthenticationService>;

  beforeEach(async () => {
    const localStorageSpy = jasmine.createSpyObj('LocalStorageService', ['get', 'set']);
    const searchServiceSpy = jasmine.createSpyObj('SavedSearchService', ['getSavedSearchTypeInfos']);
    const authenticationSpy = jasmine.createSpyObj('AuthenticationService', ['getLoggedInUser']);

    await TestBed.configureTestingModule({
      declarations: [SearchHomeComponent],
      imports: [NgbNavModule],
      providers: [
        { provide: LocalStorageService, useValue: localStorageSpy },
        { provide: SavedSearchService, useValue: searchServiceSpy },
        { provide: AuthenticationService, useValue: authenticationSpy },
        { provide: ActivatedRoute, useValue: { queryParams: of({}) } },
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();
   localStorageServiceSpy = TestBed.inject(LocalStorageService) as jasmine.SpyObj<LocalStorageService>;
    savedSearchServiceSpy = TestBed.inject(SavedSearchService) as jasmine.SpyObj<SavedSearchService>;
    authenticationServiceSpy = TestBed.inject(AuthenticationService) as jasmine.SpyObj<AuthenticationService>;
  });

  beforeEach(()=>{
    fixture = TestBed.createComponent(SearchHomeComponent);
    component = fixture.componentInstance;
    authenticationServiceSpy.getLoggedInUser.and.returnValue(new MockUser());
    localStorageServiceSpy.get.and.returnValue(component['defaultTabId']);
    component.savedSearchTypeInfos = MockSavedSearchTypeInfo;
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with default tab', () => {
    savedSearchServiceSpy.getSavedSearchTypeInfos.and.returnValue(MockSavedSearchTypeInfo);
    fixture.detectChanges();
    expect(component.activeTabId).toBe(component['defaultTabId']);
    expect(localStorageServiceSpy.get).toHaveBeenCalledWith(component["lastTabKey"]);
  });

  it('should save the last active tab to local storage on tab change', () => {
    fixture.detectChanges();
    const event = { nextId: 'NewSearch' };
    // @ts-expect-error
    component.onTabChanged(event);
    expect(localStorageServiceSpy.set).toHaveBeenCalledWith(component["lastTabKey"], 'NewSearch');
  });

  it('should display correct content based on active tab', () => {
    fixture.detectChanges();
    component.activeTabId = 'NewSearch';
    fixture.detectChanges();
    const compiled = fixture.nativeElement;
    expect(compiled.querySelector('app-candidates-search')).toBeTruthy();
    component.activeTabId = 'MySearches';
    fixture.detectChanges();
    expect(compiled.querySelector('app-browse-candidate-sources')).toBeTruthy();
  });

  it('should handle tab change event with valid tab ID', () => {
    fixture.detectChanges();
    const event = { nextId: 'SearchesSharedWithMe' };
    component.onTabChanged(event.nextId);
    expect(component.activeTabId).toBe('SearchesSharedWithMe');
    expect(localStorageServiceSpy.set).toHaveBeenCalledWith(component["lastTabKey"], 'SearchesSharedWithMe');
  });

  it('should initialize savedSearchTypeSubInfos correctly', () => {
    fixture.detectChanges();
    expect(component.savedSearchTypeSubInfos).toBeDefined();
  });
});
