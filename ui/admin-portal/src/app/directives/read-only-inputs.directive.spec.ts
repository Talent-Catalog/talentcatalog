/*
 * Copyright (c) 2025 Talent Catalog.
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

import {ReadOnlyInputsDirective} from './read-only-inputs.directive';
import {Component, DebugElement, Renderer2} from "@angular/core";
import {ComponentFixture, fakeAsync, TestBed, tick, waitForAsync} from "@angular/core/testing";
import {AbstractControl, FormControl, FormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {DatePickerComponent} from "../components/util/date-picker/date-picker.component";
import {NgbDatepickerModule} from "@ng-bootstrap/ng-bootstrap";
import {NgxWigModule} from "ngx-wig";

@Component({
  template: `
    <div [appReadOnlyInputs]="isReadOnly">
      <input type="text" />
      <textarea></textarea>
      <ng-select [items]="['a', 'b', 'c']"></ng-select>
      <app-date-picker [control]="control"></app-date-picker>
      <ngx-wig [control]="control"></ngx-wig>
    </div>

  `
})
class TestComponent {
  isReadOnly: boolean;
  control: AbstractControl = new FormControl(null);
}

describe('ReadOnlyInputsDirective', () => {
  let component: TestComponent;
  let fixture: ComponentFixture<TestComponent>;
  let inputEl: DebugElement;
  let ngSelectEl: DebugElement;
  let textareaEl: DebugElement;
  let datePickerEl: DebugElement;
  let ngxWigEl: DebugElement;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [FormsModule, NgSelectModule, NgbDatepickerModule, NgxWigModule],
      declarations: [TestComponent, ReadOnlyInputsDirective, DatePickerComponent],
      providers: [
        {
          provide: Renderer2,
        }
      ]
    })
    fixture = TestBed.createComponent(TestComponent);
    component = fixture.componentInstance;
    component.isReadOnly = false;
    inputEl = fixture.debugElement.query(e => e.name === 'input');
    textareaEl = fixture.debugElement.query(e => e.name === 'textarea');
    ngSelectEl = fixture.debugElement.query(e => e.name === 'ng-select');
    datePickerEl = fixture.debugElement.query(e => e.name === 'app-date-picker');
    ngxWigEl = fixture.debugElement.query(e => e.name === 'ngx-wig');
  }));

  it('should create an instance', () => {
    const directive = new ReadOnlyInputsDirective(null, null);
    expect(directive).toBeTruthy();
  });

  it('should disable a text input if read only', fakeAsync(() => {
    component.isReadOnly = true;
    fixture.detectChanges();
    tick(); // Simulate timeout

    expect(inputEl.nativeElement.hasAttribute('disabled')).toBeTrue();
  }));

  it('should enable a text input if not read only', fakeAsync(() => {
    component.isReadOnly = false;
    fixture.detectChanges();
    tick(); // Simulate timeout

    expect(inputEl.nativeElement.hasAttribute('disabled')).toBeFalse();
  }));

  it('should disable a textarea input if read only', fakeAsync(() => {
    component.isReadOnly = true;
    fixture.detectChanges();
    tick(); // Simulate timeout

    expect(textareaEl.nativeElement.hasAttribute('disabled')).toBeTrue();
  }));

  it('should enable a textarea input if not read only', fakeAsync(() => {
    component.isReadOnly = false;
    fixture.detectChanges();
    tick(); // Simulate timeout

    expect(textareaEl.nativeElement.hasAttribute('disabled')).toBeFalse();
  }));

  it('should disable a ng-select input if read only', fakeAsync(() => {
    component.isReadOnly = true;
    fixture.detectChanges();
    tick(); // Simulate timeout

    expect(ngSelectEl.nativeElement.hasAttribute('disabled')).toBeTrue();
    expect(ngSelectEl.nativeElement.classList.contains('read-only')).toBeTrue();
  }));

  it('should enable a ng-select input if not read only', fakeAsync(() => {
    component.isReadOnly = false;
    fixture.detectChanges();
    tick(); // Simulate timeout

    expect(ngSelectEl.nativeElement.hasAttribute('disabled')).toBeFalse();
    expect(ngSelectEl.nativeElement.classList.contains('read-only')).toBeFalse();
  }));

  it('should disable app-date-picker input if read only', fakeAsync(() => {
    component.isReadOnly = true;
    fixture.detectChanges();
    tick(); // Simulate timeout

    const dateInput = datePickerEl.query(e => e.name === 'input');

    expect(dateInput.nativeElement.hasAttribute('disabled')).toBeTrue();
  }));

  it('should enable app-date-picker input if not read only', fakeAsync(() => {
    component.isReadOnly = false;
    fixture.detectChanges();
    tick(); // Simulate timeout

    const dateInput = datePickerEl.query(e => e.name === 'input');

    expect(dateInput.nativeElement.hasAttribute('disabled')).toBeFalse();
  }));

  it('should disable ngx-wig input if read only', fakeAsync(() => {
    component.isReadOnly = true;
    fixture.detectChanges();
    tick(); // Simulate timeout

    expect(ngxWigEl.nativeElement.hasAttribute('disabled')).toBeTrue();
    expect(ngxWigEl.nativeElement.classList.contains('read-only')).toBeTrue();
  }));

  it('should enable ngx-wig input if not read only', fakeAsync(() => {
    component.isReadOnly = false;
    fixture.detectChanges();
    tick(); // Simulate timeout

    expect(ngxWigEl.nativeElement.hasAttribute('disabled')).toBeFalse();
    expect(ngxWigEl.nativeElement.classList.contains('read-only')).toBeFalse();
  }));


});
