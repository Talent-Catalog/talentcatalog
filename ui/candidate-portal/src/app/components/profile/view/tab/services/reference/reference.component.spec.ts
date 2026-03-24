import {ComponentFixture, TestBed} from '@angular/core/testing';
import {of, throwError} from 'rxjs';
import {NO_ERRORS_SCHEMA} from "@angular/core";
import {ReferenceComponent} from './reference.component';
import {CasiPortalService} from "../../../../../../services/casi-portal.service";
import {ResourceStatus} from "../../../../../../model/services";

describe('ReferenceComponent', () => {
  let component: ReferenceComponent;
  let fixture: ComponentFixture<ReferenceComponent>;
  let mockPortalService: jasmine.SpyObj<CasiPortalService>;

  beforeEach(async () => {
    mockPortalService = jasmine.createSpyObj('CasiPortalService',
      ['checkEligibility', 'getAssignment', 'assign', 'updateResourceStatus']);
    mockPortalService.checkEligibility.and.returnValue(of(true));
    mockPortalService.getAssignment.and.returnValue(of(null as any));
    mockPortalService.assign.and.returnValue(of({
      resource: {resourceCode: 'REF-001', status: ResourceStatus.RESERVED}
    } as any));
    mockPortalService.updateResourceStatus.and.returnValue(of(void 0));

    await TestBed.configureTestingModule({
      declarations: [ReferenceComponent],
      providers: [{provide: CasiPortalService, useValue: mockPortalService}],
      schemas: [NO_ERRORS_SCHEMA]
    }).compileComponents();

    fixture = TestBed.createComponent(ReferenceComponent);
    component = fixture.componentInstance;
    component.candidate = { id: 1 } as any;
    fixture.detectChanges();
  });

  it('should create the component', () => {
    expect(component).toBeTruthy();
  });

  it('should assign voucher using generic portal service', () => {
    component.assign();
    expect(mockPortalService.assign).toHaveBeenCalledWith('REFERENCE', 'VOUCHER');
  });

  it('should call updateResourceStatus on redeem', () => {
    component.assignment = {
      resource: {resourceCode: 'REF-001', status: ResourceStatus.RESERVED}
    } as any;

    component.redeem();

    expect(mockPortalService.updateResourceStatus).toHaveBeenCalledWith(
      'REFERENCE',
      'VOUCHER',
      {resourceCode: 'REF-001', status: ResourceStatus.REDEEMED}
    );
  });

  it('should surface eligibility errors', () => {
    mockPortalService.checkEligibility.and.returnValue(throwError(() => new Error('boom')));
    component.ngOnInit();
    expect(component.error).toBeTruthy();
  });
});
