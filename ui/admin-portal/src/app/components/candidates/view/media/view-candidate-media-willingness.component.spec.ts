/*
 * Copyright (c) 2024 Talent Beyond Boundaries.
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
import {NgbModal, NgbModalRef} from "@ng-bootstrap/ng-bootstrap";
import {Candidate} from "../../../../model/candidate";
import {MockCandidate} from "../../../../MockData/MockCandidate";
import {By} from "@angular/platform-browser";
import {
  EditCandidateMediaWillingnessComponent
} from "./edit/edit-candidate-media-willingness.component";

fdescribe('ViewCandidateMediaWillingnessComponent', () => {
  let component: ViewCandidateMediaWillingnessComponent;
  let fixture: ComponentFixture<ViewCandidateMediaWillingnessComponent>;
  let mockModalService: jasmine.SpyObj<NgbModal>;
  let candidate: Candidate;
  const mockCandidate = new MockCandidate();
  beforeEach(async () => {

    mockModalService = jasmine.createSpyObj('NgbModal', ['open']);

    await TestBed.configureTestingModule({
      declarations: [ViewCandidateMediaWillingnessComponent],
      providers: [
        { provide: NgbModal, useValue: mockModalService },
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateMediaWillingnessComponent);
    component = fixture.componentInstance;
    component.candidate = mockCandidate;
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

  it('should open modal on edit button click and update candidate data', () => {
    const mockModalRef = {
      componentInstance: {
        candidateId: null
      },
      result: Promise.resolve(candidate) // Simulate successful modal close with updated candidate
    } as NgbModalRef;

    mockModalService.open.and.returnValue(mockModalRef);

    component.editMediaWillingness();
    expect(mockModalService.open).toHaveBeenCalledWith(EditCandidateMediaWillingnessComponent, {
      centered: true,
      backdrop: 'static'
    });
    expect(mockModalRef.componentInstance.candidateId).toBe(1);

    mockModalRef.result.then(updatedCandidate => {
      expect(component.candidate).toEqual(candidate);
    });
  });
});
