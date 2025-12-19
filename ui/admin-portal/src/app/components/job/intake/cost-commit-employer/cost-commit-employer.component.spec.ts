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

import {CostCommitEmployerComponent} from "./cost-commit-employer.component";
import {ComponentFixture, TestBed, waitForAsync} from "@angular/core/testing";
import {ReactiveFormsModule} from "@angular/forms";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
import {NgxWigModule} from "ngx-wig";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {TranslateModule} from "@ngx-translate/core";

describe('CostCommitEmployerComponent', () => {
  let component: CostCommitEmployerComponent;
  let fixture: ComponentFixture<CostCommitEmployerComponent>;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ CostCommitEmployerComponent,AutosaveStatusComponent ],
      imports: [ ReactiveFormsModule,NgxWigModule,HttpClientTestingModule,TranslateModule.forRoot() ],
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(CostCommitEmployerComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with jobIntakeData if provided', () => {
    const testData = { employerCostCommitment: 'Test commitment' };
    component.jobIntakeData = testData;
    component.editable = true;
    component.ngOnInit();
    expect(component.form.value.employerCostCommitment).toEqual(testData.employerCostCommitment);
  });

  it('should disable form control when editable is false', () => {
    component.editable = false;
    component.ngOnInit();
    expect(component.form.controls.employerCostCommitment.disabled).toBeTrue();
  });

  it('should update jobIntakeData and emit event on successful save', () => {
    const testData = 'Test commitment';
    component.jobIntakeData = { employerCostCommitment: 'Initial commitment' };
    component.form.controls.employerCostCommitment.setValue(testData);
    spyOn(component.intakeChanged, 'emit');
    component.onSuccessfulSave();
    expect(component.jobIntakeData.employerCostCommitment).toEqual(testData);
    expect(component.intakeChanged.emit).toHaveBeenCalledWith(component.jobIntakeData);
  });
});
