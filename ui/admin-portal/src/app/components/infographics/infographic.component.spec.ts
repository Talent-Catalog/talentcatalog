/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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

import {InfographicComponent} from "./infographic.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {CandidateStatService} from "../../services/candidate-stat.service";
import {SavedListService} from "../../services/saved-list.service";
import {SavedSearchService} from "../../services/saved-search.service";
import {of, throwError} from "rxjs";
import {FormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {NgbDatepickerModule, NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {ActivatedRoute, convertToParamMap} from "@angular/router";
import {DatePickerComponent} from "../util/date-picker/date-picker.component";
import {MockSavedList} from "../../MockData/MockSavedList";
import {MockSavedSearch} from "../../MockData/MockSavedSearch";
import {RouterLinkStubDirective} from "../login/login.component.spec";
import {HttpClientTestingModule} from "@angular/common/http/testing";

fdescribe('InfographicComponent', () => {
  let component: InfographicComponent;
  let fixture: ComponentFixture<InfographicComponent>;
  let mockActivatedRoute: any;
  let mockCandidateStatService: jasmine.SpyObj<CandidateStatService>;

  beforeEach(async () => {
    mockActivatedRoute = {
      paramMap: of({
        get: (param: string) => {
          if (param === 'id') return '1';
          else if (param === 'source') return 'search';
        }
      })
    };

    mockCandidateStatService = jasmine.createSpyObj('CandidateStatService', ['getAllStats']);

    await TestBed.configureTestingModule({
      declarations: [InfographicComponent,DatePickerComponent,RouterLinkStubDirective],
      imports: [HttpClientTestingModule,ReactiveFormsModule,FormsModule,NgSelectModule,NgbDatepickerModule],
      providers: [
        FormBuilder,
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
        { provide: CandidateStatService, useValue: mockCandidateStatService }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(InfographicComponent);
    component = fixture.componentInstance;
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
      dateTo: ''
    });

    const dateFromControl = component.statsFilter.get('dateFrom');
    const dateToControl = component.statsFilter.get('dateTo');

    expect(dateFromControl?.valid).toBeFalsy();
    expect(dateToControl?.valid).toBeFalsy();
    expect(dateFromControl?.hasError('required')).toBeTruthy();
    expect(dateToControl?.hasError('required')).toBeTruthy();
  });

it('should submit stats request successfully', fakeAsync(() => {
    // Mock form values
    component.statsFilter.patchValue({
      savedList: { id: 1, name: 'Test List' },
      savedSearch: null,
      dateFrom: '2024-01-01',
      dateTo: '2024-06-01'
    });

    // Mock service response
    const mockStatReports = [{ name: 'Report 1', rows: [] }];
    // @ts-expect-error
    mockCandidateStatService.getAllStats.and.returnValue(of(mockStatReports));

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
    mockCandidateStatService.getAllStats.and.returnValue(throwError(mockError));

    // Call the method
    component.submitStatsRequest();

    // Simulate asynchronous observables
    tick();

    expect(component.loading).toBe(false);
    expect(component.error).toEqual(mockError);
  }));
});
