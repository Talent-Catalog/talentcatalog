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
import {ViewCandidateContactComponent} from "./view-candidate-contact.component";
import {CandidateService} from "../../../../services/candidate.service";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {ComponentFixture, TestBed, waitForAsync} from "@angular/core/testing";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {MockCandidate} from "../../../../MockData/MockCandidate";

describe('ViewCandidateContactComponent', () => {
  let component: ViewCandidateContactComponent;
  let fixture: ComponentFixture<ViewCandidateContactComponent>;
  let candidateServiceSpy: jasmine.SpyObj<CandidateService>;
  let modalServiceSpy: jasmine.SpyObj<NgbModal>;

  beforeEach(waitForAsync(() => {
    const candidateServiceSpyObj = jasmine.createSpyObj('CandidateService', ['get']);
    const modalServiceSpyObj = jasmine.createSpyObj('NgbModal', ['open']);

    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule],
      declarations: [ViewCandidateContactComponent],
      providers: [
        { provide: CandidateService, useValue: candidateServiceSpyObj },
        { provide: NgbModal, useValue: modalServiceSpyObj }
      ]
    }).compileComponents();

    candidateServiceSpy = TestBed.inject(CandidateService) as jasmine.SpyObj<CandidateService>;
    modalServiceSpy = TestBed.inject(NgbModal) as jasmine.SpyObj<NgbModal>;
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateContactComponent);
    component = fixture.componentInstance;
    component.editable = true; // Mocking editable input
    component.candidate = new MockCandidate();
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize with correct initial state', () => {
    expect(component.loading).toBeFalsy();
    expect(component.error).toBeUndefined();
  });
});
