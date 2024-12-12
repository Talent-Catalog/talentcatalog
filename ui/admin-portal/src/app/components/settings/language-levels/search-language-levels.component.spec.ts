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

import {SearchResults} from "../../../model/search-results";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgbModal, NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {NgSelectModule} from "@ng-select/ng-select";
import {SearchLanguageLevelsComponent} from "./search-language-levels.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {LanguageLevelService} from "../../../services/language-level.service";
import {AuthorizationService} from "../../../services/authorization.service";
import {of, throwError} from "rxjs";
import {MockUser} from "../../../MockData/MockUser";
import {LanguageLevel} from "../../../model/language-level";
import {CreateLanguageLevelComponent} from "./create/create-language-level.component";
import {EditLanguageLevelComponent} from "./edit/edit-language-level.component";
import {FileSelectorComponent} from "../../util/file-selector/file-selector.component";

describe('SearchLanguageLevelsComponent', () => {
  let component: SearchLanguageLevelsComponent;
  let fixture: ComponentFixture<SearchLanguageLevelsComponent>;
  let languageLevelServiceSpy: jasmine.SpyObj<LanguageLevelService>;
  let modalServiceSpy: jasmine.SpyObj<NgbModal>;
  let authServiceSpy: jasmine.SpyObj<AuthorizationService>;
  const searchResults: SearchResults<LanguageLevel> = {
    first: false,
    last: false,
    number: 0,
    size: 0,
    totalPages: 0,
    totalElements: 1,
    content: [{id:1, name:'IELTS', level:3, status:'Active'}]
  };
  beforeEach(async () => {
    const languageLevelSpy = jasmine.createSpyObj('LanguageLevelService', ['search', 'delete', 'addSystemLanguageTranslations']);
    const modalSpy = jasmine.createSpyObj('NgbModal', ['open']);
    const authSpy = jasmine.createSpyObj('AuthorizationService', ['isAnAdmin']);

    await TestBed.configureTestingModule({
      declarations: [SearchLanguageLevelsComponent],
      imports: [FormsModule, ReactiveFormsModule, NgbModule, NgSelectModule],
      providers: [
        { provide: LanguageLevelService, useValue: languageLevelSpy },
        { provide: NgbModal, useValue: modalSpy },
        { provide: AuthorizationService, useValue: authSpy },
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(SearchLanguageLevelsComponent);
    component = fixture.componentInstance;
    languageLevelServiceSpy = TestBed.inject(LanguageLevelService) as jasmine.SpyObj<LanguageLevelService>;
    modalServiceSpy = TestBed.inject(NgbModal) as jasmine.SpyObj<NgbModal>;
    authServiceSpy = TestBed.inject(AuthorizationService) as jasmine.SpyObj<AuthorizationService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchLanguageLevelsComponent);
    component = fixture.componentInstance;
    languageLevelServiceSpy.search.and.returnValue(of(searchResults));
    component.loggedInUser = new MockUser();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize forms and set default values', () => {
    component.ngOnInit();
    expect(component.importForm).toBeDefined();
    expect(component.searchForm).toBeDefined();
    expect(component.pageNumber).toBe(1);
    expect(component.pageSize).toBe(50);
  });

  it('should search language levels', () => {

    component.search();
    expect(languageLevelServiceSpy.search).toHaveBeenCalled();
    expect(component.results).toEqual(searchResults);
    expect(component.loading).toBeFalse();
  });

  it('should add language level', () => {
    const modalRefMock = { result: Promise.resolve(true) } as any;
    modalServiceSpy.open.and.returnValue(modalRefMock);

    component.addLanguageLevel();

    expect(modalServiceSpy.open).toHaveBeenCalledWith(CreateLanguageLevelComponent, { centered: true, backdrop: 'static' });
  });

  it('should edit language level', () => {
    const languageLevel = { id: 1, level: 'A1', name: 'Beginner', status: 'active' };
    const modalRefMock = { componentInstance: { languageLevelId: null,validExtensions:undefined  }, result: Promise.resolve(true) } as any;
    modalServiceSpy.open.and.returnValue(modalRefMock);

    component.editLanguageLevel(languageLevel);

    expect(modalServiceSpy.open).toHaveBeenCalledWith(EditLanguageLevelComponent, { centered: true, backdrop: 'static' });
    expect(modalRefMock.componentInstance.languageLevelId).toBe(languageLevel.id);
  });

  it('should delete language level', (done) => {
    const languageLevel = { id: 1, level: 'A1', name: 'Beginner', status: 'active' };
    languageLevelServiceSpy.delete.and.returnValue(of(true));
    const modalRefMock = {
      componentInstance: { message: '',validExtensions:undefined  },
      result: Promise.resolve(true)
    } as any;
    modalServiceSpy.open.and.returnValue(modalRefMock);

    component.deleteLanguageLevel(languageLevel);

    setTimeout(() => {
      expect(languageLevelServiceSpy.delete).toHaveBeenCalledWith(languageLevel.id);
      done();
    }, 0);
  });

  it('should handle delete errors', (done) => {
    const languageLevel = { id: 1, level: 'A1', name: 'Beginner', status: 'active' };
    languageLevelServiceSpy.delete.and.returnValue(throwError('Error'));
    const modalRefMock = {
      componentInstance: { message: '',validExtensions:undefined },
      result: Promise.resolve(true)
    } as any;
    modalServiceSpy.open.and.returnValue(modalRefMock);

    component.deleteLanguageLevel(languageLevel);

    setTimeout(() => {
      expect(component.error).toBe('Error');
      done();
    }, 0);
  });

  it('should import translations', () => {
    const modalRefMock = {
      componentInstance: { message: '',validExtensions:undefined },
      result: Promise.resolve([{ name: 'test.csv' }]) } as any;
    modalServiceSpy.open.and.returnValue(modalRefMock);
    component.importForm.controls['langCode'].setValue('en');

    component.importTranslations();

    expect(modalServiceSpy.open).toHaveBeenCalledWith(FileSelectorComponent, {
      centered: true,
      backdrop: 'static'
    });
  });

  it('should handle import errors', (done) => {
    component.importForm.controls['langCode'].setValue('en');
    const files = [{ name: 'test.csv' }] as any;
    languageLevelServiceSpy.addSystemLanguageTranslations.and.returnValue(throwError('Error'));

    component['doImport'](files);

    setTimeout(() => {
      expect(component.error).toBe('Error');
      done();
    }, 0);
  });
});
