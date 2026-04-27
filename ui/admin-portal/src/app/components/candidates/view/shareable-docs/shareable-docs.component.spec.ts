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
import {ShareableDocsComponent} from "./shareable-docs.component";
import {CandidateService} from "../../../../services/candidate.service";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {ReactiveFormsModule, UntypedFormBuilder} from "@angular/forms";
import {NgbTooltipModule} from "@ng-bootstrap/ng-bootstrap";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {MockCandidate} from "../../../../MockData/MockCandidate";
import {Candidate} from "../../../../model/candidate";
import {CandidateSource} from "../../../../model/base";
import {CandidateAttachment} from "../../../../model/candidate-attachment";
import {of} from "rxjs";

describe('ShareableDocsComponent', () => {
  let component: ShareableDocsComponent;
  let fixture: ComponentFixture<ShareableDocsComponent>;
  let mockCandidateService: jasmine.SpyObj<CandidateService>;

  beforeEach(async () => {
    mockCandidateService = jasmine.createSpyObj('CandidateService', ['updateShareableDocs', 'updateCandidate']);

    await TestBed.configureTestingModule({
      declarations: [ShareableDocsComponent],
      imports: [ReactiveFormsModule, NgbTooltipModule, HttpClientTestingModule],
      providers: [
        UntypedFormBuilder,
        { provide: CandidateService, useValue: mockCandidateService }
      ]
    }).compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ShareableDocsComponent);
    component = fixture.componentInstance;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize form correctly', () => {
    const candidate: Candidate = new MockCandidate();

    // @ts-expect-error
    const candidateSource: CandidateSource = { id: 1, name: 'source' };

    component.candidate = candidate;
    component.candidateSource = candidateSource;
    component.ngOnInit();

    expect(component.form.value).toEqual({
      shareableCvAttachmentId: 3,
      shareableDocAttachmentId: 4
    });
  });

  it('should update candidate shareable docs on save', () => {
    const updatedCandidate: Candidate = new MockCandidate();

    const updatedCv: CandidateAttachment = updatedCandidate.candidateAttachments[0];

    mockCandidateService.updateShareableDocs.and.returnValue(of(updatedCandidate));
    const emitSpy = component.candidateChange.emit as jasmine.Spy;
    component.candidate = updatedCandidate;
    component.form.setValue({ shareableCvAttachmentId: 2, shareableDocAttachmentId: 3 });

    component.doSave(component.form.value);

    expect(mockCandidateService.updateShareableDocs).toHaveBeenCalledWith(1, component.form.value);
    expect(mockCandidateService.updateCandidate).toHaveBeenCalledWith(updatedCandidate);
    expect(component.saving).toBe(false);
  });
});
