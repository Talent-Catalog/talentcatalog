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

import {LanguageLevelFormControlComponent} from "./language-level-form-control.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {LanguageService} from "../../../../services/language.service";
import {LanguageLevelService} from "../../../../services/language-level.service";
import {Language} from "../../../../model/language";
import {LanguageLevel} from "../../../../model/language-level";
import {ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {of} from "rxjs";

describe('LanguageLevelFormControlComponent', () => {
  let component: LanguageLevelFormControlComponent;
  let fixture: ComponentFixture<LanguageLevelFormControlComponent>;
  let languageService: jasmine.SpyObj<LanguageService>;
  let languageLevelService: jasmine.SpyObj<LanguageLevelService>;

  const mockLanguages: Language[] = [{ id: 1, name: 'English',status:'active' }, { id: 2, name: 'Spanish',status:'inactive' }];
  const mockLanguageLevels: LanguageLevel[] = [
    { id:1, level: 1, name: 'Basic',status:'active' },
    { id:2, level: 2, name: 'Intermediate',status:'active' },
    { id:3,level: 3, name: 'Advanced',status:'active'  }
  ];

  beforeEach(async () => {
    const languageServiceSpy = jasmine.createSpyObj('LanguageService', ['listLanguages']);
    const languageLevelServiceSpy = jasmine.createSpyObj('LanguageLevelService', ['listLanguageLevels']);

    await TestBed.configureTestingModule({
      imports: [ReactiveFormsModule, NgSelectModule],
      declarations: [LanguageLevelFormControlComponent],
      providers: [
        { provide: LanguageService, useValue: languageServiceSpy },
        { provide: LanguageLevelService, useValue: languageLevelServiceSpy }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    languageService = TestBed.inject(LanguageService) as jasmine.SpyObj<LanguageService>;
    languageLevelService = TestBed.inject(LanguageLevelService) as jasmine.SpyObj<LanguageLevelService>;

    languageService.listLanguages.and.returnValue(of(mockLanguages));
    languageLevelService.listLanguageLevels.and.returnValue(of(mockLanguageLevels));
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(LanguageLevelFormControlComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with default values', () => {
    expect(component.form).toBeDefined();
    expect(component.form.value).toEqual({
      languageId: null,
      writtenLevel: null,
      spokenLevel: null
    });
  });

  it('should populate languages on init', () => {
    expect(languageService.listLanguages).toHaveBeenCalled();
    expect(component.languages).toEqual(mockLanguages);
  });

  it('should populate language levels on init', () => {
    expect(languageLevelService.listLanguageLevels).toHaveBeenCalled();
    expect(component.languageLevels).toEqual(mockLanguageLevels);
  });

  it('should emit modelUpdated event on form value changes', () => {
    spyOn(component.modelUpdated, 'emit');
    component.form.controls['languageId'].setValue(1);
    expect(component.modelUpdated.emit).toHaveBeenCalledWith(component.form.value);
  });

  it('should toggle showMenu when toggle is called', () => {
    component.showMenu = false;
    component.toggle();
    expect(component.showMenu).toBeTrue();
    component.toggle();
    expect(component.showMenu).toBeFalse();
  });

  it('should close the menu when close is called', () => {
    component.showMenu = true;
    component.close();
    expect(component.showMenu).toBeFalse();
  });

  it('should clear proficiencies when clearProficiencies is called', () => {
    component.form.patchValue({ spokenLevel: 1, writtenLevel: 2 });
    component.clearProficiencies();
    expect(component.form.value.spokenLevel).toBeNull();
    expect(component.form.value.writtenLevel).toBeNull();
  });

  it('should clear spoken level when clearSpoken is called', () => {
    component.form.patchValue({ spokenLevel: 1 });
    component.clearSpoken();
    expect(component.form.value.spokenLevel).toBeNull();
  });

  it('should clear written level when clearWritten is called', () => {
    component.form.patchValue({ writtenLevel: 1 });
    component.clearWritten();
    expect(component.form.value.writtenLevel).toBeNull();
  });

  it('should disable language input when languageDisabled is true', () => {
    component.languageDisabled = true;
    component.ngOnInit();
    expect(component.form.controls['languageId'].disabled).toBeTrue();
  });

  it('should render level correctly', () => {
    component.languages = mockLanguages;
    component.languageLevels = mockLanguageLevels;
    component.form.patchValue({ languageId: 1, writtenLevel: 2, spokenLevel: 3 });
    const levelString = component.renderLevel();
    expect(levelString).toBe('English (Spoken: Advanced, Written: Intermediate)');
  });
});
