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
import {DefineSearchComponent} from "./define-search.component";
import {SearchQueryService} from "../../../services/search-query.service";
import {AuthenticationService} from "../../../services/authentication.service";
import {ComponentFixture, TestBed, waitForAsync} from "@angular/core/testing";
import {ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {
  NgbDatepickerModule,
  NgbModal,
  NgbTooltipModule,
  NgbTypeaheadModule
} from "@ng-bootstrap/ng-bootstrap";
import {RouterTestingModule} from "@angular/router/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {
  LanguageLevelFormControlComponent
} from "../../util/form/language-proficiency/language-level-form-control.component";
import {JoinSavedSearchComponent} from "../join-search/join-saved-search.component";
import {
  DateRangePickerComponent
} from "../../util/form/date-range-picker/date-range-picker.component";
import {SavedSearchService} from "../../../services/saved-search.service";
import {AuthorizationService} from "../../../services/authorization.service";
import {Subject} from "rxjs";

describe('DefineSearchComponent', () => {
  let component: DefineSearchComponent;
  let fixture: ComponentFixture<DefineSearchComponent>;
  let searchQueryServiceSpy: jasmine.SpyObj<SearchQueryService>;
  let authorizationServiceSpy: jasmine.SpyObj<AuthorizationService>;

  beforeEach(waitForAsync(() => {
    const searchQuerySpy = jasmine.createSpyObj(
      'SearchQueryService', ['changeSearchQuery'], {
        currentSearchTerms$: new Subject<string[]>()
      });
    const savedSearchSpy = jasmine.createSpyObj('SavedSearchService', ['getSavedSearchTypeInfos','load']);
    const authenticationSpy = jasmine.createSpyObj('AuthenticationService',
      ['getLoggedInUser']);
    const authorizationSpy =
      jasmine.createSpyObj<AuthorizationService>('AuthorizationService', [
        'isSourcePartner',
        'isDefaultPartner',
        'canViewCandidateName',
        'isEmployerPartner',
        'canEditCandidateSource'
    ]);

    TestBed.configureTestingModule({
      declarations: [DefineSearchComponent,LanguageLevelFormControlComponent,JoinSavedSearchComponent,DateRangePickerComponent],
      imports: [ReactiveFormsModule,NgbTooltipModule,HttpClientTestingModule,NgbTypeaheadModule,NgbDatepickerModule,RouterTestingModule,NgSelectModule],
      providers: [
        UntypedFormBuilder,
        NgbModal,
        { provide: SearchQueryService, useValue: searchQuerySpy },
        { provide: AuthenticationService, useValue: authenticationSpy },
        { provide: SavedSearchService, useValue: savedSearchSpy },
        { provide: AuthorizationService, useValue: authorizationSpy }
      ]
    }).compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DefineSearchComponent);
    component = fixture.componentInstance;
    searchQueryServiceSpy = TestBed.inject(SearchQueryService) as jasmine.SpyObj<SearchQueryService>;
    authorizationServiceSpy = TestBed.inject(AuthorizationService) as jasmine.SpyObj<AuthorizationService>;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with default values', () => {
    expect(component.searchForm).toBeDefined();
    expect(component.searchForm.get('savedSearchId').value).toBeNull();
  });

  it('should set simpleQueryString in searchQueryService on value changes', () => {
    component.searchForm.get('simpleQueryString').patchValue('test query');
    expect(searchQueryServiceSpy.changeSearchQuery).toHaveBeenCalledWith('test query');
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

  it('should call isEmployerPartner() method', () => {
    expect(authorizationServiceSpy.isEmployerPartner).toHaveBeenCalled();
  })

  it('should call isSourcePartner() method', () => {
    expect(authorizationServiceSpy.isSourcePartner).toHaveBeenCalled();
  })

  it('should hide the UNHCR status filter if isEmployerPartner returns true', () => {
    authorizationServiceSpy.isEmployerPartner.and.returnValue(true);
    fixture.detectChanges();
    const unhcrFilter = fixture.nativeElement.querySelector('#unhcrStatusFilter');
    expect(unhcrFilter).toBeNull();
  })

  it('should show the referrer filter if isEmployerPartner returns false', () => {
    authorizationServiceSpy.isEmployerPartner.and.returnValue(false);
    fixture.detectChanges();
    const referrerFilter = fixture.nativeElement.querySelector('#referrerFilter');
    expect(referrerFilter).toBeTruthy();
  })
});

