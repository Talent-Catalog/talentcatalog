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

import {UntypedFormBuilder, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {of, throwError} from "rxjs";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {SystemLanguage} from "../../../../model/language";
import {CreateLanguageComponent} from "./create-language.component";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {LanguageService} from "../../../../services/language.service";

describe('CreateLanguageComponent', () => {
  let component: CreateLanguageComponent;
  let fixture: ComponentFixture<CreateLanguageComponent>;
  let languageServiceSpy: jasmine.SpyObj<LanguageService>;
  let ngbActiveModalSpy: jasmine.SpyObj<NgbActiveModal>;
  let formBuilder: UntypedFormBuilder;
  // @ts-expect-error
  const newLanguage: SystemLanguage = { langCode: 'fr' };

  beforeEach(async () => {
    const languageServiceSpyObj = jasmine.createSpyObj('LanguageService', ['addSystemLanguage']);
    const ngbActiveModalSpyObj = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);

    await TestBed.configureTestingModule({
      declarations: [CreateLanguageComponent],
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
    fixture = TestBed.createComponent(CreateLanguageComponent);
    component = fixture.componentInstance;
    languageServiceSpy.addSystemLanguage.and.returnValue(of(newLanguage));

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call onSave and close modal when system language is successfully added', fakeAsync(() => {
    component.languageForm.patchValue({ langCode: 'fr' });

    component.onSave();
    tick(); // Waiting for async operation to complete

    expect(languageServiceSpy.addSystemLanguage).toHaveBeenCalledWith('fr');
    expect(ngbActiveModalSpy.close).toHaveBeenCalledWith(newLanguage);
    expect(component.saving).toBeFalse();
  }));

  it('should handle error when adding system language fails', fakeAsync(() => {
    const errorResponse = { status: 500, message: 'Internal Server Error' };
    languageServiceSpy.addSystemLanguage.and.returnValue(throwError(errorResponse));

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
