import {ComponentFixture, TestBed, fakeAsync, tick} from '@angular/core/testing';
import {EditCandidateOccupationComponent} from './edit-candidate-occupation.component';
import {NgbActiveModal} from '@ng-bootstrap/ng-bootstrap';
import {CandidateOccupationService} from '../../../../../services/candidate-occupation.service';
import {OccupationService} from '../../../../../services/occupation.service';
import {ReactiveFormsModule} from '@angular/forms';
import {of, throwError} from 'rxjs';
import {CandidateOccupation} from '../../../../../model/candidate-occupation';
import {Occupation} from '../../../../../model/occupation';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {MockUser} from "../../../../../MockData/MockUser";

describe('EditCandidateOccupationComponent', () => {
  let component: EditCandidateOccupationComponent;
  let fixture: ComponentFixture<EditCandidateOccupationComponent>;
  let mockOccupationService: jasmine.SpyObj<OccupationService>;
  let mockCandidateOccupationService: jasmine.SpyObj<CandidateOccupationService>;
  let mockActiveModal: jasmine.SpyObj<NgbActiveModal>;

  const mockOccupations: Occupation[] = [
    {
      id: 1,
      name: 'Software Engineer',
      isco08Code: '2512',
      status: 'ACTIVE'
    },
    {
      id: 2,
      name: 'Data Scientist',
      isco08Code: '2521',
      status: 'ACTIVE'
    }
  ];

  const mockCandidateOccupation: CandidateOccupation = {
    id: 123,
    occupation: {
      id: 1,
      name: 'Software Engineer',
      isco08Code: '2512',
      status: 'ACTIVE'
    },
    yearsExperience: 3,
    updatedDate: 2024,
    createdDate: 2004,
    createdBy: new MockUser(),
    updatedBy: new MockUser(),
    migrationOccupation: null
  };


  beforeEach(async () => {
    mockOccupationService = jasmine.createSpyObj('OccupationService', ['listOccupations']);
    mockCandidateOccupationService = jasmine.createSpyObj('CandidateOccupationService', ['update']);
    mockActiveModal = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);

    await TestBed.configureTestingModule({
      declarations: [EditCandidateOccupationComponent],
      imports: [ReactiveFormsModule],
      providers: [
        {provide: OccupationService, useValue: mockOccupationService},
        {provide: CandidateOccupationService, useValue: mockCandidateOccupationService},
        {provide: NgbActiveModal, useValue: mockActiveModal}
      ],
      schemas: [NO_ERRORS_SCHEMA]  // Ignores unknown elements like <ng-select>
    }).compileComponents();

    fixture = TestBed.createComponent(EditCandidateOccupationComponent);
    component = fixture.componentInstance;
    // Setup mocks
    mockOccupationService.listOccupations.and.returnValue(of([]));
    mockCandidateOccupationService.update.and.returnValue(of(mockCandidateOccupation));

    component.candidateOccupation = mockCandidateOccupation;
  });

  it('should initialize the form with candidate occupation data', fakeAsync(() => {
    mockOccupationService.listOccupations.and.returnValue(of(mockOccupations));

    component.ngOnInit();
    tick();

    expect(component.form.value).toEqual({
      occupationId: 1,
      yearsExperience: 3
    });
    expect(component.occupations.length).toBe(2);
    expect(component.loading).toBeFalse();
  }));

  it('should call update service and close modal on save success', fakeAsync(() => {
    component.candidateOccupation = mockCandidateOccupation;
    component.ngOnInit();

    component.form.setValue({occupationId: 2, yearsExperience: 5});

    component.onSave();
    tick();

    expect(mockCandidateOccupationService.update).toHaveBeenCalledWith(123, {
      occupationId: 2,
      yearsExperience: 5
    });
    expect(mockActiveModal.close).toHaveBeenCalledWith(mockCandidateOccupation);
  }));

  it('should dismiss the modal when dismiss() is called', () => {
    component.dismiss();
    expect(mockActiveModal.dismiss).toHaveBeenCalledWith(false);
  });

});
