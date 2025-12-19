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
import {CandidateReviewStatusItemComponent} from "./candidate-review-status-item.component";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateService} from "../../../services/candidate.service";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {CandidateReviewStatusItem} from "../../../model/candidate-review-status-item";
import {CandidateSource} from "../../../model/base";
import {
  EditCandidateReviewStatusItemComponent
} from "./edit/edit-candidate-review-status-item.component";

describe('CandidateReviewStatusItemComponent', () => {
  let component: CandidateReviewStatusItemComponent;
  let fixture: ComponentFixture<CandidateReviewStatusItemComponent>;
  let modalService: jasmine.SpyObj<NgbModal>;
  let candidateService: jasmine.SpyObj<CandidateService>;

  beforeEach(async () => {
    const modalSpy = jasmine.createSpyObj('NgbModal', ['open']);
    const candidateServiceSpy = jasmine.createSpyObj('CandidateService', ['']);

    await TestBed.configureTestingModule({
      declarations: [CandidateReviewStatusItemComponent],
      providers: [
        { provide: NgbModal, useValue: modalSpy },
        { provide: CandidateService, useValue: candidateServiceSpy }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(CandidateReviewStatusItemComponent);
    component = fixture.componentInstance;
    modalService = TestBed.inject(NgbModal) as jasmine.SpyObj<NgbModal>;
    candidateService = TestBed.inject(CandidateService) as jasmine.SpyObj<CandidateService>;
  });

  beforeEach(() => {
    component.candidateReviewStatusItems = [
      { id: 1, reviewStatus: 'Pending', savedSearch: { id: 1 } },
      { id: 2, reviewStatus: 'Approved', savedSearch: { id: 2 } }
    ] as CandidateReviewStatusItem[];
    component.savedSearch = { id: 1 } as CandidateSource;
    component.candidateId = 123;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should select the correct review status on initialization', () => {
    component.ngOnInit();
    expect(component.candidateReviewStatusItem).toEqual({ id: 1, reviewStatus: 'Pending', savedSearch: { id: 1 } });
  });

  it('should select the correct review status on input change', () => {
    component.ngOnChanges({});
    expect(component.candidateReviewStatusItem).toEqual({ id: 1, reviewStatus: 'Pending', savedSearch: { id: 1 } });
  });

  it('should open edit modal and emit event on editReviewStatusItem call', fakeAsync(() => {
    const mockModalRef = {
      componentInstance: { message: "" },
      result: Promise.resolve({ id: 1, reviewStatus: 'Approved' })
    } as any;
    modalService.open.and.returnValue(mockModalRef);

    spyOn(component.reviewStatusChange, 'emit');
    component.editReviewStatusItem();
    tick();

    expect(modalService.open).toHaveBeenCalledWith(EditCandidateReviewStatusItemComponent, {
      centered: true,
      backdrop: 'static'
    });
    expect(component.reviewStatusChange.emit).toHaveBeenCalledWith({ id: 1, reviewStatus: 'Approved' });
  }));

  it('should handle modal dismiss without error', async () => {

    const mockModalRef = {
      componentInstance: { message: "" },
      result: Promise.reject('dismissed')
    } as any;
    modalService.open.and.returnValue(mockModalRef);

    component.editReviewStatusItem();

    try {
      await mockModalRef.result;
    } catch (error) {
      expect(error).toBe('dismissed');
    }
  });
});
