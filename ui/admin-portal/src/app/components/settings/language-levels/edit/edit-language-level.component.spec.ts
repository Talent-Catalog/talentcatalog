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
import {EditLanguageLevelComponent} from "./edit-language-level.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {UntypedFormBuilder, ReactiveFormsModule} from "@angular/forms";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {LanguageLevelService} from "../../../../services/language-level.service";
import {NgSelectModule} from "@ng-select/ng-select";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {LanguageLevel} from "../../../../model/language-level";
import {of, throwError} from "rxjs";

describe('EditLanguageLevelComponent', () => {
  let component: EditLanguageLevelComponent;
  let fixture: ComponentFixture<EditLanguageLevelComponent>;
  let languageLevelServiceSpy: jasmine.SpyObj<LanguageLevelService>;
  let ngbActiveModalSpy: jasmine.SpyObj<NgbActiveModal>;
  let formBuilder: UntypedFormBuilder;
  // @ts-expect-error
  const languageLevel: LanguageLevel = { level: 5, name: 'Advanced', cefrLevel: 'B2', status: 'active' };
  // @ts-expect-error
  const updatedLanguageLevel: LanguageLevel = { level: 4, name: 'Intermediate', cefrLevel: 'B1', status: 'active' };

  beforeEach(async () => {
    const languageLevelServiceSpyObj = jasmine.createSpyObj('LanguageLevelService', ['get', 'update']);
    const ngbActiveModalSpyObj = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);

    await TestBed.configureTestingModule({
      declarations: [EditLanguageLevelComponent],
      imports: [ReactiveFormsModule,NgSelectModule,HttpClientTestingModule],
      providers: [
        { provide: LanguageLevelService, useValue: languageLevelServiceSpyObj },
        { provide: NgbActiveModal, useValue: ngbActiveModalSpyObj }
      ]
    }).compileComponents();

    languageLevelServiceSpy = TestBed.inject(LanguageLevelService) as jasmine.SpyObj<LanguageLevelService>;
    ngbActiveModalSpy = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;
    formBuilder = TestBed.inject(UntypedFormBuilder);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EditLanguageLevelComponent);
    component = fixture.componentInstance;
    component.languageLevelId = 1; // Set languageLevelId for testing
    languageLevelServiceSpy.get.and.returnValue(of(languageLevel));
    languageLevelServiceSpy.update.and.returnValue(of(updatedLanguageLevel));
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load language level and initialize form when component is initialized', fakeAsync(() => {
    fixture.detectChanges();
    tick(); // Waiting for async operation to complete

    expect(languageLevelServiceSpy.get).toHaveBeenCalledWith(component.languageLevelId);
    expect(component.loading).toBeFalse();
    expect(component.languageLevelForm.value).toEqual(languageLevel);
  }));

  it('should call onSave and close modal when language level is successfully updated', fakeAsync(() => {
    component.languageLevelForm.patchValue({ level: 4, name: 'Intermediate', cefrLevel: 'B1', status: 'active' });

    component.onSave();
    tick(); // Waiting for async operation to complete

    expect(languageLevelServiceSpy.update).toHaveBeenCalledWith(component.languageLevelId, {
      level: 4,
      name: 'Intermediate',
      cefrLevel: 'B1',
      status: 'active'
    });
    expect(ngbActiveModalSpy.close).toHaveBeenCalledWith(updatedLanguageLevel);
    expect(component.saving).toBeFalse();
  }));

  it('should handle error when updating language level fails', fakeAsync(() => {
    const errorResponse = { status: 500, message: 'Internal Server Error' };
    languageLevelServiceSpy.update.and.returnValue(throwError(errorResponse));

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
