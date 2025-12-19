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
import {SearchExternalLinksComponent} from "./search-external-links.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {SavedListService} from "../../../services/saved-list.service";
import {NgbModal, NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {AuthorizationService} from "../../../services/authorization.service";
import {UntypedFormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {of} from "rxjs";
import {MockSavedList} from "../../../MockData/MockSavedList";
import {SearchResults} from "../../../model/search-results";
import {SavedList} from "../../../model/saved-list";
import {MockUser} from "../../../MockData/MockUser";
import {RouterLinkStubDirective} from "../../login/login.component.spec";
import {CreateExternalLinkComponent} from "./create/create-external-link.component";
import {EditExternalLinkComponent} from "./edit/edit-external-link.component";
import {ConfirmationComponent} from "../../util/confirm/confirmation.component";

describe('SearchExternalLinksComponent', () => {
  let component: SearchExternalLinksComponent;
  let fixture: ComponentFixture<SearchExternalLinksComponent>;
  let savedListServiceSpy: jasmine.SpyObj<SavedListService>;
  let modalServiceSpy: jasmine.SpyObj<NgbModal>;
  let authServiceSpy: jasmine.SpyObj<AuthorizationService>;
  const searchResults: SearchResults<SavedList> = {
    first: false,
    last: false,
    number: 0,
    size: 0,
    totalPages: 0,
    totalElements: 1,
    content: [MockSavedList]
  };

  const mockSavedList = MockSavedList;
  beforeEach(async () => {
    const savedListServiceMock = jasmine.createSpyObj('SavedListService', ['searchPaged', 'updateShortName']);
    const modalServiceMock = jasmine.createSpyObj('NgbModal', ['open']);
    const authServiceMock = jasmine.createSpyObj('AuthorizationService', ['isAnAdmin']);

    await TestBed.configureTestingModule({
      declarations: [SearchExternalLinksComponent,RouterLinkStubDirective],
      imports: [FormsModule, ReactiveFormsModule, NgbModule, NgSelectModule],
      providers: [
        UntypedFormBuilder,
        {provide: SavedListService, useValue: savedListServiceMock},
        {provide: NgbModal, useValue: modalServiceMock},
        {provide: AuthorizationService, useValue: authServiceMock}
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(SearchExternalLinksComponent);
    component = fixture.componentInstance;
    savedListServiceSpy = TestBed.inject(SavedListService) as jasmine.SpyObj<SavedListService>;
    modalServiceSpy = TestBed.inject(NgbModal) as jasmine.SpyObj<NgbModal>;
    authServiceSpy = TestBed.inject(AuthorizationService) as jasmine.SpyObj<AuthorizationService>;

    // Mock initial data and methods
    savedListServiceSpy.searchPaged.and.returnValue(of(searchResults));
    authServiceSpy.isAnAdmin.and.returnValue(true);
    component.loggedInUser = new MockUser();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should search on form value changes', () => {
    const searchSpy = spyOn(component, 'search').and.callThrough();

    component.searchForm.controls['keyword'].setValue('test');
    component.searchForm.controls['status'].setValue('active');
    component.search();

    expect(searchSpy).toHaveBeenCalled();
  });

  it('should open add link modal', () => {
    const modalRefMock = {
      result: Promise.resolve(true)
    } as any;
    modalServiceSpy.open.and.returnValue(modalRefMock);

    component.addLink();
    expect(modalServiceSpy.open).toHaveBeenCalledWith(CreateExternalLinkComponent, {
      centered: true,
      backdrop: 'static'
    });
  });

  it('should open edit link modal', () => {
    const modalRefMock = {
      componentInstance: {
        savedList: null
      },
      result: Promise.resolve(true)
    } as any;
    modalServiceSpy.open.and.returnValue(modalRefMock);

    const savedList = { id: 1, name: 'Test List' } as any;
    component.editLink(savedList);

    expect(modalServiceSpy.open).toHaveBeenCalledWith(EditExternalLinkComponent, {
      centered: true,
      backdrop: 'static'
    });
    expect(modalRefMock.componentInstance.savedList).toEqual(savedList);
  });

  it('should open delete confirmation modal and delete link on confirmation', fakeAsync( () => {
    const modalRefMock = {
      componentInstance: {
        message: ''
      },
      result: Promise.resolve(true)
    } as any;
    modalServiceSpy.open.and.returnValue(modalRefMock);
    savedListServiceSpy.updateShortName.and.returnValue(of(null));

    const savedList = { id: 1, name: 'Test List' } as any;
    component.deleteLink(savedList);
    tick();
    expect(modalServiceSpy.open).toHaveBeenCalledWith(ConfirmationComponent, {
      centered: true,
      backdrop: 'static'
    });
    expect(savedListServiceSpy.updateShortName).toHaveBeenCalledWith({ savedListId: 1, tcShortName: null });
  }));

});
