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
import {MonitoringEvaluationConsentComponent} from "./monitoring-evaluation-consent.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {CandidateService} from "../../../../services/candidate.service";
import {YesNo} from "../../../../model/candidate";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";

describe('MonitoringEvaluationConsentComponent', () => {
  let component: MonitoringEvaluationConsentComponent;
  let fixture: ComponentFixture<MonitoringEvaluationConsentComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [ MonitoringEvaluationConsentComponent,AutosaveStatusComponent ],
      imports: [HttpClientTestingModule, NgSelectModule, FormsModule, ReactiveFormsModule ],
      providers: [
        { provide: CandidateService }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(MonitoringEvaluationConsentComponent);
    component = fixture.componentInstance;
    component.candidateIntakeData = { monitoringEvaluationConsent:  YesNo.Yes };
    component.editable = true;
    fixture.detectChanges();
  });

  it('should initialize the form control with the correct default value', () => {
    expect(component.form.get('monitoringEvaluationConsent').value).toBe(YesNo.Yes);
  });
});
