import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {LinkedinService} from './linkedin.service';
import {environment} from '../../environments/environment';
import {IssueReportRequest, ResourceStatus, ServiceAssignment, ServiceProvider, AssignmentStatus} from '../model/services';

describe('LinkedinService', () => {
  let service: LinkedinService;
  let httpMock: HttpTestingController;

  const BASE_URL = environment.apiUrl + '/linkedin';
  const CANDIDATE_ID = 123;

  const mockAssignment: ServiceAssignment = {
    id: 1,
    provider: ServiceProvider.LINKEDIN,
    serviceCode: 'PREMIUM_MEMBERSHIP' as any,
    resource: {
      id: 1,
      provider: ServiceProvider.LINKEDIN,
      serviceCode: 'PREMIUM_MEMBERSHIP' as any,
      resourceCode: 'https://www.linkedin.com/premium/redeem/promo?coupon=ABC',
      status: ResourceStatus.RESERVED,
      sentAt: null,
      expiresAt: null,
    },
    candidateId: CANDIDATE_ID,
    actorId: 1,
    status: AssignmentStatus.ASSIGNED,
    assignedAt: '2026-01-01T00:00:00',
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [LinkedinService],
    });
    service = TestBed.inject(LinkedinService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should call isEligible endpoint', () => {
    service.isEligible(CANDIDATE_ID).subscribe(result => {
      expect(result).toBeTrue();
    });

    const req = httpMock.expectOne(`${BASE_URL}/${CANDIDATE_ID}/eligibility`);
    expect(req.request.method).toBe('GET');
    req.flush(true);
  });

  it('should call findAssignmentWithReservedOrRedeemedResource endpoint', () => {
    service.findAssignmentWithReservedOrRedeemedResource(CANDIDATE_ID).subscribe(result => {
      expect(result).toEqual(mockAssignment);
    });

    const req = httpMock.expectOne(`${BASE_URL}/${CANDIDATE_ID}/find-assignment`);
    expect(req.request.method).toBe('GET');
    req.flush(mockAssignment);
  });

  it('should call assign endpoint with POST and null body', () => {
    service.assign(CANDIDATE_ID).subscribe(result => {
      expect(result).toEqual(mockAssignment);
    });

    const req = httpMock.expectOne(`${BASE_URL}/${CANDIDATE_ID}/assign`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toBeNull();
    req.flush(mockAssignment);
  });

  it('should call updateCouponStatus endpoint with PUT', () => {
    const request = {
      resourceCode: 'https://www.linkedin.com/premium/redeem/promo?coupon=ABC',
      status: ResourceStatus.REDEEMED,
    };

    service.updateCouponStatus(request).subscribe();

    const req = httpMock.expectOne(`${BASE_URL}/update-coupon-status`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(request);
    req.flush(null);
  });

  it('should call addCandidateToIssueReportList endpoint with POST and IssueReportRequest body', () => {
    const request: IssueReportRequest = {
      assignment: mockAssignment,
      issueComment: 'The coupon link did not work.',
    };

    service.addCandidateToIssueReportList(request).subscribe();

    const req = httpMock.expectOne(`${BASE_URL}/issue-report`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual(request);
    req.flush(null);
  });

  it('should call isOnIssueReportList endpoint', () => {
    service.isOnIssueReportList(CANDIDATE_ID).subscribe(result => {
      expect(result).toBeTrue();
    });

    const req = httpMock.expectOne(`${BASE_URL}/${CANDIDATE_ID}/issue-report`);
    expect(req.request.method).toBe('GET');
    req.flush(true);
  });

  it('should call isOnAssignmentFailureList endpoint', () => {
    service.isOnAssignmentFailureList(CANDIDATE_ID).subscribe(result => {
      expect(result).toBeFalse();
    });

    const req = httpMock.expectOne(`${BASE_URL}/${CANDIDATE_ID}/assignment-failure`);
    expect(req.request.method).toBe('GET');
    req.flush(false);
  });
});
