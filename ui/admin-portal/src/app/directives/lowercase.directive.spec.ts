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

import {Component, DebugElement} from "@angular/core";
import {ComponentFixture, TestBed, waitForAsync} from "@angular/core/testing";
import {FormsModule, NgControl} from "@angular/forms";
import {LowercaseDirective} from "./lowercase.directive";
import {By} from "@angular/platform-browser";

@Component({
  template: `
    <input type="text" appLowercase [(ngModel)]="inputValue" />
  `
})
class TestComponent {
  inputValue = '';
}

describe('LowercaseDirective', () => {
  let component: TestComponent;
  let fixture: ComponentFixture<TestComponent>;
  let inputEl: DebugElement;
  let ngControl: NgControl;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [FormsModule],
      declarations: [LowercaseDirective, TestComponent],
      providers: [
        {
          provide: NgControl,
        }
      ]
    })
    .compileComponents();
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(TestComponent);
    component = fixture.componentInstance;
    inputEl = fixture.debugElement.query(By.css('input'));
    ngControl = inputEl.injector.get(NgControl);
    fixture.detectChanges();
  });

  it('should create an instance', () => {
    const directive = new LowercaseDirective(null, null);
    expect(directive).toBeTruthy();
  });

  it('should convert input value to lowercase on input event', () => {
    const inputValue = 'HELLO';
    spyOn(ngControl.control,'setValue');

    inputEl.nativeElement.value = inputValue;
    inputEl.triggerEventHandler('input', { target: inputEl.nativeElement });

    fixture.detectChanges();

    expect(ngControl.control.setValue).toHaveBeenCalledWith(inputValue.toLowerCase());
  });

  it('should handle empty input correctly', () => {
    const inputValue = '';
    spyOn(ngControl.control,'setValue');

    inputEl.nativeElement.value = inputValue;
    inputEl.triggerEventHandler('input', { target: inputEl.nativeElement });

    fixture.detectChanges();

    expect(ngControl.control.setValue).toHaveBeenCalledWith('');
  });

  it('should update model value with lowercase input', () => {
    const inputValue = 'HeLLo';
    inputEl.nativeElement.value = inputValue;
    inputEl.triggerEventHandler('input', { target: inputEl.nativeElement });

    fixture.detectChanges();

    expect(component.inputValue).toBe(inputValue.toLowerCase());
  });
});
