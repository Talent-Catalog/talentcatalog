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
import {DefineSearchComponent} from "./define-search.component";
import {SearchQueryService} from "../../../services/search-query.service";
import {AuthenticationService} from "../../../services/authentication.service";
import {ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {SavedSearchService} from "../../../services/saved-search.service";
import {AuthorizationService} from "../../../services/authorization.service";
import {ElementRef, NO_ERRORS_SCHEMA, SimpleChange} from '@angular/core';
import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {Router} from '@angular/router';
import {BehaviorSubject, of, throwError} from 'rxjs';

import {CandidateOccupationService} from '../../../services/candidate-occupation.service';
import {CountryService} from '../../../services/country.service';
import {EducationLevelService} from '../../../services/education-level.service';
import {EducationMajorService} from '../../../services/education-major.service';
import {JobService} from '../../../services/job.service';
import {LanguageLevelService} from '../../../services/language-level.service';
import {LanguageService} from '../../../services/language.service';
import {PartnerService} from '../../../services/partner.service';
import {SurveyTypeService} from '../../../services/survey-type.service';
import {CandidateStatus, UnhcrStatus} from '../../../model/candidate';

describe('DefineSearchComponent', () => {
  let component: DefineSearchComponent;
  let fixture: ComponentFixture<DefineSearchComponent>;
  let searchQueryServiceSpy: jasmine.SpyObj<SearchQueryService>;
  let authorizationServiceSpy: jasmine.SpyObj<AuthorizationService>;
  let countryService: jasmine.SpyObj<CountryService>;
  let languageService: jasmine.SpyObj<LanguageService>;
  let partnerService: jasmine.SpyObj<PartnerService>;
  let savedSearchService: jasmine.SpyObj<SavedSearchService>;
  let educationLevelService: jasmine.SpyObj<EducationLevelService>;
  let educationMajorService: jasmine.SpyObj<EducationMajorService>;
  let candidateOccupationService: jasmine.SpyObj<CandidateOccupationService>;
  let surveyTypeService: jasmine.SpyObj<SurveyTypeService>;
  let jobService: jasmine.SpyObj<JobService>;
  let languageLevelService: jasmine.SpyObj<LanguageLevelService>;
  let modalService: jasmine.SpyObj<NgbModal>;
  let authorizationService: jasmine.SpyObj<AuthorizationService>;
  let authenticationService: jasmine.SpyObj<AuthenticationService>;
  let searchQueryService: jasmine.SpyObj<SearchQueryService>;
  let router: jasmine.SpyObj<Router>;
  let currentSearchTerms$: BehaviorSubject<string[]>;
  const english = {id: 1, name: 'English'} as any;
  const dari = {id: 2, name: 'Dari'} as any;
  const countryA = {id: 10, name: 'A'} as any;
  const countryB = {id: 11, name: 'B'} as any;
  const partnerA = {id: 20, name: 'Partner A'} as any;
  const occupationA = {id: 30, name: 'Developer'} as any;
  const majorA = {id: 40, name: 'Computer Science'} as any;
  const surveyA = {id: 50, name: 'Survey A'} as any;
  const loggedInUser = {id: 99} as any;

  function modalRef(result: Promise<any> = Promise.resolve(undefined)): any {
    return {
      componentInstance: {},
      result,
      close: jasmine.createSpy('close')
    };
  }

  function setPickerMocks(): void {
    component.modifiedDatePicker = jasmine.createSpyObj(
      'DateRangePickerComponent',
      ['clearDates', 'selectDate']
    );

    component.englishLanguagePicker = jasmine.createSpyObj(
      'LanguageLevelFormControlComponent',
      ['clearProficiencies', 'patchModel']
    );

    component.otherLanguagePicker = {
      patchModel: jasmine.createSpy('patchModel'),
      form: jasmine.createSpyObj('FormGroup', ['reset'])
    } as any;

    component.formWrapper = new ElementRef({
      click: jasmine.createSpy('click')
    });
  }

  function setSuccessfulLookupResponses(): void {
    countryService.listCountries.and.returnValue(of([countryA, countryB]));
    countryService.listCountriesRestricted.and.returnValue(of([countryA]));
    languageService.listLanguages.and.returnValue(of([english, dari]));
    languageLevelService.listLanguageLevels.and.returnValue(of([{id: 1}] as any));
    educationLevelService.listEducationLevels.and.returnValue(of([{level: 1, name: 'School'}] as any));
    educationMajorService.listMajors.and.returnValue(of([majorA]));
    partnerService.listSourcePartners.and.returnValue(of([partnerA]));
    candidateOccupationService.listOccupations.and.returnValue(of([occupationA]));
    surveyTypeService.listSurveyTypes.and.returnValue(of([surveyA]));
  }

  beforeEach(async () => {
    currentSearchTerms$ = new BehaviorSubject<string[]>([]);

    countryService = jasmine.createSpyObj('CountryService', ['listCountries', 'listCountriesRestricted']);
    languageService = jasmine.createSpyObj('LanguageService', ['listLanguages']);
    partnerService = jasmine.createSpyObj('PartnerService', ['listSourcePartners']);
    savedSearchService = jasmine.createSpyObj('SavedSearchService', [
      'load', 'clearSelection', 'getSavedSearchTypeInfos', 'delete', 'get'
    ]);
    educationLevelService = jasmine.createSpyObj('EducationLevelService', ['listEducationLevels']);
    educationMajorService = jasmine.createSpyObj('EducationMajorService', ['listMajors']);
    candidateOccupationService = jasmine.createSpyObj('CandidateOccupationService', ['listOccupations']);
    surveyTypeService = jasmine.createSpyObj('SurveyTypeService', ['listSurveyTypes']);
    jobService = jasmine.createSpyObj('JobService', ['getSkills']);
    languageLevelService = jasmine.createSpyObj('LanguageLevelService', ['listLanguageLevels']);
    modalService = jasmine.createSpyObj('NgbModal', ['open']);
    router = jasmine.createSpyObj<Router>('Router', ['navigate']);
    authorizationService = jasmine.createSpyObj('AuthorizationService', [
      'isSourcePartner', 'isDefaultPartner', 'canViewCandidateName',
      'isEmployerPartner', 'canEditCandidateSource'
    ]);
    authenticationService = jasmine.createSpyObj('AuthenticationService', ['getLoggedInUser']);
    searchQueryService = jasmine.createSpyObj('SearchQueryService', ['changeSearchQuery'], {
      currentSearchTerms$: currentSearchTerms$.asObservable()
    });

    authenticationService.getLoggedInUser.and.returnValue(loggedInUser);
    authorizationService.isSourcePartner.and.returnValue(false);
    authorizationService.isDefaultPartner.and.returnValue(false);
    authorizationService.canViewCandidateName.and.returnValue(true);
    authorizationService.isEmployerPartner.and.returnValue(false);
    authorizationService.canEditCandidateSource.and.returnValue(true);
    savedSearchService.getSavedSearchTypeInfos.and.returnValue([]);
    setSuccessfulLookupResponses();

    await TestBed.configureTestingModule({
      declarations: [DefineSearchComponent],
      imports: [ReactiveFormsModule],
      providers: [
        UntypedFormBuilder,
        {provide: CountryService, useValue: countryService},
        {provide: LanguageService, useValue: languageService},
        {provide: PartnerService, useValue: partnerService},
        {provide: SavedSearchService, useValue: savedSearchService},
        {provide: EducationLevelService, useValue: educationLevelService},
        {provide: EducationMajorService, useValue: educationMajorService},
        {provide: CandidateOccupationService, useValue: candidateOccupationService},
        {provide: SurveyTypeService, useValue: surveyTypeService},
        {provide: JobService, useValue: jobService},
        {provide: LanguageLevelService, useValue: languageLevelService},
        {provide: NgbModal, useValue: modalService},
        {provide: Router, useValue: router},
        {provide: AuthorizationService, useValue: authorizationService},
        {provide: AuthenticationService, useValue: authenticationService},
        {provide: SearchQueryService, useValue: searchQueryService}
      ],
      schemas: [NO_ERRORS_SCHEMA]
    })
    .overrideComponent(DefineSearchComponent, {set: {template: ''}})
    .compileComponents();

    fixture = TestBed.createComponent(DefineSearchComponent);
    component = fixture.componentInstance;

    component.pageNumber = 3;
    component.pageSize = 50;
    component.selectedCandidates = [];

    setPickerMocks();

    searchQueryServiceSpy = searchQueryService;
    authorizationServiceSpy = authorizationService;
  });


  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with default values', () => {
    expect(component.searchForm).toBeDefined();
    expect(component.searchForm.get('savedSearchId').value).toBeNull();
  });

  it('should update SearchQueryService when applying search', () => {
    component.searchForm
    .get('simpleQueryString')
    .patchValue('test query');

    component.apply();

    expect(
      searchQueryServiceSpy.changeSearchQuery
    ).toHaveBeenCalledWith('test query');
  });

  it('should clear the form on clearForm() method call', () => {
    component.modifiedDatePicker = jasmine.createSpyObj('DateRangePickerComponent', ['clearDates']);
    component.englishLanguagePicker = jasmine.createSpyObj('LanguageLevelFormControlComponent', ['clearProficiencies']);
    component.otherLanguagePicker = {
      form: jasmine.createSpyObj('FormGroup', ['reset'])
    } as any;

    // Set some initial values
    component.searchForm.get('simpleQueryString').patchValue('test query');
    component.searchForm.get('statuses').patchValue(['status1', 'status2']);

    // Clear value
    component.clearForm();

    // Assert main fields are cleared
    expect(component.searchForm.get('simpleQueryString').value).toBeNull();
    expect(component.searchForm.get('statuses').value).toBeNull();

    // Verify that picker methods were called
    expect(component.modifiedDatePicker.clearDates).toHaveBeenCalled();
    expect(component.englishLanguagePicker.clearProficiencies).toHaveBeenCalled();
    expect(component.otherLanguagePicker.form.reset).toHaveBeenCalled();

    // Verify the form has actually changed
    expect(component.searchForm.dirty).toBeTruthy();
  });

  it('should call apply() method to initialize search request', () => {
    const applySpy = spyOn(component, 'apply');
    component.searchForm.get('simpleQueryString').patchValue('test query');
    component.apply();
    expect(applySpy).toHaveBeenCalled();
  });

  it('should delete saved search join on deleteBaseSearchJoin() method call', () => {
    component.selectedBaseJoin = { id: '123', name: 'Base Search', searchType: 'and' };
    component.deleteBaseSearchJoin();
    expect(component.selectedBaseJoin).toBeNull();
  });

  it('should delegate isEmployerPartner to AuthorizationService', () => {
    authorizationServiceSpy.isEmployerPartner.and.returnValue(true);

    const result = component.isEmployerPartner();

    expect(authorizationServiceSpy.isEmployerPartner).toHaveBeenCalled();
    expect(result).toBeTrue();
  });

  it('should call isSourcePartner when building partner default message', () => {
    authorizationServiceSpy.isSourcePartner.and.returnValue(true);
    authorizationServiceSpy.isDefaultPartner.and.returnValue(false);

    const result = component.getPartnerDefaultMessage();

    expect(authorizationServiceSpy.isSourcePartner).toHaveBeenCalled();
    expect(authorizationServiceSpy.isDefaultPartner).toHaveBeenCalled();
    expect(result).toContain('your partner');
  });

  it('should return general partner message for non-source partners', () => {
    authorizationServiceSpy.isSourcePartner.and.returnValue(false);

    const result = component.getPartnerDefaultMessage();

    expect(authorizationServiceSpy.isSourcePartner).toHaveBeenCalled();
    expect(result).toContain('any partner');
  });

  it('should hide the UNHCR status filter if isEmployerPartner returns true', () => {
    authorizationServiceSpy.isEmployerPartner.and.returnValue(true);
    fixture.detectChanges();
    const unhcrFilter = fixture.nativeElement.querySelector('#unhcrStatusFilter');
    expect(unhcrFilter).toBeNull();
  })

  it('should return false when the user is not an employer partner', () => {
    authorizationServiceSpy.isEmployerPartner.and.returnValue(false);

    const result = component.isEmployerPartner();

    expect(result).toBeFalse();
    expect(authorizationServiceSpy.isEmployerPartner).toHaveBeenCalled();
  });

  it('should call isSourcePartner when building the partner default message', () => {
    authorizationServiceSpy.isSourcePartner.and.returnValue(true);
    authorizationServiceSpy.isDefaultPartner.and.returnValue(false);

    const message = component.getPartnerDefaultMessage();

    expect(authorizationServiceSpy.isSourcePartner).toHaveBeenCalled();
    expect(authorizationServiceSpy.isDefaultPartner).toHaveBeenCalled();
    expect(message).toContain('your partner');
  });

  it('should create with form defaults', () => {
    expect(component).toBeTruthy();
    expect(component.searchForm.get('savedSearchId').value).toBeNull();
    expect(component.searchForm.get('candidateNumbers').value).toBe('');
    expect(component.searchForm.get('countrySearchType').value).toBe('or');
    expect(component.searchForm.get('includePendingTermsCandidates').value).toBeFalse();
  });

  it('should track elastic status from keyword status changes', () => {
    const control = component.searchForm.get('simpleQueryString');
    control.patchValue('developer');
    control.markAsDirty();
    control.updateValueAndValidity();
    expect(component.searchIsElastic).toBeTrue();

    control.patchValue('');
    control.updateValueAndValidity();
    expect(component.searchIsElastic).toBeFalse();
  });

  it('should initialize lookup data, models, user and form change output', fakeAsync(() => {
    spyOn(component.onFormChange, 'emit');

    component.ngOnInit();
    component.searchForm.get('simpleQueryString').patchValue('initial');
    tick();

    expect(component.loggedInUser).toBe(loggedInUser);
    expect(component.nationalities).toEqual([countryA, countryB]);
    expect(component.countries).toEqual([countryA]);
    expect(component.languages).toEqual([english, dari]);
    expect(component.partners).toEqual([partnerA]);
    expect(component.englishLanguageModel.languageId).toBe(english.id);
    expect(component.loading).toBeFalse();
    expect(searchQueryService.changeSearchQuery).toHaveBeenCalledWith('initial');
    expect(component.onFormChange.emit).toHaveBeenCalled();
  }));

  it('should read current search terms from SearchQueryService', () => {
    component.ngOnInit();
    currentSearchTerms$.next(['one', 'two']);
    expect(component.currentSearchTerms).toEqual(['one', 'two']);
  });

  it('should load an input saved search after lookup initialization', fakeAsync(() => {
    component.savedSearch = {id: 8, defaultSearch: true} as any;
    spyOn(component, 'loadSavedSearch');

    component.ngOnInit();
    tick();

    expect(component.loadSavedSearch).toHaveBeenCalledWith(8);
    expect(component.showSearchRequest).toBeTrue();
  }));

  it('should expose lookup initialization errors', fakeAsync(() => {
    countryService.listCountries.and.returnValue(throwError('lookup failed'));

    component.ngOnInit();
    tick();

    expect(component.error).toBe('lookup failed');
    expect(component.loading).toBeFalse();
  }));

  it('should initialize and run a job-skill search', () => {
    spyOn(component, 'clearForm');
    spyOn(component, 'onSubmit');

    (component as any).runSearchWithSkills([
      {name: 'Java'},
      {name: 'Project Management'}
    ]);

    expect(component.clearForm).toHaveBeenCalled();
    expect(component.searchForm.get('simpleQueryString').value)
    .toBe('Java "Project Management"');
    expect(component.searchForm.dirty).toBeTrue();
    expect(component.onSubmit).toHaveBeenCalled();
  });

  it('should ignore empty job skills', () => {
    component.searchForm.markAsPristine();
    (component as any).initializeQueryStringWithJobSkills([]);
    expect(component.searchForm.get('simpleQueryString').value).toBeNull();
    expect(component.searchForm.pristine).toBeTrue();
  });

  it('should prevent Enter defaults after view initialization', () => {
    const input = document.createElement('input');
    document.body.appendChild(input);
    component.ngAfterViewInit();
    const event = new KeyboardEvent('keydown', {key: 'Enter', cancelable: true});
    input.dispatchEvent(event);
    expect(event.defaultPrevented).toBeTrue();
    input.remove();
  });

  it('should expose simpleQueryString and elastic state', () => {
    component.searchForm.get('simpleQueryString').patchValue(null);
    expect(component.simpleQueryString).toBeNull();
    expect(component.elastic()).toBeFalse();
    component.searchForm.get('simpleQueryString').patchValue('x');
    expect(component.elastic()).toBeTrue();
  });

  it('should process savedSearch changes except the first change', () => {
    component.savedSearch = {id: 7} as any;
    spyOn(component, 'loadSavedSearch');

    component.ngOnChanges({savedSearch: new SimpleChange({id: 1}, component.savedSearch, false)});

    expect(component.savedSearchId).toBe(7);
    expect(component.loadSavedSearch).toHaveBeenCalledWith(7);
  });

  it('should skip loading on first savedSearch change', () => {
    component.savedSearch = {id: 7} as any;
    spyOn(component, 'loadSavedSearch');
    component.ngOnChanges({savedSearch: new SimpleChange(undefined, component.savedSearch, true)});
    expect(component.loadSavedSearch).not.toHaveBeenCalled();
  });

  it('should validate duplicate base searches', () => {
    const validator = component.validateDuplicateSearches('savedSearchId');
    component.selectedBaseJoin = {savedSearchId: 10};
    component.searchForm.get('savedSearchId').patchValue(10);
    expect(validator(component.searchForm)).toEqual({
      error: "Can't select same base search as saved search."
    });

    component.searchForm.get('savedSearchId').patchValue(11);
    expect(validator(component.searchForm)).toEqual({});

    component.selectedBaseJoin = null;
    expect(validator(component.searchForm)).toBeUndefined();
  });

  it('should return component, form and empty errors correctly', () => {
    component.error = 'component error';
    expect(component.getError()).toBe('component error');
    component.error = null;
    component.searchForm.setErrors({error: 'form error'});
    expect(component.getError()).toBe('form error');
    component.searchForm.setErrors(null);
    expect(component.getError()).toBeNull();
  });

  it('should submit directly with no selected candidates', () => {
    spyOn(component, 'apply');
    component.selectedCandidates = [];
    component.onSubmit();
    expect(component.apply).toHaveBeenCalled();
  });

  it('should ask for confirmation when candidates are selected', () => {
    spyOn(component, 'confirmClearSelectionAndApply');
    component.selectedCandidates = [{} as any];
    component.onSubmit();
    expect(component.confirmClearSelectionAndApply).toHaveBeenCalled();
  });

  it('should run keyword search on Enter', () => {
    const event = jasmine.createSpyObj<KeyboardEvent>('KeyboardEvent', ['preventDefault']);
    spyOn(component, 'apply');
    component.runKeywordSearchOnEnter(event);
    expect(event.preventDefault).toHaveBeenCalled();
    expect(component.apply).toHaveBeenCalled();
  });

  it('should create a paged search request', () => {
    component.searchForm.patchValue({simpleQueryString: 'developer', candidateNumbers: '1, 2'});
    component.apply();
    expect(component.searchRequest.pageNumber).toBe(0);
    expect(component.searchRequest.pageSize).toBe(50);
    expect(component.searchRequest.sortFields).toEqual(['id']);
    expect(component.searchRequest.sortDirection).toBe('DESC');
    expect(component.searchRequest.candidateNumbers).toEqual(['1','2']);
    expect(searchQueryService.changeSearchQuery).toHaveBeenCalledWith('developer');
  });

  it('should convert all multi-select objects to IDs', () => {
    const request: any = {
      candidateNumbers: '100, 200 300',
      countries: [countryA],
      partners: [partnerA],
      nationalities: [countryB],
      occupations: [occupationA],
      educationMajors: [majorA],
      statusesDisplay: [{key: 'active'}],
      surveyTypes: [surveyA],
      unhcrStatusesDisplay: [{key: 'registered'}]
    };

    const result: any = component.getIdsMultiSelect(request);

    expect(result.candidateNumbers).toEqual(['100', '200', '300']);
    expect(result.countryIds).toEqual([10]);
    expect(result.partnerIds).toEqual([20]);
    expect(result.nationalityIds).toEqual([11]);
    expect(result.occupationIds).toEqual([30]);
    expect(result.educationMajorIds).toEqual([40]);
    expect(result.statuses).toEqual(['active']);
    expect(result.surveyTypeIds).toEqual([50]);
    expect(result.unhcrStatuses).toEqual(['registered']);
    expect(result.countries).toBeUndefined();
  });

  it('should leave missing multi-select properties untouched', () => {
    const result: any = component.getIdsMultiSelect({candidateNumbers: ''});
    expect(result.countryIds).toBeUndefined();
    expect(result.candidateNumbers).toEqual([]);
  });

  it('should clear form, joins and child controls', () => {
    component.savedSearchId = 6;
    component.searchJoinArray.push(new UntypedFormBuilder().group({name: 'join'}));
    component.selectedBaseJoin = {id: 1};
    component.storedBaseJoin = {id: 1};

    component.clearForm();

    expect(component.searchForm.get('savedSearchId').value).toBe(6);
    expect(component.searchForm.get('candidateNumbers').value).toBe('');
    expect(component.searchForm.get('countrySearchType').value).toBe('or');
    expect(component.searchJoinArray.length).toBe(0);
    expect(component.selectedBaseJoin).toBeNull();
    expect(component.storedBaseJoin).toBeNull();
    expect(component.modifiedDatePicker.clearDates).toHaveBeenCalled();
    expect(component.englishLanguagePicker.clearProficiencies).toHaveBeenCalled();
    expect(component.otherLanguagePicker.form.reset).toHaveBeenCalled();
    expect(component.searchForm.dirty).toBeTrue();
  });

  it('should navigate to a new search', () => {
    router.navigate.calls.reset();

    component.newSearch();

    expect(router.navigate).toHaveBeenCalledWith(['search']);
  });

  it('should confirm and clear current selections', fakeAsync(() => {
    modalService.open.and.returnValue(modalRef(Promise.resolve(true)));
    spyOn(component, 'clearSelection');
    component.confirmClearSelectionAndApply();
    tick();
    expect(component.clearSelection).toHaveBeenCalled();
  }));

  it('should not clear selections when confirmation is false or dismissed', fakeAsync(() => {
    spyOn(component, 'clearSelection');
    modalService.open.and.returnValue(modalRef(Promise.resolve(false)));
    component.confirmClearSelectionAndApply();
    tick();
    expect(component.clearSelection).not.toHaveBeenCalled();

    modalService.open.and.returnValue(modalRef(Promise.reject('dismissed')));
    component.confirmClearSelectionAndApply();
    tick();
    expect(component.clearSelection).not.toHaveBeenCalled();
  }));

  it('should clear saved-search selection then apply', fakeAsync(() => {
    component.savedSearch = {id: 7} as any;
    component.loggedInUser = {id: 99} as any;
    component.selectedCandidates = [{} as any];

    savedSearchService.clearSelection.and.returnValue(of(null));
    spyOn(component, 'apply');

    component.clearSelection();
    tick();

    expect(savedSearchService.clearSelection).toHaveBeenCalledWith(
      7,
      {userId: 99}
    );
    expect(component.selectedCandidates).toEqual([]);
    expect(component.apply).toHaveBeenCalled();
  }));

  it('should expose selection-clear errors', fakeAsync(() => {
    component.savedSearch = {id: 7} as any;
    component.loggedInUser = {id: 99} as any;

    savedSearchService.clearSelection.and.returnValue(
      throwError('clear failed')
    );

    component.clearSelection();
    tick();

    expect(savedSearchService.clearSelection).toHaveBeenCalledWith(
      7,
      {userId: 99}
    );
    expect(component.error).toBe('clear failed');
  }));

  it('should build breadcrumb from saved-search type infos', () => {
    component.savedSearch = {id: 1, name: 'My Search'} as any;
    savedSearchService.getSavedSearchTypeInfos.and.returnValue([]);
    expect(component.getBreadcrumb()).toBeDefined();
  });

  it('should load a saved search successfully', fakeAsync(() => {
    const request: any = {searchJoinRequests: []};
    savedSearchService.load.and.returnValue(of(request));
    spyOn(component, 'populateFormWithSavedSearch');

    component.loadSavedSearch(4);
    tick();

    expect(component.searchForm.get('savedSearchId').value).toBe(4);
    expect(component.populateFormWithSavedSearch).toHaveBeenCalledWith(request);
    expect(component.loading).toBeFalse();
  }));

  it('should load job skills after loading a job-based search', fakeAsync(() => {
    component.jobId = 3;
    savedSearchService.load.and.returnValue(of({searchJoinRequests: []} as any));
    jobService.getSkills.and.returnValue(of([{name: 'Java'}] as any));
    spyOn(component, 'populateFormWithSavedSearch');
    spyOn<any>(component, 'runSearchWithSkills');

    component.loadSavedSearch(4);
    tick();

    expect(jobService.getSkills).toHaveBeenCalledWith(3);
    expect((component as any).runSearchWithSkills).toHaveBeenCalledWith([{name: 'Java'}]);
  }));

  it('should expose job skill and saved-search load errors', fakeAsync(() => {
    component.jobId = 3;
    savedSearchService.load.and.returnValue(of({searchJoinRequests: []} as any));
    jobService.getSkills.and.returnValue(throwError('skills failed'));
    spyOn(component, 'populateFormWithSavedSearch');
    component.loadSavedSearch(4);
    tick();
    expect(component.error).toBe('skills failed');

    savedSearchService.load.and.returnValue(throwError('load failed'));
    component.loadSavedSearch(4);
    tick();
    expect(component.error).toBe('load failed');
    expect(component.loading).toBeFalse();
  }));

  it('should update exclusion, list-any and list-all controls', () => {
    component.onExclusionListSelected({id: 8} as any);
    expect(component.exclusionListId).toBe(8);
    expect(component.exclusionListIdControl.dirty).toBeTrue();

    component.onListAnySelected([{id: 1}, {id: 2}] as any);
    expect(component.listAnyIds).toEqual([1, 2]);
    expect(component.listAnyIdsControl.dirty).toBeTrue();

    component.onListAllSelected([{id: 3}] as any);
    expect(component.listAllIds).toEqual([3]);
    expect(component.listAllIdsControl.dirty).toBeTrue();
  });

  it('should show and load a chosen saved search', fakeAsync(() => {
    const saved = {id: 9, name: 'Chosen'} as any;
    modalService.open.and.returnValue(modalRef(Promise.resolve(saved)));
    spyOn(component, 'loadSavedSearch');
    component.showSavedSearches();
    tick();
    expect(component.savedSearch).toBe(saved);
    expect(component.loadSavedSearch).toHaveBeenCalledWith(9);
  }));

  it('should delegate create and update modal methods', () => {
    spyOn(component, 'openSavedSearchModal');
    component.createNewSavedSearchModal();
    expect(component.openSavedSearchModal).toHaveBeenCalledWith(true);
    component.updateSavedSearchModal();
    expect(component.openSavedSearchModal).toHaveBeenCalledWith(false);
  });

  it('should open create saved-search modal, navigate and mark pristine', fakeAsync(() => {
    router.navigate.calls.reset();

    component.savedSearch = {id: 1, name: 'Current'} as any;

    const created = {id: 2, name: 'Created'} as any;
    const ref = modalRef(Promise.resolve(created));

    modalService.open.and.returnValue(ref);
    spyOn(component, 'populateFormWithSavedSearch');

    component.searchForm.markAsDirty();

    component.openSavedSearchModal(true);
    tick();

    expect(ref.componentInstance.savedSearch).toBe(component.savedSearch);
    expect(ref.componentInstance.searchCandidateRequest).toBeDefined();
    expect(router.navigate).toHaveBeenCalled();
    expect(component.populateFormWithSavedSearch).toHaveBeenCalled();
    expect(component.searchForm.pristine).toBeTrue();
  }));

  it('should update without navigating and tolerate modal dismissal', fakeAsync(() => {
    router.navigate.calls.reset();

    component.savedSearch = {id: 1, name: 'Current'} as any;

    spyOn(component, 'populateFormWithSavedSearch');

    modalService.open.and.returnValue(
      modalRef(Promise.resolve(component.savedSearch))
    );

    component.openSavedSearchModal(false);
    tick();

    expect(router.navigate).not.toHaveBeenCalled();
    expect(component.populateFormWithSavedSearch).toHaveBeenCalled();

    modalService.open.and.returnValue(
      modalRef(Promise.reject('dismissed'))
    );

    component.openSavedSearchModal(false);
    tick();

    expect(router.navigate).not.toHaveBeenCalled();
  }));

  it('should delete a saved search and navigate', fakeAsync(() => {
    component.savedSearch = {id: 4, name: 'Delete Me'} as any;
    const ref = modalRef(Promise.resolve(true));
    modalService.open.and.returnValue(ref);
    savedSearchService.delete.and.returnValue(of(null));
    router.navigate.calls.reset();

    component.deleteSavedSearchModal();
    tick();

    expect(ref.componentInstance.message).toContain('Delete Me');
    expect(savedSearchService.delete).toHaveBeenCalledWith(4);
    expect(router.navigate).toHaveBeenCalledWith(['search']);
    expect(component.loading).toBeFalse();
  }));

  it('should expose delete failure and ignore false confirmation', fakeAsync(() => {
    component.savedSearch = {id: 4, name: 'Delete Me'} as any;
    modalService.open.and.returnValue(modalRef(Promise.resolve(true)));
    savedSearchService.delete.and.returnValue(throwError('delete failed'));
    component.deleteSavedSearchModal();
    tick();
    expect(component.error).toBe('delete failed');
    expect(component.loading).toBeFalse();

    savedSearchService.delete.calls.reset();
    modalService.open.and.returnValue(modalRef(Promise.resolve(false)));
    component.deleteSavedSearchModal();
    tick();
    expect(savedSearchService.delete).not.toHaveBeenCalled();
  }));

  it('should fully populate the form from a saved search', fakeAsync(() => {
    component.candidateOccupations = [occupationA];
    component.partners = [partnerA];
    component.educationMajors = [majorA];
    component.surveyTypes = [surveyA];
    component.countries = [countryA];
    component.nationalities = [countryB];
    savedSearchService.get.and.returnValue(of({id: 77, name: 'Joined'} as any));
    spyOn(component.onFormChange, 'emit');

    const request: any = {
      savedSearchId: 1,
      countrySearchType: null,
      nationalitySearchType: null,
      listAllSearchType: null,
      listAnySearchType: null,
      statuses: [CandidateStatus.active],
      englishMinWrittenLevel: 2,
      englishMinSpokenLevel: 3,
      occupationIds: [30],
      lastModifiedFrom: '2026-01-02',
      lastModifiedTo: '2026-02-03',
      partnerIds: [20],
      educationMajorIds: [40],
      surveyTypeIds: [50],
      otherLanguageId: 2,
      otherMinWrittenLevel: 1,
      otherMinSpokenLevel: 2,
      countryIds: [10],
      nationalityIds: [11],
      unhcrStatuses: [UnhcrStatus.RegisteredAsylum],
      searchJoinRequests: [{savedSearchId: 77, name: 'Joined', searchType: 'and'}]
    };

    component.populateFormWithSavedSearch(request);
    tick();

    expect(component.searchForm.get('countrySearchType').value).toBe('or');
    expect(component.searchForm.get('nationalitySearchType').value).toBe('or');
    expect(component.searchForm.get('listAllSearchType').value).toBe('and');
    expect(component.searchForm.get('listAnySearchType').value).toBe('or');
    expect(component.searchForm.get('occupations').value).toEqual([occupationA]);
    expect(component.searchForm.get('partners').value).toEqual([partnerA]);
    expect(component.searchForm.get('educationMajors').value).toEqual([majorA]);
    expect(component.searchForm.get('surveyTypes').value).toEqual([surveyA]);
    expect(component.searchForm.get('countries').value).toEqual([countryA]);
    expect(component.searchForm.get('nationalities').value).toEqual([countryB]);
    expect(component.englishLanguagePicker.patchModel).toHaveBeenCalled();
    expect(component.otherLanguagePicker.patchModel).toHaveBeenCalled();
    expect(component.modifiedDatePicker.selectDate).toHaveBeenCalledTimes(2);
    expect(component.searchJoinArray.length).toBe(1);
    expect(savedSearchService.get).toHaveBeenCalledTimes(1);
    expect(savedSearchService.get.calls.mostRecent().args).toEqual([77]);
    expect(component.selectedBaseJoin).toEqual({
      id: 77,
      name: 'Joined'
    } as any);
    expect(component.formWrapper.nativeElement.click).toHaveBeenCalled();
    expect(component.searchForm.pristine).toBeTrue();
    expect(component.onFormChange.emit).toHaveBeenCalledWith(false);
  }));

  it('should populate empty optional collections without failing', () => {
    const request: any = {searchJoinRequests: []};
    component.populateFormWithSavedSearch(request);
    expect(component.searchForm.get('occupations').value).toEqual([]);
    expect(component.searchForm.get('partners').value).toEqual([]);
    expect(component.searchJoinArray.length).toBe(0);
  });

  it('should add and delete saved-search joins', fakeAsync(() => {
    component.savedSearch = {id: 5} as any;
    const join = {savedSearchId: 8, name: 'Join', searchType: 'and'};
    const ref = modalRef(Promise.resolve(join));
    modalService.open.and.returnValue(ref);
    component.addSavedSearchJoin();
    tick();
    expect(ref.componentInstance.currentSavedSearchId).toBe(5);
    expect(component.searchJoinArray.length).toBe(1);

    component.deleteSavedSearchJoin(join);
    expect(component.searchJoinArray.length).toBe(0);
  }));

  it('should handle selected dates and cleared dates', () => {
    component.handleDateSelected({
      fromDate: {year: 2026, month: 2, day: 3},
      toDate: {year: 2026, month: 4, day: 5}
    }, 'lastModified');
    expect(component.searchForm.get('lastModifiedFrom').value).toBe('2026-02-03');
    expect(component.searchForm.get('lastModifiedTo').value).toBe('2026-04-05');

    component.handleDateSelected({fromDate: null, toDate: null}, 'lastModified');
    expect(component.searchForm.get('lastModifiedFrom').value).toBeNull();
    expect(component.searchForm.get('lastModifiedTo').value).toBeNull();
  });

  it('should update English and other language controls', () => {
    component.handleLanguageLevelChange({languageId: 1, writtenLevel: 2, spokenLevel: 3}, 'english');
    expect(component.searchForm.get('englishMinWrittenLevel').value).toBe(2);
    expect(component.searchForm.get('englishMinSpokenLevel').value).toBe(3);

    component.handleLanguageLevelChange({languageId: 2, writtenLevel: 1, spokenLevel: 2}, 'other');
    expect(component.searchForm.get('otherLanguageId').value).toBe(2);
    expect(component.searchForm.get('otherMinWrittenLevel').value).toBe(1);
    expect(component.searchForm.get('otherMinSpokenLevel').value).toBe(2);
  });

  it('should change search operators', () => {
    component.handleSearchTypeChange('countrySearchType', 'not');
    expect(component.searchForm.get('countrySearchType').value).toBe('not');
  });

  it('should add and remove a base search join', () => {
    spyOn(component.onFormChange, 'emit');
    component.searchJoinArray.push(new UntypedFormBuilder().group({name: 'old'}));
    component.addBaseSearchJoin({id: 12, name: 'Base'} as any);
    expect(component.selectedBaseJoin).toEqual({
      savedSearchId: 12,
      name: 'Base',
      searchType: 'and'
    });
    expect(component.searchJoinArray.length).toBe(1);
    expect(component.searchForm.get('searchJoinRequests').dirty).toBeTrue();
    expect(component.onFormChange.emit).toHaveBeenCalled();

    component.storedBaseJoin = {id: 1};
    component.deleteBaseSearchJoin();
    expect(component.searchJoinArray.length).toBe(0);
    expect(component.selectedBaseJoin).toBeNull();
    expect(component.storedBaseJoin).toBeNull();
  });

  it('should delegate search edit authorization', () => {
    component.savedSearch = {id: 1} as any;
    authorizationService.canEditCandidateSource.and.returnValue(false);
    expect(component.canChangeSearchRequest()).toBeFalse();
    expect(authorizationService.canEditCandidateSource).toHaveBeenCalledWith(component.savedSearch);
  });

  it('should select and clear all options', () => {
    const options = [{id: 1}, {id: 2}];
    component.onSelectAll(options, 'partners');
    expect(component.searchForm.get('partners').value).toBe(options);
    expect(component.searchForm.get('partners').dirty).toBeTrue();

    component.onClearAll('partners');
    expect(component.searchForm.get('partners').value).toBeNull();
  });

  it('should create enum and object tooltips', () => {
    component.searchForm.get('statusesDisplay').patchValue([
      {key: 'active', stringValue: 'Active'},
      {key: 'draft', stringValue: 'Draft'}
    ]);
    expect(component.getTooltip('statusesDisplay')).toBe('Active, Draft');

    component.searchForm.get('partners').patchValue([
      {name: 'One'}, {name: 'Two'}
    ]);
    expect(component.getTooltip('partners')).toBe('One, Two');
  });

  it('should return partner default messages for source and non-source partners', () => {
    authorizationService.isSourcePartner.and.returnValue(true);
    authorizationService.isDefaultPartner.and.returnValue(false);
    expect(component.getPartnerDefaultMessage()).toContain('your partner');

    authorizationService.isDefaultPartner.and.returnValue(true);
    expect(component.getPartnerDefaultMessage()).toContain('any partner');

    authorizationService.isSourcePartner.and.returnValue(false);
    expect(component.getPartnerDefaultMessage()).toContain('any partner');
  });

  it('should delegate candidate-name and employer-partner checks', () => {
    authorizationService.canViewCandidateName.and.returnValue(false);
    authorizationService.isEmployerPartner.and.returnValue(true);
    expect(component.canViewCandidateName()).toBeFalse();
    expect(component.isEmployerPartner()).toBeTrue();
  });

  it('should identify empty and non-empty search terms', () => {
    component.currentSearchTerms = [];
    expect(component.isEmptySearchTerms()).toBeTrue();
    component.currentSearchTerms = ['term'];
    expect(component.isEmptySearchTerms()).toBeFalse();
    component.currentSearchTerms = null;
    expect(component.isEmptySearchTerms()).toBeTrue();
  });


});

