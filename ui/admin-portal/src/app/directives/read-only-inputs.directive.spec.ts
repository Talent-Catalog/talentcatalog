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
import {FormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";

@Component({
  template: `
    <div [appReadOnlyInputs]="isReadOnly">
      <input type="text" />
      <ng-select [items]="['a', 'b', 'c']"></ng-select>
      <textarea></textarea>
    </div>

  `
})
class TestComponent {
  isReadOnly: boolean;
}

describe('ReadOnlyInputsDirective', () => {
  let component: TestComponent;
  let fixture: ComponentFixture<TestComponent>;
  let inputEl: DebugElement;
  let ngSelectEl: DebugElement;
  let textareaEl: DebugElement;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [FormsModule, NgSelectModule],
      declarations: [TestComponent, ReadOnlyInputsDirective],
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
  }));

  it('should create an instance', () => {
    const directive = new ReadOnlyInputsDirective(null, null);
    expect(directive).toBeTruthy();
  });

  it('should disable a text input if read only', fakeAsync(() => {
    component.isReadOnly = true;
    fixture.detectChanges();
    tick(); // Simulate 1 second
    expect(inputEl.nativeElement.hasAttribute('disabled')).toBeTrue();
  }));

  it('should enable a text input if not read only', fakeAsync(() => {
    component.isReadOnly = false;
    fixture.detectChanges();
    tick(); // Simulate 1 second
    expect(inputEl.nativeElement.hasAttribute('disabled')).toBeFalse();
  }));

  it('should disable a textarea input if read only', fakeAsync(() => {
    component.isReadOnly = true;
    fixture.detectChanges();
    tick(); // Simulate 1 second
    expect(textareaEl.nativeElement.hasAttribute('disabled')).toBeTrue();
  }));

  it('should enable a textarea input if not read only', fakeAsync(() => {
    component.isReadOnly = false;
    fixture.detectChanges();
    tick(); // Simulate 1 second
    expect(textareaEl.nativeElement.hasAttribute('disabled')).toBeFalse();
  }));

  it('should disable a ng-select input if read only', fakeAsync(() => {
    component.isReadOnly = true;
    fixture.detectChanges();
    tick(); // Simulate 1 second
    expect(ngSelectEl.nativeElement.hasAttribute('disabled')).toBeTrue();
    expect(ngSelectEl.nativeElement.classList.contains('read-only')).toBeTrue();
  }));

  it('should enable a ng-select input if not read only', fakeAsync(() => {
    component.isReadOnly = false;
    fixture.detectChanges();
    tick(); // Simulate 1 second
    expect(ngSelectEl.nativeElement.hasAttribute('disabled')).toBeFalse();
    expect(ngSelectEl.nativeElement.classList.contains('read-only')).toBeFalse();
  }));


});
