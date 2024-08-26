import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';

import {EditCandidateDestinationsComponent} from './edit-candidate-destinations.component';
import {NgbActiveModal} from "@ng-bootstrap/ng-bootstrap";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {of, throwError} from "rxjs";
import {
  CandidateDestinationService
} from "../../../../../../services/candidate-destination.service";
import {MockCandidate} from "../../../../../../MockData/MockCandidate";

fdescribe('EditCandidateDestinationsComponent', () => {
  let component: EditCandidateDestinationsComponent;
  let fixture: ComponentFixture<EditCandidateDestinationsComponent>;
  let candidateDestinationServiceSpy: jasmine.SpyObj<CandidateDestinationService>;
  let activeModalSpy: jasmine.SpyObj<NgbActiveModal>;
  const mockCandidate = new MockCandidate();
  const mockCandidateDestination = mockCandidate.candidateDestinations[0];

  beforeEach(waitForAsync(() => {
    const candidateDestinationSpy = jasmine.createSpyObj('CandidateDestinationService', ['update']);
    const activeModalMock = jasmine.createSpyObj('NgbActiveModal', ['close', 'dismiss']);

    TestBed.configureTestingModule({
      declarations: [EditCandidateDestinationsComponent],
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule, NgSelectModule],
      providers: [
        { provide: NgbActiveModal, useValue: activeModalMock },
        { provide: CandidateDestinationService, useValue: candidateDestinationSpy }
      ]
    }).compileComponents();

    candidateDestinationServiceSpy = TestBed.inject(CandidateDestinationService) as jasmine.SpyObj<CandidateDestinationService>;
    activeModalSpy = TestBed.inject(NgbActiveModal) as jasmine.SpyObj<NgbActiveModal>;
  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(EditCandidateDestinationsComponent);
    component = fixture.componentInstance;
    component.candidateDestination = mockCandidateDestination;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should initialize the form with candidateDestination data', () => {
    expect(component.form).toBeDefined();
    expect(component.form.controls['interest'].value).toBe('Yes');
    expect(component.form.controls['notes'].value).toBe('I like this country.');
  });

  it('form should be valid when filled correctly', () => {
    expect(component.form.valid).toBeTruthy();
  });

  it('should call the update method on the service when form is valid and submitted', () => {
    candidateDestinationServiceSpy.update.and.returnValue(of(mockCandidateDestination));

    component.onSave();

    expect(candidateDestinationServiceSpy.update.calls.count()).toBe(1);
    expect(activeModalSpy.close.calls.count()).toBe(1);
  });

  it('should handle errors on form submission', () => {
    const mockError = 'An error occurred';
    candidateDestinationServiceSpy.update.and.returnValue(throwError(mockError));

    component.onSave();

    expect(component.error).toBe(mockError);
    expect(candidateDestinationServiceSpy.update.calls.count()).toBe(1);
    expect(activeModalSpy.close.calls.count()).toBe(0);
  });

  it('should dismiss the modal without saving changes', () => {
    component.dismiss();
    expect(activeModalSpy.dismiss.calls.count()).toBe(1);
  });
});
