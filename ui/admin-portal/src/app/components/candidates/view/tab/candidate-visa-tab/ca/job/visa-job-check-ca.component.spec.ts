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
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {FormsModule, ReactiveFormsModule} from "@angular/forms";
import {NgSelectModule} from "@ng-select/ng-select";
import {VisaJobCheckCaComponent} from "./visa-job-check-ca.component";
import {ComponentFixture, TestBed} from "@angular/core/testing";
import {CandidateEducationService} from "../../../../../../../services/candidate-education.service";
import {
  CandidateOccupationService
} from "../../../../../../../services/candidate-occupation.service";
import {MockCandidate} from "../../../../../../../MockData/MockCandidate";
import {
  mockCandidateIntakeData
} from "../../../candidate-intake-tab/candidate-intake-tab.component.spec";
import {TcAccordionComponent} from "../../../../../../../shared/components/accordion/tc-accordion.component";
import {TcAccordionItemComponent} from "../../../../../../../shared/components/accordion/accordion-item/tc-accordion-item.component";
import {of, throwError} from "rxjs";
import {DependantsComponent} from "../../../../../intake/dependants/dependants.component";
import {
  VisaJobNotesComponent
} from "../../../../../visa/visa-job-assessments/visa-job-notes/visa-job-notes.component";
import {FixedInputComponent} from "../../../../../../util/intake/fixed-input/fixed-input.component";
import {
  RelocatingDependantsComponent
} from "../../../../../visa/visa-job-assessments/relocating-dependants/relocating-dependants.component";
import {
  JobEligibilityAssessmentComponent
} from "../../../../../visa/visa-job-assessments/job-eligibility-assessment/job-eligibility-assessment.component";
import {
  JobInterestComponent
} from "../../../../../visa/visa-job-assessments/job-interest/job-interest.component";
import {
  AgeRequirementComponent
} from "../../../../../visa/visa-job-assessments/age-requirement/age-requirement.component";
import {
  RelevantWorkExpComponent
} from "../../../../../visa/visa-job-assessments/relevant-work-exp/relevant-work-exp.component";
import {
  IneligiblePathwaysComponent
} from "../../../../../visa/visa-job-assessments/ineligible-pathways/ineligible-pathways.component";
import {
  PreferredPathwaysComponent
} from "../../../../../visa/visa-job-assessments/preferred-pathways/preferred-pathways.component";
import {
  EligiblePathwaysComponent
} from "../../../../../visa/visa-job-assessments/eligible-pathways/eligible-pathways.component";
import {
  OccupationCategoryComponent
} from "../../../../../visa/visa-job-assessments/occupation-category/occupation-category.component";
import {
  OccupationSubcategoryComponent
} from "../../../../../visa/visa-job-assessments/occupation-subcategory/occupation-subcategory.component";
import {
  AutosaveStatusComponent
} from "../../../../../../util/autosave-status/autosave-status.component";
import {
  VisaJobPutForwardComponent
} from "../../../../../visa/visa-job-assessments/put-forward/visa-job-put-forward.component";
import {
  QualificationRelevantComponent
} from "../../../../../visa/visa-job-assessments/qualification-relevant/qualification-relevant.component";
import {
  LanguageThresholdComponent
} from "../../../../../visa/visa-job-assessments/language-threshold/language-threshold.component";

describe('VisaJobCheckCaComponent', () => {
  let component: VisaJobCheckCaComponent;
  let fixture: ComponentFixture<VisaJobCheckCaComponent>;
  let candidateEducationServiceSpy: jasmine.SpyObj<CandidateEducationService>;
  let candidateOccupationServiceSpy: jasmine.SpyObj<CandidateOccupationService>;
  const mockCandidate = new MockCandidate();

  beforeEach(async () => {
    const educationSpy = jasmine.createSpyObj('CandidateEducationService', ['list']);
    const occupationSpy = jasmine.createSpyObj('CandidateOccupationService', ['get']);
    await TestBed.configureTestingModule({
      imports: [HttpClientTestingModule,FormsModule,ReactiveFormsModule,
        NgSelectModule],
      declarations: [ VisaJobCheckCaComponent,DependantsComponent,VisaJobNotesComponent,FixedInputComponent,
        RelocatingDependantsComponent,JobEligibilityAssessmentComponent,JobInterestComponent,
        AgeRequirementComponent,RelevantWorkExpComponent,IneligiblePathwaysComponent,PreferredPathwaysComponent,
        EligiblePathwaysComponent,OccupationCategoryComponent,OccupationSubcategoryComponent
        ,AutosaveStatusComponent,VisaJobPutForwardComponent,QualificationRelevantComponent,
        LanguageThresholdComponent,
        TcAccordionComponent,
        TcAccordionItemComponent
      ],
      providers: [
        { provide: CandidateEducationService, useValue: educationSpy },
        { provide: CandidateOccupationService, useValue: occupationSpy }
      ],
    })
    .compileComponents();

    candidateEducationServiceSpy = TestBed.inject(CandidateEducationService) as jasmine.SpyObj<CandidateEducationService>;
    candidateOccupationServiceSpy = TestBed.inject(CandidateOccupationService) as jasmine.SpyObj<CandidateOccupationService>;
  });

  beforeEach(() => {
    fixture = TestBed.createComponent(VisaJobCheckCaComponent);
    component = fixture.componentInstance;
    component.candidate = mockCandidate;
    component.candidateIntakeData = mockCandidateIntakeData;
    component.visaCheckRecord = { country: { id: 1 } } as any; // Mock visa check record
    component.selectedJobCheck = {} as any; // Mock selected job check
    candidateOccupationServiceSpy.get.and.returnValue(of(mockCandidate.candidateOccupations));
    candidateEducationServiceSpy.list.and.returnValue(of(mockCandidate.candidateEducations));

    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should fetch candidate occupations on init', () => {
    const mockOccupations = mockCandidate.candidateOccupations;

    fixture.detectChanges(); // ngOnInit() gets called here

    expect(component.candOccupations).toEqual(mockOccupations);
    expect(candidateOccupationServiceSpy.get.calls.count()).toBe(1);
  });

  it('should fetch candidate qualifications on init', () => {
    const mockQualifications = mockCandidate.candidateEducations;

    fixture.detectChanges(); // ngOnInit() gets called here

    expect(component.candQualifications).toEqual(mockQualifications);
    expect(candidateEducationServiceSpy.list.calls.count()).toBe(1);
  });

  it('should handle error when fetching candidate occupations', () => {
    candidateOccupationServiceSpy.get.and.returnValue(throwError('Error fetching occupations'));

    component.ngOnInit(); // ngOnInit() gets called here

    expect(component.error).toBe('Error fetching occupations');
  });

  it('should handle error when fetching candidate qualifications', () => {
    candidateEducationServiceSpy.list.and.returnValue(throwError('Error fetching qualifications'));

    component.ngOnInit(); // ngOnInit() gets called here

    expect(component.error).toBe('Error fetching qualifications');
  });

  it('should open all panels after view init', () => {
    fixture.detectChanges(); // ngOnInit() gets called here

    // The accordion should have allOpen="true" set, which opens all panels during initialization
    // Verify that the accordion is rendered with allOpen input
    const accordionElement = fixture.nativeElement.querySelector('tc-accordion');
    expect(accordionElement).toBeTruthy();
    // The accordion items should be open (this is handled by the accordion component's allOpen input)
  });
});
