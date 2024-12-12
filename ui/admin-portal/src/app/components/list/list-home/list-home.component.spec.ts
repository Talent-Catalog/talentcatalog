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
import {ListHomeComponent} from "./list-home.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {SavedSearchService, SavedSearchTypeInfo} from "../../../services/saved-search.service";
import {AuthorizationService} from "../../../services/authorization.service";
import {AuthenticationService} from "../../../services/authentication.service";
import {NgbActiveModal, NgbNavModule, NgbPaginationModule} from "@ng-bootstrap/ng-bootstrap";
import {SalesforceService} from "../../../services/salesforce.service";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {MockPartner} from "../../../MockData/MockPartner";
import {MockUser} from "../../../MockData/MockUser";
import {SavedSearchSubtype, SavedSearchType} from "../../../model/saved-search";
import {
  BrowseCandidateSourcesComponent
} from "../../candidates/show/browse/browse-candidate-sources.component";
import {UntypedFormBuilder, ReactiveFormsModule} from "@angular/forms";
import {RouterLinkStubDirective} from "../../login/login.component.spec";
import {RouterTestingModule} from "@angular/router/testing";
import {DatePipe, TitleCasePipe} from "@angular/common";
import {LocalStorageService} from "../../../services/local-storage.service";

describe('ListHomeComponent', () => {
  let component: ListHomeComponent;
  let fixture: ComponentFixture<ListHomeComponent>;
  let savedSearchServiceSpy: jasmine.SpyObj<SavedSearchService>;
  let localStorageServiceSpy: jasmine.SpyObj<LocalStorageService>;
  let authorizationServiceSpy: jasmine.SpyObj<AuthorizationService>;
  let authenticationServiceSpy: jasmine.SpyObj<AuthenticationService>;
  let activeModalSpy: jasmine.SpyObj<NgbActiveModal>;
  let salesforceServiceSpy: jasmine.SpyObj<SalesforceService>;

  beforeEach(async () => {
    const savedSearchServiceSpyObj = jasmine.createSpyObj('SavedSearchService', ['getSavedSearchTypeInfos']);
    const localStorageServiceSpyObj = jasmine.createSpyObj('LocalStorageService', ['get','set']);
    const authorizationServiceSpyObj = jasmine.createSpyObj('AuthorizationService', ['isEmployerPartner', 'canSeeJobDetails']);
    const authenticationServiceSpyObj = jasmine.createSpyObj('AuthenticationService', ['getLoggedInUser']);
    const activeModalSpyObj = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);
    const salesforceServiceSpyObj = jasmine.createSpyObj('SalesforceService', ['fetchJob']);

    authorizationServiceSpyObj.canSeeJobDetails.and.returnValue(true);

    await TestBed.configureTestingModule({
      declarations: [ListHomeComponent,BrowseCandidateSourcesComponent,RouterLinkStubDirective],
      imports: [HttpClientTestingModule,NgbNavModule,RouterTestingModule,NgbPaginationModule,ReactiveFormsModule],
      providers: [
        UntypedFormBuilder,
        DatePipe,
        TitleCasePipe,
        { provide: SavedSearchService, useValue: savedSearchServiceSpyObj },
        { provide: LocalStorageService, useValue: localStorageServiceSpyObj },
        { provide: AuthorizationService, useValue: authorizationServiceSpyObj },
        { provide: AuthenticationService, useValue: authenticationServiceSpyObj },
        { provide: NgbActiveModal, useValue: activeModalSpyObj },
        { provide: SalesforceService, useValue: salesforceServiceSpyObj }
      ]
    }).compileComponents();

    savedSearchServiceSpy = TestBed.inject(SavedSearchService) as jasmine.SpyObj<SavedSearchService>;
    localStorageServiceSpy = TestBed.inject(LocalStorageService) as jasmine.SpyObj<LocalStorageService>;
    authorizationServiceSpy = TestBed.inject(AuthorizationService) as jasmine.SpyObj<AuthorizationService>;
    authenticationServiceSpy = TestBed.inject(AuthenticationService) as jasmine.SpyObj<AuthenticationService>;
    activeModalSpy = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;
    salesforceServiceSpy = TestBed.inject(SalesforceService) as jasmine.SpyObj<SalesforceService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ListHomeComponent);
    component = fixture.componentInstance;
    component.activeTabId= "type:job";
    component.loggedInPartner = new MockPartner();
    const mockSavedSearchTypeInfo:SavedSearchTypeInfo = {
      savedSearchType:SavedSearchType.job,
      title:"SavedSearchTypeInfo",
      categories: [{
        savedSearchSubtype:SavedSearchSubtype.it,
        title:"SavedSearchType"
      }]
    }
    component.savedSearchTypeInfos = [mockSavedSearchTypeInfo];

    // savedSearchServiceSpy.getSavedSearchTypeInfos.and.returnValue([mockSavedSearchTypeInfo]);
    authenticationServiceSpy.getLoggedInUser.and.returnValue(new MockUser());
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should set default tab ID to "MyLists"', () => {
    expect(component['defaultTabId']).toBe('MyLists');
  });

  it('should see public lists for non-employer users', () => {
    // Mocking isEmployerPartner to return false
    authorizationServiceSpy.isEmployerPartner.and.returnValue(false);
    expect(component.seesPublicLists()).toBeTrue();
  });

  it('should not see public lists for employer users', () => {
    // Mocking isEmployerPartner to return true
    authorizationServiceSpy.isEmployerPartner.and.returnValue(true);
    expect(component.seesPublicLists()).toBeFalse();
  });

  it('should not show public lists to partners who are not source, destination or employer', () => {
    authorizationServiceSpy.canSeeJobDetails.and.returnValue(false);
    expect(component.canSeeJobDetails()).toBeFalse();
  });

});
