/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {TranslateModule} from '@ngx-translate/core';

import {BuildLinkComponent} from './build-link.component';

describe('BuildLinkComponent', () => {
  let component: BuildLinkComponent;
  let fixture: ComponentFixture<BuildLinkComponent>;
  let modalSpy: jasmine.SpyObj<NgbActiveModal>;

  beforeEach(async () => {
    modalSpy = jasmine.createSpyObj<NgbActiveModal>(
      'NgbActiveModal',
      ['close']
    );

    await TestBed.configureTestingModule({
      declarations: [BuildLinkComponent],
      imports: [
        ReactiveFormsModule,
        TranslateModule.forRoot()
      ],
      providers: [
        {
          provide: NgbActiveModal,
          useValue: modalSpy
        }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(BuildLinkComponent);
    component = fixture.componentInstance;

    component.placeholder = 'Talent Catalog';
    component.currentUrl = '';

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialise the form with the input values', () => {
    expect(component.form.value).toEqual({
      placeholder: 'Talent Catalog',
      url: ''
    });
  });

  it('should set title to Add Link when currentUrl is empty', () => {
    expect(component.title).toBe('Add Link');
  });

  it('should set title to Edit Link when currentUrl is provided', () => {
    const editFixture = TestBed.createComponent(BuildLinkComponent);
    const editComponent = editFixture.componentInstance;

    editComponent.placeholder = 'Talent Catalog';
    editComponent.currentUrl = 'https://www.talentcatalog.net';

    editFixture.detectChanges();

    expect(editComponent.title).toBe('Edit Link');
  });

  it('should require a placeholder', () => {
    const placeholderControl = component.form.get('placeholder');

    placeholderControl.setValue('');

    expect(placeholderControl.hasError('required')).toBeTrue();
    expect(component.form.invalid).toBeTrue();
  });

  it('should require a URL', () => {
    const urlControl = component.form.get('url');

    urlControl.setValue('');

    expect(urlControl.hasError('required')).toBeTrue();
    expect(component.form.invalid).toBeTrue();
  });

  it('should save the link and close the modal', () => {
    component.form.setValue({
      placeholder: 'Talent Catalog',
      url: 'https://www.talentcatalog.net'
    });

    component.save();

    expect(modalSpy.close).toHaveBeenCalledWith({
      placeholder: 'Talent Catalog',
      url: 'https://www.talentcatalog.net'
    });
  });

  it('should close the modal without a result when cancelled', () => {
    component.cancel();

    expect(modalSpy.close).toHaveBeenCalledWith();
  });

  it('should prevent the default Enter key behaviour', () => {
    component.form.setValue({
      placeholder: 'Talent Catalog',
      url: ''
    });

    const event = jasmine.createSpyObj<KeyboardEvent>(
      'KeyboardEvent',
      ['preventDefault']
    );

    component.onKeydownEnter(event);

    expect(event.preventDefault).toHaveBeenCalled();
    expect(modalSpy.close).not.toHaveBeenCalled();
  });

  it('should save when Enter is pressed and the form is valid', () => {
    component.form.setValue({
      placeholder: 'Talent Catalog',
      url: 'https://www.talentcatalog.net'
    });

    const event = jasmine.createSpyObj<KeyboardEvent>(
      'KeyboardEvent',
      ['preventDefault']
    );

    component.onKeydownEnter(event);

    expect(event.preventDefault).toHaveBeenCalled();
    expect(modalSpy.close).toHaveBeenCalledWith({
      placeholder: 'Talent Catalog',
      url: 'https://www.talentcatalog.net'
    });
  });

  it('should not save when Enter is pressed and the form is invalid', () => {
    component.form.setValue({
      placeholder: '',
      url: ''
    });

    const event = jasmine.createSpyObj<KeyboardEvent>(
      'KeyboardEvent',
      ['preventDefault']
    );

    component.onKeydownEnter(event);

    expect(event.preventDefault).toHaveBeenCalled();
    expect(modalSpy.close).not.toHaveBeenCalled();
  });

  it('should focus the URL input after the view is initialised', () => {
    const urlInput = fixture.nativeElement.querySelector(
      '#url'
    ) as HTMLInputElement;

    expect(document.activeElement).toBe(urlInput);
  });
});
