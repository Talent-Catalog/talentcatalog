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
import {UntypedFormBuilder, ReactiveFormsModule} from '@angular/forms';
import {TravelDocumentComponent} from './travel-document.component';
import {CandidateVisaCheckService} from '../../../../services/candidate-visa-check.service';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";

describe('TravelDocumentComponent', () => {
  let component: TravelDocumentComponent;
  let fixture: ComponentFixture<TravelDocumentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule,ReactiveFormsModule, NgSelectModule],
      declarations: [TravelDocumentComponent,AutosaveStatusComponent],
      providers: [
        UntypedFormBuilder,
        {
          provide: CandidateVisaCheckService,
          useValue: jasmine.createSpyObj('CandidateVisaCheckService', ['someMethod'])
        }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TravelDocumentComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('hasNotes should return true when visaValidTravelDocs is "Expired"', () => {
    component.form.controls['visaValidTravelDocs'].setValue('Expired');
    expect(component.hasNotes).toBeTrue();
  });
});

