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

import {DropdownTranslationsComponent} from "./dropdown-translations.component";
import {ComponentFixture, fakeAsync, TestBed, tick, waitForAsync} from "@angular/core/testing";
import {LanguageService} from "../../../../services/language.service";
import {CountryService} from "../../../../services/country.service";
import {TranslationService} from "../../../../services/translation.service";
import {ReactiveFormsModule, UntypedFormArray, UntypedFormBuilder} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgbModal, NgbPaginationModule} from "@ng-bootstrap/ng-bootstrap";
import {MockUser} from "../../../../MockData/MockUser";
import {SystemLanguage} from "../../../../model/language";
import {of, throwError} from "rxjs";
import {SearchResults} from "../../../../model/search-results";
import {TranslatedObject} from "../../../../model/translated-object";
import {Translation} from "../../../../model/translation";

describe('DropdownTranslationsComponent', () => {
  let component: DropdownTranslationsComponent;
  let fixture: ComponentFixture<DropdownTranslationsComponent>;
  let languageServiceSpy: jasmine.SpyObj<LanguageService>;
  let countryServiceSpy: jasmine.SpyObj<CountryService>;
  let translationServiceSpy: jasmine.SpyObj<TranslationService>;
  let formBuilder: UntypedFormBuilder;
  const systemLanguages: SystemLanguage[] = [
    {id:1, language: 'fr', label: 'French',rtl:false },
    {id:2, language: 'es', label: 'Spanish',rtl:false }
  ];
  const results: SearchResults<TranslatedObject> = {
    first: false, last: false, number: 0, size: 0, totalPages: 0,
    totalElements: 2,
    content: [
      { id: 1, translatedId: 1, name: 'Translation 1', status: 'Active', translatedName: 'Translation One' },
      { id: 2, translatedId: 2, name: 'Translation 2', status: 'Inactive', translatedName: 'Translation Two' }
    ]
  };
  const translationData:Translation = {
    id: 1,
    objectId: 123,
    objectType: 'country',
    value: 'Updated Translation'
  };
  beforeEach(waitForAsync(() => {
    const languageServiceSpyObj = jasmine.createSpyObj('LanguageService', ['listSystemLanguages']);
    const countryServiceSpyObj = jasmine.createSpyObj('CountryService', ['']); // Add methods if needed
    const translationServiceSpyObj = jasmine.createSpyObj('TranslationService', ['search', 'update', 'create']);

    TestBed.configureTestingModule({
      declarations: [DropdownTranslationsComponent],
      imports: [ReactiveFormsModule,NgSelectModule,HttpClientTestingModule,NgbPaginationModule],
      providers: [
        { provide: LanguageService, useValue: languageServiceSpyObj },
        { provide: CountryService, useValue: countryServiceSpyObj },
        { provide: TranslationService, useValue: translationServiceSpyObj },
        UntypedFormBuilder,
        NgbModal,
      ]
    }).compileComponents();

    languageServiceSpy = TestBed.inject(LanguageService) as jasmine.SpyObj<LanguageService>;
    countryServiceSpy = TestBed.inject(CountryService) as jasmine.SpyObj<CountryService>;
    translationServiceSpy = TestBed.inject(TranslationService) as jasmine.SpyObj<TranslationService>;
    formBuilder = TestBed.inject(UntypedFormBuilder);
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(DropdownTranslationsComponent);
    component = fixture.componentInstance;
    component.loggedInUser = new MockUser();
    languageServiceSpy.listSystemLanguages.and.returnValue(of(systemLanguages));
    translationServiceSpy.search.and.returnValue(of(results));
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form and load system languages on ngOnInit', fakeAsync(() => {
    component.ngOnInit();
    tick();
    expect(component.searchForm).toBeTruthy();
    expect(component.systemLanguages).toEqual(systemLanguages);
    expect(component.loading).toBeFalse();
  }));

  it('should search translations and update form on search', (() => {
    component.searchForm.patchValue({ keyword: 'test', type: 'country', language: 'fr' });
    component.search();
    expect(translationServiceSpy.search).toHaveBeenCalledWith('country', {
      keyword: 'test',
      type: 'country',
      language: 'fr',
      pageNumber: 0,
      pageSize: component.pageSize
    });
    expect(component.results).toEqual(results);
    expect((component.topLevelForm.get('translations')  as UntypedFormArray).length).toBe(2);
    expect(component.loading).toBeFalse();
  }));

  it('should update translation and search again on updateTranslation', (() => {
    const translationFormValue = {
      translatedId: 1,
      objectId: 1,
      objectType: 'country',
      language: 'fr',
      value: 'Updated Translation'
    };


    translationServiceSpy.update.and.returnValue(of(translationData));
    translationServiceSpy.search.and.returnValue(of(results));
    component.topLevelForm = formBuilder.group({
      translations: formBuilder.array([
        formBuilder.group(translationFormValue)
      ])
    });
    component.searchForm.patchValue({ keyword: 'test', type: 'country', language: 'fr' });
    component.updateTranslation(0);
    expect(translationServiceSpy.update).toHaveBeenCalledWith(1, translationFormValue);
    expect(translationServiceSpy.search).toHaveBeenCalled(); // Should trigger search again
  }));

  it('should handle error on search', (() => {
    const errorMessage = 'Error searching translations';
    translationServiceSpy.search.and.returnValue(throwError(errorMessage));
    component.searchForm.patchValue({ keyword: 'test', type: 'country', language: 'fr' });

    component.search();
    expect(component.error).toEqual(errorMessage);
    expect(component.loading).toBeFalse();
  }));


  it('should create new translation and search again on updateTranslation if translatedId is not provided', (() => {
    const translationFormValue = {
      translatedId: null,
      objectId: 1,
      objectType: 'country',
      language: 'fr',
      value: 'New Translation'
    };
    translationServiceSpy.create.and.returnValue(of(translationData));
    component.searchForm.patchValue({ keyword: 'test', type: 'country', language: 'fr' });
    component.topLevelForm = formBuilder.group({
      translations: formBuilder.array([
        formBuilder.group(translationFormValue)
      ])
    });
    component.updateTranslation(0);
    expect(translationServiceSpy.create).toHaveBeenCalledWith(translationFormValue);
    expect(translationServiceSpy.search).toHaveBeenCalled(); // Should trigger search again
  }));

});
