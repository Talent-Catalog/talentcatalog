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

import {SearchOccupationsComponent} from "./search-occupations.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {OccupationService} from "../../../services/occupation.service";
import {NgbModal, NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {AuthorizationService} from "../../../services/authorization.service";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {of, throwError} from "rxjs";
import {EditOccupationComponent} from "./edit/edit-occupation.component";
import {FileSelectorComponent} from "../../util/file-selector/file-selector.component";
import {MockUser} from "../../../MockData/MockUser";
import {SearchResults} from "../../../model/search-results";
import {Occupation} from "../../../model/occupation";

describe('SearchOccupationsComponent', () => {
  let component: SearchOccupationsComponent;
  let fixture: ComponentFixture<SearchOccupationsComponent>;
  let occupationServiceSpy: jasmine.SpyObj<OccupationService>;
  let modalServiceSpy: jasmine.SpyObj<NgbModal>;
  let authServiceSpy: jasmine.SpyObj<AuthorizationService>;
  const mockResults: SearchResults<Occupation> = {
    first: false,
    last: false,
    number: 0,
    size: 0,
    totalPages: 0,
    totalElements: 1,
    content: [{id:1, name:'Job', isco08Code:'3', status:'Active'}]
  };
  beforeEach(async () => {
    const occupationSpy = jasmine.createSpyObj('OccupationService', ['search', 'delete', 'addSystemLanguageTranslations']);
    const modalSpy = jasmine.createSpyObj('NgbModal', ['open']);
    const authSpy = jasmine.createSpyObj('AuthorizationService', ['isAnAdmin']);

    await TestBed.configureTestingModule({
      declarations: [SearchOccupationsComponent],
      imports: [FormsModule, ReactiveFormsModule, NgbModule, NgSelectModule],
      providers: [
        { provide: OccupationService, useValue: occupationSpy },
        { provide: NgbModal, useValue: modalSpy },
        { provide: AuthorizationService, useValue: authSpy },
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(SearchOccupationsComponent);
    component = fixture.componentInstance;
    occupationServiceSpy = TestBed.inject(OccupationService) as jasmine.SpyObj<OccupationService>;
    modalServiceSpy = TestBed.inject(NgbModal) as jasmine.SpyObj<NgbModal>;
    authServiceSpy = TestBed.inject(AuthorizationService) as jasmine.SpyObj<AuthorizationService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchOccupationsComponent);
    component = fixture.componentInstance;
    occupationServiceSpy.search.and.returnValue(of(mockResults));
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

  it('should search occupations', () => {
    component.search();

    expect(occupationServiceSpy.search).toHaveBeenCalled();
    expect(component.results).toEqual(mockResults);
    expect(component.loading).toBeFalse();
  });

  it('should open edit occupation modal', () => {
    const occupation = { id: 1, name: 'Occupation 1', isco08Code: '1234', status: 'active' };
    const modalRefMock = { componentInstance: { occupationId: null }, result: Promise.resolve(true) } as any;
    modalServiceSpy.open.and.returnValue(modalRefMock);

    component.editOccupation(occupation);

    expect(modalServiceSpy.open).toHaveBeenCalledWith(EditOccupationComponent, { centered: true, backdrop: 'static' });
    expect(modalRefMock.componentInstance.occupationId).toBe(occupation.id);
  });

  it('should delete occupation', (done) => {
    const occupation = { id: 1, name: 'Occupation 1', isco08Code: '1234', status: 'active' };
    occupationServiceSpy.delete.and.returnValue(of(true));
    const modalRefMock = {
      componentInstance: { message: '' },
      result: Promise.resolve(true)
    } as any;
    modalServiceSpy.open.and.returnValue(modalRefMock);

    component.deleteOccupation(occupation);

    setTimeout(() => {
      expect(occupationServiceSpy.delete).toHaveBeenCalledWith(occupation.id);
      done();
    }, 0);
  });

  it('should handle delete errors', (done) => {
    const occupation = { id: 1, name: 'Occupation 1', isco08Code: '1234', status: 'active' };
    occupationServiceSpy.delete.and.returnValue(throwError('Error'));
    const modalRefMock = {
      componentInstance: { message: '' },
      result: Promise.resolve(true)
    } as any;
    modalServiceSpy.open.and.returnValue(modalRefMock);

    component.deleteOccupation(occupation);

    setTimeout(() => {
      expect(component.error).toBe('Error');
      done();
    }, 0);
  });

  it('should open import translations modal', () => {
    const modalRefMock = {
      componentInstance: { validExtensions: undefined },
      result: Promise.resolve([{ name: 'test.csv' }])
    } as any;
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
    occupationServiceSpy.addSystemLanguageTranslations.and.returnValue(throwError('Error'));

    component['doImport'](files);

    setTimeout(() => {
      expect(component.error).toBe('Error');
      done();
    }, 0);
  });

  it('should check if user is admin', () => {
    authServiceSpy.isAnAdmin.and.returnValue(true);
    expect(component.isAnAdmin()).toBeTrue();
  });
});
