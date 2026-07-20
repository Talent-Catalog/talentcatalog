import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {VerifyPlusScanResult, VerifyPlusService} from './verify-plus.service';
import {environment} from '../../environments/environment';

describe('VerifyPlusService', () => {
  let service: VerifyPlusService;
  let httpMock: HttpTestingController;

  const BASE_URL = environment.apiUrl + '/verify-plus';

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [VerifyPlusService]
    });

    service = TestBed.inject(VerifyPlusService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should submit scan payload with POST', () => {
    const rawPayload = '{"v":"mock-1","unhcrId":"123-45C67890"}';
    const response: VerifyPlusScanResult = {
      unhcrNumber: '123-45C67890',
      duplicate: false
    };

    service.submitScan(rawPayload).subscribe(result => {
      expect(result).toEqual(response);
    });

    const req = httpMock.expectOne(BASE_URL);
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({rawPayload});
    req.flush(response);
  });

  it('should surface http errors from submit endpoint', () => {
    const rawPayload = '{"v":"mock-2","unhcrId":"bad"}';

    service.submitScan(rawPayload).subscribe({
      next: () => fail('Expected an error response'),
      error: (error) => {
        expect(error.status).toBe(400);
      }
    });

    const req = httpMock.expectOne(BASE_URL);
    expect(req.request.method).toBe('POST');
    req.flush({message: 'Invalid Verify+ payload'}, {status: 400, statusText: 'Bad Request'});
  });
});
