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

import {SearchCountriesComponent} from "./search-countries.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {NgbModal, NgbModalRef, NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {CountryService} from "../../../services/country.service";
import {AuthorizationService} from "../../../services/authorization.service";
import {UntypedFormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {CreateCountryComponent} from "./create/create-country.component";
import {of} from "rxjs";
import {Country} from "../../../model/country";
import {MockJob} from "../../../MockData/MockJob";
import {SearchResults} from "../../../model/search-results";
import {EditCountryComponent} from "./edit/edit-country.component";
import {ConfirmationComponent} from "../../util/confirm/confirmation.component";
import {NgSelectModule} from "@ng-select/ng-select";
import {MockUser} from "../../../MockData/MockUser";

describe('SearchCountriesComponent', () => {
  let component: SearchCountriesComponent;
  let fixture: ComponentFixture<SearchCountriesComponent>;
  let modalService: NgbModal;
  let countryService: jasmine.SpyObj<CountryService>;
  let authService: jasmine.SpyObj<AuthorizationService>;
  let activeModalSpy: jasmine.SpyObj<NgbModalRef>;

  beforeEach(async () => {
    const countryServiceSpy = jasmine.createSpyObj('CountryService', ['searchPaged', 'delete']);
    const authServiceSpy = jasmine.createSpyObj('AuthorizationService', ['isAnAdmin']);

    await TestBed.configureTestingModule({
      imports: [FormsModule, ReactiveFormsModule, NgbModule,NgSelectModule],
      declarations: [SearchCountriesComponent],
      providers: [
        UntypedFormBuilder,
        { provide: CountryService, useValue: countryServiceSpy },
        { provide: AuthorizationService, useValue: authServiceSpy },
        NgbModal,
      ],
    }).compileComponents();

    countryService = TestBed.inject(CountryService) as jasmine.SpyObj<CountryService>;
    authService = TestBed.inject(AuthorizationService) as jasmine.SpyObj<AuthorizationService>;
    modalService = TestBed.inject(NgbModal);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(SearchCountriesComponent);
    component = fixture.componentInstance;

    // Creating a spy object for NgbModalRef (activeModal)
    activeModalSpy = jasmine.createSpyObj('NgbModalRef', ['close', 'dismiss']);
    countryService.searchPaged.and.returnValue(of({ totalElements: 1, content: [{ id: 1, name: 'Added Country', status: 'active' }] } as SearchResults<Country>));
    component.loggedInUser = new MockUser();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should open add country modal and refresh list on success', fakeAsync(() => {
    // Mocking modalService.open to return the NgbModalRef spy object
    spyOn(modalService, 'open').and.returnValue(activeModalSpy);

    // Mocking the result of modal close to simulate success
    activeModalSpy.result = Promise.resolve('added country');

    // Mocking the countryService.searchPaged response
    countryService.searchPaged.and.returnValue(of({ totalElements: 1, content: [{ id: 1, name: 'Added Country', status: 'active' }] } as SearchResults<Country>));

    // Calling the addCountry method
    component.addCountry();
    fixture.detectChanges();
    tick(); // Resolve promise

    // Expect modalService.open to have been called with CreateCountryComponent
    expect(modalService.open).toHaveBeenCalledWith(CreateCountryComponent, jasmine.any(Object));

    // Expect searchPaged method to have been called once after modal closes
    expect(countryService.searchPaged).toHaveBeenCalledTimes(2);

  }));

  it('should open edit country modal and refresh list on success', fakeAsync(() => {
    const mockCountry: Country = MockJob.country;

    // Creating a mock modal instance
    const mockModalRef: Partial<NgbModalRef> = {
      componentInstance: { countryId: 1 },
      result: Promise.resolve(mockCountry)
    };

    // Mocking modalService.open to return the mock modal instance
    spyOn(modalService, 'open').and.returnValue(mockModalRef as NgbModalRef);

    // Mocking the countryService.searchPaged response
    countryService.searchPaged.and.returnValue(of({ totalElements: 1, content: [mockCountry] } as SearchResults<Country>));

    // Calling the editCountry method
    component.editCountry(mockCountry);
    fixture.detectChanges();
    tick(); // Resolve promise

    // Expect modalService.open to have been called with EditCountryComponent
    expect(modalService.open).toHaveBeenCalledWith(EditCountryComponent, jasmine.any(Object));

    // Ensure the countryId is set correctly on the componentInstance
    expect(mockModalRef.componentInstance.countryId).toBe(mockCountry.id);

    // Expect searchPaged method to have been called once after modal closes
    expect(countryService.searchPaged).toHaveBeenCalledTimes(2);

  }));

  it('should open confirmation modal and delete country on confirm', fakeAsync(() => {
    const mockCountry: Country = MockJob.country;

    // Creating a mock modal instance
    const mockModalRef: Partial<NgbModalRef> = {
      componentInstance: { message: "" },
      result: Promise.resolve(true)
    };

    // Mocking modalService.open to return the mock modal instance
    spyOn(modalService, 'open').and.returnValue(mockModalRef as NgbModalRef);

    // Mocking the result of modal close to simulate confirmation
    activeModalSpy.result = Promise.resolve(true);

    // Mocking the countryService.delete response
    countryService.delete.and.returnValue(of(true));

    // Mocking the countryService.searchPaged response
    countryService.searchPaged.and.returnValue(of({ totalElements: 0, content: [] } as SearchResults<Country>));

    // Calling the deleteCountry method
    component.deleteCountry(mockCountry);
    fixture.detectChanges();
    tick(); // Resolve promise

    // Expect modalService.open to have been called with ConfirmationComponent
    expect(modalService.open).toHaveBeenCalledWith(ConfirmationComponent, jasmine.any(Object));

    // Expect delete method to have been called with the correct country id
    expect(countryService.delete).toHaveBeenCalled();

    // Expect searchPaged method to have been called once after deletion
    expect(countryService.searchPaged).toHaveBeenCalledTimes(3);
  }));
});
