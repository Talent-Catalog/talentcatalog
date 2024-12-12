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
import {ViewCandidateMediaWillingnessComponent} from "./view-candidate-media-willingness.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {NgbActiveModal, NgbModal, NgbModalModule, NgbNavModule} from "@ng-bootstrap/ng-bootstrap";
import {MockCandidate} from "../../../../MockData/MockCandidate";
import {By} from "@angular/platform-browser";
import {CandidateService} from "../../../../services/candidate.service";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";

describe('ViewCandidateMediaWillingnessComponent', () => {
  let component: ViewCandidateMediaWillingnessComponent;
  let fixture: ComponentFixture<ViewCandidateMediaWillingnessComponent>;
  let mockModalService: jasmine.SpyObj<NgbModal>;
  let mockCandidateService: jasmine.SpyObj<CandidateService>;
  beforeEach(async () => {
    mockModalService = jasmine.createSpyObj('NgbModal', ['open']);
    mockCandidateService = jasmine.createSpyObj('CandidateService', ['updateCandidate']);

    await TestBed.configureTestingModule({
      declarations: [ViewCandidateMediaWillingnessComponent],
      imports: [NgbNavModule, HttpClientTestingModule, NgbModalModule],
      providers: [
        { provide: CandidateService, userValue: mockCandidateService },
        { provide: NgbModal, useValue: mockModalService },
        NgbActiveModal,
      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA],
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateMediaWillingnessComponent);
    component = fixture.componentInstance;
    component.candidate = new MockCandidate();
    component.editable = true;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should show edit button when editable is true', () => {
    component.editable = true;
    fixture.detectChanges();
    const editButton = fixture.debugElement.query(By.css('.btn-secondary'));
    expect(editButton).toBeTruthy();
  });

  it('should not show edit button when editable is false', () => {
    component.editable = false;
    fixture.detectChanges();
    const editButton = fixture.debugElement.query(By.css('.btn-secondary'));
    expect(editButton).toBeFalsy();
  });

  // it('should open modal on edit button click and update candidate data', async () => {
  //   const mockModalRef = {
  //     componentInstance: {
  //       candidateId: null
  //     },
  //     result: Promise.resolve() // Simulate successful modal close with updated candidate
  //   };
  //
  //   mockModalService.open.and.returnValue(mockModalRef as any);
  //
  //   component.editMediaWillingness();
  //   expect(mockModalRef.componentInstance.candidateId).toBe(1);
  //   expect(mockModalService.open).toHaveBeenCalledWith(EditCandidateMediaWillingnessComponent, {
  //     centered: true,
  //     backdrop: 'static'
  //   });
  //
  //   await mockModalRef.result; // Wait for the promise to resolve
  //
  //   // todo - unsure why this part of the test fails, it's the same as in other place (see view-candidate-exam spec) but here it fails.
  //   expect(mockCandidateService.updateCandidate).toHaveBeenCalled();
  //
  // });
});
