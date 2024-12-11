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
import {CandidateOccupationModel, TailoredCvComponent} from "./tailored-cv.component";
import {ComponentFixture, fakeAsync, TestBed, tick} from "@angular/core/testing";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {CandidateOccupationService} from "../../../services/candidate-occupation.service";
import {CandidateService} from "../../../services/candidate.service";
import {of} from "rxjs";
describe('TailoredCvComponent', () => {
  let component: TailoredCvComponent;
  let fixture: ComponentFixture<TailoredCvComponent>;
  let mockActiveModal: Partial<NgbActiveModal>;
  let mockCandidateOccupationService: Partial<CandidateOccupationService>;
  let mockCandidateService: Partial<CandidateService>;

  beforeEach(async () => {
    mockActiveModal = {
      dismiss: jasmine.createSpy('dismiss')
    };

    mockCandidateOccupationService = {
      get: jasmine.createSpy('get').and.returnValue(of([
        { id: 1, occupation: 'Software Engineer', yearsExperience: 5 },
        { id: 2, occupation: 'Data Analyst', yearsExperience: 3 }
      ]))
    };

    mockCandidateService = {
      generateToken: jasmine.createSpy('generateToken').and.returnValue(of('token123'))
    };

    await TestBed.configureTestingModule({
      declarations: [ TailoredCvComponent ],
      providers: [
        { provide: NgbActiveModal, useValue: mockActiveModal },
        { provide: CandidateOccupationService, useValue: mockCandidateOccupationService },
        { provide: CandidateService, useValue: mockCandidateService }
      ]
    })
    .compileComponents();
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(TailoredCvComponent);
    component = fixture.componentInstance;
    component.candidateId = 1;
    component.candidateNumber = 123;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should dismiss modal when dismissed', () => {
    component.dismiss();
    expect(mockActiveModal.dismiss).toHaveBeenCalled();
  });

  it('should select all candidate occupations when (de)select all is clicked', () => {
    component.selectAll(false);
    expect(component.candidateOccupations.every(occupation => !occupation.selected)).toBeTrue();
    component.selectAll(true);
    expect(component.candidateOccupations.every(occupation => occupation.selected)).toBeTrue();
  });

  it('should update public CV link when selecting/deselecting individual occupation', () => {
    component.candidateOccupations = [
      new CandidateOccupationModel(1, 'Software Engineer', 5, true),
      new CandidateOccupationModel(2, 'Data Analyst', 3, false)
    ];
    component.updateLink();
    expect(component.publicCvLink).toBe('http://localhost/public-portal/cv/token123');

    component.selectItem(true, 1); // Select Data Analyst
    expect(component.publicCvLink).toBe('http://localhost/public-portal/cv/token123');
  });
});
