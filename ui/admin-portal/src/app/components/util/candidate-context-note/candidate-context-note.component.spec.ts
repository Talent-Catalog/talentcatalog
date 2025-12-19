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
import {CandidateContextNoteComponent} from "./candidate-context-note.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {ReactiveFormsModule} from "@angular/forms";
import {CandidateSourceService} from "../../../services/candidate-source.service";
import {MockCandidate} from "../../../MockData/MockCandidate";
import {MockCandidateSource} from "../../../MockData/MockCandidateSource";
import {By} from "@angular/platform-browser";
import {AutosaveStatusComponent} from "../autosave-status/autosave-status.component";
import {of} from "rxjs";
import {AuthorizationService} from "../../../services/authorization.service";

describe('CandidateContextNoteComponent', () => {
  let component: CandidateContextNoteComponent;
  let fixture: ComponentFixture<CandidateContextNoteComponent>;
  let candidateSourceService: jasmine.SpyObj<CandidateSourceService>;

  const mockCandidate = new MockCandidate();
  const mockCandidateSource = new MockCandidateSource();

  beforeEach(async () => {
    const candidateSourceSpy = jasmine.createSpyObj('CandidateSourceService', ['updateContextNote']);
    const authorizationServiceSpy = jasmine.createSpyObj('AuthorizationService', ['canEditCandidateSource']);

    await TestBed.configureTestingModule({
      declarations: [CandidateContextNoteComponent, AutosaveStatusComponent],
      imports: [ReactiveFormsModule],
      providers: [
        { provide: CandidateSourceService, useValue: candidateSourceSpy },
        { provide: AuthorizationService, useValue: authorizationServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CandidateContextNoteComponent);
    component = fixture.componentInstance;
    candidateSourceService = TestBed.inject(CandidateSourceService) as jasmine.SpyObj<CandidateSourceService>;
  });

  beforeEach(() => {
    component.candidate = mockCandidate;
    component.candidateSource = mockCandidateSource;
    candidateSourceService.updateContextNote.and.returnValue(of(null));
    fixture.detectChanges();
  });
  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with candidate context note', () => {
    component.form.controls['contextNote'].setValue('New context note');

    expect(component.form.value.contextNote).toBe('New context note');
  });

  it('should update form value when candidate input changes', () => {
    component.candidate = { ...mockCandidate, contextNote: 'Updated context note' };
    component.ngOnChanges({
      candidate: {
        currentValue: component.candidate,
        previousValue: mockCandidate,
        firstChange: false,
        isFirstChange: () => false
      }
    });
    fixture.detectChanges();
    expect(component.form.value.contextNote).toBe('Updated context note');
  });

  it('should call updateContextNote on save', () => {
    component.form.controls['contextNote'].setValue('New context note');
    component.doSave(component.form.value).subscribe();
    expect(candidateSourceService.updateContextNote).toHaveBeenCalledWith(
      mockCandidateSource, { candidateId: 1, contextNote: 'New context note' }
    );
  });

  it('should update candidate context note on successful save', () => {
    component.form.controls['contextNote'].setValue('New context note');
    component.doSave(component.form.value);
    component.onSuccessfulSave();
    expect(component.candidate.contextNote).toBe('New context note');
  });

  it('should display error message when there is an error', () => {
    component.error = 'An error occurred';
    fixture.detectChanges();
    const errorMsg = fixture.debugElement.query(By.css('.alert-danger')).nativeElement;
    expect(errorMsg.textContent).toContain('An error occurred');
  });
});
