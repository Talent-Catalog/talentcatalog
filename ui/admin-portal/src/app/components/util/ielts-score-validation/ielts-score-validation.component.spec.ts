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

import {IeltsScoreValidationComponent} from "./ielts-score-validation.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {UntypedFormControl, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {SimpleChanges} from "@angular/core";

describe('IeltsScoreValidationComponent', () => {
  let component: IeltsScoreValidationComponent;
  let fixture: ComponentFixture<IeltsScoreValidationComponent>;

  beforeEach(async () => {
    await TestBed.configureTestingModule({
      imports: [FormsModule, ReactiveFormsModule],
      declarations: [IeltsScoreValidationComponent]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(IeltsScoreValidationComponent);
    component = fixture.componentInstance;
    component.control = new UntypedFormControl('');
    component.examType = 'IELTSGen'; // Set an initial exam type for testing
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with correct values', () => {
    expect(component.value).toBe('');
    expect(component.ieltsExams).toEqual(['IELTSGen', 'IELTSAca']);
    expect(component.regex.test('7')).toBe(true); // Example test for regex pattern
    expect(component.errorMsg).toBeTruthy();
    expect(component.error).toBe('The IELTS score must be between 0-9 and with decimal increments of .5 only.');
  });

  it('should update control value on valid input', fakeAsync(() => {
    component.value = '7.5';
    component.update();
    tick(); // Ensure asynchronous operations are complete
    expect(component.control.value).toBe('7.5');
    expect(component.error).toBeNull();
  }));

  it('should set control value to NoResponse on empty input', fakeAsync(() => {
    component.value = '';
    component.update();
    tick();
    expect(component.control.value).toBe('NoResponse');
    expect(component.error).toBe('The IELTS score must be between 0-9 and with decimal increments of .5 only.');
  }));

  it('should show error message for invalid input', fakeAsync(() => {
    component.value = '10';
    component.update();
    tick();
    expect(component.control.value).toBe('');
    expect(component.error).toBeTruthy(); // Check that error message is set
  }));

  it('should update error message when examType changes', () => {
    const changes: SimpleChanges = {
      examType: {
        currentValue: 'IELTSAca',
        previousValue: 'IELTSGen',
        firstChange: false, // mock this property as well
        isFirstChange: () => false // mock this method as well
      }
    };
    component.ngOnChanges(changes);
    expect(component.error).toBe('The IELTS score must be between 0-9 and with decimal increments of .5 only.'); // Assuming IELTSAca is valid for this test
  });
});
