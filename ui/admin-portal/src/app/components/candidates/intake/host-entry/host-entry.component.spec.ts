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
import {HostEntryComponent} from "./host-entry.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
import {DatePickerComponent} from "../../../util/date-picker/date-picker.component";
import {NgbDatepickerModule} from "@ng-bootstrap/ng-bootstrap";
import {HostChallengesComponent} from "../host-challenges/host-challenges.component";
import {MockCandidate} from "../../../../MockData/MockCandidate";

describe('HostEntryComponent', () => {
  let component: HostEntryComponent;
  let fixture: ComponentFixture<HostEntryComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [HostEntryComponent,AutosaveStatusComponent,DatePickerComponent],
      imports: [HttpClientTestingModule,NgbDatepickerModule,NgSelectModule,FormsModule,ReactiveFormsModule],
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(HostEntryComponent);
    component = fixture.componentInstance;
    component.entity = new MockCandidate();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display birth country input field', () => {
    const compiled = fixture.nativeElement;
    const birthCountryInput = compiled.querySelector('#birthCountryId');
    expect(birthCountryInput).toBeTruthy();
  });

});
