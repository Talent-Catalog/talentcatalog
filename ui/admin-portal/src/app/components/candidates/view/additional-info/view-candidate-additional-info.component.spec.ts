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
import {ViewCandidateAdditionalInfoComponent} from "./view-candidate-additional-info.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {NgbModal} from "@ng-bootstrap/ng-bootstrap";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {MockCandidate} from "../../../../MockData/MockCandidate";

describe('ViewCandidateAdditionalInfoComponent', () => {
  let component: ViewCandidateAdditionalInfoComponent;
  let fixture: ComponentFixture<ViewCandidateAdditionalInfoComponent>;
  let modalService: jasmine.SpyObj<NgbModal>;
  const mockCandidate = new MockCandidate();
  beforeEach(async () => {
    const modalServiceSpy = jasmine.createSpyObj('NgbModal', ['open']);

    await TestBed.configureTestingModule({
      declarations: [ ViewCandidateAdditionalInfoComponent ],
      imports:[HttpClientTestingModule],
      providers: [{ provide: NgbModal, useValue: modalServiceSpy }]
    })
    .compileComponents();

    modalService = TestBed.inject(NgbModal) as jasmine.SpyObj<NgbModal>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateAdditionalInfoComponent);
    component = fixture.componentInstance;
    component.candidate = mockCandidate;
    component.editable = true; // Set to true or false based on your test case
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should render the candidate additional info', () => {
    const compiled = fixture.nativeElement;
    const additionalInfoElement = compiled.querySelector('.card-body p');
    expect(additionalInfoElement.textContent).toContain('Additional Information about candidate');
  });

  it('should render edit button if editable is true', () => {
    const compiled = fixture.nativeElement;
    const editButton = compiled.querySelector('.card-header button');

    expect(editButton).toBeTruthy();
  });

  it('should not render edit button if editable is false', () => {
    component.editable = false;
    fixture.detectChanges();

    const compiled = fixture.nativeElement;
    const editButton = compiled.querySelector('.card-header button');

    expect(editButton).toBeFalsy();
  });
});
