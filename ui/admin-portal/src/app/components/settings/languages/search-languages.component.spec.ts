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

import {SearchLanguagesComponent} from "./search-languages.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {LanguageService} from "../../../services/language.service";
import {NgbModal, NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {AuthorizationService} from "../../../services/authorization.service";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {of} from "rxjs";
import {CreateLanguageComponent} from "./create/create-language.component";
import {MockUser} from "../../../MockData/MockUser";

describe('SearchLanguagesComponent', () => {
  let component: SearchLanguagesComponent;
  let fixture: ComponentFixture<SearchLanguagesComponent>;
  let languageServiceSpy: jasmine.SpyObj<LanguageService>;
  let modalServiceSpy: jasmine.SpyObj<NgbModal>;
  let authServiceSpy: jasmine.SpyObj<AuthorizationService>;
  const mockResults = [];

  beforeEach(async () => {
    const languageSpy = jasmine.createSpyObj('LanguageService', ['listSystemLanguages']);
    const modalSpy = jasmine.createSpyObj('NgbModal', ['open']);
    const authSpy = jasmine.createSpyObj('AuthorizationService', ['isAnAdmin']);

    await TestBed.configureTestingModule({
      declarations: [SearchLanguagesComponent],
      imports: [FormsModule, ReactiveFormsModule, NgbModule, NgSelectModule],
      providers: [
        { provide: LanguageService, useValue: languageSpy },
        { provide: NgbModal, useValue: modalSpy },
        { provide: AuthorizationService, useValue: authSpy },
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(SearchLanguagesComponent);
    component = fixture.componentInstance;
    languageServiceSpy = TestBed.inject(LanguageService) as jasmine.SpyObj<LanguageService>;
    modalServiceSpy = TestBed.inject(NgbModal) as jasmine.SpyObj<NgbModal>;
    authServiceSpy = TestBed.inject(AuthorizationService) as jasmine.SpyObj<AuthorizationService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchLanguagesComponent);
    component = fixture.componentInstance;
    languageServiceSpy.listSystemLanguages.and.returnValue(of(mockResults));
    component.loggedInUser = new MockUser();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form and set default values', () => {
    component.ngOnInit();
    expect(component.searchForm).toBeDefined();
    expect(component.pageNumber).toBe(1);
    expect(component.pageSize).toBe(50);
  });


  it('should search languages', () => {

    component.search();

    expect(languageServiceSpy.listSystemLanguages).toHaveBeenCalled();
    expect(component.results).toEqual(mockResults);
    expect(component.loading).toBeFalse();
  });

  it('should add language', () => {
    const modalRefMock = { result: Promise.resolve(true) } as any;
    modalServiceSpy.open.and.returnValue(modalRefMock);

    component.addLanguage();

    expect(modalServiceSpy.open).toHaveBeenCalledWith(CreateLanguageComponent, { centered: true, backdrop: 'static' });
  });

  it('should check if user is admin', () => {
    authServiceSpy.isAnAdmin.and.returnValue(true);
    expect(component.isAnAdmin()).toBeTrue();
  });
});
