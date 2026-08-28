import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from "@angular/common/http/testing";
import {environment} from "../../environments/environment";
import {CasiAdminService} from './casi-admin.service';
import {ListAction, ServiceList} from "../model/service-list";

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

  it('should get service list for a saved list', () => {
    const mockServiceList: ServiceList = {
      id: 99,
      provider: 'LINKEDIN',
      serviceCode: 'PREMIUM_MEMBERSHIP',
      listRole: 'USER_ISSUE_REPORT',
      permittedActions: [ListAction.REASSIGN]
    };

    service.getServiceList(789).subscribe(result => {
      expect(result).toEqual(mockServiceList);
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/services/list/789`);
    expect(req.request.method).toBe('GET');
    req.flush(mockServiceList);
  });

  it('should perform a service list action', () => {
    service.performServiceListAction(99, ListAction.REASSIGN, ['12345', '67890']).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/services/list/99/action`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({
      action: ListAction.REASSIGN,
      candidateNumbers: ['12345', '67890']
    });
    req.flush(null);
  });


  it('should list shared links', () => {
    service.listSharedLinks('UNHCR', 'HELP_SITE_LINK').subscribe(response => {
      expect(response.length).toBe(1);
      expect(response[0].countryIsoCode).toBe('PK');
    });

    const req = httpMock.expectOne(`${environment.apiUrl}/services/UNHCR/HELP_SITE_LINK/shared`);
    expect(req.request.method).toBe('GET');
    req.flush([{id: 1, countryIsoCode: 'PK', resourceCode: 'https://help.example.com'}]);
  });

  it('should create a shared link', () => {
    service.createSharedLink('UNHCR', 'HELP_SITE_LINK', 'JO', 'https://help.example.com/jo').subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/services/UNHCR/HELP_SITE_LINK/shared`);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({countryIsoCode: 'JO', resourceCode: 'https://help.example.com/jo'});
    req.flush({id: 2});
  });

  it('should update a shared link', () => {
    service.updateSharedLink('UNHCR', 'HELP_SITE_LINK', 9, 'AU', 'https://example.com/au').subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/services/UNHCR/HELP_SITE_LINK/shared/9`);
    expect(req.request.method).toBe('PUT');
    expect(req.request.body).toEqual({countryIsoCode: 'AU', resourceCode: 'https://example.com/au'});
    req.flush({id: 9});
  });

  it('should disable a shared link', () => {
    service.disableSharedLink('UNHCR', 'HELP_SITE_LINK', 10).subscribe();

    const req = httpMock.expectOne(`${environment.apiUrl}/services/UNHCR/HELP_SITE_LINK/shared/10`);
    expect(req.request.method).toBe('DELETE');
    req.flush(null);
  });

});
