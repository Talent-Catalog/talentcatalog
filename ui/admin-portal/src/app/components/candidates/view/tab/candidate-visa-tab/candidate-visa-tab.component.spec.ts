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
import {CandidateVisaTabComponent} from "./candidate-visa-tab.component";
import {CandidateService} from "../../../../../services/candidate.service";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {AuthorizationService} from "../../../../../services/authorization.service";
import {CandidateVisaCheckService} from "../../../../../services/candidate-visa-check.service";
import {CountryService} from "../../../../../services/country.service";
import {MockCandidate} from "../../../../../MockData/MockCandidate";
import {HasNameSelectorComponent} from "../../../../util/has-name-selector/has-name-selector.component";
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {of} from "rxjs";
import {MockJob} from "../../../../../MockData/MockJob";
import {CandidateVisa} from "../../../../../model/candidate";
import {Country} from "../../../../../model/country";
import {ExportPdfComponent} from "../../../../util/export-pdf/export-pdf.component";

describe('CandidateVisaTabComponent', () => {
  let component: CandidateVisaTabComponent;
  let fixture: ComponentFixture<CandidateVisaTabComponent>;
  let candidateServiceMock: jasmine.SpyObj<CandidateService>;
  let countryServiceMock: jasmine.SpyObj<CountryService>;
  let candidateVisaCheckServiceMock: jasmine.SpyObj<CandidateVisaCheckService>;
  let modalServiceMock: jasmine.SpyObj<NgbModal>;
  let authServiceMock: jasmine.SpyObj<AuthorizationService>;
  const mockCandidate = new MockCandidate();
  beforeEach(async () => {
    candidateServiceMock = jasmine.createSpyObj('CandidateService', ['getIntakeData']);
    countryServiceMock = jasmine.createSpyObj('CountryService', ['listTCDestinations']);
    candidateVisaCheckServiceMock = jasmine.createSpyObj('CandidateVisaCheckService', ['create', 'list']);
    modalServiceMock = jasmine.createSpyObj('NgbModal', ['open']);
    authServiceMock = jasmine.createSpyObj('AuthorizationService', ['isSystemAdminOnly', 'isEditableCandidate']);

    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule],
      declarations: [CandidateVisaTabComponent,ExportPdfComponent, HasNameSelectorComponent],
      providers: [
        UntypedFormBuilder,
        { provide: CandidateService, useValue: candidateServiceMock },
        { provide: CountryService, useValue: countryServiceMock },
        { provide: CandidateVisaCheckService, useValue: candidateVisaCheckServiceMock },
        { provide: NgbModal, useValue: modalServiceMock },
        // { provide: LocalStorageService, useValue: {} },
        { provide: AuthorizationService, useValue: authServiceMock },
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateVisaTabComponent);
    component = fixture.componentInstance;
    component.candidate = mockCandidate;
    candidateServiceMock.getIntakeData.and.returnValue(of({}));
    countryServiceMock.listTCDestinations.and.returnValue(of([MockJob.country]));
    candidateVisaCheckServiceMock.list.and.returnValue(of([]));
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should create a visa check when a country is selected', fakeAsync(() => {
    const country: Country = MockJob.country;
    const newVisaCheck: CandidateVisa = { id: 1, country: country } as CandidateVisa;

    // Mock the modal result
    const modalRef = {
      result: Promise.resolve(country),
      componentInstance: {}
    };
    modalServiceMock.open.and.returnValue(modalRef as any);

    // Mock the create service call
    candidateVisaCheckServiceMock.create.and.returnValue(of(newVisaCheck));
    candidateVisaCheckServiceMock.list.and.returnValue(of([newVisaCheck]));

    // Simulate button click to add a country
    component.addRecord();
    tick();

    fixture.detectChanges();

    // Verify the service call and the update to visaChecks
    expect(candidateVisaCheckServiceMock.create).toHaveBeenCalledWith(1, { countryId: country.id });
    expect(component.visaChecks.length).toBe(1);
    expect(component.visaChecks[0]).toEqual(newVisaCheck);
  }));
});
