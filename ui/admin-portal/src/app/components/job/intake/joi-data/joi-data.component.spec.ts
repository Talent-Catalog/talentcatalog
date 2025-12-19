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

import {ComponentFixture, TestBed, waitForAsync} from "@angular/core/testing";
import {ReactiveFormsModule} from "@angular/forms";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
import {NgxWigModule} from "ngx-wig";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {TranslateModule} from "@ngx-translate/core";
import {JoiDataComponent} from "./joi-data.component";

describe('JoiDataComponent', () => {
  let component: JoiDataComponent;
  let fixture: ComponentFixture<JoiDataComponent>;
  let testFieldName: string;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      declarations: [ JoiDataComponent,AutosaveStatusComponent ],
      imports: [ ReactiveFormsModule,NgxWigModule,HttpClientTestingModule,TranslateModule.forRoot() ],
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(JoiDataComponent);
    component = fixture.componentInstance;
    testFieldName = "occupationCode";
    component.formFieldName=testFieldName
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with jobIntakeData if provided', () => {
    const testData = {};
    testData[testFieldName] = "Test field data"
    component.jobIntakeData = testData;
    component.editable = true;
    component.ngOnInit();
    expect(component.form.value[testFieldName]).toEqual(testData[testFieldName]);
  });

  it('should disable form control when editable is false', () => {
    component.editable = false;
    component.ngOnInit();
    expect(component.form.controls[testFieldName].disabled).toBeTrue();
  });

  it('should update form control value on input change', () => {
    // Find the input element
    const occupationCodeInput: HTMLInputElement = fixture.nativeElement.querySelector('input');

    // Simulate user input
    const newData = '123456';
    occupationCodeInput.value = newData;
    occupationCodeInput.dispatchEvent(new Event('input'));

    // Detect changes
    fixture.detectChanges();

    // Check if the form control value is updated correctly
    expect(component.form.get(testFieldName).value).toEqual(newData);
  });

});
