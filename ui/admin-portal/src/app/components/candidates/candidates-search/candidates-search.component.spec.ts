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
import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {CandidatesSearchComponent} from './candidates-search.component';
import {ActivatedRoute} from '@angular/router';
import {SavedSearchService} from '../../../services/saved-search.service';
import {of, throwError} from 'rxjs';
import {MockSavedSearch} from "../../../MockData/MockSavedSearch";
import {NgbModal, NgbModalRef} from "@ng-bootstrap/ng-bootstrap";
import {ConfirmationComponent} from "../../util/confirm/confirmation.component";

describe('CandidatesSearchComponent', () => {
  let component: CandidatesSearchComponent;
  let fixture: ComponentFixture<CandidatesSearchComponent>;
  let mockActivatedRoute: any;
  let mockSavedSearchService: any;
  let modalService: NgbModal;

  beforeEach(async () => {
    mockActivatedRoute = {
      paramMap: of({ get: () => null }),
      queryParamMap: of({ get: () => null })
    };
    mockSavedSearchService = jasmine.createSpyObj('SavedSearchService', ['get', 'getDefault']);

    await TestBed.configureTestingModule({
      declarations: [CandidatesSearchComponent],
      providers: [
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
        { provide: SavedSearchService, useValue: mockSavedSearchService },
        { provide: NgbModal  },
      ]
    })
    .compileComponents();
    mockSavedSearchService.getDefault.and.returnValue(of());
    modalService = TestBed.inject(NgbModal);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidatesSearchComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });
  it('should load default search when no ID is provided in the route', fakeAsync(() => {
    const mockDefaultSearch = new MockSavedSearch();
    mockSavedSearchService.getDefault.and.returnValue(of(mockDefaultSearch));

    component.ngOnInit();
    tick();

    expect(component.loading).toBe(false);
    expect(component.savedSearch).toEqual(mockDefaultSearch);
    expect(component.error).toBeUndefined();
  }));

  it('should handle error from saved search service when loading default search', fakeAsync(() => {
    const mockError = 'Error loading default search';
    mockSavedSearchService.getDefault.and.returnValue(throwError(mockError));

    component.ngOnInit();
    tick();

    expect(component.loading).toBe(false);
    expect(component.savedSearch).toBeUndefined();
    expect(component.error).toEqual(mockError);
  }));

  it('should update page number and page size when route parameters change', () => {
    const mockParams = { get: (param: string) => param === 'pageNumber' ? '2' : '10' };
    mockActivatedRoute.queryParamMap = of(mockParams);

    component.ngOnInit();

    expect(component.pageNumber).toEqual(2);
    expect(component.pageSize).toEqual(10);
  });

  it('should display confirmation modal if form dirty and navigate away', () => {
    spyOn(modalService, 'open').and.returnValue({
      componentInstance: {},
      result: Promise.resolve('saved')
    } as NgbModalRef);

    component.formDirty = true;

    component.canExit();

    expect(modalService.open).toHaveBeenCalledWith(ConfirmationComponent, jasmine.any(Object));
  });

});
