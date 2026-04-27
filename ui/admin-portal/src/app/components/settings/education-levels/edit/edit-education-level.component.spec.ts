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
import {EditEducationLevelComponent} from "./edit-education-level.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {EducationLevelService} from "../../../../services/education-level.service";
import {UntypedFormBuilder, ReactiveFormsModule} from "@angular/forms";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {EducationLevel} from "../../../../model/education-level";
import {of, throwError} from "rxjs";

describe('EditEducationLevelComponent', () => {
  let component: EditEducationLevelComponent;
  let fixture: ComponentFixture<EditEducationLevelComponent>;
  let educationLevelServiceSpy: jasmine.SpyObj<EducationLevelService>;
  let ngbActiveModalSpy: jasmine.SpyObj<NgbActiveModal>;
  let formBuilder: UntypedFormBuilder;
  // @ts-expect-error
  const educationLevel: EducationLevel = { level: 1, name: 'Test', status: 'active' };

  beforeEach(async () => {
    const educationLevelServiceSpyObj = jasmine.createSpyObj('EducationLevelService', ['get', 'update']);
    const ngbActiveModalSpyObj = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);

    await TestBed.configureTestingModule({
      declarations: [EditEducationLevelComponent],
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
    fixture = TestBed.createComponent(EditEducationLevelComponent);
    component = fixture.componentInstance;
    component.educationLevelId = 1; // Mock education level ID
    educationLevelServiceSpy.get.and.returnValue(of(educationLevel));
    educationLevelServiceSpy.update.and.returnValue(of(educationLevel));

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with education level data', fakeAsync(() => {
    component.ngOnInit();
    tick(); // Waiting for async operation to complete

    expect(component.educationLevelForm.value).toEqual(educationLevel);
    expect(component.loading).toBeFalse();
  }));

  it('should call onSave and close modal when education level is successfully updated', fakeAsync(() => {
    component.ngOnInit();
    tick(); // Waiting for async operation to complete
    component.onSave();
    tick(); // Waiting for async operation to complete

    expect(educationLevelServiceSpy.update).toHaveBeenCalledWith(1, educationLevel);
    expect(ngbActiveModalSpy.close).toHaveBeenCalledWith(educationLevel);
    expect(component.saving).toBeFalse();
  }));

  it('should handle error when education level update fails', fakeAsync(() => {
    const errorResponse = { status: 500, message: 'Internal Server Error' };
    educationLevelServiceSpy.update.and.returnValue(throwError(errorResponse));

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
