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

import {SearchHelpLinksComponent} from "./search-help-links.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {HelpLinkService} from "../../../services/help-link.service";
import {CountryService} from "../../../services/country.service";
import {NgbModal, NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {AuthorizationService} from "../../../services/authorization.service";
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {of, throwError} from "rxjs";
import {SearchResults} from "../../../model/search-results";
import MOCK_HELP_LINK from "../../../MockData/MockHelpLink";
import {HelpLink} from "../../../model/help-link";
import {HelpComponent} from "../../help/help.component";
import {
  CreateUpdateHelpLinkComponent
} from "./create-update-help-link/create-update-help-link.component";
import {By} from "@angular/platform-browser";

describe('SearchHelpLinksComponent', () => {
  let component: SearchHelpLinksComponent;
  let fixture: ComponentFixture<SearchHelpLinksComponent>;
  let helpLinkServiceSpy: jasmine.SpyObj<HelpLinkService>;
  let countryServiceSpy: jasmine.SpyObj<CountryService>;
  let modalServiceSpy: jasmine.SpyObj<NgbModal>;
  let authServiceSpy: jasmine.SpyObj<AuthorizationService>;
  const searchResults: SearchResults<HelpLink> = {
    first: false,
    last: false,
    number: 0,
    size: 0,
    totalPages: 0,
    totalElements: 1,
    content: [MOCK_HELP_LINK]
  };

  beforeEach(async () => {
    const helpLinkServiceMock = jasmine.createSpyObj('HelpLinkService', ['searchPaged']);
    const countryServiceMock = jasmine.createSpyObj('CountryService', ['listTCDestinations']);
    const modalServiceMock = jasmine.createSpyObj('NgbModal', ['open']);
    const authServiceMock = jasmine.createSpyObj('AuthorizationService', ['isReadOnly']);

    await TestBed.configureTestingModule({
      declarations: [SearchHelpLinksComponent, HelpComponent],
      imports: [FormsModule, ReactiveFormsModule, NgbModule, NgSelectModule],
      providers: [
        UntypedFormBuilder,
        {provide: HelpLinkService, useValue: helpLinkServiceMock},
        {provide: CountryService, useValue: countryServiceMock},
        {provide: NgbModal, useValue: modalServiceMock},
        {provide: AuthorizationService, useValue: authServiceMock}
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(SearchHelpLinksComponent);
    component = fixture.componentInstance;
    helpLinkServiceSpy = TestBed.inject(HelpLinkService) as jasmine.SpyObj<HelpLinkService>;
    countryServiceSpy = TestBed.inject(CountryService) as jasmine.SpyObj<CountryService>;
    modalServiceSpy = TestBed.inject(NgbModal) as jasmine.SpyObj<NgbModal>;
    authServiceSpy = TestBed.inject(AuthorizationService) as jasmine.SpyObj<AuthorizationService>;

    // Mock initial data and methods
    helpLinkServiceSpy.searchPaged.and.returnValue(of(searchResults));
    countryServiceSpy.listTCDestinations.and.returnValue(of([]));
    authServiceSpy.isReadOnly.and.returnValue(false);

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });


  it('should handle search results', () => {

    // helpLinkServiceSpy.searchPaged.and.returnValue(of(searchResults));

    component.search();

    expect(component.results).toEqual(searchResults);
    expect(component.loading).toBeFalse();
  });

  it('should handle search errors', () => {
    const error = 'An error occurred';
    helpLinkServiceSpy.searchPaged.and.returnValue(throwError(error));

    component.search();

    expect(component.error).toEqual(error);
    expect(component.loading).toBeFalse();
  });

  it('should open add help link modal', () => {
    const modalRefMock = {
      componentInstance: { destinationCountries: undefined },
      result: Promise.resolve(true)
    } as any;
    modalServiceSpy.open.and.returnValue(modalRefMock);

    component.addHelpLink();

    expect(modalServiceSpy.open).toHaveBeenCalledWith(CreateUpdateHelpLinkComponent, {
      centered: true,
      backdrop: 'static'
    });
  });

  it('should open edit help link modal', () => {
    const modalRefMock = {
      componentInstance: {
        helpLink: null,
        destinationCountries: []
      },
      result: Promise.resolve(true)
    } as any;
    modalServiceSpy.open.and.returnValue(modalRefMock);

    const helpLink = { id: 1, label: 'Test Link' } as HelpLink;
    component.editHelpLink(helpLink);

    expect(modalServiceSpy.open).toHaveBeenCalledWith(CreateUpdateHelpLinkComponent, {
      centered: true,
      backdrop: 'static'
    });
    expect(modalRefMock.componentInstance.helpLink).toEqual(helpLink);
  });

  it('should display error message', () => {
    const error = 'An error occurred';
    component.error = error;
    fixture.detectChanges();

    const errorMessage = fixture.debugElement.query(By.css('tc-alert'));
    expect(errorMessage).toBeTruthy();
    expect(errorMessage.nativeElement.textContent).toContain(error);
  });

});
