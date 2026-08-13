/*
 * Copyright (c) 2026 Talent Catalog.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License
 * for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */
import {GeneralTranslationsComponent} from "./general-translations.component";
import {TranslationService} from "../../../../services/translation.service";
import {AuthorizationService} from "../../../../services/authorization.service";
import {LanguageService} from "../../../../services/language.service";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {of, throwError} from "rxjs";
import {By} from "@angular/platform-browser";
import {SystemLanguage} from "../../../../model/language";
import {MockUser} from "../../../../MockData/MockUser";

describe('GeneralTranslationsComponent', () => {
  let component: GeneralTranslationsComponent;
  let fixture: ComponentFixture<GeneralTranslationsComponent>;
  let translationService: jasmine.SpyObj<TranslationService>;
  let languageService: jasmine.SpyObj<LanguageService>;
  let authService: jasmine.SpyObj<AuthorizationService>;
  const systemLanguage: SystemLanguage = {id:1, language: 'fr', label: 'French',rtl:false };

  beforeEach(async () => {
    const translationSpy = jasmine.createSpyObj('TranslationService', [
      'loadTranslationsFile',
      'updateTranslationFile',
      'importPatch',
      'exportPatch'
    ]);
    const languageSpy = jasmine.createSpyObj('LanguageService', ['listSystemLanguages']);
    const authSpy = jasmine.createSpyObj('AuthorizationService', ['isAnAdmin', 'isSystemAdminOnly']);

    await TestBed.configureTestingModule({
      imports: [FormsModule,ReactiveFormsModule,NgSelectModule,HttpClientTestingModule],
      declarations: [GeneralTranslationsComponent],
      providers: [
        { provide: TranslationService, useValue: translationSpy },
        { provide: LanguageService, useValue: languageSpy },
        { provide: AuthorizationService, useValue: authSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(GeneralTranslationsComponent);
    component = fixture.componentInstance;
    translationService = TestBed.inject(TranslationService) as jasmine.SpyObj<TranslationService>;
    languageService = TestBed.inject(LanguageService) as jasmine.SpyObj<LanguageService>;
    authService = TestBed.inject(AuthorizationService) as jasmine.SpyObj<AuthorizationService>;

    languageService.listSystemLanguages.and.returnValue(of([systemLanguage]));
    translationService.loadTranslationsFile.and.returnValue(of({}));
    authService.isAnAdmin.and.returnValue(true);
    authService.isSystemAdminOnly.and.returnValue(true);
    component.loggedInUser = new MockUser();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load languages on init', () => {
    expect(languageService.listSystemLanguages).toHaveBeenCalled();
    expect(component.languages).toEqual([systemLanguage]);
    expect(component.loading).toBeFalse();
  });

  it('should set language and load translations', () => {
    component.setLanguage(systemLanguage);
    expect(translationService.loadTranslationsFile).toHaveBeenCalledWith('fr');
    expect(component.fields).toBeDefined();
    expect(component.fieldsFiltered).toEqual(component.fields);
    expect(component.loading).toBeFalse();
  });

  it('should handle error when loading translations', () => {
    translationService.loadTranslationsFile.and.returnValue(throwError('error'));
    component.setLanguage(systemLanguage);
    expect(component.error).toBe('error');
    expect(component.loading).toBeFalse();
  });

  it('should filter items', () => {
    component.fields = [
      { path: 'header.nav.account', value: 'Account' },
      { path: 'header.nav.logout', value: 'Logout' }
    ];
    component.filterItems('header');
    expect(component.fieldsFiltered.length).toBe(2);
    component.filterItems(null);
    expect(component.fieldsFiltered.length).toBe(2);
  });

  it('should save translations', () => {
    component.fields = [
      { path: 'header.nav.account', value: 'Account' },
      { path: 'header.nav.logout', value: 'Logout' }
    ];
    translationService.updateTranslationFile.and.returnValue(of({}));
    component.save();
    expect(translationService.updateTranslationFile).toHaveBeenCalled();
    expect(component.saving).toBeFalse();
  });

  it('should handle error when saving translations', () => {
    component.fields = [
      { path: 'header.nav.account', value: 'Account' },
      { path: 'header.nav.logout', value: 'Logout' }
    ];
    translationService.updateTranslationFile.and.returnValue(throwError('error'));
    component.save();
    expect(component.saveError).toBe('error');
    expect(component.saving).toBeFalse();
  });

  it('should check if user is an admin', () => {
    expect(component.isAnAdmin()).toBeTrue();
    expect(authService.isAnAdmin).toHaveBeenCalled();
  });

  it('should display loading message', () => {
    component.loading = true;
    fixture.detectChanges();
    const loadingElement = fixture.debugElement.query(By.css('.main'));
    expect(loadingElement).toBeFalsy();
  });

  it('should display save button for admin users', () => {
    fixture.detectChanges();
    const saveButton = fixture.debugElement.query(By.css('tc-button#save-translations'));
    expect(saveButton).toBeTruthy();
  });

  it('should display import and export controls for system admins', () => {
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css('tc-button#import-translation-patch'))).toBeTruthy();
    expect(fixture.debugElement.query(By.css('tc-button#toggle-export-translation-patch'))).toBeTruthy();
  });

  it('should disable save button if saving or errors exist', () => {
    let saveButton = fixture.debugElement.query(By.css('tc-button#save-translations'));

    component.saving = true;
    fixture.detectChanges();
    expect(saveButton.componentInstance.disabled).toBeTruthy();

    component.saving = false;
    component.error = 'Some error';
    fixture.detectChanges();
    expect(saveButton.componentInstance.disabled).toBeTruthy();

    component.error = null;
    component.saveError = 'Save error';
    fixture.detectChanges();
    expect(saveButton.componentInstance.disabled).toBeTruthy();
  });

  it('should not display save button for non-admin users', () => {
    authService.isAnAdmin.and.returnValue(false);
    fixture.detectChanges();
    const saveButton = fixture.debugElement.query(By.css('button.btn-success'));
    expect(saveButton).toBeFalsy();
  });

  it('should hide import and export controls for non-system admins', () => {
    authService.isSystemAdminOnly.and.returnValue(false);
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css('tc-button#import-translation-patch'))).toBeFalsy();
    expect(fixture.debugElement.query(By.css('tc-button#toggle-export-translation-patch'))).toBeFalsy();
  });

  it('should hide import and export controls for read-only users', () => {
    component.loggedInUser.readOnly = true;
    fixture.detectChanges();
    expect(fixture.debugElement.query(By.css('tc-button#import-translation-patch'))).toBeFalsy();
    expect(fixture.debugElement.query(By.css('tc-button#toggle-export-translation-patch'))).toBeFalsy();
  });

  it('should call export patch with parsed prefixes and keys', () => {
    component.exportPrefixesText = 'SERVICES.VERIFY_PLUS\nSERVICES.UNHCR';
    component.exportKeysText = 'REGISTRATION.HEADER.TITLE.VERIFYPLUS';
    component.exportLanguages = ['en', 'ar'];
    translationService.exportPatch.and.returnValue(of({version: 1, entries: []}));

    component.exportPatch();

    expect(translationService.exportPatch).toHaveBeenCalledWith({
      prefixes: ['SERVICES.VERIFY_PLUS', 'SERVICES.UNHCR'],
      keys: ['REGISTRATION.HEADER.TITLE.VERIFYPLUS'],
      languages: ['en', 'ar']
    });
  });

  it('should apply spacing class on header action buttons', () => {
    fixture.detectChanges();
    const headerActions = fixture.debugElement.query(By.css('.header > div.d-flex.gap-2'));
    expect(headerActions).toBeTruthy();
  });

  it('should show dry-run review in tab after patch import selection', () => {
    const dryRunSummary = {
      languages: {
        en: {totalKeys: 1, updatedKeys: 1, unchangedKeys: 0}
      },
      warnings: []
    };
    translationService.importPatch.and.returnValue(of(dryRunSummary));

    component.startPatchDryRun({version: 1, entries: []});

    expect(translationService.importPatch).toHaveBeenCalledWith({version: 1, entries: []}, true, false);
    expect(component.patchReview).toEqual(dryRunSummary);
    expect(component.pendingPatch).toEqual({version: 1, entries: []});
    expect(component.patchAppliedSummary).toBeFalsy();
  });

  it('should run dry-run then apply import patch when confirmed', () => {
    const dryRunSummary = {
      languages: {
        en: {totalKeys: 1, updatedKeys: 1, unchangedKeys: 0}
      },
      warnings: []
    };
    const applySummary = {
      languages: {
        en: {totalKeys: 1, updatedKeys: 1, unchangedKeys: 0}
      },
      warnings: []
    };
    translationService.importPatch.and.returnValues(of(dryRunSummary), of(applySummary));
    const refreshSpy = spyOn(component, 'setLanguage').and.callThrough();

    component.startPatchDryRun({version: 1, entries: []});
    component.confirmPatchImport();

    expect(translationService.importPatch).toHaveBeenCalledWith({version: 1, entries: []}, true, false);
    expect(translationService.importPatch).toHaveBeenCalledWith({version: 1, entries: []}, false, false);
    expect(refreshSpy).toHaveBeenCalledWith(systemLanguage);
    expect(component.patchReview).toBeNull();
    expect(component.pendingPatch).toBeNull();
    expect(component.patchAppliedSummary).toEqual(applySummary);
  });

  it('should clear review and not apply when canceling import', () => {
    const dryRunSummary = {
      languages: {
        en: {totalKeys: 1, updatedKeys: 0, unchangedKeys: 1}
      },
      warnings: ['Warning one']
    };
    translationService.importPatch.and.returnValue(of(dryRunSummary));
    component.startPatchDryRun({version: 1, entries: []});

    component.cancelPatchImport();

    expect(translationService.importPatch.calls.count()).toBe(1);
    expect(component.patchReview).toBeNull();
    expect(component.pendingPatch).toBeNull();
    expect(component.patchAppliedSummary).toBeFalsy();
  });
});
