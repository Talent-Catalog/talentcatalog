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
import {DrivingLicenseComponent} from "./driving-license.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
import {CandidateService} from "../../../../services/candidate.service";
import {CandidateIntakeData, DrivingLicenseStatus, YesNo} from "../../../../model/candidate";

describe('DrivingLicenseComponent', () => {
  let component: DrivingLicenseComponent;
  let fixture: ComponentFixture<DrivingLicenseComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ DrivingLicenseComponent,AutosaveStatusComponent ],
      imports: [HttpClientTestingModule,NgSelectModule,FormsModule,ReactiveFormsModule],
      providers: [UntypedFormBuilder, CandidateService]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DrivingLicenseComponent);
    component = fixture.componentInstance;
    component.countries = []; // Provide empty array of countries

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form group and controls', () => {
    expect(component.form).toBeDefined();
    expect(component.form.controls['canDrive']).toBeDefined();
    expect(component.form.controls['drivingLicense']).toBeDefined();
    expect(component.form.controls['drivingLicenseExp']).toBeDefined();
    expect(component.form.controls['drivingLicenseCountryId']).toBeDefined();
  });

  it('should initialize form controls with default values', () => {
    expect(component.canDrive).toBeNull(); // By default, it should be undefined
    expect(component.drivingLicense).toBeNull(); // By default, it should be undefined
    expect(component.form.controls['drivingLicenseExp'].value).toBeNull(); // By default, it should be null
    expect(component.form.controls['drivingLicenseCountryId'].value).toBeNull(); // By default, it should be null
  });

  it('should initialize form controls with provided input data', () => {
    const testData = {
      canDrive: YesNo.Yes,
      drivingLicense: DrivingLicenseStatus.Valid,
      drivingLicenseExp:'No',
    } as CandidateIntakeData;

    component.candidateIntakeData = testData;
    component.ngOnInit(); // Trigger initialization

    expect(component.canDrive).toEqual(testData.canDrive);
    expect(component.drivingLicense).toEqual(testData.drivingLicense);
    expect(component.form.controls['drivingLicenseExp'].value).toEqual('No');
  });
});
