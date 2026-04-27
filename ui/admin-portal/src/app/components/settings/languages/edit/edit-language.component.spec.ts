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
import {EditLanguageComponent} from "./edit-language.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {LanguageService} from "../../../../services/language.service";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {UntypedFormBuilder, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {Language} from "../../../../model/language";
import {of, throwError} from "rxjs";

describe('EditLanguageComponent', () => {
  let component: EditLanguageComponent;
  let fixture: ComponentFixture<EditLanguageComponent>;
  let languageServiceSpy: jasmine.SpyObj<LanguageService>;
  let ngbActiveModalSpy: jasmine.SpyObj<NgbActiveModal>;
  let formBuilder: UntypedFormBuilder;
  const mockLanguage: Language = { id: 1, name: 'English', status: 'active' };
  const updatedLanguage: Language = { id: 1, name: 'Updated Language', status: 'inactive' };

  beforeEach(async () => {
    const languageServiceSpyObj = jasmine.createSpyObj('LanguageService', ['get', 'update']);
    const ngbActiveModalSpyObj = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);

    await TestBed.configureTestingModule({
      declarations: [EditLanguageComponent],
      imports: [ReactiveFormsModule,NgSelectModule,HttpClientTestingModule],
      providers: [
        { provide: LanguageService, useValue: languageServiceSpyObj },
        { provide: NgbActiveModal, useValue: ngbActiveModalSpyObj }
      ]
    }).compileComponents();

    languageServiceSpy = TestBed.inject(LanguageService) as jasmine.SpyObj<LanguageService>;
    ngbActiveModalSpy = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;
    formBuilder = TestBed.inject(UntypedFormBuilder);
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EditLanguageComponent);
    component = fixture.componentInstance;
    component.languageId = 1; // Assuming languageId is set before ngOnInit
    languageServiceSpy.get.and.returnValue(of(mockLanguage));
    languageServiceSpy.update.and.returnValue(of(updatedLanguage));
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load language data on initialization', fakeAsync(() => {

    component.ngOnInit();
    tick(); // Waiting for async operation to complete

    expect(component.languageForm.value).toEqual({ name: 'English', status: 'active' });
    expect(component.loading).toBeFalse();
  }));

  it('should call onSave and close modal when language is successfully updated', fakeAsync(() => {

    component.languageForm.patchValue({ name: 'Updated Language', status: 'inactive' });

    component.onSave();
    tick(); // Waiting for async operation to complete

    expect(languageServiceSpy.update).toHaveBeenCalledWith(1, { name: 'Updated Language', status: 'inactive' });
    expect(ngbActiveModalSpy.close).toHaveBeenCalledWith(updatedLanguage);
    expect(component.saving).toBeFalse();
  }));

  it('should handle error when updating language fails', fakeAsync(() => {
    const errorResponse = { status: 500, message: 'Internal Server Error' };
    languageServiceSpy.update.and.returnValue(throwError(errorResponse));

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
