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
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {DestinationLimitComponent} from './destination-limit.component';
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from '@angular/forms';
import {CandidateService} from '../../../../services/candidate.service';
import {YesNo} from "../../../../model/candidate";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";

describe('DestinationLimitComponent', () => {
  let component: DestinationLimitComponent;
  let fixture: ComponentFixture<DestinationLimitComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [DestinationLimitComponent,AutosaveStatusComponent],
      imports: [HttpClientTestingModule,NgSelectModule,FormsModule,ReactiveFormsModule],
      providers: [UntypedFormBuilder, CandidateService]
    })
    .compileComponents();
  });
  beforeEach(() => {
    fixture = TestBed.createComponent(DestinationLimitComponent);
    component = fixture.componentInstance;
    component.candidateIntakeData = {
      destLimit: YesNo.Yes, // Set any required fields
      destLimitNotes: 'Test notes' // Set any required fields
    };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with provided candidate intake data', () => {
    expect(component.form.value.destLimit).toBe(YesNo.Yes);
    expect(component.form.value.destLimitNotes).toBe('Test notes');
  });
});
