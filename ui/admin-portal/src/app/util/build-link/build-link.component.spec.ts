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

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormsModule, ReactiveFormsModule} from '@angular/forms';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {By} from '@angular/platform-browser';
import {BuildLinkComponent} from './build-link.component';
import {ElementRef} from '@angular/core';
import {TranslateModule} from "@ngx-translate/core";

describe('BuildLinkComponent', () => {
  let component: BuildLinkComponent;
  let fixture: ComponentFixture<BuildLinkComponent>;
  let activeModalSpy: jasmine.SpyObj<NgbActiveModal>;
  let urlInputElement: ElementRef;

  beforeEach(async () => {
    activeModalSpy = jasmine.createSpyObj('NgbActiveModal', ['close']);

    await TestBed.configureTestingModule({
      declarations: [BuildLinkComponent],
      imports: [FormsModule, ReactiveFormsModule, TranslateModule.forRoot({})],
      providers: [{provide: NgbActiveModal, useValue: activeModalSpy}]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(BuildLinkComponent);
    component = fixture.componentInstance;
    urlInputElement = fixture.debugElement.query(By.css('#url')).nativeElement;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the form with placeholder and currentUrl', () => {
    component.placeholder = 'Test Placeholder';
    component.currentUrl = 'https://example.com';
    component.ngOnInit();
    expect(component.form.get('placeholder')?.value).toBe('Test Placeholder');
    expect(component.form.get('url')?.value).toBe('https://example.com');
  });

  it('should focus on the URL input field after view initialization', () => {
    spyOn(component.urlInput.nativeElement, 'focus');
    component.ngAfterViewInit();
    expect(component.urlInput.nativeElement.focus).toHaveBeenCalled();
  });

  it('should call save method when form is valid and enter key is pressed', () => {
    spyOn(component, 'save');
    component.form.setValue({placeholder: 'Test', url: 'https://example.com'});

    const event = new KeyboardEvent('keydown', {key: 'Enter'});
    component.onKeydownEnter(event);

    expect(component.save).toHaveBeenCalled();
  });

  it('should not call save method when form is invalid and enter key is pressed', () => {
    spyOn(component, 'save');
    component.form.setValue({placeholder: '', url: ''});

    const event = new KeyboardEvent('keydown', {key: 'Enter'});
    component.onKeydownEnter(event);

    expect(component.save).not.toHaveBeenCalled();
  });

  it('should close the modal with created link on save', () => {
    component.form.setValue({placeholder: 'Test', url: 'https://example.com'});
    component.save();
    expect(activeModalSpy.close).toHaveBeenCalledWith({
      placeholder: 'Test',
      url: 'https://example.com'
    });
  });

  it('should close the modal on cancel', () => {
    component.cancel();
    expect(activeModalSpy.close).toHaveBeenCalled();
  });

  it('should disable the save button when the form is invalid', () => {
    component.form.setValue({placeholder: '', url: ''});
    fixture.detectChanges();
    const saveButton = fixture.debugElement.query(By.css('.btn-success')).nativeElement;
    expect(saveButton.disabled).toBeTrue();
  });

  it('should enable the save button when the form is valid', () => {
    component.form.setValue({placeholder: 'Test', url: 'https://example.com'});
    fixture.detectChanges();
    const saveButton = fixture.debugElement.query(By.css('.btn-success')).nativeElement;
    expect(saveButton.disabled).toBeFalse();
  });

  it('should show an error message when the URL format is incorrect', () => {
    const invalidUrl = 'invalid-url';
    component.form.controls['url'].setValue(invalidUrl);
    component.form.controls['url'].markAsDirty();
    fixture.detectChanges();

    const errorElement = fixture.debugElement.query(By.css('.text-danger')).nativeElement;
    expect(errorElement).toBeTruthy();
    expect(errorElement.textContent).toContain('CHAT.URL-PREFIX-ERROR');
  });
});
