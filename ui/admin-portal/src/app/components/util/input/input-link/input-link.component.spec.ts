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

import {InputLinkComponent, UpdateLinkRequest} from "./input-link.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {UntypedFormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";

describe('InputLinkComponent', () => {
  let component: InputLinkComponent;
  let fixture: ComponentFixture<InputLinkComponent>;
  let modalMock: Partial<NgbActiveModal>;

  beforeEach(async () => {
    modalMock = {
      close: jasmine.createSpy('close'),
      dismiss: jasmine.createSpy('dismiss')
    };

    await TestBed.configureTestingModule({
      imports: [FormsModule, ReactiveFormsModule],
      declarations: [InputLinkComponent],
      providers: [
        { provide: NgbActiveModal, useValue: modalMock },
        UntypedFormBuilder
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(InputLinkComponent);
    component = fixture.componentInstance;
    component.initialValue = { name: 'InitialName', url: 'https://example.com' };
    component.instructions = 'Enter link details';
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with initial values', () => {
    expect(component.form.get('name').value).toBe('InitialName');
    expect(component.form.get('url').value).toBe('https://example.com');
  });

  it('should dismiss the modal when dismiss() is called', () => {
    component.dismiss();
    expect(modalMock.dismiss).toHaveBeenCalledWith(false);
  });

  it('should close the modal with form values when close() is called', fakeAsync(() => {
    const updatedValue: UpdateLinkRequest = { name: 'UpdatedName', url: 'https://updated.com' };
    component.form.patchValue(updatedValue);
    fixture.detectChanges();

    component.close();
    tick(); // Simulate async operation

    expect(modalMock.close).toHaveBeenCalledWith(updatedValue);
  }));

  it('should handle empty initial values gracefully', () => {
    component.initialValue = null;
    component.ngOnInit();

    expect(component.form.get('name').value).toBeNull();
    expect(component.form.get('url').value).toBeNull();
  });
});
