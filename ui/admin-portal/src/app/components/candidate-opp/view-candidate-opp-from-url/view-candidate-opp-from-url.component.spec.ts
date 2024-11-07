import {ComponentFixture, TestBed, waitForAsync} from '@angular/core/testing';
import {ActivatedRoute, convertToParamMap} from '@angular/router';
import {of, throwError} from 'rxjs';
import {ViewCandidateOppFromUrlComponent} from './view-candidate-opp-from-url.component';
import {CandidateOpportunityService} from '../../../services/candidate-opportunity.service';
import {mockCandidateOpportunity} from "../../../MockData/MockCandidateOpportunity";
import {ViewCandidateOppComponent} from "../view-candidate-opp/view-candidate-opp.component";
import {HttpClientTestingModule} from "@angular/common/http/testing";
import {NgbNavModule} from "@ng-bootstrap/ng-bootstrap";
import {MockPartner} from "../../../MockData/MockPartner";
import {AuthenticationService} from "../../../services/authentication.service";
import {
  OpportunityStageNextStepComponent
} from "../../util/opportunity-stage-next-step/opportunity-stage-next-step.component";
import {RouterLinkStubDirective} from "../../login/login.component.spec";
import {CUSTOM_ELEMENTS_SCHEMA} from "@angular/core";

describe('ViewCandidateOppFromUrlComponent', () => {
  let component: ViewCandidateOppFromUrlComponent;
  let fixture: ComponentFixture<ViewCandidateOppFromUrlComponent>;
  let mockActivatedRoute: any;
  let mockOpportunityService: jasmine.SpyObj<CandidateOpportunityService>;
  let mockAuthService: any;

  beforeEach(waitForAsync(() => {
    // Create a spy object for CandidateOpportunityService
    const spyOpportunityService = jasmine.createSpyObj('CandidateOpportunityService', ['get']);
    mockAuthService = jasmine.createSpyObj('AuthenticationService', ['getLoggedInUser']);
    spyOpportunityService.get.and.callThrough();

    TestBed.configureTestingModule({
      declarations: [ ViewCandidateOppFromUrlComponent,OpportunityStageNextStepComponent,RouterLinkStubDirective ,ViewCandidateOppComponent ],
      imports: [HttpClientTestingModule,NgbNavModule],
      providers: [
        { provide: ActivatedRoute, useValue: { paramMap: of(convertToParamMap({ id: '1' })) } },
        { provide: CandidateOpportunityService, useValue: spyOpportunityService },
        { provide: AuthenticationService, useValue: mockAuthService },

      ],
      schemas: [CUSTOM_ELEMENTS_SCHEMA]
    })
    .compileComponents();

    mockActivatedRoute = TestBed.inject(ActivatedRoute);
    mockOpportunityService = TestBed.inject(CandidateOpportunityService) as jasmine.SpyObj<CandidateOpportunityService>;
    mockAuthService.getLoggedInUser.and.returnValue({ partner: new MockPartner()});
    mockOpportunityService.get.and.returnValue(of(mockCandidateOpportunity));

  }));

  beforeEach(() => {
    fixture = TestBed.createComponent(ViewCandidateOppFromUrlComponent);
    component = fixture.componentInstance;
    component.opp = mockCandidateOpportunity;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  it('should load opportunity on init', () => {
    mockOpportunityService.get.and.returnValue(of(mockCandidateOpportunity));

    component.ngOnInit();

    expect(component.loading).toBe(false); // Ensure loading is set to false after data is fetched
    expect(component.error).toBeNull(); // Ensure error is null after successful data fetch
    expect(component.opp).toEqual(mockCandidateOpportunity); // Ensure opportunity data is set correctly
  });

  it('should handle error when loading opportunity', () => {
    const errorMessage = 'Error fetching opportunity';
    mockOpportunityService.get.and.returnValue(throwError(errorMessage));

    component.ngOnInit();

    expect(component.loading).toBe(false); // Ensure loading is set to false after error
    expect(component.error).toEqual(errorMessage); // Ensure error message is set correctly
    expect(component.opp).toBeNull(); // Ensure opportunity data is null after error
  });
});
