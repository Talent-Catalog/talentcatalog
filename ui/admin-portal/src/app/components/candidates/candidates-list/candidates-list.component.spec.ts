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
import {CandidatesListComponent} from "./candidates-list.component";
import {ComponentFixture, TestBed, waitForAsync} from "@angular/core/testing";
import {of, throwError} from "rxjs";
import {ActivatedRoute, Router} from "@angular/router";
import {SavedListService} from "../../../services/saved-list.service";
import {NgbModal, NgbTypeaheadModule} from "@ng-bootstrap/ng-bootstrap";
import {SavedList} from "../../../model/saved-list";
import {MockSavedList} from "../../../MockData/MockSavedList";
import {ShowCandidatesComponent} from "../show/show-candidates.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {UntypedFormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {CommonModule, DatePipe, TitleCasePipe} from "@angular/common";
import {SortedByComponent} from "../../util/sort/sorted-by.component";
import {NgSelectModule} from "@ng-select/ng-select";
import {CandidateSourceDescriptionComponent} from "../../util/candidate-source-description/candidate-source-description.component";
import {AutosaveStatusComponent} from "../../util/autosave-status/autosave-status.component";
import {RouterLinkStubDirective} from "../../login/login.component.spec";

describe('CandidatesListComponent', () => {
  let component: CandidatesListComponent;
  let fixture: ComponentFixture<CandidatesListComponent>;
  let mockActivatedRoute: any;
  let mockRouter: any;
  let mockSavedListService: any;
  let mockModalService: any;
  let mockLocation: any;

  beforeEach(waitForAsync(() => {
    mockActivatedRoute = {
      paramMap: of({ get: (param: string) => '1' }), // Mocking route parameters
      queryParamMap: of({ get: (param: string) => null }) // Mocking query parameters
    };

    mockRouter = jasmine.createSpyObj('Router', ['navigate']);
    mockSavedListService = jasmine.createSpyObj('SavedListService', ['get']);
    mockModalService = jasmine.createSpyObj('NgbModal', ['open']);
    mockLocation = jasmine.createSpyObj('Location', ['back']);
    mockSavedListService.get.and.returnValue(of(MockSavedList));
    TestBed.configureTestingModule({
      declarations: [CandidatesListComponent, CandidateSourceDescriptionComponent, AutosaveStatusComponent, RouterLinkStubDirective ,ShowCandidatesComponent, SortedByComponent],
      imports: [HttpClientTestingModule, NgbTypeaheadModule,CommonModule, FormsModule, ReactiveFormsModule,NgSelectModule
      ],
      providers: [
        UntypedFormBuilder,
        DatePipe,
        TitleCasePipe,
        { provide: ActivatedRoute, useValue: mockActivatedRoute },
        { provide: Router, useValue: mockRouter },
        { provide: SavedListService, useValue: mockSavedListService },
        { provide: NgbModal, useValue: mockModalService },
        { provide: Location, useValue: mockLocation }
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidatesListComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load saved list on initialization', () => {
    const savedList: SavedList = MockSavedList;

    mockSavedListService.get.and.returnValue(of(savedList));

    fixture.detectChanges();

    expect(component.savedList).toEqual(savedList);
    expect(component.loading).toBe(false);
  });

  it('should handle errors from saved list service', () => {
    const errorMessage = 'Error fetching saved list';
    mockSavedListService.get.and.returnValue(throwError(errorMessage));
    component.ngOnInit();
    fixture.detectChanges();
    expect(component.error).toEqual(errorMessage);
    expect(component.loading).toBe(false);
  });

  it('should create a new list when no ID is specified in the route', waitForAsync(() => {
    mockActivatedRoute.paramMap = of({ get: (param: string) => null }); // Set id param to null

    const createdList: SavedList = MockSavedList; // Assuming you have a mock SavedList object
    const modalResultPromise = Promise.resolve(createdList);
    mockModalService.open.and.returnValue({ result: modalResultPromise });
    component.ngOnInit();

    modalResultPromise.then(() => {
      expect(mockRouter.navigate).toHaveBeenCalled();
    }).catch(error => {
      console.error('Error:', error); // Log any errors
    });
  }));
});
