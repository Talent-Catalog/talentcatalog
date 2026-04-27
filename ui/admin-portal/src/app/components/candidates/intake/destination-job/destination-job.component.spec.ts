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

import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {CandidateService} from "../../../../services/candidate.service";
import {DestinationJobComponent} from "./destination-job.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {YesNo} from "../../../../model/candidate";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";

describe('DestinationJobComponent', () => {
  let component: DestinationJobComponent;
  let fixture: ComponentFixture<DestinationJobComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DestinationJobComponent,AutosaveStatusComponent],
      imports: [HttpClientTestingModule,NgSelectModule,FormsModule,ReactiveFormsModule],
      providers: [UntypedFormBuilder, CandidateService]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DestinationJobComponent);
    component = fixture.componentInstance;
    component.candidateIntakeData = {
      destJob:  YesNo.Yes, // Set any required fields
      destJobNotes: 'Test notes' // Set any required fields
    };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with provided candidate intake data', () => {
    expect(component.form.value.destJob).toBe(YesNo.Yes);
    expect(component.form.value.destJobNotes).toBe('Test notes');
  });
});
