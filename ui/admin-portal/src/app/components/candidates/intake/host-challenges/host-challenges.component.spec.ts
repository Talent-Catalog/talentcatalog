import {ComponentFixture, TestBed} from '@angular/core/testing';
import {FormBuilder, FormsModule, ReactiveFormsModule} from '@angular/forms';
import {of} from 'rxjs';

import {HostChallengesComponent} from './host-challenges.component';
import {CandidateService} from '../../../../services/candidate.service';
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgSelectModule} from "@ng-select/ng-select";
import {AutosaveStatusComponent} from "../../../util/autosave-status/autosave-status.component";
import {MockCandidate} from "../../../../MockData/MockCandidate";

fdescribe('HostChallengesComponent', () => {
  let component: HostChallengesComponent;
  let fixture: ComponentFixture<HostChallengesComponent>;
  let mockCandidateService: jasmine.SpyObj<CandidateService>;
  let formBuilder: FormBuilder;

  beforeEach(async () => {
    // Create a mock CandidateService
    mockCandidateService = jasmine.createSpyObj('CandidateService', ['getIntakeData']);

    // Provide the mock data for candidateIntakeData
    const mockCandidateData = {
      hostChallenges: 'Language barrier and cultural differences'
    };
    mockCandidateService.getIntakeData.and.returnValue(of(mockCandidateData));

    await TestBed.configureTestingModule({
      declarations: [HostChallengesComponent, AutosaveStatusComponent],
      imports: [HttpClientTestingModule,NgSelectModule,FormsModule,ReactiveFormsModule],
      providers: [
        FormBuilder,
        { provide: CandidateService, useValue: mockCandidateService }
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(HostChallengesComponent);
    component = fixture.componentInstance;
    formBuilder = TestBed.inject(FormBuilder);

    // Mock data for candidateIntakeData
    component.candidateIntakeData = mockCandidateData;
  });
  it('should initialize the form with the correct data', () => {
    // Mock the candidate object
    const mockCandidate = new MockCandidate();
    // Set the mock candidate object to be returned by the CandidateService
    mockCandidateService.getIntakeData.and.returnValue(of(mockCandidate));

    // Initialize the form
    component.ngOnInit();

    // Check if the form is initialized correctly
    const hostChallengesControl = component.form.get('hostChallenges');
    expect(hostChallengesControl).toBeTruthy();
    expect(hostChallengesControl.value).toBe('Language barrier and cultural differences');
  });
});
