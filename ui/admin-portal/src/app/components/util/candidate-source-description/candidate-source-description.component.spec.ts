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

import {CandidateSourceDescriptionComponent} from "./candidate-source-description.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {CandidateService} from "../../../services/candidate.service";
import {CandidateSourceService} from "../../../services/candidate-source.service";
import {UntypedFormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {CandidateSource} from "../../../model/base";
import {of, throwError} from "rxjs";
import {AutosaveStatusComponent} from "../autosave-status/autosave-status.component";
import {AuthorizationService} from "../../../services/authorization.service";

describe('CandidateSourceDescriptionComponent', () => {
  let component: CandidateSourceDescriptionComponent;
  let fixture: ComponentFixture<CandidateSourceDescriptionComponent>;
  let mockCandidateService: jasmine.SpyObj<CandidateService>;
  let mockCandidateSourceService: jasmine.SpyObj<CandidateSourceService>;
  let mockAuthorizationService: jasmine.SpyObj<AuthorizationService>;

  beforeEach(async () => {
    mockCandidateService = jasmine.createSpyObj('CandidateService', ['']);
    mockCandidateSourceService = jasmine.createSpyObj('CandidateSourceService', ['updateDescription']);
    mockAuthorizationService = jasmine.createSpyObj('AuthorizationService', ['canEditCandidateSource']);

    await TestBed.configureTestingModule({
      declarations: [CandidateSourceDescriptionComponent,AutosaveStatusComponent],
      imports: [ReactiveFormsModule, FormsModule],
      providers: [
        UntypedFormBuilder,
        { provide: CandidateService, useValue: mockCandidateService },
        { provide: CandidateSourceService, useValue: mockCandidateSourceService },
        { provide: AuthorizationService, useValue: mockAuthorizationService }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(CandidateSourceDescriptionComponent);
    component = fixture.componentInstance;
    component.candidateSource = { description: 'Initial Description' } as CandidateSource;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form with candidateSource description', () => {
    expect(component.form.value.description).toBe('Initial Description');
  });

  it('should update form value when candidateSource input changes', () => {
    component.candidateSource = { description: 'Updated Description' } as CandidateSource;
    component.ngOnChanges({});
    expect(component.form.controls['description'].value).toBe('Updated Description');
  });

  it('should save description successfully', () => {
    const newDescription = 'New Description';
    component.form.controls['description'].patchValue(newDescription);
    mockCandidateSourceService.updateDescription.and.returnValue(of());

    component.doSave(component.form.value).subscribe(() => {
      expect(component.candidateSource.description).toBe(newDescription);
    });

    expect(mockCandidateSourceService.updateDescription).toHaveBeenCalledWith(component.candidateSource, { description: newDescription });
  });

  it('should handle save error', () => {
    const newDescription = 'New Description';
    component.form.controls['description'].setValue(newDescription);
    mockCandidateSourceService.updateDescription.and.returnValue(throwError('Error'));

    component.doSave(component.form.value).subscribe(
      () => fail('expected an error'),
      error => expect(error).toBe('Error')
    );

    expect(mockCandidateSourceService.updateDescription).toHaveBeenCalledWith(component.candidateSource, { description: newDescription });
  });
});
