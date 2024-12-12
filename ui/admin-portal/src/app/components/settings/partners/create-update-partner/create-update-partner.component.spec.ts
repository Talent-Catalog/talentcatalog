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

import {CreateUpdatePartnerComponent} from "./create-update-partner.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {PartnerService} from "../../../../services/partner.service";
import {CountryService} from "../../../../services/country.service";
import {UserService} from "../../../../services/user.service";
import {NgbActiveModal, NgbModule} from "@ng-bootstrap/ng-bootstrap";
import {UntypedFormBuilder, FormsModule, ReactiveFormsModule, Validators} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {MockPartner} from "../../../../MockData/MockPartner";
import {Partner, UpdatePartnerRequest} from "../../../../model/partner";
import {Country} from "../../../../model/country";
import {MockJob} from "../../../../MockData/MockJob";
import {of, throwError} from "rxjs";
import {MockUser} from "../../../../MockData/MockUser";
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe('CreateUpdatePartnerComponent', () => {
  let component: CreateUpdatePartnerComponent;
  let fixture: ComponentFixture<CreateUpdatePartnerComponent>;
  let partnerServiceSpy: jasmine.SpyObj<PartnerService>;
  let countryServiceSpy: jasmine.SpyObj<CountryService>;
  let userServiceSpy: jasmine.SpyObj<UserService>;
  let activeModalSpy: jasmine.SpyObj<NgbActiveModal>;
  let fb: UntypedFormBuilder;
  const mockCountries: Country[] = [MockJob.country];
  const mockUsers = [new MockUser()];

  beforeEach(async () => {
    const partnerSpy = jasmine.createSpyObj('PartnerService', ['create', 'update']);
    const countrySpy = jasmine.createSpyObj('CountryService', ['listCountriesRestricted']);
    const userSpy = jasmine.createSpyObj('UserService', ['search']);
    const modalSpy = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);

    await TestBed.configureTestingModule({
      declarations: [CreateUpdatePartnerComponent],
      imports: [HttpClientTestingModule, FormsModule, ReactiveFormsModule, NgbModule, NgSelectModule],
      providers: [
        { provide: PartnerService, useValue: partnerSpy },
        { provide: CountryService, useValue: countrySpy },
        { provide: UserService, useValue: userSpy },
        { provide: NgbActiveModal, useValue: modalSpy }
      ]
    }).compileComponents();

    partnerServiceSpy = TestBed.inject(PartnerService) as jasmine.SpyObj<PartnerService>;
    countryServiceSpy = TestBed.inject(CountryService) as jasmine.SpyObj<CountryService>;
    userServiceSpy = TestBed.inject(UserService) as jasmine.SpyObj<UserService>;
    activeModalSpy = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;
    fb = TestBed.inject(UntypedFormBuilder);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateUpdatePartnerComponent);
    component = fixture.componentInstance;
    component.partner = new MockPartner();
    countryServiceSpy.listCountriesRestricted.and.returnValue(of(mockCountries));
    userServiceSpy.search.and.returnValue(of(mockUsers));
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form for create', () => {
    component.ngOnInit();
    expect(component.form).toBeDefined();
    expect(component.form.controls['name'].value).toBe('Mock Partner');
    expect(component.form.controls['status'].value).toBe('Active');
    // Add more expectations as needed
  });

  it('should initialize form for update', () => {
    const partner: Partner = new MockPartner();
    component.partner = partner;
    component.ngOnInit();
    expect(component.form).toBeDefined();
    expect(component.form.controls['name'].value).toBe(partner.name);
    expect(component.form.controls['status'].value).toBe(partner.status);
  });

  it('should load countries on init', () => {

    component.ngOnInit();

    expect(countryServiceSpy.listCountriesRestricted).toHaveBeenCalled();
    expect(component.countries).toEqual(mockCountries);
    expect(component.working).toBeFalse();
  });

  it('should handle country service error', (done) => {
    countryServiceSpy.listCountriesRestricted.and.returnValue(throwError('Error'));
    userServiceSpy.search.and.returnValue(throwError('Error'));

    component.ngOnInit();
    setTimeout(() => {
      expect(component.error).toBe('Error');
      done();
    }, 0);
  });

  it('should load partner users for update', () => {
    const partner: Partner = new MockPartner();
    component.partner = partner;

    component.ngOnInit();

    expect(userServiceSpy.search).toHaveBeenCalled();
    expect(component.partnerUsers).toEqual(mockUsers);
    expect(component.working).toBeFalse();
  });

  it('should handle user service error', () => {
    userServiceSpy.search.and.returnValue(throwError('Error'));
    countryServiceSpy.listCountriesRestricted.and.returnValue(throwError('Error'));

    component.ngOnInit();

    expect(component.error).toBe('Error');
    expect(component.working).toBeFalse();
  });

  it('should save new partner', fakeAsync(() => {
    const mockPartner: Partner = new MockPartner();
    component.partner = undefined;
    const request: UpdatePartnerRequest = {
      autoAssignable: false,
      defaultPartnerRef: false,
      jobCreator: false,
      logo: "",
      notificationEmail: "",
      registrationLandingPage: "",
      sflink: "",
      sourceCountryIds: [],
      sourcePartner: false,
      websiteUrl: "",
      name: 'Test Partner', abbreviation: 'TP', status: 'active' };
    partnerServiceSpy.create.and.returnValue(of(mockPartner));
    partnerServiceSpy.update.and.returnValue(of(mockPartner));

    component.form = fb.group({
      name: ['Test Partner', Validators.required],
      abbreviation: ['TP', Validators.required],
      status: ['active', Validators.required]
    });

    component.save();

    tick(); // Simulate async operation

    expect(partnerServiceSpy.create).toHaveBeenCalled();
    expect(activeModalSpy.close).toHaveBeenCalledWith(mockPartner);
    expect(component.working).toBeFalse();
  }));

  it('should update existing partner', fakeAsync(() => {
    const partner: Partner = new MockPartner();
    // @ts-ignore
    const request: UpdatePartnerRequest = { id: 1, name: 'Test Partner', abbreviation: 'TP', status: 'active' };
    partnerServiceSpy.update.and.returnValue(of(partner));
    component.partner = partner;

    component.form = fb.group({
      name: ['Test Partner', Validators.required],
      abbreviation: ['TP', Validators.required],
      status: ['active', Validators.required]
    });

    component.save();

    tick(); // Simulate async operation

    expect(partnerServiceSpy.update).toHaveBeenCalled();
    expect(activeModalSpy.close).toHaveBeenCalledWith(partner);
    expect(component.working).toBeFalse();
  }));

  it('should handle save error', fakeAsync(() => {
    partnerServiceSpy.create.and.returnValue(throwError('Error'));
    partnerServiceSpy.update.and.returnValue(throwError('Error'));
    component.form = fb.group({
      name: ['Test Partner', Validators.required],
      abbreviation: ['TP', Validators.required],
      status: ['active', Validators.required]
    });

    component.save();

    tick(); // Simulate async operation

    expect(component.error).toBe('Error');
    expect(component.working).toBeFalse();
  }));

  it('should close modal on dismiss', () => {
    component.dismiss();
    expect(activeModalSpy.dismiss).toHaveBeenCalledWith(false);
  });

});
