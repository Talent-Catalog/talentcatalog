import {TestBed} from '@angular/core/testing';
import {HttpClientTestingModule, HttpTestingController} from '@angular/common/http/testing';
import {AdminService} from './admin.service';
import {environment} from '../../environments/environment';
import {of} from "rxjs";
import {HttpClient} from "@angular/common/http";

describe('AdminService', () => {
  let service: AdminService;
  let httpMock: HttpTestingController;
  let httpClient: HttpClient;

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [AdminService]
    });

    service = TestBed.inject(AdminService);
    httpMock = TestBed.inject(HttpTestingController);
    httpClient = TestBed.inject(HttpClient);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should make a GET request to the correct URL', () => {
    const apicall = 'test-call';
    const apiUrl = `${environment.apiUrl}/system/${apicall}`;

    // Mocking the return value of the HttpClient's get method
    const getSpy = spyOn(httpClient, 'get').and.returnValue(of(void 0));

    service.call(apicall).subscribe(response => {
      expect(response).toBeUndefined();
    });

    expect(getSpy).toHaveBeenCalledWith(apiUrl);
  });
});
