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
import {CreateEducationMajorComponent} from "./create-education-major.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {EducationMajorService} from "../../../../services/education-major.service";
import {UntypedFormBuilder, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {EducationMajor} from "../../../../model/education-major";
import {of, throwError} from "rxjs";

describe('CreateEducationMajorComponent', () => {
  let component: CreateEducationMajorComponent;
  let fixture: ComponentFixture<CreateEducationMajorComponent>;
  let educationMajorServiceSpy: jasmine.SpyObj<EducationMajorService>;
  let ngbActiveModalSpy: jasmine.SpyObj<NgbActiveModal>;
  let formBuilder: UntypedFormBuilder;

  beforeEach(async () => {
    const educationMajorServiceSpyObj = jasmine.createSpyObj('EducationMajorService', ['create']);
    const ngbActiveModalSpyObj = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);

    await TestBed.configureTestingModule({
      declarations: [CreateEducationMajorComponent],
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
    fixture = TestBed.createComponent(CreateEducationMajorComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with required fields', () => {
    expect(component.educationMajorForm).toBeDefined();
    expect(component.educationMajorForm.get('name')).toBeTruthy();
    expect(component.educationMajorForm.get('status')).toBeTruthy();
  });

  it('should call onSave and close modal when education major is successfully created', () => {
    const educationMajor: EducationMajor = { id: 1, name: 'Test', status: 'active' };
    educationMajorServiceSpy.create.and.returnValue(of(educationMajor));

    component.educationMajorForm.patchValue({ name: 'Test', status: 'active' });
    component.onSave();

    expect(educationMajorServiceSpy.create).toHaveBeenCalledWith({ name: 'Test', status: 'active' });
    expect(ngbActiveModalSpy.close).toHaveBeenCalledWith(educationMajor);
    expect(component.saving).toBeFalse();
  });

  it('should handle error when education major creation fails', () => {
    const errorResponse = { status: 500, message: 'Internal Server Error' };
    educationMajorServiceSpy.create.and.returnValue(throwError(errorResponse));

    component.onSave();

    expect(educationMajorServiceSpy.create).toHaveBeenCalled();
    expect(component.error).toEqual(errorResponse);
    expect(component.saving).toBeFalse();
  });

  it('should dismiss modal when dismiss is called', () => {
    component.dismiss();
    expect(ngbActiveModalSpy.dismiss).toHaveBeenCalledWith(false);
  });
});
