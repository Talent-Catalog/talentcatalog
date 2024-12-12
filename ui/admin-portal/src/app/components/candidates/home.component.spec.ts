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

import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {HomeComponent} from './home.component';
import {NgbNavModule} from '@ng-bootstrap/ng-bootstrap';
import {SavedSearchService} from '../../services/saved-search.service';
import {AuthenticationService} from '../../services/authentication.service';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {LocalStorageService} from "../../services/local-storage.service";

describe('HomeComponent', () => {
  let component: HomeComponent;
  let fixture: ComponentFixture<HomeComponent>;
  let localStorageService: jasmine.SpyObj<LocalStorageService>;
  let savedSearchService: SavedSearchService;
  let authenticationService: AuthenticationService;
  // Define the value of lastTabKey
  const lastTabKey = 'HomeLastTab';
  const lastCategoryTabKey = 'HomeLastCategoryTab';

  beforeEach(waitForAsync(() => {
    const localStorageSpy = jasmine.createSpyObj('LocalStorageService', ['get', 'set']);
    TestBed.configureTestingModule({
      declarations: [HomeComponent],
      imports: [
        NgbNavModule,
        HttpClientTestingModule
      ],
      providers: [
        { provide: LocalStorageService, useValue: localStorageSpy },
        SavedSearchService,
        AuthenticationService
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(HomeComponent);
    component = fixture.componentInstance;
    localStorageService = TestBed.inject(LocalStorageService) as jasmine.SpyObj<LocalStorageService>;
    savedSearchService = TestBed.inject(SavedSearchService);
    authenticationService = TestBed.inject(AuthenticationService);
    // Set up mock behavior for LocalStorageService
    localStorageService.get.and.callFake(<T>(key: string): T => {
      if (key === 'HomeLastTab') {
        return 'defaultTabId' as unknown as T; // Providing a default value
      }
      return null;
    });

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should select the default tab and default saved search subtype upon initialization', () => {
    const defaultTabId = 'defaultTabId';
    const defaultCategoryValue = '0'; // Ensure this is a string

    // Set up mock behavior for LocalStorageService
    localStorageService.get.withArgs(lastCategoryTabKey).and.returnValue(defaultCategoryValue);

    // Initialize the component
    component.ngOnInit();

    // Check if the default tab is selected
    expect(component.activeTabId).toEqual(defaultTabId);

    // Check if the default saved search subtype is selected
    expect(component.selectedSavedSearchSubtype).toEqual(parseInt(defaultCategoryValue));

    // Check if the last tab key is set in the local storage
    expect(localStorageService.set).toHaveBeenCalledWith(lastTabKey, defaultTabId);
  });
  it('should update selectedSavedSearchSubtype when saved search subtype changes', () => {
    // Define the new saved search subtype
    const newSubtype: number = 1;

    // Simulate a change event
    component.onSavedSearchSubtypeChange({ title:"test", savedSearchSubtype: newSubtype });

    // Check if selectedSavedSearchSubtype is updated
    expect(component.selectedSavedSearchSubtype).toEqual(newSubtype);
  });
});
