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
import {of, throwError} from 'rxjs';
import {By} from '@angular/platform-browser';
import {EditCountryComponent} from './edit-country.component';
import {CountryService} from '../../../../services/country.service';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {NgSelectModule} from '@ng-select/ng-select';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {MockJob} from "../../../../MockData/MockJob";

describe('EditCountryComponent', () => {
  let component: EditCountryComponent;
  let fixture: ComponentFixture<EditCountryComponent>;
  let countryServiceSpy: jasmine.SpyObj<CountryService>;
  let activeModalSpy: jasmine.SpyObj<NgbActiveModal>;

  beforeEach(async () => {
    const countryServiceSpyObj = jasmine.createSpyObj('CountryService', ['get', 'update']);
    const activeModalSpyObj = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);

    await TestBed.configureTestingModule({
      declarations: [EditCountryComponent],
      imports: [HttpClientTestingModule, ReactiveFormsModule, NgSelectModule],
      providers: [
        { provide: CountryService, useValue: countryServiceSpyObj },
        { provide: NgbActiveModal, useValue: activeModalSpyObj }
      ]
    }).compileComponents();

    countryServiceSpy = TestBed.inject(CountryService) as jasmine.SpyObj<CountryService>;
    activeModalSpy = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EditCountryComponent);
    component = fixture.componentInstance;
    component.countryId = 1; // Set the countryId for testing
    countryServiceSpy.update.and.returnValue(of(MockJob.country));
    countryServiceSpy.get.and.returnValue(of(MockJob.country));
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the form with country data on ngOnInit', waitForAsync(() => {

    component.ngOnInit();
    fixture.detectChanges();

    expect(component.countryForm).toBeTruthy();
    expect(component.countryForm.get('name').value).toBe(MockJob.country.name);
    expect(component.countryForm.get('status').value).toBe(MockJob.country.status);
    expect(component.loading).toBeFalse();
  }));

  it('should call CountryService.update and close the modal when the save button is clicked', waitForAsync(() => {

    component.countryForm.get('name').setValue(MockJob.country.name);
    component.countryForm.get('status').setValue(MockJob.country.status);
    fixture.detectChanges();

    const button = fixture.debugElement.query(By.css('button.btn-primary')).nativeElement;
    button.click();
    fixture.detectChanges();

    expect(countryServiceSpy.update).toHaveBeenCalledWith(component.countryId, { name: MockJob.country.name, status: MockJob.country.status });
    expect(activeModalSpy.close).toHaveBeenCalledWith(MockJob.country);
  }));

  it('should display error message when there is an error', waitForAsync(() => {
    countryServiceSpy.update.and.returnValue(throwError('Error occurred'));

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
