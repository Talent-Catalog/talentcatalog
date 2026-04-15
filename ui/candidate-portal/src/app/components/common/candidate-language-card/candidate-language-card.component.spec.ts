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

import {Component, Input, SimpleChange, forwardRef} from '@angular/core';
import {ComponentFixture, TestBed} from '@angular/core/testing';
import {By} from '@angular/platform-browser';
import {ControlValueAccessor, FormsModule, NG_VALUE_ACCESSOR} from '@angular/forms';
import {TranslateModule} from '@ngx-translate/core';

import {CandidateLanguageCardComponent} from './candidate-language-card.component';
import {CandidateLanguage} from '../../../model/candidate-language';
import {Language} from '../../../model/language';
import {LanguageLevel} from '../../../model/language-level';

@Component({
  selector: 'tc-button',
  template: '<ng-content></ng-content>'
})
class TcButtonStubComponent {
  @Input() color?: string;
}

@Component({
  selector: 'tc-label',
  template: '<ng-content></ng-content>'
})
class TcLabelStubComponent {}

@Component({
  selector: 'tc-description-list',
  template: '<ng-content></ng-content>'
})
class TcDescriptionListStubComponent {
  @Input() direction?: string;
  @Input() compact?: boolean;
  @Input() size?: string;
}

@Component({
  selector: 'tc-description-item',
  template: '<ng-content></ng-content>'
})
class TcDescriptionItemStubComponent {
  @Input() label?: string;
}

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
  @Input() bindValue?: string;
  @Input() bindLabel?: string;
  @Input() clearable?: boolean;
  @Input() disabled?: boolean | string;
  writeValue(): void {}
  registerOnChange(): void {}
  registerOnTouched(): void {}
}

function makeLanguage(id: number, name: string): Language {
  return {id, name};
}

function makeLevel(id: number, name: string): LanguageLevel {
  return {id, name, level: id};
}

function makeCandidateLanguage(overrides: Partial<CandidateLanguage> = {}): CandidateLanguage {
  return {
    id: 1,
    language: makeLanguage(2, 'Arabic'),
    languageId: 2,
    spokenLevel: makeLevel(1, 'Advanced'),
    spokenLevelId: 1,
    writtenLevel: makeLevel(2, 'Intermediate'),
    writtenLevelId: 2,
    ...overrides
  };
}

describe('CandidateLanguageCardComponent', () => {
  let component: CandidateLanguageCardComponent;
  let fixture: ComponentFixture<CandidateLanguageCardComponent>;

  async function configureAndCreate(options?: {
    preview?: boolean;
    language?: CandidateLanguage;
    languages?: Language[];
    languageLevels?: LanguageLevel[];
    english?: Language;
  }) {
    await TestBed.configureTestingModule({
      declarations: [
        CandidateLanguageCardComponent,
        TcButtonStubComponent,
        TcLabelStubComponent,
        TcDescriptionListStubComponent,
        TcDescriptionItemStubComponent,
        NgSelectStubComponent
      ],
      imports: [FormsModule, TranslateModule.forRoot()]
    }).compileComponents();

    fixture = TestBed.createComponent(CandidateLanguageCardComponent);
    component = fixture.componentInstance;
    component.preview = options?.preview ?? false;
    component.language = options?.language ?? makeCandidateLanguage();
    component.languages = options?.languages ?? [makeLanguage(1, 'English'), makeLanguage(2, 'Arabic')];
    component.languageLevels = options?.languageLevels ?? [makeLevel(1, 'Advanced'), makeLevel(2, 'Intermediate')];
    component.english = options?.english ?? makeLanguage(1, 'English');

    fixture.detectChanges();
  }

  afterEach(() => TestBed.resetTestingModule());

  it('should create', async () => {
    await configureAndCreate();
    expect(component).toBeTruthy();
  });

  describe('template', () => {
    it('should render tc-label and ng-select.tc-select controls in edit mode', async () => {
      await configureAndCreate();

      const labels = fixture.debugElement.queryAll(By.directive(TcLabelStubComponent));
      const selects = fixture.debugElement.queryAll(By.directive(NgSelectStubComponent));
      const button = fixture.debugElement.query(By.directive(TcButtonStubComponent));

      expect(labels.length).toBeGreaterThan(0);
      expect(selects.length).toBe(3);
      selects.forEach(debugEl => expect(debugEl.nativeElement.classList).toContain('tc-select'));
      expect(button.componentInstance.color).toBe('error');
    });

    it('should render preview values inside description lists in preview mode', async () => {
      await configureAndCreate({preview: true});

      component.ngOnChanges({
        language: new SimpleChange(null, component.language, true),
        languages: new SimpleChange(null, component.languages, true)
      });
      fixture.detectChanges();

      const items = fixture.debugElement.queryAll(By.directive(TcDescriptionItemStubComponent));
      const labels = items.map(debugEl => debugEl.componentInstance.label);
      const text = (fixture.nativeElement as HTMLElement).textContent || '';

      expect(labels).toContain('REGISTRATION.LANGUAGE.LABEL.LANGUAGE');
      expect(text).toContain('Arabic');
      expect(text).toContain('Advanced');
      expect(text).toContain('Intermediate');
    });

    it('should not render the delete action for English', async () => {
      await configureAndCreate({
        language: makeCandidateLanguage({
          language: makeLanguage(1, 'English'),
          languageId: 1
        })
      });

      expect(fixture.debugElement.query(By.directive(TcButtonStubComponent))).toBeNull();
    });
  });

  describe('helpers', () => {
    beforeEach(async () => configureAndCreate());

    it('should update translatedLanguageName on changes', () => {
      component.ngOnChanges({
        language: new SimpleChange(null, component.language, true),
        languages: new SimpleChange(null, component.languages, true)
      });

      expect(component.translatedLanguageName).toBe('Arabic');
    });

    it('should return the matching language name and level', () => {
      expect(component.lookupLanguageName(component.language)).toBe('Arabic');
      expect(component.getLangLevel(makeLevel(1, 'Ignored'))).toBe('Advanced');
    });

    it('should return false when the language is not English', () => {
      expect(component.isEnglish(2)).toBeFalse();
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
});
