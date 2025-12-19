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

import {EditCandidateStatusComponent} from "./edit-candidate-status.component";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {CandidateStatus, UpdateCandidateStatusInfo} from "../../../../model/candidate";
import {
  CandidateStatusSelectorComponent
} from "../../../util/candidate-status-selector/candidate-status-selector.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {NgxWigModule} from "ngx-wig";

describe('EditCandidateStatusComponent', () => {
  let component: EditCandidateStatusComponent;
  let fixture: ComponentFixture<EditCandidateStatusComponent>;
  let activeModal: jasmine.SpyObj<NgbActiveModal>;

  beforeEach(async () => {
    const activeModalSpy = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);

    await TestBed.configureTestingModule({
      declarations: [
        EditCandidateStatusComponent,
        CandidateStatusSelectorComponent
      ],
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule,NgxWigModule],
      providers: [
        { provide: NgbActiveModal, useValue: activeModalSpy }
      ]
    }).compileComponents();

    activeModal = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(EditCandidateStatusComponent);
    component = fixture.componentInstance;
    component.candidateStatus = CandidateStatus.active;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with default candidate status if none is provided', () => {
    expect(component.getInitialStatus()).toBe(CandidateStatus.active);
  });

  it('should initialize with provided candidate status', () => {
    component.candidateStatus = CandidateStatus.pending;
    expect(component.getInitialStatus()).toBe(CandidateStatus.pending);
  });

  it('should close the modal with status info on save', () => {
    const statusInfo: UpdateCandidateStatusInfo = { status: CandidateStatus.active };
    component.onStatusInfoUpdate(statusInfo);

    component.onSave();
    expect(activeModal.close).toHaveBeenCalledWith(statusInfo);
  });

  it('should dismiss the modal on cancel', () => {
    component.cancel();
    expect(activeModal.dismiss).toHaveBeenCalled();
  });

  it('should update candidateStatusInfo when onStatusInfoUpdate is called', () => {
    const statusInfo: UpdateCandidateStatusInfo = { status: CandidateStatus.active };
    component.onStatusInfoUpdate(statusInfo);
    expect(component['candidateStatusInfo']).toBe(statusInfo);
  });
});
