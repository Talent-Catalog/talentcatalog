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

  it('should trigger hidden import input click and clear applied summary', () => {
    const fileInput = document.createElement('input');
    const clickSpy = spyOn(fileInput, 'click');
    component.patchAppliedSummary = {status: 'success'};

    component.triggerImport(fileInput);

    expect(clickSpy).toHaveBeenCalled();
    expect(component.patchAppliedSummary).toBeNull();
  });

  it('should not trigger import input click when patch is busy', () => {
    const fileInput = document.createElement('input');
    const clickSpy = spyOn(fileInput, 'click');
    component.patchBusy = true;

    component.triggerImport(fileInput);

    expect(clickSpy).not.toHaveBeenCalled();
  });

  it('should not trigger import input click for non-system admins', () => {
    const fileInput = document.createElement('input');
    const clickSpy = spyOn(fileInput, 'click');
    authService.isSystemAdminOnly.and.returnValue(false);

    component.triggerImport(fileInput);

    expect(clickSpy).not.toHaveBeenCalled();
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

  it('should handle dry-run import error', () => {
    translationService.importPatch.and.returnValue(throwError('dry-run error'));
    component.startPatchDryRun({version: 1, entries: []});

    expect(component.patchBusy).toBeFalse();
    expect(component.patchError).toBe('dry-run error');
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

  it('should not call apply import when no pending patch', () => {
    component.pendingPatch = null;

    component.confirmPatchImport();

    expect(translationService.importPatch.calls.count()).toBe(0);
  });

  it('should handle apply import error', () => {
    component.pendingPatch = {version: 1, entries: []};
    component.patchReview = {languages: {}};
    translationService.importPatch.and.returnValue(throwError('apply error'));

    component.confirmPatchImport();

    expect(component.patchBusy).toBeFalse();
    expect(component.patchError).toBe('apply error');
    expect(component.pendingPatch).toEqual({version: 1, entries: []});
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

  it('should ignore file selection when no files are provided', () => {
    const startDryRunSpy = spyOn(component, 'startPatchDryRun');
    const event = {
      target: { files: [] }
    } as unknown as Event;

    component.onPatchFileSelected(event);

    expect(startDryRunSpy).not.toHaveBeenCalled();
  });

  it('should parse selected patch file and start dry-run', () => {
    const startDryRunSpy = spyOn(component, 'startPatchDryRun');
    const readerMock = {
      result: '{"version":1,"entries":[]}',
      onload: null as any,
      onerror: null as any,
      readAsText: function() {
        this.onload();
      }
    };
    spyOn(window as any, 'FileReader').and.returnValue(readerMock as unknown as FileReader);
    const file = new File(['{"version":1}'], 'patch.json', {type: 'application/json'});
    const input = document.createElement('input');
    Object.defineProperty(input, 'files', { value: [file] });

    component.onPatchFileSelected({ target: input } as unknown as Event);

    expect(startDryRunSpy).toHaveBeenCalledWith({version: 1, entries: []});
  });

  it('should set patchError when selected patch file is invalid JSON', () => {
    const readerMock = {
      result: 'not-json',
      onload: null as any,
      onerror: null as any,
      readAsText: function() {
        this.onload();
      }
    };
    spyOn(window as any, 'FileReader').and.returnValue(readerMock as unknown as FileReader);
    const file = new File(['not-json'], 'patch.json', {type: 'application/json'});
    const input = document.createElement('input');
    Object.defineProperty(input, 'files', { value: [file] });

    component.onPatchFileSelected({ target: input } as unknown as Event);

    expect(component.patchError).toBeTruthy();
  });

  it('should toggle export form and initialize languages when empty', () => {
    component.showExportForm = false;
    component.exportLanguages = [];
    component.languages = [{id:1, language: 'en', label: 'English', rtl:false} as SystemLanguage];

    component.toggleExportForm();

    expect(component.showExportForm).toBeTrue();
    expect(component.exportLanguages).toEqual(['en']);
  });

  it('should set export error when no prefixes or keys are provided', () => {
    component.exportPrefixesText = '';
    component.exportKeysText = '';
    component.exportLanguages = ['en'];

    component.exportPatch();

    expect(component.patchError).toBe('Specify at least one prefix or key before export.');
    expect(translationService.exportPatch).not.toHaveBeenCalled();
  });

  it('should set export error when no languages are provided', () => {
    component.exportPrefixesText = 'SERVICES.VERIFY_PLUS';
    component.exportKeysText = '';
    component.exportLanguages = [];

    component.exportPatch();

    expect(component.patchError).toBe('Select at least one language before export.');
    expect(translationService.exportPatch).not.toHaveBeenCalled();
  });

  it('should handle export patch request errors', () => {
    component.exportPrefixesText = 'SERVICES.VERIFY_PLUS';
    component.exportLanguages = ['en'];
    translationService.exportPatch.and.returnValue(throwError('export error'));

    component.exportPatch();

    expect(component.patchBusy).toBeFalse();
    expect(component.patchError).toBe('export error');
  });

  it('should download exported patch file', () => {
    component.exportPrefixesText = 'SERVICES.VERIFY_PLUS';
    component.exportLanguages = ['en'];
    translationService.exportPatch.and.returnValue(of({version: 1, entries: []}));
    const createSpy = spyOn(window.URL, 'createObjectURL').and.returnValue('blob:test');
    const revokeSpy = spyOn(window.URL, 'revokeObjectURL');
    const clickSpy = jasmine.createSpy('click');
    spyOn(document, 'createElement').and.returnValue({
      click: clickSpy
    } as unknown as HTMLAnchorElement);

    component.exportPatch();

    expect(createSpy).toHaveBeenCalled();
    expect(clickSpy).toHaveBeenCalled();
    expect(revokeSpy).toHaveBeenCalledWith('blob:test');
  });

  it('should return language rows and warning state helpers', () => {
    expect(component.getPatchLanguageRows(null)).toEqual([]);
    expect(component.hasPatchWarnings({warnings: []})).toBeFalse();
    expect(component.hasPatchWarnings({warnings: ['warning']})).toBeTrue();
  });
});
