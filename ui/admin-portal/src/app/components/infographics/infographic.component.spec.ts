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
import {InfographicComponent} from "./infographic.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {CandidateStatService} from "../../services/candidate-stat.service";
import {of, throwError} from "rxjs";
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {NgbDatepickerModule} from "@ng-bootstrap/ng-bootstrap";
import {ActivatedRoute} from "@angular/router";
import {DatePickerComponent} from "../util/date-picker/date-picker.component";
import {RouterLinkStubDirective} from "../login/login.component.spec";
import {provideHttpClientTesting} from "@angular/common/http/testing";
import {provideHttpClient, withInterceptorsFromDi, withXhr} from "@angular/common/http";
import {SavedListService} from "../../services/saved-list.service";
import {SavedSearchService} from "../../services/saved-search.service";
import {MockSavedList} from "../../MockData/MockSavedList";
import {MockSavedSearch} from "../../MockData/MockSavedSearch";
import {StatReport} from "../../model/stat-report";
import {ChartType} from "chart.js";
import {expect, vi} from 'vitest';

describe('InfographicComponent', () => {
    let component: InfographicComponent;
    let fixture: ComponentFixture<InfographicComponent>;
    let mockActivatedRoute: any;
    let mockCandidateStatService: MockedObject<CandidateStatService>;
    let mockSavedListService: MockedObject<SavedListService>;
    let mockSavedSearchService: MockedObject<SavedSearchService>;
    beforeEach(async () => {
        mockActivatedRoute = {
            paramMap: of({
                get: (param: string) => {
                    if (param === 'id')
                        return '1';
                    else if (param === 'source')
                        return 'search';
                }
            })
        };

        mockCandidateStatService = ({
            getAllStats: vi.fn().mockName("CandidateStatService.getAllStats")
        } as unknown as typeof mockCandidateStatService);
        mockSavedListService = ({
            search: vi.fn().mockName("SavedListService.search"),
            get: vi.fn().mockName("SavedListService.get")
        } as unknown as typeof mockSavedListService);
        mockSavedSearchService = ({
            search: vi.fn().mockName("SavedSearchService.search"),
            get: vi.fn().mockName("SavedSearchService.get")
        } as unknown as typeof mockSavedSearchService);

        await TestBed.configureTestingModule({
            declarations: [InfographicComponent, DatePickerComponent, RouterLinkStubDirective],
            imports: [ReactiveFormsModule, FormsModule, NgSelectModule, NgbDatepickerModule],
            providers: [
                UntypedFormBuilder,
                { provide: ActivatedRoute, useValue: mockActivatedRoute },
                { provide: CandidateStatService, useValue: mockCandidateStatService },
                { provide: SavedListService, useValue: mockSavedListService },
                { provide: SavedSearchService, useValue: mockSavedSearchService },
                provideHttpClient(withXhr(), withInterceptorsFromDi()),
                provideHttpClientTesting()
            ]
        }).compileComponents();
    });

    beforeEach(() => {
        fixture = TestBed.createComponent(InfographicComponent);
        component = fixture.componentInstance;
        mockCandidateStatService.getAllStats.mockReturnValue(of());
        mockSavedSearchService.get.mockReturnValue(of());
        fixture.detectChanges();
    });

    it('should create', () => {
        expect(component).toBeTruthy();
    });

    it('should initialize statsFilter FormGroup correctly', () => {
        expect(component.statsFilter).toBeDefined();
        expect(component.statsFilter.get('savedList')).toBeDefined();
        expect(component.statsFilter.get('savedSearch')).toBeDefined();
        expect(component.statsFilter.get('dateFrom')).toBeDefined();
        expect(component.statsFilter.get('dateTo')).toBeDefined();

        expect(component.statsFilter.value).toEqual({
            savedList: null,
            savedSearch: null,
            dateFrom: '',
            dateTo: '',
            selectedStats: []
        });

        const dateFromControl = component.statsFilter.get('dateFrom');
        const dateToControl = component.statsFilter.get('dateTo');

        expect(dateFromControl?.valid).toBeFalsy();
        expect(dateToControl?.valid).toBeFalsy();
        expect(dateFromControl?.hasError('required')).toBeTruthy();
        expect(dateToControl?.hasError('required')).toBeTruthy();
    });

    it('should load lists and searches', fakeAsync(() => {
        const mockLists = [MockSavedList];
        const mockSearches = [new MockSavedSearch()];

        mockSavedListService.search.mockReturnValue(of(mockLists));
        mockSavedSearchService.search.mockReturnValue(of(mockSearches));

        component['loadListsAndSearches']();
        tick();

        expect(component.loading).toBe(false);
        expect(component.lists).toEqual(mockLists);
        expect(component.searches).toEqual(mockSearches);
    }));

    it('should handle error while initializing filter fields from URL parameters', fakeAsync(() => {
        const mockError = 'Internal Server Error';

        mockSavedListService.get.mockReturnValue(throwError(mockError));
        mockSavedSearchService.get.mockReturnValue(throwError(mockError));

        component['initializeFilterFields']();
        tick();

        expect(component.error).toEqual(mockError);
    }));

    it('should submit stats request successfully', fakeAsync(() => {
        // Mock form values
        component.statsFilter.patchValue({
            savedList: { id: 1, name: 'Test List' },
            savedSearch: null,
            dateFrom: '2024-01-01',
            dateTo: '2024-06-01'
        });
        // Mock service response
        const mockStatReports: StatReport[] = [{ name: 'Report 1', rows: [], chartType: 'bar' as ChartType }];

        mockCandidateStatService.getAllStats.mockReturnValue(of(mockStatReports));

        // Call the method
        component.submitStatsRequest();

        // Simulate asynchronous observables
        tick();

        expect(component.loading).toBe(false);
        expect(component.dataLoaded).toBe(true);
        expect(component.statsName).toBe('list Test List');
    }));
    it('should handle error while submitting stats request', fakeAsync(() => {
        // Mock form values
        component.statsFilter.patchValue({
            savedList: null,
            savedSearch: null,
            dateFrom: '2024-01-01',
            dateTo: '2024-06-01'
        });

        // Mock service error response
        const mockError = 'Internal Server Error';
        mockCandidateStatService.getAllStats.mockReturnValue(throwError(mockError));

        // Call the method
        component.submitStatsRequest();

        // Simulate asynchronous observables
        tick();

        expect(component.loading).toBe(false);
        expect(component.error).toEqual(mockError);
    }));

});
