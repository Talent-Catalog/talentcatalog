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

import {ComponentFixture, TestBed} from '@angular/core/testing';

import {TcRadioComponent} from './tc-radio.component';
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {By} from "@angular/platform-browser";

describe('TcRadioComponent', () => {
  let component: TcRadioComponent;
  let fixture: ComponentFixture<TcRadioComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      declarations: [TcRadioComponent],
      imports: [FormsModule, ReactiveFormsModule]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TcRadioComponent);
    component = fixture.componentInstance;

    component.id = 'radio1';
    component.name = 'testGroup';
    component.label = 'Option 1';
    component.value = 1;

    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should display the label', () => {
    const labelEl = fixture.nativeElement.querySelector('label');
    expect(labelEl.textContent).toContain('Option 1');
  });

  it('should emit change event when clicked', () => {
    spyOn(component.change, 'emit');

    const inputEl = fixture.debugElement.query(By.css('input')).nativeElement;
    inputEl.dispatchEvent(new Event('change'));

    fixture.detectChanges();

    expect(component.change.emit).toHaveBeenCalledWith(1);
  });

  it('should update innerValue via writeValue (ControlValueAccessor)', () => {
    component.writeValue(1);
    expect(component.innerValue).toBe(1);

    component.writeValue(2);
    expect(component.innerValue).toBe(2);
  });

  it('should call onChange callback when value changes', () => {
    const onChangeSpy = jasmine.createSpy('onChange');
    component.registerOnChange(onChangeSpy);

    component.onChangeValue(1);

    expect(onChangeSpy).toHaveBeenCalledWith(1);
  });

  it('should call onTouched callback when value changes', () => {
    const onTouchedSpy = jasmine.createSpy('onTouched');
    component.registerOnTouched(onTouchedSpy);

    component.onChangeValue(1);

    expect(onTouchedSpy).toHaveBeenCalled();
  });

  it('should reflect checked state correctly', () => {
    component.writeValue(1);
    fixture.detectChanges();

    const inputEl = fixture.debugElement.query(By.css('input')).nativeElement;
    expect(inputEl.checked).toBeTrue();

    component.writeValue(2);
    fixture.detectChanges();
    expect(inputEl.checked).toBeFalse();
  });

  it('should work with boolean value', () => {
    component.value = true;
    component.writeValue(true);
    fixture.detectChanges();

    const inputEl = fixture.debugElement.query(By.css('input')).nativeElement;
    expect(inputEl.checked).toBeTrue();

    component.writeValue(false);
    fixture.detectChanges();
    expect(inputEl.checked).toBeFalse();
  });
});
