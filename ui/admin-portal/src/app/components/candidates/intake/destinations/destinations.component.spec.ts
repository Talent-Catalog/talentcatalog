/*
 * Copyright (c) 2021 Talent Beyond Boundaries.
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
import {DestinationsComponent} from './destinations.component';
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {YesNo} from "../../../../model/candidate";
import {MockCandidate} from "../../../../MockData/MockCandidate";

fdescribe('DestinationsComponent', () => {
  let component: DestinationsComponent;
  let fixture: ComponentFixture<DestinationsComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule,NgSelectModule,FormsModule,ReactiveFormsModule],
      declarations: [ DestinationsComponent,AutosaveStatusComponent ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DestinationsComponent);
    component = fixture.componentInstance;
    component.candidateIntakeData = {
      candidateDestinations:undefined
    };
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with undefined candidate and candidate intake data inputs', () => {
    expect(component.candidate).toBeUndefined();
    expect(component.candidateIntakeData.candidateDestinations).toBeUndefined();
  });
});
