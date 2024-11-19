import {ComponentFixture, TestBed} from '@angular/core/testing';
import {UntypedFormBuilder, UntypedFormControl, UntypedFormGroup, ReactiveFormsModule} from '@angular/forms';
import {NgbModal, NgbPaginationModule} from '@ng-bootstrap/ng-bootstrap';
import {Router} from '@angular/router';
import {BrowseCandidateSourcesComponent} from './browse-candidate-sources.component';
import {AuthenticationService} from '../../../../services/authentication.service';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {DatePipe, TitleCasePipe} from "@angular/common";
import {CandidateSourceService} from "../../../../services/candidate-source.service";
import {of} from "rxjs";
import {CandidateSourceType, DtoType} from "../../../../model/base";
import {MockSearchResults} from "../../../../MockData/MockSearchResults";
import {MockCandidateSource} from "../../../../MockData/MockCandidateSource";
import {CandidateSourceResultsComponent} from "../returns/candidate-source-results.component";
import {CandidateSourceComponent} from "../../../util/candidate-source/candidate-source.component";
import {RouterLinkStubDirective} from "../../../login/login.component.spec";
import {CandidateFieldService} from "../../../../services/candidate-field.service";
import {SearchBy} from "../../../../model/saved-list";
import {SearchSavedSearchRequest} from "../../../../model/saved-search";
import {LocalStorageService} from "../../../../services/local-storage.service";

describe('BrowseCandidateSourcesComponent', () => {
  let component: BrowseCandidateSourcesComponent;
  let fixture: ComponentFixture<BrowseCandidateSourcesComponent>;
   let mockCandidateSourceService: any;
  let mockLocalStorageService: any;
  let mockAuthenticationService: any;
  let mockCandidateFieldService: any;
  beforeEach(async () => {
    mockCandidateSourceService = jasmine.createSpyObj('CandidateSourceService', ['searchPaged']);
    mockLocalStorageService = jasmine.createSpyObj('LocalStorageService', ['get', 'set']);
    mockAuthenticationService = jasmine.createSpyObj('AuthenticationService', ['getLoggedInUser']);
    mockCandidateFieldService = jasmine.createSpyObj('CandidateFieldService', ['getCandidateSourceFields']);
    mockCandidateFieldService.getCandidateSourceFields.and.returnValue(of());
    mockCandidateSourceService.searchPaged.and.returnValue(of(new MockSearchResults()));
    await TestBed.configureTestingModule({
      declarations: [BrowseCandidateSourcesComponent,CandidateSourceResultsComponent,CandidateSourceComponent,RouterLinkStubDirective],
      imports: [
        HttpClientTestingModule,
        ReactiveFormsModule ,
        NgbPaginationModule
      ],
      providers: [
        DatePipe,
        TitleCasePipe,
        UntypedFormBuilder,
        NgbModal,
        LocalStorageService,
        { provide: Router, useValue: {} },
        { provide: CandidateSourceService, useValue: mockCandidateSourceService },
        { provide: LocalStorageService, useValue: mockLocalStorageService },
        { provide: AuthenticationService, useValue: mockAuthenticationService },
        { provide: CandidateFieldService, useValue: mockCandidateFieldService }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(BrowseCandidateSourcesComponent);
    component = fixture.componentInstance;
    component.searchForm = new UntypedFormGroup({
      keyword: new UntypedFormControl(''), // Initialize keyword FormControl
      selectedStages: new UntypedFormControl([])
    });
    component.sourceType = CandidateSourceType.SavedSearch;
    component.searchBy = SearchBy.mine;
    fixture.detectChanges();
  });
  it('should create', () => {
    expect(component).toBeTruthy();
  });
  it('should search for sources when search method is called', () => {
    const mockResults: MockSearchResults = new MockSearchResults();
    mockCandidateSourceService.searchPaged.and.returnValue(of(mockResults));

    component.search();

    expect(component.loading).toBeFalsy();
    expect(component.results).toEqual(mockResults);
  });

  it('should select a source when select method is called', () => {
    const mockSource = new MockCandidateSource();
    component.select(mockSource);
    expect(component.selectedSource).toEqual(mockSource);
  });

  it('should correctly send the search request based on the input parameters', () => {
    const expectedRequest = new SearchSavedSearchRequest();
    expectedRequest.keyword = component.searchForm.get('keyword').value;
    expectedRequest.pageNumber = 0;
    expectedRequest.pageSize = 30;
    expectedRequest.sortFields = ['name'];
    expectedRequest.sortDirection = 'ASC';
    expectedRequest.owned = true;
    expectedRequest.dtoType = DtoType.MINIMAL;

    component.search();

    expect(mockCandidateSourceService.searchPaged).toHaveBeenCalledWith(1, expectedRequest);
  });

});

