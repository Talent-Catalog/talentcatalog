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

import {SearchEducationMajorsComponent} from "./search-education-majors.component";
import {EducationMajorService} from "../../../services/education-major.service";
import {NgbModal, NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {AuthorizationService} from "../../../services/authorization.service";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {MockUser} from "../../../MockData/MockUser";
import {SearchResults} from "../../../model/search-results";
import {EducationMajor} from "../../../model/education-major";
import {of} from "rxjs";
import {ConfirmationComponent} from "../../util/confirm/confirmation.component";

describe('SearchEducationMajorsComponent', () => {
  let component: SearchEducationMajorsComponent;
  let fixture: ComponentFixture<SearchEducationMajorsComponent>;
  let educationMajorService: jasmine.SpyObj<EducationMajorService>;
  let modalService: jasmine.SpyObj<NgbModal>;
  let authService: jasmine.SpyObj<AuthorizationService>;
  const mockResults: SearchResults<EducationMajor> = {
    first: false, last: false, number: 0, size: 0,
    content: [{ id: 1, name: 'Test Major', status: 'active' }],
    totalElements: 1,
    totalPages: 1
  };

  beforeEach(async () => {
    const educationMajorServiceSpy = jasmine.createSpyObj('EducationMajorService', ['search', 'delete', 'addSystemLanguageTranslations']);
    const modalServiceSpy = jasmine.createSpyObj('NgbModal', ['open']);
    const authServiceSpy = jasmine.createSpyObj('AuthorizationService', ['isAnAdmin']);

    await TestBed.configureTestingModule({
      declarations: [ SearchEducationMajorsComponent ],
      imports: [FormsModule, ReactiveFormsModule, NgbModule,NgSelectModule],
      providers: [
        { provide: EducationMajorService, useValue: educationMajorServiceSpy },
        { provide: NgbModal, useValue: modalServiceSpy },
        { provide: AuthorizationService, useValue: authServiceSpy }
      ]
    })
    .compileComponents();

    educationMajorService = TestBed.inject(EducationMajorService) as jasmine.SpyObj<EducationMajorService>;
    modalService = TestBed.inject(NgbModal) as jasmine.SpyObj<NgbModal>;
    authService = TestBed.inject(AuthorizationService) as jasmine.SpyObj<AuthorizationService>;

    fixture = TestBed.createComponent(SearchEducationMajorsComponent);
    component = fixture.componentInstance;

    component.loggedInUser = new MockUser();
    educationMajorService.search.and.returnValue(of(mockResults));

    fixture.detectChanges();
  });

  it('should create the forms on initialization', () => {
    expect(component.importForm).toBeDefined();
    expect(component.searchForm).toBeDefined();
  });
  it('should call search method and update results', () => {
    component.search();

    expect(educationMajorService.search).toHaveBeenCalled();
    expect(component.results).toEqual(mockResults);
    expect(component.loading).toBeFalse();
  });

  it('should call delete method and refresh search on confirmation', fakeAsync( () => {
    const mockModalRef = {
      componentInstance: { message: "" },
      result: Promise.resolve(true)
    } as any;
    modalService.open.and.returnValue(mockModalRef);
    mockModalRef.result = Promise.resolve(true);

    educationMajorService.delete.and.returnValue(of(true));
    component.deleteEducationMajor({ id: 1, name: 'Test Major', status: 'active' });
    fixture.detectChanges();
    tick();
    expect(modalService.open).toHaveBeenCalledWith(ConfirmationComponent, jasmine.any(Object));

    // Expect delete method to have been called with the correct country id
    expect(educationMajorService.delete).toHaveBeenCalled();
  }));

  it('should check if the user is an admin', () => {
    authService.isAnAdmin.and.returnValue(true);
    expect(component.isAnAdmin()).toBeTrue();

    authService.isAnAdmin.and.returnValue(false);
    expect(component.isAnAdmin()).toBeFalse();
  });
});

