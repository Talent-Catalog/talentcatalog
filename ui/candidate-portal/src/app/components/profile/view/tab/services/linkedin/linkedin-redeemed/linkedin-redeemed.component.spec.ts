import {ComponentFixture, TestBed} from '@angular/core/testing';
import {LinkedinRedeemedComponent} from './linkedin-redeemed.component';
import {LinkedinService} from '../../../../../../../services/linkedin.service';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {of, throwError} from 'rxjs';
import {
  ResourceStatus,
  ServiceAssignment,
  ServiceProvider,
  AssignmentStatus,
} from '../../../../../../../model/services';
import {Candidate} from '../../../../../../../model/candidate';

describe('LinkedinRedeemedComponent', () => {
  let component: LinkedinRedeemedComponent;
  let fixture: ComponentFixture<LinkedinRedeemedComponent>;
  let mockLinkedinService: jasmine.SpyObj<LinkedinService>;

  const mockCandidate = {
    id: 123,
    linkedInLink: 'https://www.linkedin.com/in/testuser',
  } as Candidate;

  const mockAssignment: ServiceAssignment = {
    id: 1,
    provider: ServiceProvider.LINKEDIN,
    serviceCode: 'PREMIUM_MEMBERSHIP' as any,
    resource: {
      id: 1,
      provider: ServiceProvider.LINKEDIN,
      serviceCode: 'PREMIUM_MEMBERSHIP' as any,
      resourceCode: 'https://www.linkedin.com/premium/redeem/promo?coupon=ABC',
      status: ResourceStatus.REDEEMED,
      sentAt: null,
      expiresAt: null,
    },
    candidateId: 123,
    actorId: 1,
    status: AssignmentStatus.ASSIGNED,
    assignedAt: '2026-01-01T00:00:00',
  };

  beforeEach(async () => {
    mockLinkedinService = jasmine.createSpyObj('LinkedinService', [
      'isOnIssueReportList',
      'addCandidateToIssueReportList',
    ]);
    mockLinkedinService.isOnIssueReportList.and.returnValue(of(false));

    await TestBed.configureTestingModule({
      declarations: [LinkedinRedeemedComponent],
      providers: [{provide: LinkedinService, useValue: mockLinkedinService}],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(LinkedinRedeemedComponent);
    component = fixture.componentInstance;
    component.assignment = mockAssignment;
    component.candidate = mockCandidate;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
    it('should check if candidate is on issue report list', () => {
      expect(mockLinkedinService.isOnIssueReportList).toHaveBeenCalledWith(123);
    });

    it('should set isOnIssueReportList to false when candidate is not on list', () => {
      expect(component.isOnIssueReportList).toBeFalse();
    });

    it('should set isOnIssueReportList to true when candidate is on list', () => {
      mockLinkedinService.isOnIssueReportList.and.returnValue(of(true));
      component.ngOnInit();
      expect(component.isOnIssueReportList).toBeTrue();
    });
  });

  describe('reportIssue', () => {
    it('should call addCandidateToIssueReportList and re-check list status on success', () => {
      mockLinkedinService.addCandidateToIssueReportList.and.returnValue(of(undefined));
      mockLinkedinService.isOnIssueReportList.and.returnValue(of(true));

      component.reportIssue();

      expect(mockLinkedinService.addCandidateToIssueReportList)
        .toHaveBeenCalledWith(mockAssignment);
      expect(mockLinkedinService.isOnIssueReportList).toHaveBeenCalled();
      expect(component.isOnIssueReportList).toBeTrue();
      expect(component.loading).toBeFalse();
    });

    it('should set error and loading false when reportIssue fails', () => {
      const error = new Error('Report failed');
      mockLinkedinService.addCandidateToIssueReportList.and.returnValue(throwError(error));

      component.reportIssue();

      expect(component.error).toEqual(error);
      expect(component.loading).toBeFalse();
    });

    it('should set loading to true while the report is in progress', () => {
      let loadingDuringCall = false;
      mockLinkedinService.addCandidateToIssueReportList.and.callFake(() => {
        loadingDuringCall = component.loading;
        return of(undefined);
      });
      mockLinkedinService.isOnIssueReportList.and.returnValue(of(false));

      component.reportIssue();

      expect(loadingDuringCall).toBeTrue();
    });
  });
});
