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
import {of, throwError} from "rxjs";
import {EditCandidateLanguageComponent} from "./edit/edit-candidate-language.component";
import {CreateCandidateLanguageComponent} from "./create/create-candidate-language.component";
import {ConfirmationComponent} from "../../../util/confirm/confirmation.component";
import {ViewCandidateLanguageComponent} from "./view-candidate-language.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {CandidateLanguageService} from "../../../../services/candidate-language.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateService} from "../../../../services/candidate.service";
import {MockCandidate} from "../../../../MockData/MockCandidate";
import {CandidateLanguage} from "../../../../model/candidate-language";
import {HttpClientTestingModule} from "@angular/common/http/testing";

describe('ViewCandidateLanguageComponent', () => {
  let component: ViewCandidateLanguageComponent;
  let fixture: ComponentFixture<ViewCandidateLanguageComponent>;
  let mockCandidateLanguageService: jasmine.SpyObj<CandidateLanguageService>;
  let mockModalService: jasmine.SpyObj<NgbModal>;
  let mockCandidateService: jasmine.SpyObj<CandidateService>;

  const mockCandidate = new MockCandidate();
  const mockCandidateLanguages: CandidateLanguage[] = mockCandidate.candidateLanguages;

  beforeEach(async () => {
    const candidateLanguageServiceSpy = jasmine.createSpyObj('CandidateLanguageService', ['list', 'delete']);
    const candidateServiceSpy = jasmine.createSpyObj('CandidateService', ['updateCandidate']);
    const modalServiceSpy = jasmine.createSpyObj('NgbModal', ['open']);

    await TestBed.configureTestingModule({
      declarations: [ViewCandidateLanguageComponent],
      imports: [HttpClientTestingModule],
      providers: [
        { provide: CandidateLanguageService, useValue: candidateLanguageServiceSpy },
        { provide: CandidateService, useValue: candidateServiceSpy },
        { provide: NgbModal, useValue: modalServiceSpy }
      ]
    }).compileComponents();

    mockCandidateLanguageService = TestBed.inject(CandidateLanguageService) as jasmine.SpyObj<CandidateLanguageService>;
    mockModalService = TestBed.inject(NgbModal) as jasmine.SpyObj<NgbModal>;
    mockCandidateService = TestBed.inject(CandidateService) as jasmine.SpyObj<CandidateService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateLanguageComponent);
    component = fixture.componentInstance;
    component.candidate = mockCandidate;
    component.candidateLanguages = mockCandidateLanguages;
    component.loading = true;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should display TC loading indicator initially', () => {
    expect(fixture.nativeElement.querySelector('tc-loading')).toBeTruthy();
  });

  it('should run ngOnInit', () => {
    expect(() => component.ngOnInit()).not.toThrow();
  });

  it('should open edit language modal and refresh candidate after success', fakeAsync(() => {
    const language = mockCandidateLanguages[0];

    const modalRef = {
      componentInstance: {},
      result: Promise.resolve(language)
    } as any;

    mockModalService.open.and.returnValue(modalRef);

    component.editCandidateLanguage(language);
    tick();

    expect(mockModalService.open).toHaveBeenCalledWith(
      EditCandidateLanguageComponent,
      {
        centered: true,
        backdrop: 'static'
      }
    );

    expect(modalRef.componentInstance.candidateLanguage)
    .toBe(language);

    expect(mockCandidateService.updateCandidate)
    .toHaveBeenCalledTimes(1);
  }));

  it('should ignore edit language modal dismissal', fakeAsync(() => {
    const language = mockCandidateLanguages[0];

    const modalRef = {
      componentInstance: {},
      result: Promise.reject('dismissed')
    } as any;

    mockModalService.open.and.returnValue(modalRef);

    component.editCandidateLanguage(language);
    tick();

    expect(modalRef.componentInstance.candidateLanguage)
    .toBe(language);

    expect(mockCandidateService.updateCandidate)
    .not.toHaveBeenCalled();
  }));

  it('should open create language modal and refresh candidate after success', fakeAsync(() => {
    const modalRef = {
      componentInstance: {},
      result: Promise.resolve({})
    } as any;

    mockModalService.open.and.returnValue(modalRef);

    component.createCandidateLanguage();
    tick();

    expect(mockModalService.open).toHaveBeenCalledWith(
      CreateCandidateLanguageComponent,
      {
        centered: true,
        backdrop: 'static'
      }
    );

    expect(modalRef.componentInstance.candidateId)
    .toBe(component.candidate.id);

    expect(mockCandidateService.updateCandidate)
    .toHaveBeenCalledTimes(1);
  }));

  it('should ignore create language modal dismissal', fakeAsync(() => {
    const modalRef = {
      componentInstance: {},
      result: Promise.reject('dismissed')
    } as any;

    mockModalService.open.and.returnValue(modalRef);

    component.createCandidateLanguage();
    tick();

    expect(modalRef.componentInstance.candidateId)
    .toBe(component.candidate.id);

    expect(mockCandidateService.updateCandidate)
    .not.toHaveBeenCalled();
  }));

  it('should delete language after confirmation and refresh candidate', fakeAsync(() => {
    const language = mockCandidateLanguages[0];

    const modalRef = {
      componentInstance: {},
      result: Promise.resolve(true)
    } as any;

    mockModalService.open.and.returnValue(modalRef);
    mockCandidateLanguageService.delete.and.returnValue(
      of({} as any)
    );

    component.loading = true;

    component.deleteCandidateLanguage(language);
    tick();

    expect(mockModalService.open).toHaveBeenCalledWith(
      ConfirmationComponent,
      {
        centered: true,
        backdrop: 'static'
      }
    );

    expect(modalRef.componentInstance.message)
    .toBe("Are you sure you want to delete this candidate's language?");

    expect(mockCandidateLanguageService.delete)
    .toHaveBeenCalledWith(language.id);

    expect(component.loading).toBeFalse();

    expect(mockCandidateService.updateCandidate)
    .toHaveBeenCalledTimes(1);
  }));

  it('should expose delete error and clear loading', fakeAsync(() => {
    const language = mockCandidateLanguages[0];
    const error = new Error('delete failed');

    const modalRef = {
      componentInstance: {},
      result: Promise.resolve(true)
    } as any;

    mockModalService.open.and.returnValue(modalRef);
    mockCandidateLanguageService.delete.and.returnValue(
      throwError(error)
    );

    component.loading = true;

    component.deleteCandidateLanguage(language);
    tick();

    expect(component.error).toBe(error);
    expect(component.loading).toBeFalse();

    expect(mockCandidateService.updateCandidate)
    .not.toHaveBeenCalled();
  }));

  it('should not delete language when confirmation is false', fakeAsync(() => {
    const language = mockCandidateLanguages[0];

    mockModalService.open.and.returnValue({
      componentInstance: {},
      result: Promise.resolve(false)
    } as any);

    component.deleteCandidateLanguage(language);
    tick();

    expect(mockCandidateLanguageService.delete)
    .not.toHaveBeenCalled();

    expect(mockCandidateService.updateCandidate)
    .not.toHaveBeenCalled();
  }));

  it('should ignore delete language modal dismissal', fakeAsync(() => {
    const language = mockCandidateLanguages[0];

    mockModalService.open.and.returnValue({
      componentInstance: {},
      result: Promise.reject('dismissed')
    } as any);

    component.deleteCandidateLanguage(language);
    tick();

    expect(mockCandidateLanguageService.delete)
    .not.toHaveBeenCalled();

    expect(mockCandidateService.updateCandidate)
    .not.toHaveBeenCalled();
  }));

  it('should return true when candidate has an IELTS General exam', () => {
    const candidateWithIelts = {
      ...mockCandidate,
      candidateExams: [
        {
          id: 1,
          exam: 'IELTSGen',
          score: '7.5'
        }
      ]
    } as any;

    expect(component.hasIelts(candidateWithIelts)).toBeTrue();
  });

  it('should return false when candidate has no IELTS General exam', () => {
    const candidateWithoutIelts = {
      ...mockCandidate,
      candidateExams: [
        {
          id: 1,
          exam: 'IELTSAca',
          score: '7.5'
        }
      ]
    } as any;

    expect(component.hasIelts(candidateWithoutIelts)).toBeFalse();
  });

  it('should return false when candidate has no IELTS General exam', () => {
    const candidateWithoutIelts = {
      ...mockCandidate,
      candidateExams: [
        {
          id: 1,
          exam: 'IELTSAca',
          score: '7.5'
        }
      ]
    } as any;

    expect(component.hasIelts(candidateWithoutIelts)).toBeFalse();
  });
  
  it('should return false when candidate has no IELTS exam data', () => {
    const candidateWithoutIelts = {
      ...mockCandidate,
      ieltsScore: null,
      englishAssessmentScoreIelts: null
    } as any;

    expect(component.hasIelts(candidateWithoutIelts)).toBeFalse();
  });

});
