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

import {Component, forwardRef, Input, NO_ERRORS_SCHEMA, SimpleChange} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {ControlValueAccessor, FormsModule, NG_VALUE_ACCESSOR} from '@angular/forms';
import {TranslateModule, TranslateService} from '@ngx-translate/core';

import {CandidateLanguageCardComponent} from './candidate-language-card.component';
import {CandidateLanguage} from '../../../model/candidate-language';
import {Language} from '../../../model/language';
import {LanguageLevel} from '../../../model/language-level';

@Component({
  selector: 'ng-select',
  template: '',
  providers: [{
    provide: NG_VALUE_ACCESSOR,
    useExisting: forwardRef(() => NgSelectStubComponent),
    multi: true
  }]
})
class NgSelectStubComponent implements ControlValueAccessor {
  @Input() items?: unknown[];
  @Input() clearable?: boolean;
  @Input() bindValue?: string;
  @Input() bindLabel?: string;
  @Input() disabled?: boolean | string;

  writeValue(): void {}
  registerOnChange(): void {}
  registerOnTouched(): void {}
}

function makeLanguage(id: number, name: string): Language {
  return {id, name};
}

function makeLanguageLevel(id: number, name: string): LanguageLevel {
  return {id, name, level: id};
}

function makeCandidateLanguage(overrides: Partial<CandidateLanguage> = {}): CandidateLanguage {
  return {
    id: 1,
    language: makeLanguage(2, 'Arabic'),
    languageId: 2,
    spokenLevel: makeLanguageLevel(10, 'Advanced'),
    spokenLevelId: 10,
    writtenLevel: makeLanguageLevel(20, 'Intermediate'),
    writtenLevelId: 20,
    ...overrides
  };
}

describe('CandidateLanguageCardComponent', () => {
  let component: CandidateLanguageCardComponent;
  let fixture: ComponentFixture<CandidateLanguageCardComponent>;

  async function configureAndCreate(options?: {
    preview?: boolean;
    language?: CandidateLanguage;
    english?: Language;
    languages?: Language[];
    languageLevels?: LanguageLevel[];
  }) {
    await TestBed.configureTestingModule({
      declarations: [CandidateLanguageCardComponent, NgSelectStubComponent],
      imports: [FormsModule, TranslateModule.forRoot()],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(CandidateLanguageCardComponent);
    component = fixture.componentInstance;

    component.preview = options?.preview ?? false;
    component.language = options?.language ?? makeCandidateLanguage();
    component.english = options?.english ?? makeLanguage(1, 'English');
    component.languages = options?.languages ?? [
      makeLanguage(1, 'English'),
      makeLanguage(2, 'Arabic')
    ];
    component.languageLevels = options?.languageLevels ?? [
      makeLanguageLevel(10, 'Advanced'),
      makeLanguageLevel(20, 'Intermediate')
    ];

    const translateService = TestBed.inject(TranslateService);
    translateService.use('en');

    fixture.detectChanges();
  }

  afterEach(() => TestBed.resetTestingModule());

  it('should create', async () => {
    await configureAndCreate();
    expect(component).toBeTruthy();
  });

  describe('template', () => {
    it('should render a tc-button delete action when not in preview and not English', async () => {
      await configureAndCreate();
      const buttons = (fixture.nativeElement as HTMLElement).querySelectorAll('tc-button');

      expect(buttons.length).toBe(1);
    });

    it('should not render the delete action in preview mode', async () => {
      await configureAndCreate({preview: true});
      const buttons = (fixture.nativeElement as HTMLElement).querySelectorAll('tc-button');

      expect(buttons.length).toBe(0);
    });

    it('should not render the delete action for English', async () => {
      await configureAndCreate({
        language: makeCandidateLanguage({
          language: makeLanguage(1, 'English'),
          languageId: 1
        })
      });
      const buttons = (fixture.nativeElement as HTMLElement).querySelectorAll('tc-button');

      expect(buttons.length).toBe(0);
    });

    it('should render tc-label elements and tc-select classes in edit mode', async () => {
      await configureAndCreate();
      const nativeElement = fixture.nativeElement as HTMLElement;
      const selects = nativeElement.querySelectorAll('ng-select.tc-select');

      expect(nativeElement.querySelectorAll('tc-label').length).toBe(3);
      expect(selects.length).toBe(3);
    });

    it('should render preview values instead of selects in preview mode', async () => {
      await configureAndCreate({preview: true});
      component.ngOnChanges({
        language: new SimpleChange(null, component.language, true),
        languages: new SimpleChange(null, component.languages, true)
      });
      fixture.detectChanges();

      const text = (fixture.nativeElement as HTMLElement).textContent || '';

      expect(component.translatedLanguageName).toBe('Arabic');
      // These level names come from the default makeCandidateLanguage() and languageLevels fixture data.
      expect(text).toContain('Advanced');
      expect(text).toContain('Intermediate');
    });
  });

  describe('events', () => {
    beforeEach(async () => configureAndCreate());

    it('should emit onDelete when delete is called', () => {
      const onDeleteSpy = spyOn(component.onDelete, 'emit');

      component.delete();

      expect(onDeleteSpy).toHaveBeenCalled();
    });
  });

  describe('helpers', () => {
    beforeEach(async () => configureAndCreate());

    it('should update translatedLanguageName on changes', () => {
      component.ngOnChanges({
        language: new SimpleChange(null, component.language, true)
      });

      expect(component.translatedLanguageName).toBe('Arabic');
    });

    it('should return the matching language name from language.id', () => {
      expect(component.lookupLanguageName(makeCandidateLanguage())).toBe('Arabic');
    });

    it('should return the matching language name from languageId', () => {
      expect(component.lookupLanguageName(makeCandidateLanguage({
        language: undefined,
        languageId: 2
      }))).toBe('Arabic');
    });

    it('should return an empty string when the language is not found', () => {
      expect(component.lookupLanguageName(makeCandidateLanguage({
        language: undefined,
        languageId: 999
      }))).toBe('');
    });

    it('should return the matching language level name', () => {
      expect(component.getLangLevel(makeLanguageLevel(10, 'Ignored'))).toBe('Advanced');
    });

    it('should return undefined when the level is not found', () => {
      expect(component.getLangLevel(makeLanguageLevel(999, 'Missing'))).toBeUndefined();
    });

    it('should return true when the language is English', () => {
      expect(component.isEnglish(1)).toBeTrue();
    });

    it('should return false when the language is not English', () => {
      expect(component.isEnglish(2)).toBeFalse();
    });
  });
});
