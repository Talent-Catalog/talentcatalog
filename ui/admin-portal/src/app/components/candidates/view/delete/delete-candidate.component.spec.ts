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

import {DeleteCandidateComponent} from "./delete-candidate.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateService} from "../../../../services/candidate.service";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {MockCandidate} from "../../../../MockData/MockCandidate";

import {CandidatePipe} from "../../../../pipes/candidate.pipe";
import {of} from "rxjs";

describe('DeleteCandidateComponent', () => {
  let component: DeleteCandidateComponent;
  let fixture: ComponentFixture<DeleteCandidateComponent>;
  let mockActiveModal: jasmine.SpyObj<NgbActiveModal>;
  let mockCandidateService: jasmine.SpyObj<CandidateService>;

  const mockCandidate =  new MockCandidate();
  beforeEach(async () => {
    const activeModalSpy = jasmine.createSpyObj('NgbActiveModal', ['dismiss', 'close']);
    const candidateServiceSpy = jasmine.createSpyObj('CandidateService', ['delete']);

    await TestBed.configureTestingModule({
      declarations: [DeleteCandidateComponent,CandidatePipe],
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule],
      providers: [
        { provide: NgbActiveModal, useValue: activeModalSpy },
        { provide: CandidateService, useValue: candidateServiceSpy }
      ]
    })
    .compileComponents();

    mockActiveModal = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;
    mockCandidateService = TestBed.inject(CandidateService) as jasmine.SpyObj<CandidateService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(DeleteCandidateComponent);
    component = fixture.componentInstance;
    component.candidate = mockCandidate;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should dismiss the modal when cancel is called', () => {
    component.cancel();
    expect(mockActiveModal.dismiss).toHaveBeenCalled();
  });

  it('should set deleting to false and close the modal when confirm is called', () => {
    component.candidate = mockCandidate;
    mockCandidateService.delete.and.returnValue(of(true));
    component.confirm();
    expect(component.deleting).toBeFalse();
    expect(mockCandidateService.delete).toHaveBeenCalledWith(mockCandidate.id);
    expect(mockActiveModal.close).toHaveBeenCalled();
  });
});

