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
import {Component, DebugElement} from "@angular/core";
import {ComponentFixture, TestBed, waitForAsync} from "@angular/core/testing";
import {FormsModule, NgControl} from "@angular/forms";
import {By} from "@angular/platform-browser";
import {NgSelectModule} from "@ng-select/ng-select";

@Component({
  template: `
    <div [appReadOnlyInputs]="readOnly">
      <input type="text" />
      <ng-select [items]="['a', 'b', 'c']"></ng-select>
      <textarea></textarea>
    </div>

  `
})
class TestComponent {
  readOnly = true;
}

fdescribe('ReadOnlyInputsDirective', () => {
  let component: TestComponent;
  let fixture: ComponentFixture<TestComponent>;
  let inputEl: DebugElement;
  let ngSelectEl: DebugElement;
  let textareaEl: DebugElement;
  let directiveEl: DebugElement;

  beforeEach(waitForAsync(() => {
    TestBed.configureTestingModule({
      imports: [FormsModule, NgSelectModule],
      declarations: [TestComponent, ReadOnlyInputsDirective],
      providers: [
        {
          provide: NgControl,
        }
      ]
    })
    fixture = TestBed.createComponent(TestComponent);
    component = fixture.componentInstance;
    inputEl = fixture.debugElement.query(By.css('input'));
    ngSelectEl = fixture.debugElement.query(By.css('ng-select'));
    fixture.detectChanges();
  }));

  it('should create an instance', () => {
    const directive = new ReadOnlyInputsDirective(null, null);
    expect(directive).toBeTruthy();
  });

  it('should disable a text input if read only', () => {
    fixture.componentInstance.readOnly = true;
    fixture.detectChanges();
    expect(inputEl.nativeElement.hasAttribute('disabled')).toBeTrue();
  });

  // it('should not disable a text input if not read only', () => {
  //   fixture.componentInstance.readOnly = false;
  //   fixture.detectChanges();
  //   expect(inputEl.nativeElement.hasAttribute('disabled')).toBeFalse();
  // });
});
