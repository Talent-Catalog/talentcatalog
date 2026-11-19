/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import type {MockedObject} from "vitest";
import {ComponentFixture, TestBed, fakeAsync, tick} from '@angular/core/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {of} from 'rxjs';
import {SearchSavedSearchesComponent} from './search-saved-searches.component';
import {SavedSearchService} from '../../../services/saved-search.service';
import {NgbActiveModal, NgbPaginationModule} from '@ng-bootstrap/ng-bootstrap';
import {SearchResults} from '../../../model/search-results';
import {SavedSearch} from '../../../model/saved-search';
import {MockSavedSearch} from "../../../MockData/MockSavedSearch";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {RouterTestingModule} from "@angular/router/testing";
import {CandidateSourceComponent} from "../../util/candidate-source/candidate-source.component";
import {expect, vi} from 'vitest';

const mockSearchResult: SearchResults<SavedSearch> = {
    number: 1,
    size: 10,
    totalElements: 1,
    totalPages: 1,
    first: true,
    last: true,
    content: [new MockSavedSearch()] // Use the MockCandidateSource as content
};

describe('SearchSavedSearchesComponent', () => {
    let component: SearchSavedSearchesComponent;
    let fixture: ComponentFixture<SearchSavedSearchesComponent>;
    let savedSearchServiceSpy: MockedObject<SavedSearchService>;
    let activeModalSpy: MockedObject<NgbActiveModal>;

    beforeEach(async () => {
        const searchServiceSpy = {
            searchPaged: vi.fn().mockName("SavedSearchService.searchPaged")
        };
        const modalSpy = {
            close: vi.fn().mockName("NgbActiveModal.close"),
            dismiss: vi.fn().mockName("NgbActiveModal.dismiss")
        };

        await TestBed.configureTestingModule({
            declarations: [SearchSavedSearchesComponent, CandidateSourceComponent],
            imports: [ReactiveFormsModule, RouterTestingModule, NgbPaginationModule],
            providers: [
                { provide: SavedSearchService, useValue: searchServiceSpy },
                { provide: NgbActiveModal, useValue: modalSpy },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ]
        }).compileComponents();

        fixture = TestBed.createComponent(SearchSavedSearchesComponent);
        component = fixture.componentInstance;
        savedSearchServiceSpy = TestBed.inject(SavedSearchService) as MockedObject<SavedSearchService>;
        activeModalSpy = TestBed.inject(NgbActiveModal) as MockedObject<NgbActiveModal>;

        savedSearchServiceSpy.searchPaged.mockReturnValue(of(mockSearchResult));
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize the form and perform search on init', () => {
        fixture.detectChanges();
        expect(component.searchForm).toBeTruthy();
        expect(component.searchForm.controls['keyword']).toBeTruthy();
        expect(savedSearchServiceSpy.searchPaged).toHaveBeenCalled();
    });

    it('should perform search when form value changes', fakeAsync(() => {
        fixture.detectChanges();
        savedSearchServiceSpy.searchPaged.mockClear();

        component.searchForm.controls['keyword'].setValue('Test');

        tick(399);
        expect(savedSearchServiceSpy.searchPaged).not.toHaveBeenCalled();

        tick(1);
        expect(savedSearchServiceSpy.searchPaged).toHaveBeenCalledTimes(1);
        expect(savedSearchServiceSpy.searchPaged).toHaveBeenCalledWith(
            expect.objectContaining({keyword: 'Test'})
        );
    }));

    it('should not display loading indicator when search is done', () => {
        fixture.detectChanges();
        component.search();
        expect(component.loading).toBeFalsy();
    });

    it('should update results after search is completed', () => {
        fixture.detectChanges();
        component.search();
        expect(component.loading).toBe(false);
        expect(component.results.content.length).toBeGreaterThan(0);
    });

    it('should close the modal with selected search on selectSearch()', () => {
        fixture.detectChanges();
        const selectedSearch: SavedSearch = { id: 1, name: 'Search 1' } as SavedSearch;
        component.selectSearch(selectedSearch);
        expect(component.selectedSearch).toBe(selectedSearch);
        expect(activeModalSpy.close).toHaveBeenCalledWith(selectedSearch);
    });

    it('should dismiss the modal on dismiss()', () => {
        fixture.detectChanges();
        component.dismiss();
        expect(activeModalSpy.dismiss).toHaveBeenCalledWith(false);
    });
});
