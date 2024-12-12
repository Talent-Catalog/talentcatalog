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
import {EditCandidateLanguageComponent} from "./edit-candidate-language.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {LanguageService} from "../../../../../services/language.service";
import {LanguageLevelService} from "../../../../../services/language-level.service";
import {CandidateLanguageService} from "../../../../../services/candidate-language.service";
import {CandidateLanguage} from "../../../../../model/candidate-language";
import {MockCandidate} from "../../../../../MockData/MockCandidate";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormsModule, ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {of, throwError} from "rxjs";
import {LanguageLevel} from "../../../../../model/language-level";
import {Language} from "../../../../../model/language";

describe('EditCandidateLanguageComponent', () => {
  let component: EditCandidateLanguageComponent;
  let fixture: ComponentFixture<EditCandidateLanguageComponent>;
  let mockActiveModal: jasmine.SpyObj<NgbActiveModal>;
  let mockLanguageService: jasmine.SpyObj<LanguageService>;
  let mockLanguageLevelService: jasmine.SpyObj<LanguageLevelService>;
  let mockCandidateLanguageService: jasmine.SpyObj<CandidateLanguageService>;
  const mockCandidate = new MockCandidate();

  const mockLanguages: Language[] = [
    { id: 1, name: 'English' , status: 'active'},
    { id: 2, name: 'French' ,  status: 'active' },
    // Add more languages as needed
  ];
  const mockLanguageLevels: LanguageLevel[] = [
    { id: 1, name: 'Beginner', level: 1, status: 'Active' },
    { id: 2, name: 'Intermediate', level: 2, status: 'Active' },
    { id: 3, name: 'Advanced', level: 3, status: 'Active' },
    // Add more language levels as needed
  ];

  const mockCandidateLanguages = mockCandidate.candidateLanguages;
  beforeEach(async () => {
    mockActiveModal = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);
    mockLanguageService = jasmine.createSpyObj('LanguageService', ['listLanguages']);
    mockLanguageLevelService = jasmine.createSpyObj('LanguageLevelService', ['listLanguageLevels']);
    mockCandidateLanguageService = jasmine.createSpyObj('CandidateLanguageService', ['update']);

    await TestBed.configureTestingModule({
      declarations: [EditCandidateLanguageComponent],
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule],
      providers: [
        { provide: NgbActiveModal, useValue: mockActiveModal },
        { provide: LanguageService, useValue: mockLanguageService },
        { provide: LanguageLevelService, useValue: mockLanguageLevelService },
        { provide: CandidateLanguageService, useValue: mockCandidateLanguageService },
        UntypedFormBuilder
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EditCandidateLanguageComponent);
    component = fixture.componentInstance;
    mockLanguageService.listLanguages.and.returnValue(of(mockLanguages));
    mockLanguageLevelService.listLanguageLevels.and.returnValue(of(mockLanguageLevels));
    component.candidateLanguage = mockCandidateLanguages[0];
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with correct values', () => {
    expect(component.candidateForm.get('languageId').value).toEqual(mockCandidateLanguages[0].language.id);
    expect(component.candidateForm.get('spokenLevelId').value).toEqual(mockCandidateLanguages[0].spokenLevel.id);
    expect(component.candidateForm.get('writtenLevelId').value).toEqual(mockCandidateLanguages[0].writtenLevel.id);
  });

  it('should load languages and language levels on initialization', fakeAsync(() => {
    mockLanguageService.listLanguages.and.returnValue(of(mockLanguages));
    mockLanguageLevelService.listLanguageLevels.and.returnValue(of(mockLanguageLevels));

    fixture.detectChanges();
    tick();

    expect(component.languages).toEqual(mockLanguages);
    expect(component.languageLevels).toEqual(mockLanguageLevels);
  }));

  it('should handle error when loading languages', fakeAsync(() => {
    mockLanguageService.listLanguages.and.returnValue(throwError('Error loading languages'));

    component.ngOnInit();
    tick();

    expect(component.error).toEqual('Error loading languages');
    expect(component.loading).toBeFalse();
  }));

  it('should handle error when loading language levels', fakeAsync(() => {
    mockLanguageLevelService.listLanguageLevels.and.returnValue(throwError('Error loading language levels'));

    component.ngOnInit();
    tick();

    expect(component.error).toEqual('Error loading language levels');
    expect(component.loading).toBeFalse();
  }));

  it('should call candidate language service update on save', fakeAsync(() => {
    const updatedCandidateLanguage: CandidateLanguage = mockCandidateLanguages[0];
    mockCandidateLanguageService.update.and.returnValue(of(updatedCandidateLanguage));

    component.candidateForm.patchValue({
      languageId: updatedCandidateLanguage.language.id,
      spokenLevelId: updatedCandidateLanguage.spokenLevel.id,
      writtenLevelId: updatedCandidateLanguage.writtenLevel.id
    });

    component.onSave();
    tick();

    expect(mockCandidateLanguageService.update).toHaveBeenCalled();
    expect(mockActiveModal.close).toHaveBeenCalledWith(updatedCandidateLanguage);
  }));

  it('should handle error when updating candidate language', fakeAsync(() => {
    mockCandidateLanguageService.update.and.returnValue(throwError('Error updating candidate language'));

    component.onSave();
    tick();

    expect(component.error).toEqual('Error updating candidate language');
    expect(mockActiveModal.close).not.toHaveBeenCalled();
  }));

  it('should close modal when dismissed', () => {
    component.dismiss();
    expect(mockActiveModal.dismiss).toHaveBeenCalledWith(false);
  });
});
