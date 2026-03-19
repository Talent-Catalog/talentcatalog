import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from "@angular/common/http/testing";
import {environment} from "../../environments/environment";
import {CasiAdminService} from './casi-admin.service';

describe('CasiAdminService', () => {
  let service: CasiAdminService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [CasiAdminService],
    });

    service = TestBed.inject(CasiAdminService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should import inventory for provider service', () => {
    const file = new File(['voucher_code,expires_at'], 'reference.csv', { type: 'text/csv' });

    service.importInventory('REFERENCE', 'VOUCHER', file).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/services/REFERENCE/VOUCHER/import`);
    expect(req.request.method).toBe('POST');
    expect(req.request.headers.get('enctype')).toBe('multipart/form-data');
    req.flush({ status: 'success' });
  });

  it('should count available inventory', () => {
    service.countAvailable('REFERENCE', 'VOUCHER').subscribe(response => {
      expect(response.count).toBe(5);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/services/REFERENCE/VOUCHER/available/count`);
    expect(req.request.method).toBe('GET');
    req.flush({ count: 5 });
  });

  it('should assign to candidate', () => {
    service.assignToCandidate('REFERENCE', 'VOUCHER', 123).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/services/REFERENCE/VOUCHER/assign/candidate/123`);
    expect(req.request.method).toBe('POST');
    req.flush({});
  });

  it('should assign to list', () => {
    service.assignToList('REFERENCE', 'VOUCHER', 456).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/services/REFERENCE/VOUCHER/assign/list/456`);
    expect(req.request.method).toBe('POST');
    req.flush([]);
  });
});
