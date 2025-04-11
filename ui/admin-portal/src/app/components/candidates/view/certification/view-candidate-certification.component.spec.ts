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

import {ViewCandidateCertificationComponent} from "./view-candidate-certification.component";
import {CandidateCertificationService} from "../../../../services/candidate-certification.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {ComponentFixture, TestBed, waitForAsync} from "@angular/core/testing";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {CandidateCertification} from "../../../../model/candidate-certification";
import {of} from "rxjs";

describe('ViewCandidateCertificationComponent', () => {
  let component: ViewCandidateCertificationComponent;
  let fixture: ComponentFixture<ViewCandidateCertificationComponent>;
  let candidateCertificationServiceSpy: jasmine.SpyObj<CandidateCertificationService>;
  let modalServiceSpy: jasmine.SpyObj<NgbModal>;
  const mockCertifications: CandidateCertification[] = [
    { id: 1, name: 'Certification 1', institution: 'Institution 1', dateCompleted: '2024-05-29' },
    { id: 2, name: 'Certification 2', institution: 'Institution 2', dateCompleted: '2024-05-30' }
  ];
  beforeEach(waitForAsync(() => {
    const candidateCertificationSpy = jasmine.createSpyObj('CandidateCertificationService', ['list', 'delete']);
    const modalSpy = jasmine.createSpyObj('NgbModal', ['open']);

    TestBed.configureTestingModule({
      declarations: [ViewCandidateCertificationComponent],
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule],
      providers: [
        { provide: CandidateCertificationService, useValue: candidateCertificationSpy },
        { provide: NgbModal, useValue: modalSpy }
      ]
    }).compileComponents();

    candidateCertificationServiceSpy = TestBed.inject(CandidateCertificationService) as jasmine.SpyObj<CandidateCertificationService>;
    modalServiceSpy = TestBed.inject(NgbModal) as jasmine.SpyObj<NgbModal>;
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateCertificationComponent);
    component = fixture.componentInstance;
    component.candidateCertifications = mockCertifications;
    component.candidate = { id: 1 } as any; // Mocking candidate object
    component.editable = true; // Mocking editable input
    component.adminUser = true; // Mocking adminUser input
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize without errors', () => {
    expect(component.loading).toBeFalsy();
    expect(component.error).toBeUndefined();
  });

  it('should load certifications for the given candidate', () => {
    candidateCertificationServiceSpy.list.and.returnValue(of(mockCertifications));

    component.ngOnChanges({});
    expect(component.loading).toBeFalsy();
    expect(component.error).toBeUndefined();
    expect(component.candidateCertifications).toEqual(mockCertifications);
  });
});
