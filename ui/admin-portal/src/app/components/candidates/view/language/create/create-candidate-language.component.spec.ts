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
import {CreateCandidateLanguageComponent} from "./create-candidate-language.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {LanguageService} from "../../../../../services/language.service";
import {CandidateLanguageService} from "../../../../../services/candidate-language.service";
import {LanguageLevel} from "../../../../../model/language-level";
import {of} from "rxjs";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {MockCandidate} from "../../../../../MockData/MockCandidate";

describe('CreateCandidateLanguageComponent', () => {
  let component: CreateCandidateLanguageComponent;
  let fixture: ComponentFixture<CreateCandidateLanguageComponent>;
  let mockLanguageService: jasmine.SpyObj<LanguageService>;
  let mockCandidateLanguageService: jasmine.SpyObj<CandidateLanguageService>;
  let mockActiveModal: jasmine.SpyObj<NgbActiveModal>;

  const mockLanguageLevels: LanguageLevel[] = [
    { id: 1, name: 'Beginner', level: 1, cefrLevel: 'A1', status: 'Active' },
    { id: 2, name: 'Intermediate', level: 2, cefrLevel: 'A2', status: 'Active' },
    { id: 3, name: 'Advanced', level: 3, cefrLevel: 'B2', status: 'Active' },
  ];
  const mockCandidate =  new MockCandidate();
  beforeEach(async () => {
    mockLanguageService = jasmine.createSpyObj('LanguageService', ['listLanguages']);
    mockActiveModal = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);
    mockLanguageService.listLanguages.and.returnValue(of([]));
    mockCandidateLanguageService = jasmine.createSpyObj('CandidateLanguageService', ['create']);
    await TestBed.configureTestingModule({
      declarations: [CreateCandidateLanguageComponent],
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule],
      providers: [
        { provide: NgbActiveModal, useValue:mockActiveModal  },
        { provide: LanguageService, useValue: mockLanguageService },
        { provide: CandidateLanguageService, useValue: mockCandidateLanguageService }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CreateCandidateLanguageComponent);
    component = fixture.componentInstance;
    component.languageLevels = mockLanguageLevels;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with required controls', () => {
    expect(component.candidateForm.get('languageId')).toBeTruthy();
    expect(component.candidateForm.get('spokenLevelId')).toBeTruthy();
    expect(component.candidateForm.get('writtenLevelId')).toBeTruthy();
  });

  it('should display loading spinner initially', () => {
    component.loading = true;
    expect(component.loading).toBeTrue();
  });

  it('should load language levels', () => {
    mockLanguageService.listLanguages.and.returnValue(of(mockLanguageLevels));
    fixture.detectChanges();
    expect(component.languageLevels).toEqual(mockLanguageLevels);
    expect(component.loading).toBeFalse();
  });

  it('should call onSave() and close modal on successful save', () => {
    const candidateLanguage = mockCandidate.candidateLanguages[0]; // Mock data
    mockCandidateLanguageService.create.and.returnValue(of(candidateLanguage));
    component.onSave();
    expect(mockCandidateLanguageService.create).toHaveBeenCalledOnceWith(jasmine.any(Object));
    expect(mockActiveModal.close).toBeTruthy();
  });

  it('should dismiss modal on cancel', () => {
    mockActiveModal.dismiss.and.callThrough();
    component.dismiss();
    expect(mockActiveModal.dismiss).toHaveBeenCalledOnceWith(false);
  });
});
