import { TestBed } from '@angular/core/testing';

import { PresetEmbedService } from './preset-embed.service';
import {HttpClientTestingModule, HttpTestingController} from "@angular/common/http/testing";
import {environment} from "../../environments/environment";

describe('PresetEmbedService', () => {
  let service: PresetEmbedService;
  let httpMock: HttpTestingController;
  const mockDashboardId: string = 'dksljf78oi&^Rghk';
  const mockGuestToken: string = 'sdfaghjRDFSD69-s'

  beforeEach(() => {
    TestBed.configureTestingModule({
      imports: [HttpClientTestingModule],
      providers: [PresetEmbedService]
    });

    service = TestBed.inject(PresetEmbedService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('should be created', () => {
    expect(service).toBeTruthy();
  });

  it('should fetch a guest token', () => {
    // Call the method
    service.fetchGuestToken(mockDashboardId).subscribe(token => {
      // Assert the token returned is correct
      expect(token).toBe(mockGuestToken);
    });

    // Set up the mock request
    const req =
      httpMock.expectOne(`${environment.apiUrl}/preset/${mockDashboardId}/guest-token`);

    expect(req.request.method).toBe('POST');
    expect(req.request.body).toBeNull();

    req.flush(mockGuestToken);
    httpMock.verify();
  })

});
