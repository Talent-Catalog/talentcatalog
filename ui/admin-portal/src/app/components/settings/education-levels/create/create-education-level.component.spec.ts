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
import {CreateEducationLevelComponent} from "./create-education-level.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {UntypedFormBuilder, ReactiveFormsModule} from "@angular/forms";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {EducationLevelService} from "../../../../services/education-level.service";
import {NgSelectModule} from "@ng-select/ng-select";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {EducationLevel} from "../../../../model/education-level";
import {of, throwError} from "rxjs";

describe('CreateEducationLevelComponent', () => {
  let component: CreateEducationLevelComponent;
  let fixture: ComponentFixture<CreateEducationLevelComponent>;
  let educationLevelServiceSpy: jasmine.SpyObj<EducationLevelService>;
  let ngbActiveModalSpy: jasmine.SpyObj<NgbActiveModal>;
  let formBuilder: UntypedFormBuilder;

  beforeEach(async () => {
    const educationLevelServiceSpyObj = jasmine.createSpyObj('EducationLevelService', ['create']);
    const ngbActiveModalSpyObj = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);

    await TestBed.configureTestingModule({
      declarations: [CreateEducationLevelComponent],
      imports: [HttpClientTestingModule,ReactiveFormsModule,NgSelectModule],
      providers: [
        { provide: EducationLevelService, useValue: educationLevelServiceSpyObj },
        { provide: NgbActiveModal, useValue: ngbActiveModalSpyObj }
      ]
    }).compileComponents();

    educationLevelServiceSpy = TestBed.inject(EducationLevelService) as jasmine.SpyObj<EducationLevelService>;
    ngbActiveModalSpy = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;
    formBuilder = TestBed.inject(UntypedFormBuilder);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateEducationLevelComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with required fields', () => {
    expect(component.educationLevelForm).toBeDefined();
    expect(component.educationLevelForm.get('level')).toBeTruthy();
    expect(component.educationLevelForm.get('name')).toBeTruthy();
    expect(component.educationLevelForm.get('status')).toBeTruthy();
  });

  it('should call onSave and close modal when education level is successfully created', () => {
    const educationLevel: EducationLevel = { id: 1, level: 1, name: 'Test', status: 'active' };

    educationLevelServiceSpy.create.and.returnValue(of(educationLevel));

    component.educationLevelForm.patchValue({ level: 1, name: 'Test', status: 'active' });
    component.onSave();

    expect(educationLevelServiceSpy.create).toHaveBeenCalledWith({ level: 1, name: 'Test', status: 'active' });
    expect(ngbActiveModalSpy.close).toHaveBeenCalledWith(educationLevel);
    expect(component.saving).toBeFalse();
  });

  it('should handle error when education level creation fails', () => {
    const errorResponse = { status: 500, message: 'Internal Server Error' };
    educationLevelServiceSpy.create.and.returnValue(throwError(errorResponse));

    component.onSave();

    expect(educationLevelServiceSpy.create).toHaveBeenCalled();
    expect(component.error).toEqual(errorResponse);
    expect(component.saving).toBeFalse();
  });

  it('should dismiss modal when dismiss is called', () => {
    component.dismiss();
    expect(ngbActiveModalSpy.dismiss).toHaveBeenCalledWith(false);
  });
});
