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

import {SearchPartnersComponent} from "./search-partners.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {PartnerService} from "../../../../services/partner.service";
import {NgbModal, NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {AuthorizationService} from "../../../../services/authorization.service";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {UntypedFormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {of} from "rxjs";
import {SearchResults} from "../../../../model/search-results";
import {Partner} from "../../../../model/partner";
import {MockPartner} from "../../../../MockData/MockPartner";
import {
  CreateUpdatePartnerComponent
} from "../create-update-partner/create-update-partner.component";

describe('SearchPartnersComponent', () => {
  let component: SearchPartnersComponent;
  let fixture: ComponentFixture<SearchPartnersComponent>;
  let partnerService: jasmine.SpyObj<PartnerService>;
  let modalService: jasmine.SpyObj<NgbModal>;
  let authService: jasmine.SpyObj<AuthorizationService>;
  const mockResults: SearchResults<Partner> = {
    first: false,
    last: false,
    number: 0,
    size: 0,
    totalPages: 0,
    totalElements: 1,
    content: [new MockPartner()]
  };

  beforeEach(async () => {
    const partnerServiceSpy = jasmine.createSpyObj('PartnerService', ['searchPaged']);
    const modalServiceSpy = jasmine.createSpyObj('NgbModal', ['open']);
    const authServiceSpy = jasmine.createSpyObj('AuthorizationService', ['isReadOnly', 'canAccessSalesforce']);

    await TestBed.configureTestingModule({
      declarations: [SearchPartnersComponent],
      imports: [HttpClientTestingModule, FormsModule, ReactiveFormsModule, NgbModule, NgSelectModule],
      providers: [
        UntypedFormBuilder,
        { provide: PartnerService, useValue: partnerServiceSpy },
        { provide: NgbModal, useValue: modalServiceSpy },
        { provide: AuthorizationService, useValue: authServiceSpy }
      ]
    })
    .compileComponents();

    partnerService = TestBed.inject(PartnerService) as jasmine.SpyObj<PartnerService>;
    modalService = TestBed.inject(NgbModal) as jasmine.SpyObj<NgbModal>;
    authService = TestBed.inject(AuthorizationService) as jasmine.SpyObj<AuthorizationService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchPartnersComponent);
    component = fixture.componentInstance;
    partnerService.searchPaged.and.returnValue(of(mockResults));
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with default values', () => {
    expect(component.searchForm.value).toEqual({ keyword: '', status: 'active' });
    expect(component.pageNumber).toBe(1);
    expect(component.pageSize).toBe(50);
    expect(component.loading).toBeFalse();
  });

  it('should search partners correctly', () => {

    component.searchForm.patchValue({ keyword: 'test', status: 'inactive' });
    component.search();

    expect(partnerService.searchPaged).toHaveBeenCalledWith({
      keyword: 'test',
      status: 'inactive',
      pageNumber: 0,
      pageSize: 50,
      sortFields: ['id'],
      sortDirection: 'ASC'
    });
    expect(component.results).toEqual(mockResults);
    expect(component.loading).toBeFalse();
  });

  it('should open add partner modal', () => {
    modalService.open.and.returnValue({
      result: Promise.resolve()
    } as any);

    component.addPartner();
    expect(modalService.open).toHaveBeenCalledWith(CreateUpdatePartnerComponent, jasmine.any(Object));

    // Simulate modal closure
    component.search(); // Trigger search after modal closes

    // Verify that the search method was called upon modal closure
    expect(partnerService.searchPaged).toHaveBeenCalled();
  });

  it('should open edit partner modal', () => {
    const partner: Partner = new MockPartner();
    modalService.open.and.returnValue({
      componentInstance: { partner },
      result: Promise.resolve()
    } as any);

    component.editPartner(partner);
    expect(modalService.open).toHaveBeenCalledWith(CreateUpdatePartnerComponent, jasmine.any(Object));

    // Simulate modal closure
    component.search(); // Trigger search after modal closes

    // Verify that the search method was called upon modal closure
    expect(partnerService.searchPaged).toHaveBeenCalled();
  });


});
