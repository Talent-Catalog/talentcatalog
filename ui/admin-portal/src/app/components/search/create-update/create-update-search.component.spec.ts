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
import {ReactiveFormsModule} from '@angular/forms';
import {NgbActiveModal, NgbTypeaheadModule} from '@ng-bootstrap/ng-bootstrap';
import {of, throwError} from 'rxjs';
import {CreateUpdateSearchComponent} from './create-update-search.component';
import {SavedSearchService} from '../../../services/saved-search.service';
import {SalesforceService} from '../../../services/salesforce.service';
import {MockSavedSearch} from "../../../MockData/MockSavedSearch";
import {JoblinkComponent} from "../../util/joblink/joblink.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import MOCK_SAVED_SEARCH_TYPE_INFO from "../../../MockData/MockSavedSearchTypeInfo";

describe('CreateUpdateSearchComponent', () => {
  let component: CreateUpdateSearchComponent;
  let fixture: ComponentFixture<CreateUpdateSearchComponent>;
  let mockSavedSearchService;
  let mockSalesforceService;
  let mockActiveModal;

  beforeEach(async () => {
    mockSavedSearchService = jasmine.createSpyObj(['create', 'update', 'load','getSavedSearchTypeInfos']);
    mockSalesforceService = jasmine.createSpyObj(['']);
    mockActiveModal = jasmine.createSpyObj(['close', 'dismiss']);

    await TestBed.configureTestingModule({
      declarations: [CreateUpdateSearchComponent,JoblinkComponent],
      imports: [HttpClientTestingModule,ReactiveFormsModule,NgbTypeaheadModule],
      providers: [
        { provide: SavedSearchService, useValue: mockSavedSearchService },
        { provide: SalesforceService, useValue: mockSalesforceService },
        { provide: NgbActiveModal, useValue: mockActiveModal },
      ],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateUpdateSearchComponent);
    component = fixture.componentInstance;
    component.savedSearch = new MockSavedSearch(); // Initial saved search data
    component.savedSearchTypeInfos = MOCK_SAVED_SEARCH_TYPE_INFO;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with saved search data', () => {
    const updatedMockSavedSearch = new MockSavedSearch();
    updatedMockSavedSearch.defaultSearch = false;
    updatedMockSavedSearch.reviewable = true;
    component.savedSearch = updatedMockSavedSearch;
    component.ngOnInit();
    expect(component.form.get('name').value).toBe('Mock Saved Search');
    expect(component.form.get('reviewable').value).toBe(true);
  });

  it('should validate form as invalid if name is empty', () => {
    component.form.get('name').setValue('');
    expect(component.form.invalid).toBe(true);
  });

  it('should call create method on save when in create mode', () => {
    component.savedSearch.id = 0; // Set to create mode
    mockSavedSearchService.create.and.returnValue(of({}));

    component.save();

    expect(mockSavedSearchService.create).toHaveBeenCalled();
    expect(mockActiveModal.close).toHaveBeenCalled();
  });

  it('should call update method on save when in update mode', () => {
    component.savedSearch.id = 1; // Set to update mode
    component.savedSearch.defaultSearch = false;
    mockSavedSearchService.update.and.returnValue(of({}));

    component.save();

    expect(mockSavedSearchService.update).toHaveBeenCalled();
    expect(mockActiveModal.close).toHaveBeenCalled();
  });

  it('should show error message on save failure', () => {
    component.savedSearch.id = 1; // Set to update mode
    component.savedSearch.defaultSearch = false;
    mockSavedSearchService.update.and.returnValue(throwError('Error'));

    component.save();

    expect(component.error).toBe('Error');
  });

  it('should dismiss the modal on cancel', () => {
    component.cancel();
    expect(mockActiveModal.dismiss).toHaveBeenCalled();
  });
});
