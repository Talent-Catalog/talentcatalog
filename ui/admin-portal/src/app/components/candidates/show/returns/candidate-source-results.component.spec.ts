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
import {CandidateSourceResultsComponent} from "./candidate-source-results.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {
  CandidateSourceResultsCacheService
} from "../../../../services/candidate-source-results-cache.service";
import {
  CandidateSourceCandidateService
} from "../../../../services/candidate-source-candidate.service";
import {AuthorizationService} from "../../../../services/authorization.service";
import {CandidateFieldService} from "../../../../services/candidate-field.service";
import {CandidateService} from "../../../../services/candidate.service";
import {SavedSearchService} from "../../../../services/saved-search.service";
import {NgbModal, NgbPaginationModule} from "@ng-bootstrap/ng-bootstrap";
import {Router} from "@angular/router";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {ReactiveFormsModule} from "@angular/forms";


describe('CandidateSourceResultsComponent', () => {
  let component: CandidateSourceResultsComponent;
  let fixture: ComponentFixture<CandidateSourceResultsComponent>;
  beforeEach(async () => {
    const candidateServiceMock = jasmine.createSpyObj('CandidateService', ['']);
    const savedSearchServiceMock = jasmine.createSpyObj('SavedSearchService', ['']);
    const candidateSourceResultsCacheServiceMock = jasmine.createSpyObj('CandidateSourceResultsCacheService', ['getFromCache', 'cache']);
    const candidateSourceCandidateServiceMock = jasmine.createSpyObj('CandidateSourceCandidateService', ['searchPaged']);
    const authorizationServiceMock = jasmine.createSpyObj('AuthorizationService', ['canViewCandidateName', 'canViewCandidateCountry', 'canAccessSalesforce']);
    const candidateFieldServiceMock = jasmine.createSpyObj('CandidateFieldService', ['getCandidateSourceFields']);
    const ngbModalMock = jasmine.createSpyObj('NgbModal', ['open']);
    const routerMock = jasmine.createSpyObj('Router', ['navigate']);

    await TestBed.configureTestingModule({
      declarations: [CandidateSourceResultsComponent],
      imports: [
        HttpClientTestingModule,
        ReactiveFormsModule ,
        NgbPaginationModule
      ],
      providers: [
        { provide: CandidateService, useValue: candidateServiceMock },
        { provide: SavedSearchService, useValue: savedSearchServiceMock },
        { provide: CandidateSourceResultsCacheService, useValue: candidateSourceResultsCacheServiceMock },
        { provide: CandidateSourceCandidateService, useValue: candidateSourceCandidateServiceMock },
        { provide: AuthorizationService, useValue: authorizationServiceMock },
        { provide: CandidateFieldService, useValue: candidateFieldServiceMock },
        { provide: NgbModal, useValue: ngbModalMock },
        { provide: Router, useValue: routerMock }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateSourceResultsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should initialize with the correct initial state', () => {
    expect(component.error).toBeNull();
    expect(component.pageNumber).toBeUndefined();
    expect(component.pageSize).toBeUndefined();
    expect(component.results).toBeUndefined();
    expect(component.candidateSource).toBeUndefined();
    expect(component.showSourceDetails).toBeTrue();
    expect(component.searching).toBeUndefined();
    expect(component.selectedFields).toEqual([]);
    expect(component.sortField).toBeUndefined();
    expect(component.sortDirection).toBeUndefined();
    expect(component.timestamp).toBeUndefined();
  });

});
