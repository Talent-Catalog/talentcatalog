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
import {LanguageLevelService} from "../../../../services/language-level.service";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CreateLanguageLevelComponent} from "./create-language-level.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {LanguageLevel} from "../../../../model/language-level";
import {of, throwError} from "rxjs";

describe('CreateLanguageLevelComponent', () => {
  let component: CreateLanguageLevelComponent;
  let fixture: ComponentFixture<CreateLanguageLevelComponent>;
  let languageLevelServiceSpy: jasmine.SpyObj<LanguageLevelService>;
  let ngbActiveModalSpy: jasmine.SpyObj<NgbActiveModal>;
  let formBuilder: UntypedFormBuilder;
  const languageLevel: LanguageLevel = { id: 1, level: 5, name: 'Advanced', cefrLevel: 'B2', status: 'active' };

  beforeEach(async () => {
    const languageLevelServiceSpyObj = jasmine.createSpyObj('LanguageLevelService', ['create']);
    const ngbActiveModalSpyObj = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);

    await TestBed.configureTestingModule({
      declarations: [CreateLanguageLevelComponent],
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
    fixture = TestBed.createComponent(CreateLanguageLevelComponent);
    component = fixture.componentInstance;
    languageLevelServiceSpy.create.and.returnValue(of(languageLevel));

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should call onSave and close modal when language level is successfully created', fakeAsync(() => {
    component.languageLevelForm.patchValue({ level: 5, name: 'Advanced', status: 'active' });

    component.onSave();
    tick(); // Waiting for async operation to complete

    expect(languageLevelServiceSpy.create).toHaveBeenCalledWith({
      level: 5,
      name: 'Advanced',
      status: 'active'
    });
    expect(ngbActiveModalSpy.close).toHaveBeenCalledWith(languageLevel);
    expect(component.saving).toBeFalse();
  }));

  it('should handle error when creating language level fails', fakeAsync(() => {
    const errorResponse = { status: 500, message: 'Internal Server Error' };
    languageLevelServiceSpy.create.and.returnValue(throwError(errorResponse));

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
