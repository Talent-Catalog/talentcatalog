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
import {EditEducationMajorComponent} from "./edit-education-major.component";
import {EducationMajorService} from "../../../../services/education-major.service";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {UntypedFormBuilder, ReactiveFormsModule} from "@angular/forms";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {of, throwError} from "rxjs";
import {EducationMajor} from "../../../../model/education-major";

describe('EditEducationMajorComponent', () => {
  let component: EditEducationMajorComponent;
  let fixture: ComponentFixture<EditEducationMajorComponent>;
  let educationMajorServiceSpy: jasmine.SpyObj<EducationMajorService>;
  let ngbActiveModalSpy: jasmine.SpyObj<NgbActiveModal>;
  let formBuilder: UntypedFormBuilder;
  // @ts-expect-error
  const educationMajor: EducationMajor = { name: 'Test', status: 'active' };

  beforeEach(async () => {
    const educationMajorServiceSpyObj = jasmine.createSpyObj('EducationMajorService', ['get', 'update']);
    const ngbActiveModalSpyObj = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);

    await TestBed.configureTestingModule({
      declarations: [EditEducationMajorComponent],
      imports: [ReactiveFormsModule,NgSelectModule,HttpClientTestingModule],
      providers: [
        { provide: EducationMajorService, useValue: educationMajorServiceSpyObj },
        { provide: NgbActiveModal, useValue: ngbActiveModalSpyObj }
      ]
    }).compileComponents();

    educationMajorServiceSpy = TestBed.inject(EducationMajorService) as jasmine.SpyObj<EducationMajorService>;
    ngbActiveModalSpy = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;
    formBuilder = TestBed.inject(UntypedFormBuilder);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EditEducationMajorComponent);
    component = fixture.componentInstance;
    component.educationMajorId = 1; // Mock education major ID
    educationMajorServiceSpy.get.and.returnValue(of(educationMajor));
    educationMajorServiceSpy.update.and.returnValue(of(educationMajor));

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with education major data', fakeAsync(() => {
    component.ngOnInit();
    tick(); // Waiting for async operation to complete

    expect(component.educationMajorForm.value).toEqual(educationMajor);
    expect(component.loading).toBeFalse();
  }));

  it('should call onSave and close modal when education major is successfully updated', fakeAsync(() => {

    component.ngOnInit();
    tick(); // Waiting for async operation to complete
    component.onSave();
    tick(); // Waiting for async operation to complete

    expect(educationMajorServiceSpy.update).toHaveBeenCalledWith(1, educationMajor);
    expect(ngbActiveModalSpy.close).toHaveBeenCalledWith(educationMajor);
    expect(component.saving).toBeFalse();
  }));

  it('should handle error when education major update fails', fakeAsync(() => {
    const educationMajor: EducationMajor = { id: 1, name: 'Test', status: 'active' };
    const errorResponse = { status: 500, message: 'Internal Server Error' };
    educationMajorServiceSpy.get.and.returnValue(of(educationMajor));
    educationMajorServiceSpy.update.and.returnValue(throwError(errorResponse));

    component.ngOnInit();
    tick(); // Waiting for async operation to complete
    component.onSave();
    tick(); // Waiting for async operation to complete

    expect(component.error).toEqual(errorResponse);
    expect(component.saving).toBeFalse();
  }));

  it('should dismiss modal when dismiss is called', () => {
    component.dismiss();
    expect(ngbActiveModalSpy.dismiss).toHaveBeenCalledWith(false);
  });
});
