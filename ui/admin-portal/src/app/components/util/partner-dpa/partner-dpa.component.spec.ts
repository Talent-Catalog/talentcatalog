import {ComponentFixture, fakeAsync, TestBed, tick} from '@angular/core/testing';
import {Router} from '@angular/router';
import {of} from 'rxjs';
import {PartnerDpaComponent} from './partner-dpa.component';
import {AuthorizationService} from '../../../services/authorization.service';
import {AuthenticationService} from '../../../services/authentication.service';
import {TermsInfoService} from '../../../services/terms-info.service';
import {PartnerService} from '../../../services/partner.service';
import {TermsType} from '../../../model/terms-info-dto';
import {DtoType} from '../../../model/base';

describe('PartnerDpaComponent', () => {
  let component: PartnerDpaComponent;
  let fixture: ComponentFixture<PartnerDpaComponent>;

  // mocks
  let mockAuthService: any;
  let mockAuthorizationService: any;
  let mockTermsInfoService: any;
  let mockPartnerService: any;
  let mockRouter: any;

  const mockPartner = {
    id: 'p1',
    name: 'Test Org',
    acceptedDataProcessingAgreementId: 'dpa123',
    acceptedDataProcessingAgreementDate: '2025-09-08',
    firstDpaSeenDate: '2025-09-01'
  };

  const mockDpa = {
    id: 'dpa123',
    content: 'Agreement for [Your Organization]'
  };

  beforeEach(async () => {
    mockAuthService = {
      getLoggedInUser: jasmine.createSpy().and.returnValue({partner: {id: 'p1'}})
    };
    mockAuthorizationService = {
      isSourcePartner: jasmine.createSpy().and.returnValue(true)
    };
    mockTermsInfoService = {
      getCurrentByType: jasmine.createSpy().and.returnValue(of(mockDpa))
    };
    mockPartnerService = {
      getPartner: jasmine.createSpy().and.returnValue(of(mockPartner)),
      setFirstDpaSeen: jasmine.createSpy().and.returnValue(of(mockPartner)),
      updateAcceptedDpa: jasmine.createSpy().and.returnValue(of(mockPartner))
    };
    mockRouter = {
      navigateByUrl: jasmine.createSpy()
    };

    await TestBed.configureTestingModule({
      declarations: [PartnerDpaComponent],
      providers: [
        {provide: AuthenticationService, useValue: mockAuthService},
        {provide: AuthorizationService, useValue: mockAuthorizationService},
        {provide: TermsInfoService, useValue: mockTermsInfoService},
        {provide: PartnerService, useValue: mockPartnerService},
        {provide: Router, useValue: mockRouter}
      ]
    }).compileComponents();

    fixture = TestBed.createComponent(PartnerDpaComponent);
    component = fixture.componentInstance;
  });

  it('should create component', () => {
    expect(component).toBeTruthy();
  });

  it('ngOnInit should load partner and dpa', fakeAsync(() => {
    component.ngOnInit();
    tick();

    expect(mockTermsInfoService.getCurrentByType).toHaveBeenCalledWith(TermsType.DATA_PROCESSING_AGREEMENT);
    expect(mockPartnerService.getPartner).toHaveBeenCalledWith('p1', DtoType.MINIMAL);

    expect(component.currentDpa).toEqual(mockDpa);
    expect(component.organizationName).toBe('Test Org');
    expect(component.content).toContain('Agreement for Test Org');
    expect(component.acceptedDpaId).toBe('dpa123');
    expect(component.loading).toBeFalse();
  }));

  it('ngOnInit should call setFirstDpaSeen if firstDpaSeenDate is missing', fakeAsync(() => {
    const partnerWithoutSeen = {...mockPartner, firstDpaSeenDate: null};
    mockPartnerService.getPartner.and.returnValue(of(partnerWithoutSeen));

    component.ngOnInit();
    tick();

    expect(mockPartnerService.setFirstDpaSeen).toHaveBeenCalled();
  }));

  it('onScroll should set termsRead when near bottom', () => {
    const mockEvent = {
      target: {scrollHeight: 1000, scrollTop: 950, clientHeight: 60}
    } as any;

    component.onScroll(mockEvent);

    expect(component.termsRead).toBeTrue();
  });

  it('acceptTerms should do nothing if terms not read', () => {
    component.termsRead = false;
    component.acceptTerms();
    expect(mockPartnerService.updateAcceptedDpa).not.toHaveBeenCalled();
  });

  it('acceptTerms should update and navigate when termsRead is true', fakeAsync(() => {
    component.termsRead = true;
    component.currentDpa = mockDpa as any;

    component.acceptTerms();
    tick();

    expect(mockPartnerService.updateAcceptedDpa).toHaveBeenCalledWith('dpa123');
    expect(component.acceptedDpaId).toBe('dpa123');
    expect(mockRouter.navigateByUrl).toHaveBeenCalledWith('/');
  }));


  it('skipDpa should navigate away', () => {
    component.skipDpa();
    expect(mockRouter.navigateByUrl).toHaveBeenCalledWith('/');
  });
});
