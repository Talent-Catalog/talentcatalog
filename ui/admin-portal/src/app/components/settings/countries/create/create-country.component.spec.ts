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

import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {ReactiveFormsModule} from '@angular/forms';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {By} from '@angular/platform-browser';
import {CreateCountryComponent} from './create-country.component';
import {CountryService} from '../../../../services/country.service';
import {of, throwError} from 'rxjs';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {MockJob} from "../../../../MockData/MockJob";

describe('CreateCountryComponent', () => {
  let component: CreateCountryComponent;
  let fixture: ComponentFixture<CreateCountryComponent>;
  let countryServiceSpy: jasmine.SpyObj<CountryService>;
  let activeModalSpy: jasmine.SpyObj<NgbActiveModal>;

  beforeEach(async () => {
    const countryServiceSpyObj = jasmine.createSpyObj('CountryService', ['create']);
    const activeModalSpyObj = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);

    await TestBed.configureTestingModule({
      declarations: [ CreateCountryComponent ],
      imports: [HttpClientTestingModule, NgSelectModule, ReactiveFormsModule ],
      providers: [
        { provide: CountryService, useValue: countryServiceSpyObj },
        { provide: NgbActiveModal, useValue: activeModalSpyObj }
      ]
    }).compileComponents();

    countryServiceSpy = TestBed.inject(CountryService) as jasmine.SpyObj<CountryService>;
    activeModalSpy = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateCountryComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the form with empty controls', () => {
    expect(component.countryForm).toBeTruthy();
    expect(component.countryForm.get('name').value).toBeNull();
    expect(component.countryForm.get('status').value).toBeNull();
  });

  it('should update the form control values when input values change', () => {
    const nameInput = fixture.debugElement.query(By.css('input#name')).nativeElement;

    nameInput.value = 'Test Country';
    nameInput.dispatchEvent(new Event('input'));

    fixture.detectChanges();

    expect(component.countryForm.get('name').value).toBe('Test Country');

    component.countryForm.get('status').setValue('active');

    fixture.detectChanges();

    expect(component.countryForm.get('status').value).toBe('active');
  });

  it('should call CountryService.create and close the modal when the save button is clicked', waitForAsync(() => {
    component.countryForm.get('name').setValue('Test Country');
    component.countryForm.get('status').setValue('active');
    fixture.detectChanges();

    countryServiceSpy.create.and.returnValue(of(MockJob.country));
    activeModalSpy.close.and.returnValue();
    const button = fixture.debugElement.query(By.css('button.btn-primary')).nativeElement;
    button.click();
    fixture.detectChanges();
    expect(countryServiceSpy.create).toHaveBeenCalledWith({ name: 'Test Country', status: 'active' });
    expect(activeModalSpy.close).toHaveBeenCalled();
  }));

  it('should display error message when there is an error', waitForAsync(() => {
    countryServiceSpy.create.and.returnValue(throwError('Error occurred'));

    component.countryForm.get('name').setValue('Test Country');
    component.countryForm.get('status').setValue('active');
    fixture.detectChanges();

    const button = fixture.debugElement.query(By.css('button.btn-primary')).nativeElement;
    button.click();
    fixture.detectChanges();

    expect(component.error).toBe('Error occurred');

    const errorMessage = fixture.debugElement.query(By.css('.alert-danger')).nativeElement;
    expect(errorMessage).toBeTruthy();
    expect(errorMessage.textContent).toContain('Error occurred');
  }));


  it('should dismiss the modal when dismiss button is clicked', () => {
    const dismissButton = fixture.debugElement.query(By.css('button.btn-close')).nativeElement;
    dismissButton.click();

    expect(activeModalSpy.dismiss).toHaveBeenCalledWith(false);
  });

});
