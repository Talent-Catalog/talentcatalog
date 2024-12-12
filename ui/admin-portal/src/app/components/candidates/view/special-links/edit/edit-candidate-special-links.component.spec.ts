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
import {EditCandidateSpecialLinksComponent} from "./edit-candidate-special-links.component";
import {CandidateService} from "../../../../../services/candidate.service";
import {EnvService} from "../../../../../services/env.service";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {UntypedFormBuilder, FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {MockCandidate} from "../../../../../MockData/MockCandidate";
import {Candidate} from "../../../../../model/candidate";
import {of, throwError} from "rxjs";

describe('EditCandidateSpecialLinksComponent', () => {
  let component: EditCandidateSpecialLinksComponent;
  let fixture: ComponentFixture<EditCandidateSpecialLinksComponent>;
  let candidateService: jasmine.SpyObj<CandidateService>;
  let envService: jasmine.SpyObj<EnvService>;
  let activeModal: jasmine.SpyObj<NgbActiveModal>;

  const mockCandidate = new MockCandidate();
  beforeEach(async () => {
    const candidateServiceSpy = jasmine.createSpyObj('CandidateService', ['get', 'updateLinks']);
    const envServiceSpy = jasmine.createSpyObj('EnvService', [], { sfLightningUrl: 'https://salesforce.com' });
    const activeModalSpy = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);

    await TestBed.configureTestingModule({
      declarations: [EditCandidateSpecialLinksComponent],
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule],
      providers: [
        UntypedFormBuilder,
        { provide: CandidateService, useValue: candidateServiceSpy },
        { provide: EnvService, useValue: envServiceSpy },
        { provide: NgbActiveModal, useValue: activeModalSpy }
      ]
    }).compileComponents();

    candidateService = TestBed.inject(CandidateService) as jasmine.SpyObj<CandidateService>;
    envService = TestBed.inject(EnvService) as jasmine.SpyObj<EnvService>;
    activeModal = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EditCandidateSpecialLinksComponent);
    component = fixture.componentInstance;
    component.candidateId = 1;
    candidateService.get.and.returnValue(of(mockCandidate));
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the form with the correct candidate data', fakeAsync(() => {
    const candidate: Candidate = mockCandidate;

    candidateService.get.and.returnValue(of(candidate));

    component.ngOnInit();
    tick(); // simulate async

    expect(component.candidateForm).toBeDefined();
    expect(component.candidateForm.value).toEqual({
      linkedInLink: candidate.linkedInLink,
      folderlink: candidate.folderlink,
      sflink: candidate.sflink,
      videolink: candidate.videolink
    });
    expect(component.loading).toBe(false);
  }));



  it('should call updateLinks when onSave is called', fakeAsync(() => {
    const candidate: Candidate = mockCandidate;

    candidateService.get.and.returnValue(of(candidate));
    candidateService.updateLinks.and.returnValue(of(candidate));

    component.ngOnInit();
    tick(); // simulate async

    component.onSave();

    tick(); // simulate async
    expect(candidateService.updateLinks).toHaveBeenCalledWith(component.candidateId, component.candidateForm.value);
    expect(component.saving).toBe(false);
    expect(activeModal.close).toHaveBeenCalledWith(candidate);
  }));

  it('should display an error message if updating links fails', fakeAsync(() => {
    const candidate: Candidate = mockCandidate;
    const errorResponse = 'Error updating links';

    candidateService.get.and.returnValue(of(candidate));
    candidateService.updateLinks.and.returnValue(throwError(errorResponse));

    component.ngOnInit();
    tick(); // simulate async

    component.onSave();

    tick(); // simulate async
    expect(candidateService.updateLinks).toHaveBeenCalledWith(component.candidateId, component.candidateForm.value);
    expect(component.saving).toBe(false);
    expect(component.error).toBe(errorResponse);
  }));

  it('should dismiss the modal when dismiss is called', () => {
    component.dismiss();
    expect(activeModal.dismiss).toHaveBeenCalledWith(false);
  });
});
