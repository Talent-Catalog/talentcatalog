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
import {ViewCandidateAdditionalInfoComponent} from "./view-candidate-additional-info.component";
import {NgbModal, NgbModalRef} from "@ng-bootstrap/ng-bootstrap";
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {CandidateService} from '../../../../services/candidate.service';
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {
  EditCandidateAdditionalInfoComponent
} from "./edit/edit-candidate-additional-info.component";

describe('ViewCandidateAdditionalInfoComponent', () => {
  let component: ViewCandidateAdditionalInfoComponent;
  let fixture: ComponentFixture<ViewCandidateAdditionalInfoComponent>;

  let modalService: jasmine.SpyObj<NgbModal>;
  let candidateService: jasmine.SpyObj<CandidateService>;

  const candidate = {
    id: 123,
    additionalInfo: 'Additional candidate information'
  } as any;

  function modalRef(result: Promise<any>): NgbModalRef {
    return {
      componentInstance: {},
      result,
      close: jasmine.createSpy('close'),
      dismiss: jasmine.createSpy('dismiss')
    } as unknown as NgbModalRef;
  }

  beforeEach(async () => {
    modalService = jasmine.createSpyObj<NgbModal>(
      'NgbModal',
      ['open']
    );

    candidateService = jasmine.createSpyObj<CandidateService>(
      'CandidateService',
      ['updateCandidate']
    );

    await TestBed.configureTestingModule({
      declarations: [ViewCandidateAdditionalInfoComponent],
      providers: [
        {
          provide: NgbModal,
          useValue: modalService
        },
        {
          provide: CandidateService,
          useValue: candidateService
        }
      ],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(
      ViewCandidateAdditionalInfoComponent
    );
    component = fixture.componentInstance;
    component.candidate = candidate;
    component.editable = true;

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should run ngOnInit', () => {
    expect(() => component.ngOnInit()).not.toThrow();
  });

  it('should render the card header', () => {
    const cardHeader =
      fixture.nativeElement.querySelector('tc-card-header');

    expect(cardHeader).toBeTruthy();
    expect(cardHeader.textContent)
    .toContain('Anything else we should know?');
  });

  it('should render the edit button when editable is true', () => {
    component.editable = true;
    fixture.detectChanges();

    const editButton =
      fixture.nativeElement.querySelector(
        'tc-card-header tc-button'
      );

    expect(editButton).toBeTruthy();
  });

  it('should not render the edit button when editable is false', () => {
    component.editable = false;
    fixture.detectChanges();

    const editButton =
      fixture.nativeElement.querySelector(
        'tc-card-header tc-button'
      );

    expect(editButton).toBeNull();
  });

  it('should display candidate additional information', () => {
    const description: HTMLElement =
      fixture.nativeElement.querySelector('tc-description-item');

    expect(description).toBeTruthy();
    expect(description.innerHTML)
    .toContain(candidate.additionalInfo);
  });

  it('should open the edit modal and refresh the candidate after success', fakeAsync(() => {
    const ref = modalRef(Promise.resolve(candidate));
    modalService.open.and.returnValue(ref);

    component.editAdditionalInfo();
    tick();

    expect(modalService.open).toHaveBeenCalledWith(
      EditCandidateAdditionalInfoComponent,
      {
        centered: true,
        backdrop: 'static'
      }
    );

    expect(ref.componentInstance.candidateId)
    .toBe(candidate.id);

    expect(candidateService.updateCandidate)
    .toHaveBeenCalledTimes(1);
  }));

  it('should ignore modal dismissal', fakeAsync(() => {
    const ref = modalRef(Promise.reject('dismissed'));
    modalService.open.and.returnValue(ref);

    component.editAdditionalInfo();
    tick();

    expect(modalService.open).toHaveBeenCalledWith(
      EditCandidateAdditionalInfoComponent,
      {
        centered: true,
        backdrop: 'static'
      }
    );

    expect(ref.componentInstance.candidateId)
    .toBe(candidate.id);

    expect(candidateService.updateCandidate)
    .not.toHaveBeenCalled();
  }));
});
