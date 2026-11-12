import type { MockedObject } from "vitest";
import { ComponentFixture, TestBed } from '@angular/core/testing';
import { of, throwError } from 'rxjs';
import { NO_ERRORS_SCHEMA, Pipe, PipeTransform } from "@angular/core";
import { ReferenceComponent } from './reference.component';
import { CasiPortalService } from "../../../../../../services/casi-portal.service";
import { ResourceStatus } from "../../../../../../model/services";

@Pipe({ name: 'translate', standalone: false })
class MockTranslatePipe implements PipeTransform {
    transform(value: string): string {
        return value;
    }
}

describe('ReferenceComponent', () => {
    let component: ReferenceComponent;
    let fixture: ComponentFixture<ReferenceComponent>;
    let mockPortalService: MockedObject<CasiPortalService>;

    beforeEach(async () => {
        mockPortalService = {
            checkEligibility: vi.fn().mockName("CasiPortalService.checkEligibility"),
            getAssignment: vi.fn().mockName("CasiPortalService.getAssignment"),
            assign: vi.fn().mockName("CasiPortalService.assign"),
            updateResourceStatus: vi.fn().mockName("CasiPortalService.updateResourceStatus"),
            checkNeedsAgreement: vi.fn().mockName("CasiPortalService.checkNeedsAgreement"),
            getProviderTerms: vi.fn().mockName("CasiPortalService.getProviderTerms"),
            acceptProviderTerms: vi.fn().mockName("CasiPortalService.acceptProviderTerms"),
            checkNeedsOpcDpa: vi.fn().mockName("CasiPortalService.checkNeedsOpcDpa"),
            getOpcDpaTerms: vi.fn().mockName("CasiPortalService.getOpcDpaTerms"),
            acceptOpcDpa: vi.fn().mockName("CasiPortalService.acceptOpcDpa")
        } as unknown as MockedObject<CasiPortalService>;
        mockPortalService.checkEligibility.mockReturnValue(of(true));
        mockPortalService.getAssignment.mockReturnValue(of(null as any));
        mockPortalService.checkNeedsOpcDpa.mockReturnValue(of(false));
        mockPortalService.getOpcDpaTerms.mockReturnValue(of(null));
        mockPortalService.acceptOpcDpa.mockReturnValue(of(void 0));
        mockPortalService.checkNeedsAgreement.mockReturnValue(of(false));
        mockPortalService.getProviderTerms.mockReturnValue(of(null));
        mockPortalService.acceptProviderTerms.mockReturnValue(of(void 0));
        mockPortalService.assign.mockReturnValue(of({
            resource: { resourceCode: 'REF-001', status: ResourceStatus.RESERVED }
        } as any));
        mockPortalService.updateResourceStatus.mockReturnValue(of(void 0));

        await TestBed.configureTestingModule({
            declarations: [ReferenceComponent, MockTranslatePipe],
            providers: [{ provide: CasiPortalService, useValue: mockPortalService }],
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
            resource: { resourceCode: 'REF-001', status: ResourceStatus.RESERVED }
        } as any;

        component.redeem();

        expect(mockPortalService.updateResourceStatus).toHaveBeenCalledWith('REFERENCE', 'VOUCHER', { resourceCode: 'REF-001', status: ResourceStatus.REDEEMED });
    });

    it('should surface eligibility errors', () => {
        mockPortalService.checkEligibility.mockReturnValue(throwError(() => new Error('boom')));
        component.ngOnInit();
        expect(component.error).toBeTruthy();
    });

    it('should load agreement state when eligible', () => {
        mockPortalService.checkNeedsOpcDpa.mockReturnValue(of(false));
        mockPortalService.getOpcDpaTerms.mockReturnValue(of(null));
        mockPortalService.checkNeedsAgreement.mockReturnValue(of(true));
        mockPortalService.getProviderTerms.mockReturnValue(of({
            id: 'ReferenceServiceTermsV1',
            content: '<p>Terms</p>'
        }));

        component.ngOnInit();

        expect(component.needsAgreement).toBe(true);
        expect(component.termsInfo?.id).toEqual('ReferenceServiceTermsV1');
        expect(mockPortalService.checkNeedsAgreement).toHaveBeenCalledWith('REFERENCE', 'VOUCHER');
        expect(mockPortalService.getProviderTerms).toHaveBeenCalledWith('REFERENCE', 'VOUCHER');
    });

    it('should load OPC DPA state when eligible', () => {
        mockPortalService.checkNeedsOpcDpa.mockReturnValue(of(true));
        mockPortalService.getOpcDpaTerms.mockReturnValue(of({
            id: 'OpcDataProcessingAgreementV1',
            content: '<p>OPC DPA</p>'
        }));

        component.ngOnInit();

        expect(component.needsOpcDpa).toBe(true);
        expect(component.opcDpaTermsInfo?.id).toEqual('OpcDataProcessingAgreementV1');
        expect(mockPortalService.checkNeedsOpcDpa).toHaveBeenCalledWith('REFERENCE', 'VOUCHER');
        expect(mockPortalService.getOpcDpaTerms).toHaveBeenCalledWith('REFERENCE', 'VOUCHER');
    });

    it('should accept OPC DPA and reload', () => {
        const reloadSpy = vi.spyOn(component as any, 'checkEligibilityAndLoad').mockReturnValue(undefined);
        component.acceptOpcDpa();
        expect(mockPortalService.acceptOpcDpa).toHaveBeenCalledWith('REFERENCE', 'VOUCHER');
        expect(reloadSpy).toHaveBeenCalled();
    });

    it('should accept terms and reload', () => {
        const reloadSpy = vi.spyOn(component as any, 'checkEligibilityAndLoad').mockReturnValue(undefined);
        component.acceptTerms();
        expect(mockPortalService.acceptProviderTerms).toHaveBeenCalledWith('REFERENCE', 'VOUCHER');
        expect(reloadSpy).toHaveBeenCalled();
    });
});
