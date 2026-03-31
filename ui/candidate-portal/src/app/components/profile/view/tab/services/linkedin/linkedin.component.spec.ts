import {ComponentFixture, TestBed} from '@angular/core/testing';
import {LinkedinComponent} from './linkedin.component';
import {LinkedinService} from '../../../../../../services/linkedin.service';
import {CandidateService} from '../../../../../../services/candidate.service';
import {NO_ERRORS_SCHEMA} from '@angular/core';
import {of, throwError} from 'rxjs';
import {
  ResourceStatus,
  ServiceAssignment,
  ServiceProvider,
  AssignmentStatus,
} from '../../../../../../model/services';
import {Candidate} from '../../../../../../model/candidate';

describe('LinkedinComponent', () => {
  let component: LinkedinComponent;
  let fixture: ComponentFixture<LinkedinComponent>;
  let mockLinkedinService: jasmine.SpyObj<LinkedinService>;
  let mockCandidateService: jasmine.SpyObj<CandidateService>;

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
      status: ResourceStatus.RESERVED,
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
      'findAssignmentWithReservedOrRedeemedResource',
      'isOnAssignmentFailureList',
      'assign',
      'updateCouponStatus',
    ]);
    mockCandidateService = jasmine.createSpyObj('CandidateService', [
      'updateCandidateOtherInfo',
    ]);

    mockLinkedinService.findAssignmentWithReservedOrRedeemedResource.and.returnValue(of(null));
    mockLinkedinService.isOnAssignmentFailureList.and.returnValue(of(false));

    await TestBed.configureTestingModule({
      declarations: [LinkedinComponent],
      providers: [
        {provide: LinkedinService, useValue: mockLinkedinService},
        {provide: CandidateService, useValue: mockCandidateService},
      ],
      schemas: [NO_ERRORS_SCHEMA],
    }).compileComponents();

    fixture = TestBed.createComponent(LinkedinComponent);
    component = fixture.componentInstance;
    component.candidate = mockCandidate;
    fixture.detectChanges();
  });

  it('should create', () => {
    expect(component).toBeTruthy();
  });

  describe('ngOnInit', () => {
    it('should load assignment on init', () => {
      expect(mockLinkedinService.findAssignmentWithReservedOrRedeemedResource)
        .toHaveBeenCalledWith(123);
    });

    it('should check assignment failure list on init', () => {
      expect(mockLinkedinService.isOnAssignmentFailureList).toHaveBeenCalledWith(123);
    });

    it('should initialize linkedInLinkInput from candidate.linkedInLink', () => {
      expect(component.linkedInLinkInput).toBe('https://www.linkedin.com/in/testuser');
    });

    it('should set assignment when service returns one', () => {
      mockLinkedinService.findAssignmentWithReservedOrRedeemedResource.and.returnValue(
        of(mockAssignment)
      );
      component.ngOnInit();
      expect(component.assignment).toEqual(mockAssignment);
    });

    it('should set isOnAssignmentFailureList to true when candidate is on failure list', () => {
      mockLinkedinService.isOnAssignmentFailureList.and.returnValue(of(true));
      component.ngOnInit();
      expect(component.isOnAssignmentFailureList).toBeTrue();
    });
  });

  describe('isValidLinkedInUrl', () => {
    it('should return true for valid LinkedIn profile URL', () => {
      component.linkedInLinkInput = 'https://www.linkedin.com/in/johndoe';
      expect(component.isValidLinkedInUrl).toBeTrue();
    });

    it('should return true for LinkedIn URL with trailing slash', () => {
      component.linkedInLinkInput = 'https://www.linkedin.com/in/johndoe/';
      expect(component.isValidLinkedInUrl).toBeTrue();
    });

    it('should return true for http LinkedIn URL', () => {
      component.linkedInLinkInput = 'http://www.linkedin.com/in/johndoe';
      expect(component.isValidLinkedInUrl).toBeTrue();
    });

    it('should return false for non-LinkedIn URL', () => {
      component.linkedInLinkInput = 'https://www.facebook.com/profile';
      expect(component.isValidLinkedInUrl).toBeFalse();
    });

    it('should return false for empty string', () => {
      component.linkedInLinkInput = '';
      expect(component.isValidLinkedInUrl).toBeFalse();
    });

    it('should return false for linkedin.com without /in/ path', () => {
      component.linkedInLinkInput = 'https://www.linkedin.com';
      expect(component.isValidLinkedInUrl).toBeFalse();
    });
  });

  describe('canRedeem', () => {
    it('should return false when there is no assignment', () => {
      component.assignment = undefined;
      expect(component.canRedeem).toBeFalsy();
    });

    it('should return true when assignment resource is RESERVED', () => {
      component.assignment = mockAssignment;
      expect(component.canRedeem).toBeTrue();
    });

    it('should return false when assignment resource is REDEEMED', () => {
      component.assignment = {
        ...mockAssignment,
        resource: {...mockAssignment.resource, status: ResourceStatus.REDEEMED},
      };
      expect(component.canRedeem).toBeFalse();
    });
  });

  describe('verify', () => {
    it('should call assign without updating profile when URL unchanged', () => {
      mockLinkedinService.assign.and.returnValue(of(mockAssignment));
      component.linkedInLinkInput = mockCandidate.linkedInLink;

      component.verify();

      expect(mockCandidateService.updateCandidateOtherInfo).not.toHaveBeenCalled();
      expect(mockLinkedinService.assign).toHaveBeenCalledWith(123);
      expect(component.verified).toBeTrue();
      expect(component.assignment).toEqual(mockAssignment);
    });

    it('should update profile first when LinkedIn URL has changed', () => {
      const newLink = 'https://www.linkedin.com/in/newuser';
      mockCandidateService.updateCandidateOtherInfo.and.returnValue(of(null));
      mockLinkedinService.assign.and.returnValue(of(mockAssignment));
      component.linkedInLinkInput = newLink;

      component.verify();

      expect(mockCandidateService.updateCandidateOtherInfo).toHaveBeenCalledWith({
        linkedInLink: newLink,
      });
      expect(mockLinkedinService.assign).toHaveBeenCalledWith(123);
    });

    it('should set error and loading false when assign fails', () => {
      const error = new Error('Assignment failed');
      mockLinkedinService.assign.and.returnValue(throwError(error));
      component.linkedInLinkInput = mockCandidate.linkedInLink;

      component.verify();

      expect(component.error).toEqual(error);
      expect(component.loading).toBeFalse();
    });
  });

  describe('redeem', () => {
    it('should do nothing when there is no assignment', () => {
      component.assignment = undefined;
      component.redeem();
      expect(mockLinkedinService.updateCouponStatus).not.toHaveBeenCalled();
    });

    it('should update coupon status to REDEEMED and open URL on success', () => {
      component.assignment = mockAssignment;
      mockLinkedinService.updateCouponStatus.and.returnValue(of(undefined));
      spyOn(window, 'open');

      component.redeem();

      expect(mockLinkedinService.updateCouponStatus).toHaveBeenCalledWith({
        resourceCode: mockAssignment.resource.resourceCode,
        status: ResourceStatus.REDEEMED,
      });
      expect(window.open).toHaveBeenCalledWith(mockAssignment.resource.resourceCode, '_blank');
      expect(component.assignment.resource.status).toBe(ResourceStatus.REDEEMED);
      expect(component.loading).toBeFalse();
    });

    it('should set error and loading false when updateCouponStatus fails', () => {
      component.assignment = mockAssignment;
      const error = new Error('Update failed');
      mockLinkedinService.updateCouponStatus.and.returnValue(throwError(error));

      component.redeem();

      expect(component.error).toEqual(error);
      expect(component.loading).toBeFalse();
    });
  });

  describe('onBackButtonClicked', () => {
    it('should emit backButtonClicked event', () => {
      spyOn(component.backButtonClicked, 'emit');
      component.onBackButtonClicked();
      expect(component.backButtonClicked.emit).toHaveBeenCalled();
    });
  });
});
