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
import {ConfirmContactComponent} from "./confirm-contact.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {MockCandidate} from "../../../../MockData/MockCandidate";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {FixedInputComponent} from "../../../util/intake/fixed-input/fixed-input.component";

describe('ConfirmContactComponent', () => {
  let component: ConfirmContactComponent;
  let fixture: ComponentFixture<ConfirmContactComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ConfirmContactComponent, FixedInputComponent],
      imports: [HttpClientTestingModule, NgSelectModule,FormsModule,ReactiveFormsModule],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ConfirmContactComponent);
    component = fixture.componentInstance;
    // Mock candidate data
    component.candidate = new MockCandidate();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display correct candidate information', () => {
    const fullNameElement = component.candidate.user.firstName + ' ' + component.candidate.user.lastName;
    expect(component.candidate.user.firstName).toEqual('John');
    expect(component.candidate.user.lastName).toEqual('Doe');
    expect(fullNameElement).toContain('John Doe');
    const talentCatalogElement = component.candidate.candidateNumber;
    expect(talentCatalogElement).toEqual('123456');

    const dobElement = component.date;
    expect(dobElement).toContain('01 Jan 90 (Age 35)');

    const emailElement = component.candidate.user.email;
    expect(emailElement).toEqual('john.doe@example.com');
  });
});
