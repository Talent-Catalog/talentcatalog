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

import {ComponentFixture, TestBed} from '@angular/core/testing';
import {DuplicatesDetailComponent} from './duplicates-detail.component';
import {CandidateService} from "../../../../services/candidate.service";
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {MockCandidate} from "../../../../MockData/MockCandidate";
import {of} from "rxjs";
import {By} from "@angular/platform-browser";


describe('DuplicatesDetailComponent', () => {
  let component: DuplicatesDetailComponent;
  let fixture: ComponentFixture<DuplicatesDetailComponent>;
  let candidateServiceSpy: jasmine.SpyObj<CandidateService>;
  let activeModalSpy: jasmine.SpyObj<NgbActiveModal>;
  const mockCandidate = new MockCandidate();
  const mockDuplicate = new MockCandidate();
  mockDuplicate.id = 2;
  const mockDuplicate2 = new MockCandidate();
  mockDuplicate.id = 3;
  const mockDuplicates = [mockDuplicate, mockDuplicate2];


  beforeEach(async () => {
    const candidateService =
      jasmine.createSpyObj('CandidateService', ['fetchPotentialDuplicates']);
    const activeModal =
      jasmine.createSpyObj('NgbActiveModal', ['close']);

    await TestBed.configureTestingModule({
      declarations: [DuplicatesDetailComponent],
      providers: [
        {provide: CandidateService, useValue: candidateService},
        {provide: NgbActiveModal, useValue: activeModal}
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(DuplicatesDetailComponent);
    component = fixture.componentInstance;
    candidateServiceSpy = TestBed.inject(CandidateService) as jasmine.SpyObj<CandidateService>;
    activeModalSpy = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;

    // ngOnInit
    component.selectedCandidate = mockCandidate;
    candidateServiceSpy.fetchPotentialDuplicates.and.returnValue(of([mockDuplicate, mockDuplicate2]));

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it(
    'should fetch potential duplicates on ngOnInit and set property with result',
    () => {
    expect(candidateServiceSpy.fetchPotentialDuplicates).toHaveBeenCalled();
    expect(component.potentialDuplicates).toEqual(mockDuplicates);
  });

  it('should call fetchPotentialDuplicates when refresh button clicked',
    () => {
    spyOn(component, 'refresh')
    const refreshButton = fixture.debugElement.nativeElement.querySelector('.modal-footer .btn-primary');
    refreshButton.click;
    expect(candidateServiceSpy.fetchPotentialDuplicates).toHaveBeenCalled();
    })

  it('should close the modal', () => {
    component.closeModal();
    expect(activeModalSpy.close).toHaveBeenCalled();
  });

  it('should display different text when no duplicates are returned', () => {
    component.potentialDuplicates = [];
    fixture.detectChanges();

    const paragraphs = fixture.debugElement.queryAll(By.css('p'));
    const noDuplicatesElement = paragraphs[1];

    // Assert: Check if the text content matches
    expect(noDuplicatesElement).toBeTruthy(); // Ensure the element exists
    expect(noDuplicatesElement.nativeElement.innerText).toContain(
      'Candidate data have been updated and there are currently no suspected'
    );
  })

  it('should show error message if error is present', () => {
    component.error = 'Test error message';
    fixture.detectChanges();
    const errorMessage = fixture.debugElement.nativeElement.querySelector('.alert-danger');
    expect(errorMessage).toBeTruthy();
    expect(errorMessage.textContent).toContain('Test error message');
  });

  it('should call close modal when cancel button is clicked', () => {
    spyOn(component, 'closeModal');
    const crossButton = fixture.debugElement.nativeElement.querySelector('.btn-close');
    crossButton.click();
    expect(component.closeModal).toHaveBeenCalled();
  });

});
