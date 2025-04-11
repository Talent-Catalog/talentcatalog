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

import {SearchIndustriesComponent} from "./search-industries.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {IndustryService} from "../../../services/industry.service";
import {NgbModal, NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {AuthorizationService} from "../../../services/authorization.service";
import {UntypedFormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {of} from "rxjs";
import {SearchResults} from "../../../model/search-results";
import {Industry} from "../../../model/industry";
import {MockUser} from "../../../MockData/MockUser";
import {ConfirmationComponent} from "../../util/confirm/confirmation.component";
import {EditIndustryComponent} from "./edit/edit-industry.component";
import {CreateIndustryComponent} from "./create/create-industry.component";

describe('SearchIndustriesComponent', () => {
  let component: SearchIndustriesComponent;
  let fixture: ComponentFixture<SearchIndustriesComponent>;
  let industryServiceSpy: jasmine.SpyObj<IndustryService>;
  let modalServiceSpy: jasmine.SpyObj<NgbModal>;
  let authServiceSpy: jasmine.SpyObj<AuthorizationService>;
  const searchResults: SearchResults<Industry> = {
    first: false,
    last: false,
    number: 0,
    size: 0,
    totalPages: 0,
    totalElements: 1,
    content: [{id:1,name:'IT',status:'active'}]
  };

  beforeEach(async () => {
    const industryServiceMock = jasmine.createSpyObj('IndustryService', ['search', 'delete']);
    const modalServiceMock = jasmine.createSpyObj('NgbModal', ['open']);
    const authServiceMock = jasmine.createSpyObj('AuthorizationService', ['isAnAdmin']);

    await TestBed.configureTestingModule({
      declarations: [SearchIndustriesComponent],
      imports: [FormsModule, ReactiveFormsModule, NgbModule, NgSelectModule],
      providers: [
        UntypedFormBuilder,
        { provide: IndustryService, useValue: industryServiceMock },
        { provide: NgbModal, useValue: modalServiceMock },
        { provide: AuthorizationService, useValue: authServiceMock }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(SearchIndustriesComponent);
    component = fixture.componentInstance;
    industryServiceSpy = TestBed.inject(IndustryService) as jasmine.SpyObj<IndustryService>;
    modalServiceSpy = TestBed.inject(NgbModal) as jasmine.SpyObj<NgbModal>;
    authServiceSpy = TestBed.inject(AuthorizationService) as jasmine.SpyObj<AuthorizationService>;

    // Mock initial data and methods
    industryServiceSpy.search.and.returnValue(of(searchResults));
    authServiceSpy.isAnAdmin.and.returnValue(true);
    component.loggedInUser = new MockUser();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the form with default values', () => {
    expect(component.searchForm.get('keyword').value).toBe('');
    expect(component.searchForm.get('status').value).toBe('active');
  });

  it('should handle search results', () => {
    component.search();

    expect(component.results).toEqual(searchResults);
    expect(component.loading).toBeFalse();
  });

  it('should open add industry modal', () => {
    const modalRefMock = {
      result: Promise.resolve(true)
    } as any;
    modalServiceSpy.open.and.returnValue(modalRefMock);

    component.addIndustry();

    expect(modalServiceSpy.open).toHaveBeenCalledWith(CreateIndustryComponent, {
      centered: true,
      backdrop: 'static'
    });
  });

  it('should open edit industry modal', () => {
    const modalRefMock = {
      componentInstance: {
        industryId: null,
        message: ''
      },
      result: Promise.resolve(true)
    } as any;
    modalServiceSpy.open.and.returnValue(modalRefMock);

    const industry = { id: 1, name: 'Test Industry' };
    component.editIndustry(industry);

    expect(modalServiceSpy.open).toHaveBeenCalledWith(EditIndustryComponent, {
      centered: true,
      backdrop: 'static'
    });
    expect(modalRefMock.componentInstance.industryId).toEqual(industry.id);
  });

  it('should open delete industry confirmation modal', () => {
    const modalRefMock = {
      componentInstance: { message: '' },
      result: Promise.resolve(true)
    } as any;
    modalServiceSpy.open.and.returnValue(modalRefMock);

    const industry = { id: 1, name: 'Test Industry' };
    component.deleteIndustry(industry);

    expect(modalServiceSpy.open).toHaveBeenCalledWith(ConfirmationComponent, {
      centered: true,
      backdrop: 'static'
    });
  });

  it('should delete industry', (done) => {
    const industry = { id: 1, name: 'Test Industry' };
    industryServiceSpy.delete.and.returnValue(of(true));
    const modalRefMock = {
      componentInstance: { message: '' },
      result: Promise.resolve(true)
    } as any;
    modalServiceSpy.open.and.returnValue(modalRefMock);

    // Call the deleteIndustry method
    component.deleteIndustry(industry);

    // Use setTimeout to allow async operations to complete
    setTimeout(() => {
      // Verify that the delete method was called
      expect(industryServiceSpy.delete).toHaveBeenCalledWith(industry.id);
      done();
    }, 0);
  });

  it('should check if the user is an admin', () => {
    authServiceSpy.isAnAdmin.and.returnValue(true);
    expect(component.isAnAdmin()).toBeTrue();

    authServiceSpy.isAnAdmin.and.returnValue(false);
    expect(component.isAnAdmin()).toBeFalse();
  });
});
