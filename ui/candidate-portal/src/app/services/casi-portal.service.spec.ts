import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from "@angular/common/http/testing";
import {environment} from "../../environments/environment";
import {CasiPortalService} from './casi-portal.service';
import {ResourceStatus} from "../model/services";

describe('CasiPortalService', () => {
  let service: CasiPortalService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CasiPortalService],
    });

    service = TestBed.inject(CasiPortalService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should check eligibility', () => {
    service.checkEligibility('REFERENCE', 'VOUCHER').subscribe(response => {
      expect(response).toBeTrue();
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/services/REFERENCE/VOUCHER/eligibility`);
    expect(req.request.method).toBe('GET');
    req.flush(true);
  });

  it('should get current assignment', () => {
    service.getAssignment('REFERENCE', 'VOUCHER').subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/services/REFERENCE/VOUCHER/assignment`);
    expect(req.request.method).toBe('GET');
    req.flush({});
  });

  it('should assign service', () => {
    service.assign('REFERENCE', 'VOUCHER').subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/services/REFERENCE/VOUCHER/assign`);
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('should update resource status', () => {
    const request = { resourceCode: 'REF-001', status: ResourceStatus.REDEEMED };
    service.updateResourceStatus('REFERENCE', 'VOUCHER', request).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/services/REFERENCE/VOUCHER/resources/status`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual(request);
    req.flush({});
  });
});
