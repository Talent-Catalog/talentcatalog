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

import {InputTextComponent} from "./input-text.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";

describe('InputTextComponent', () => {
  let component: InputTextComponent;
  let fixture: ComponentFixture<InputTextComponent>;
  let modalMock: Partial<NgbActiveModal>;

  beforeEach(async () => {
    modalMock = {
      close: jasmine.createSpy('close'),
      dismiss: jasmine.createSpy('dismiss')
    };

    await TestBed.configureTestingModule({
      imports: [FormsModule, ReactiveFormsModule],
      declarations: [InputTextComponent],
      providers: [
        { provide: NgbActiveModal, useValue: modalMock }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(InputTextComponent);
    component = fixture.componentInstance;
    component.initialText = 'Initial text';
    component.message = 'Please enter text';
    component.nRows = 3;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with initial text', () => {
    expect(component.form.get('text').value).toBe('Initial text');
  });

  it('should dismiss the modal when dismiss() is called', () => {
    component.dismiss();
    expect(modalMock.dismiss).toHaveBeenCalledWith(false);
  });

  it('should close the modal with form value when close() is called', fakeAsync(() => {
    component.form.patchValue({ text: 'Updated text' });
    fixture.detectChanges();

    component.close();
    tick(); // Simulate async operation

    expect(modalMock.close).toHaveBeenCalledWith('Updated text');
  }));

  it('should render the message if message is provided', () => {
    const labelElement: HTMLElement = fixture.nativeElement.querySelector('tc-label');
    expect(labelElement).toBeTruthy();
    expect(labelElement.textContent).toContain('Please enter text');
  });

  it('should not render the message if message is not provided', () => {
    component.message = null;
    fixture.detectChanges();

    const labelElement: HTMLElement = fixture.nativeElement.querySelector('tc-label');
    expect(labelElement).toBeNull();
  });
});
